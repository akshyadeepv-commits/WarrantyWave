package com.warrantywave.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardKpiDto(
    long totalCustomers,
    long totalVehicles,
    long activeContracts,
    BigDecimal totalFinancingVolume,
    BigDecimal totalApprovedClaimsAmount,
    long pendingClaimsCount,
    long pendingFinancingCount,
    BigDecimal monthlyRevenue,
    double claimsLossRatio,
    List<ClaimResponseDto> recentClaims,
    List<FinancingResponseDto> recentFinancing,
    List<PlanDistributionDto> planDistribution
) {}
