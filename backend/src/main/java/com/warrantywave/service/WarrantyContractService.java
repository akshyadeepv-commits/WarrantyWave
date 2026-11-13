package com.warrantywave.service;

import com.warrantywave.exception.BadRequestException;
import com.warrantywave.exception.ResourceNotFoundException;
import com.warrantywave.model.dto.*;
import com.warrantywave.model.entity.*;
import com.warrantywave.model.enums.ContractStatus;
import com.warrantywave.repository.WarrantyContractRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WarrantyContractService {

    private final WarrantyContractRepository contractRepository;
    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final WarrantyPlanService planService;
    private final AuditService auditService;

    public WarrantyContractService(
        WarrantyContractRepository contractRepository,
        CustomerService customerService,
        VehicleService vehicleService,
        WarrantyPlanService planService,
        AuditService auditService
    ) {
        this.contractRepository = contractRepository;
        this.customerService = customerService;
        this.vehicleService = vehicleService;
        this.planService = planService;
        this.auditService = auditService;
    }

    public Page<WarrantyContractDto> getAllContracts(ContractStatus status, String query, Pageable pageable) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        Page<WarrantyContract> page = contractRepository.findFiltered(status, cleanQuery, pageable);
        return page.map(this::toDto);
    }

    public WarrantyContractDto getContractById(Long id) {
        return contractRepository.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
    }

    public WarrantyContract getContractEntity(Long id) {
        return contractRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
    }

    public List<WarrantyContractDto> getContractsByCustomerId(Long customerId) {
        return contractRepository.findByCustomerId(customerId).stream()
            .map(this::toDto)
            .toList();
    }

    public List<WarrantyContractDto> getContractsByVehicleId(Long vehicleId) {
        return contractRepository.findByVehicleId(vehicleId).stream()
            .map(this::toDto)
            .toList();
    }

    public EligibilityCheckResponse validateEligibility(EligibilityCheckRequest request) {
        Vehicle vehicle = vehicleService.getVehicleEntity(request.vehicleId());
        WarrantyPlan plan = planService.getPlanEntity(request.planId());

        int currentMileage = (request.currentMileage() != null) ? request.currentMileage() : vehicle.getMileage();
        int currentYear = Year.now().getValue();
        int vehicleAge = Math.max(0, currentYear - vehicle.getModelYear());

        int maxAgeYears = switch (plan.getPlanType()) {
            case BUMPER_TO_BUMPER -> 7;
            case COMPREHENSIVE -> 10;
            case EV_DRIVETRAIN -> 8;
            case POWERTRAIN -> 12;
        };

        if (currentMileage > plan.getMaxMileageCoverage()) {
            return new EligibilityCheckResponse(
                false,
                "Vehicle mileage (" + currentMileage + " miles) exceeds the plan's maximum threshold of " + plan.getMaxMileageCoverage() + " miles.",
                currentMileage,
                plan.getMaxMileageCoverage(),
                vehicleAge,
                maxAgeYears
            );
        }

        if (vehicleAge > maxAgeYears) {
            return new EligibilityCheckResponse(
                false,
                "Vehicle age (" + vehicleAge + " years) exceeds the maximum allowed age of " + maxAgeYears + " years for " + plan.getPlanName() + ".",
                currentMileage,
                plan.getMaxMileageCoverage(),
                vehicleAge,
                maxAgeYears
            );
        }

        return new EligibilityCheckResponse(
            true,
            "Vehicle qualifies for " + plan.getPlanName() + "! Age: " + vehicleAge + " years, Current mileage: " + currentMileage + " miles.",
            currentMileage,
            plan.getMaxMileageCoverage(),
            vehicleAge,
            maxAgeYears
        );
    }

    @Transactional
    public WarrantyContractDto createContract(WarrantyContractDto dto, String performedBy) {
        Customer customer = customerService.getCustomerEntity(dto.customerId());
        Vehicle vehicle = vehicleService.getVehicleEntity(dto.vehicleId());
        WarrantyPlan plan = planService.getPlanEntity(dto.planId());

        // Validate eligibility
        EligibilityCheckResponse eligibility = validateEligibility(new EligibilityCheckRequest(vehicle.getId(), plan.getId(), vehicle.getMileage()));
        if (!eligibility.eligible()) {
            throw new BadRequestException("Eligibility check failed: " + eligibility.message());
        }

        LocalDate startDate = (dto.startDate() != null) ? dto.startDate() : LocalDate.now();
        LocalDate endDate = (dto.endDate() != null) ? dto.endDate() : startDate.plusMonths(plan.getDurationMonths());
        int startMileage = (dto.startMileage() != null) ? dto.startMileage() : vehicle.getMileage();
        int maxMileage = (dto.maxMileage() != null) ? dto.maxMileage() : (startMileage + plan.getMaxMileageCoverage());

        BigDecimal totalPremium = (dto.totalPremium() != null) ? dto.totalPremium() : plan.getBasePremium();
        BigDecimal monthlyPremium = totalPremium.divide(BigDecimal.valueOf(plan.getDurationMonths()), 2, RoundingMode.HALF_UP);

        String contractNumber = "WC-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        WarrantyContract contract = new WarrantyContract();
        contract.setContractNumber(contractNumber);
        contract.setCustomer(customer);
        contract.setVehicle(vehicle);
        contract.setPlan(plan);
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        contract.setStartMileage(startMileage);
        contract.setMaxMileage(maxMileage);
        contract.setTotalPremium(totalPremium);
        contract.setMonthlyPremium(monthlyPremium);
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setRenewalCount(0);
        contract.setNotes(dto.notes());

        WarrantyContract saved = contractRepository.save(contract);

        auditService.log("WARRANTY_CONTRACTS", saved.getId(), "CREATE", performedBy != null ? performedBy : "SYSTEM",
            "Created warranty contract " + saved.getContractNumber() + " for vehicle VIN: " + vehicle.getVin());

        return toDto(saved);
    }

    @Transactional
    public WarrantyContractDto renewContract(Long id, ContractRenewalDto dto, String performedBy) {
        WarrantyContract contract = getContractEntity(id);

        if (contract.getStatus() == ContractStatus.CANCELLED) {
            throw new BadRequestException("Cannot renew a cancelled contract.");
        }

        contract.setEndDate(contract.getEndDate().plusMonths(dto.additionalMonths()));
        contract.setMaxMileage(contract.getMaxMileage() + dto.additionalMileage());
        contract.setRenewalCount(contract.getRenewalCount() + 1);
        contract.setStatus(ContractStatus.ACTIVE);

        if (dto.updatedTotalPremium() != null) {
            contract.setTotalPremium(contract.getTotalPremium().add(dto.updatedTotalPremium()));
        }

        if (dto.notes() != null) {
            String existingNotes = contract.getNotes() != null ? contract.getNotes() + " | " : "";
            contract.setNotes(existingNotes + dto.notes());
        }

        WarrantyContract updated = contractRepository.save(contract);

        auditService.log("WARRANTY_CONTRACTS", updated.getId(), "RENEW", performedBy != null ? performedBy : "SYSTEM",
            "Renewed contract " + updated.getContractNumber() + " (Renewal #" + updated.getRenewalCount() + ")");

        return toDto(updated);
    }

    @Transactional
    public WarrantyContractDto updateStatus(Long id, ContractStatus newStatus, String performedBy) {
        WarrantyContract contract = getContractEntity(id);
        ContractStatus oldStatus = contract.getStatus();
        contract.setStatus(newStatus);
        WarrantyContract updated = contractRepository.save(contract);

        auditService.log("WARRANTY_CONTRACTS", updated.getId(), "STATUS_CHANGE", performedBy != null ? performedBy : "SYSTEM",
            "Changed contract " + updated.getContractNumber() + " status from " + oldStatus + " to " + newStatus);

        return toDto(updated);
    }

    public WarrantyContractDto toDto(WarrantyContract c) {
        return new WarrantyContractDto(
            c.getId(),
            c.getContractNumber(),
            c.getCustomer() != null ? c.getCustomer().getId() : null,
            c.getCustomer() != null ? c.getCustomer().getFullName() : null,
            c.getVehicle() != null ? c.getVehicle().getId() : null,
            c.getVehicle() != null ? c.getVehicle().getVin() : null,
            c.getVehicle() != null ? (c.getVehicle().getModelYear() + " " + c.getVehicle().getMake() + " " + c.getVehicle().getModel()) : null,
            c.getPlan() != null ? c.getPlan().getId() : null,
            c.getPlan() != null ? c.getPlan().getPlanName() : null,
            c.getPlan() != null ? c.getPlan().getPlanType() : null,
            c.getStartDate(),
            c.getEndDate(),
            c.getStartMileage(),
            c.getMaxMileage(),
            c.getTotalPremium(),
            c.getMonthlyPremium(),
            c.getStatus(),
            c.getRenewalCount(),
            c.getNotes(),
            c.getCreatedAt()
        );
    }
}
