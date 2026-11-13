package com.warrantywave.service;

import com.warrantywave.exception.BadRequestException;
import com.warrantywave.exception.ResourceNotFoundException;
import com.warrantywave.model.dto.*;
import com.warrantywave.model.entity.*;
import com.warrantywave.model.enums.CreditTier;
import com.warrantywave.model.enums.FinancingStatus;
import com.warrantywave.repository.FinancingApplicationRepository;
import com.warrantywave.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FinancingService {

    private final FinancingApplicationRepository financingRepository;
    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public FinancingService(
        FinancingApplicationRepository financingRepository,
        CustomerService customerService,
        VehicleService vehicleService,
        UserRepository userRepository,
        AuditService auditService
    ) {
        this.financingRepository = financingRepository;
        this.customerService = customerService;
        this.vehicleService = vehicleService;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public Page<FinancingResponseDto> getAllApplications(FinancingStatus status, String query, Pageable pageable) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        Page<FinancingApplication> page = financingRepository.findFiltered(status, cleanQuery, pageable);
        return page.map(this::toDto);
    }

    public FinancingResponseDto getApplicationById(Long id) {
        return financingRepository.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Financing application not found with id: " + id));
    }

    public List<FinancingResponseDto> getApplicationsByCustomerId(Long customerId) {
        return financingRepository.findByCustomerId(customerId).stream()
            .map(this::toDto)
            .toList();
    }

    public EmiCalculationResponse calculateEmi(EmiCalculationRequest request) {
        if (request.downPayment().compareTo(request.vehiclePrice()) >= 0) {
            throw new BadRequestException("Down payment cannot be equal to or greater than the vehicle price.");
        }

        BigDecimal loanAmount = request.vehiclePrice().subtract(request.downPayment());
        int creditScore = request.creditScore() != null ? request.creditScore() : 700;
        CreditTier tier = evaluateCreditTier(creditScore);
        BigDecimal interestRate = determineInterestRateForTier(tier);

        BigDecimal monthlyEmi = computeEmi(loanAmount, interestRate, request.loanTermMonths());
        BigDecimal totalPayment = monthlyEmi.multiply(BigDecimal.valueOf(request.loanTermMonths())).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalInterest = totalPayment.subtract(loanAmount).setScale(2, RoundingMode.HALF_UP);

        return new EmiCalculationResponse(
            request.vehiclePrice(),
            request.downPayment(),
            loanAmount,
            request.loanTermMonths(),
            interestRate,
            monthlyEmi,
            totalPayment,
            totalInterest,
            tier
        );
    }

    @Transactional
    public FinancingResponseDto submitApplication(FinancingRequestDto dto, String performedBy) {
        Customer customer = customerService.getCustomerEntity(dto.customerId());
        Vehicle vehicle = vehicleService.getVehicleEntity(dto.vehicleId());

        if (dto.downPayment().compareTo(dto.vehiclePrice()) >= 0) {
            throw new BadRequestException("Down payment cannot equal or exceed vehicle price.");
        }

        BigDecimal loanAmount = dto.vehiclePrice().subtract(dto.downPayment());
        CreditTier tier = evaluateCreditTier(customer.getCreditScore());
        BigDecimal interestRate = determineInterestRateForTier(tier);
        BigDecimal calculatedEmi = computeEmi(loanAmount, interestRate, dto.loanTermMonths());

        BigDecimal monthlyIncome = customer.getAnnualIncome().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal dtiRatio = (monthlyIncome.compareTo(BigDecimal.ZERO) > 0)
            ? calculatedEmi.divide(monthlyIncome, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.valueOf(100.00);

        String applicationNumber = "FIN-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        FinancingApplication application = new FinancingApplication();
        application.setApplicationNumber(applicationNumber);
        application.setCustomer(customer);
        application.setVehicle(vehicle);
        application.setVehiclePrice(dto.vehiclePrice());
        application.setDownPayment(dto.downPayment());
        application.setLoanAmount(loanAmount);
        application.setLoanTermMonths(dto.loanTermMonths());
        application.setInterestRate(interestRate);
        application.setCalculatedEmi(calculatedEmi);
        application.setStatus(FinancingStatus.SUBMITTED);
        application.setMonthlyIncome(monthlyIncome);
        application.setDebtToIncomeRatio(dtiRatio);
        application.setCreditTier(tier);

        FinancingApplication saved = financingRepository.save(application);

        auditService.log("FINANCING_APPLICATIONS", saved.getId(), "SUBMIT", performedBy != null ? performedBy : "SYSTEM",
            "Submitted auto financing application " + saved.getApplicationNumber() + " for loan amount $" + saved.getLoanAmount() + " (DTI: " + dtiRatio + "%)");

        return toDto(saved);
    }

    @Transactional
    public FinancingResponseDto reviewApplication(Long id, FinancingReviewDto dto, String reviewerUsername) {
        FinancingApplication application = financingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Financing application not found with id: " + id));

        User reviewer = userRepository.findByUsername(reviewerUsername).orElse(null);
        application.setReviewer(reviewer);
        application.setOfficerNotes(dto.officerNotes());

        if (Boolean.TRUE.equals(dto.approved())) {
            if (dto.customInterestRate() != null && dto.customInterestRate().compareTo(BigDecimal.ZERO) > 0) {
                application.setInterestRate(dto.customInterestRate());
                BigDecimal newEmi = computeEmi(application.getLoanAmount(), application.getInterestRate(), application.getLoanTermMonths());
                application.setCalculatedEmi(newEmi);
            }
            application.setStatus(FinancingStatus.APPROVED);

            auditService.log("FINANCING_APPLICATIONS", application.getId(), "APPROVE", reviewerUsername,
                "Approved financing application " + application.getApplicationNumber() + " at " + application.getInterestRate() + "% APR. EMI: $" + application.getCalculatedEmi());
        } else {
            application.setStatus(FinancingStatus.REJECTED);

            auditService.log("FINANCING_APPLICATIONS", application.getId(), "REJECT", reviewerUsername,
                "Rejected financing application " + application.getApplicationNumber() + ". Notes: " + application.getOfficerNotes());
        }

        FinancingApplication updated = financingRepository.save(application);
        return toDto(updated);
    }

    public CreditTier evaluateCreditTier(int creditScore) {
        if (creditScore >= 750) return CreditTier.TIER_1_EXCELLENT;
        if (creditScore >= 670) return CreditTier.TIER_2_GOOD;
        if (creditScore >= 580) return CreditTier.TIER_3_FAIR;
        return CreditTier.HIGH_RISK;
    }

    public BigDecimal determineInterestRateForTier(CreditTier tier) {
        return switch (tier) {
            case TIER_1_EXCELLENT -> new BigDecimal("5.25");
            case TIER_2_GOOD -> new BigDecimal("6.49");
            case TIER_3_FAIR -> new BigDecimal("9.99");
            case HIGH_RISK -> new BigDecimal("15.49");
        };
    }

    public BigDecimal computeEmi(BigDecimal principal, BigDecimal annualRatePercent, int months) {
        if (principal.compareTo(BigDecimal.ZERO) <= 0 || months <= 0) {
            return BigDecimal.ZERO;
        }

        double p = principal.doubleValue();
        double r = (annualRatePercent.doubleValue() / 100.0) / 12.0;
        int n = months;

        double emi = (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }

    public FinancingResponseDto toDto(FinancingApplication f) {
        return new FinancingResponseDto(
            f.getId(),
            f.getApplicationNumber(),
            f.getCustomer() != null ? f.getCustomer().getId() : null,
            f.getCustomer() != null ? f.getCustomer().getFullName() : null,
            f.getVehicle() != null ? f.getVehicle().getId() : null,
            f.getVehicle() != null ? f.getVehicle().getVin() : null,
            f.getVehicle() != null ? (f.getVehicle().getModelYear() + " " + f.getVehicle().getMake() + " " + f.getVehicle().getModel()) : null,
            f.getVehiclePrice(),
            f.getDownPayment(),
            f.getLoanAmount(),
            f.getLoanTermMonths(),
            f.getInterestRate(),
            f.getCalculatedEmi(),
            f.getStatus(),
            f.getMonthlyIncome(),
            f.getDebtToIncomeRatio(),
            f.getCreditTier(),
            f.getOfficerNotes(),
            f.getReviewer() != null ? f.getReviewer().getFullName() : null,
            f.getCreatedAt(),
            f.getUpdatedAt()
        );
    }
}
