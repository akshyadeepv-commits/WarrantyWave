package com.warrantywave.service;

import com.warrantywave.model.dto.*;
import com.warrantywave.model.entity.WarrantyPlan;
import com.warrantywave.model.enums.ClaimStatus;
import com.warrantywave.model.enums.ContractStatus;
import com.warrantywave.model.enums.FinancingStatus;
import com.warrantywave.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final WarrantyContractRepository contractRepository;
    private final ClaimRepository claimRepository;
    private final FinancingApplicationRepository financingRepository;
    private final WarrantyPlanRepository planRepository;
    private final ClaimService claimService;
    private final FinancingService financingService;

    public DashboardService(
        CustomerRepository customerRepository,
        VehicleRepository vehicleRepository,
        WarrantyContractRepository contractRepository,
        ClaimRepository claimRepository,
        FinancingApplicationRepository financingRepository,
        WarrantyPlanRepository planRepository,
        ClaimService claimService,
        FinancingService financingService
    ) {
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.contractRepository = contractRepository;
        this.claimRepository = claimRepository;
        this.financingRepository = financingRepository;
        this.planRepository = planRepository;
        this.claimService = claimService;
        this.financingService = financingService;
    }

    public DashboardKpiDto getDashboardKpis() {
        long totalCustomers = customerRepository.count();
        long totalVehicles = vehicleRepository.count();
        long activeContracts = contractRepository.countByStatus(ContractStatus.ACTIVE);
        long pendingClaimsCount = claimRepository.countByStatus(ClaimStatus.SUBMITTED) + claimRepository.countByStatus(ClaimStatus.UNDER_REVIEW);
        long pendingFinancingCount = financingRepository.countByStatus(FinancingStatus.SUBMITTED) + financingRepository.countByStatus(FinancingStatus.UNDER_REVIEW);

        BigDecimal totalFinancingVolume = financingRepository.sumApprovedLoanAmount();
        BigDecimal totalApprovedClaims = claimRepository.sumApprovedClaimsAmount();

        // Calculate monthly revenue from active warranty contracts
        BigDecimal monthlyRevenue = contractRepository.findAll().stream()
            .filter(c -> c.getStatus() == ContractStatus.ACTIVE)
            .map(c -> c.getMonthlyPremium() != null ? c.getMonthlyPremium() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Annualized revenue for Loss Ratio calculation
        BigDecimal annualizedRevenue = monthlyRevenue.multiply(BigDecimal.valueOf(12));
        double lossRatio = 0.0;
        if (annualizedRevenue.compareTo(BigDecimal.ZERO) > 0 && totalApprovedClaims.compareTo(BigDecimal.ZERO) > 0) {
            lossRatio = totalApprovedClaims.divide(annualizedRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        }

        // Recent claims and financing
        List<ClaimResponseDto> recentClaims = claimRepository.findTop5ByOrderByCreatedAtDesc().stream()
            .map(claimService::toDto)
            .toList();

        List<FinancingResponseDto> recentFinancing = financingRepository.findTop5ByOrderByCreatedAtDesc().stream()
            .map(financingService::toDto)
            .toList();

        // Warranty plan distribution
        List<WarrantyPlan> plans = planRepository.findAll();
        List<PlanDistributionDto> planDistribution = new ArrayList<>();
        long totalActive = (activeContracts > 0) ? activeContracts : 1;

        for (WarrantyPlan plan : plans) {
            long count = contractRepository.findAll().stream()
                .filter(c -> c.getStatus() == ContractStatus.ACTIVE && c.getPlan().getId().equals(plan.getId()))
                .count();

            double pct = ((double) count / totalActive) * 100.0;
            planDistribution.add(new PlanDistributionDto(
                plan.getPlanName(),
                plan.getPlanType().name(),
                count,
                Math.round(pct * 10.0) / 10.0
            ));
        }

        return new DashboardKpiDto(
            totalCustomers,
            totalVehicles,
            activeContracts,
            totalFinancingVolume,
            totalApprovedClaims,
            pendingClaimsCount,
            pendingFinancingCount,
            monthlyRevenue,
            lossRatio,
            recentClaims,
            recentFinancing,
            planDistribution
        );
    }
}
