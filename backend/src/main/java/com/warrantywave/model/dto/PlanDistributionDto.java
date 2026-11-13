package com.warrantywave.model.dto;

public record PlanDistributionDto(
    String planName,
    String planType,
    long activeContractsCount,
    double percentage
) {}
