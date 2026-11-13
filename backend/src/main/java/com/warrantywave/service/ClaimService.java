package com.warrantywave.service;

import com.warrantywave.exception.BadRequestException;
import com.warrantywave.exception.ResourceNotFoundException;
import com.warrantywave.model.dto.ClaimRequestDto;
import com.warrantywave.model.dto.ClaimResponseDto;
import com.warrantywave.model.dto.ClaimReviewDto;
import com.warrantywave.model.entity.*;
import com.warrantywave.model.enums.ClaimStatus;
import com.warrantywave.model.enums.ContractStatus;
import com.warrantywave.repository.ClaimRepository;
import com.warrantywave.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final WarrantyContractService contractService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public ClaimService(
        ClaimRepository claimRepository,
        WarrantyContractService contractService,
        UserRepository userRepository,
        AuditService auditService
    ) {
        this.claimRepository = claimRepository;
        this.contractService = contractService;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public Page<ClaimResponseDto> getAllClaims(ClaimStatus status, String query, Pageable pageable) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        Page<Claim> page = claimRepository.findFiltered(status, cleanQuery, pageable);
        return page.map(this::toDto);
    }

    public ClaimResponseDto getClaimById(Long id) {
        return claimRepository.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + id));
    }

    public List<ClaimResponseDto> getClaimsByCustomerId(Long customerId) {
        return claimRepository.findByCustomerId(customerId).stream()
            .map(this::toDto)
            .toList();
    }

    public List<ClaimResponseDto> getClaimsByContractId(Long contractId) {
        return claimRepository.findByContractId(contractId).stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional
    public ClaimResponseDto submitClaim(ClaimRequestDto dto, String performedBy) {
        WarrantyContract contract = contractService.getContractEntity(dto.contractId());

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BadRequestException("Cannot submit claim against inactive contract (Current status: " + contract.getStatus() + ").");
        }

        if (dto.currentMileage() > contract.getMaxMileage()) {
            throw new BadRequestException("Current vehicle mileage (" + dto.currentMileage() + ") exceeds the contract's covered maximum mileage (" + contract.getMaxMileage() + ").");
        }

        if (dto.incidentDate().isBefore(contract.getStartDate()) || dto.incidentDate().isAfter(contract.getEndDate())) {
            throw new BadRequestException("Incident date must fall within contract validity period: " + contract.getStartDate() + " to " + contract.getEndDate());
        }

        if (dto.incidentDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Incident date cannot be in the future.");
        }

        String claimNumber = "CLM-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Claim claim = new Claim();
        claim.setClaimNumber(claimNumber);
        claim.setContract(contract);
        claim.setCustomer(contract.getCustomer());
        claim.setVehicle(contract.getVehicle());
        claim.setIncidentDate(dto.incidentDate());
        claim.setCurrentMileage(dto.currentMileage());
        claim.setDescription(dto.description().trim());
        claim.setRepairShopName(dto.repairShopName().trim());
        claim.setRepairEstimate(dto.repairEstimate());
        claim.setStatus(ClaimStatus.SUBMITTED);

        Claim saved = claimRepository.save(claim);

        auditService.log("CLAIMS", saved.getId(), "SUBMIT", performedBy != null ? performedBy : "SYSTEM",
            "Submitted repair claim " + saved.getClaimNumber() + " for estimate $" + saved.getRepairEstimate());

        return toDto(saved);
    }

    @Transactional
    public ClaimResponseDto reviewClaim(Long id, ClaimReviewDto dto, String reviewerUsername) {
        Claim claim = claimRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + id));

        User reviewer = userRepository.findByUsername(reviewerUsername).orElse(null);
        claim.setReviewer(reviewer);
        claim.setOfficerNotes(dto.officerNotes());

        WarrantyPlan plan = claim.getContract().getPlan();

        if (Boolean.TRUE.equals(dto.approved())) {
            BigDecimal deductible = dto.deductiblePaid() != null ? dto.deductiblePaid() : plan.getDeductibleAmount();
            claim.setDeductiblePaid(deductible);

            BigDecimal netEstimate = claim.getRepairEstimate().subtract(deductible);
            if (netEstimate.compareTo(BigDecimal.ZERO) < 0) {
                netEstimate = BigDecimal.ZERO;
            }

            BigDecimal maxCoverage = plan.getMaxClaimLimit();
            BigDecimal calculatedApproval = netEstimate.min(maxCoverage);

            BigDecimal finalApprovedAmount = (dto.approvedAmount() != null && dto.approvedAmount().compareTo(BigDecimal.ZERO) > 0)
                ? dto.approvedAmount().min(maxCoverage)
                : calculatedApproval;

            claim.setApprovedAmount(finalApprovedAmount);
            claim.setStatus(ClaimStatus.APPROVED);
            claim.setRejectionReason(null);

            auditService.log("CLAIMS", claim.getId(), "APPROVE", reviewerUsername,
                "Approved claim " + claim.getClaimNumber() + " for payout $" + finalApprovedAmount + " (Deductible: $" + deductible + ")");
        } else {
            if (dto.rejectionReason() == null || dto.rejectionReason().trim().isEmpty()) {
                throw new BadRequestException("A valid rejection reason must be supplied when rejecting a warranty claim.");
            }
            claim.setStatus(ClaimStatus.REJECTED);
            claim.setRejectionReason(dto.rejectionReason().trim());
            claim.setApprovedAmount(BigDecimal.ZERO);

            auditService.log("CLAIMS", claim.getId(), "REJECT", reviewerUsername,
                "Rejected claim " + claim.getClaimNumber() + ". Reason: " + claim.getRejectionReason());
        }

        Claim updated = claimRepository.save(claim);
        return toDto(updated);
    }

    @Transactional
    public ClaimResponseDto markClaimPaid(Long id, String performedBy) {
        Claim claim = claimRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + id));

        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new BadRequestException("Only approved claims can be marked as PAID. Current status: " + claim.getStatus());
        }

        claim.setStatus(ClaimStatus.PAID);
        Claim updated = claimRepository.save(claim);

        auditService.log("CLAIMS", claim.getId(), "PAYMENT_DISBURSED", performedBy,
            "Disbursed settlement payout of $" + claim.getApprovedAmount() + " for claim " + claim.getClaimNumber());

        return toDto(updated);
    }

    public ClaimResponseDto toDto(Claim c) {
        return new ClaimResponseDto(
            c.getId(),
            c.getClaimNumber(),
            c.getContract() != null ? c.getContract().getId() : null,
            c.getContract() != null ? c.getContract().getContractNumber() : null,
            c.getCustomer() != null ? c.getCustomer().getId() : null,
            c.getCustomer() != null ? c.getCustomer().getFullName() : null,
            c.getVehicle() != null ? c.getVehicle().getId() : null,
            c.getVehicle() != null ? c.getVehicle().getVin() : null,
            c.getVehicle() != null ? (c.getVehicle().getModelYear() + " " + c.getVehicle().getMake() + " " + c.getVehicle().getModel()) : null,
            c.getIncidentDate(),
            c.getCurrentMileage(),
            c.getDescription(),
            c.getRepairShopName(),
            c.getRepairEstimate(),
            c.getApprovedAmount(),
            c.getDeductiblePaid(),
            c.getStatus(),
            c.getRejectionReason(),
            c.getOfficerNotes(),
            c.getReviewer() != null ? c.getReviewer().getFullName() : null,
            c.getCreatedAt(),
            c.getUpdatedAt()
        );
    }
}
