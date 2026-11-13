package com.warrantywave.model.dto;

import com.warrantywave.model.enums.ContractStatus;
import com.warrantywave.model.enums.PlanType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record WarrantyContractDto(
    Long id,
    String contractNumber,

    @NotNull(message = "Customer ID is required")
    Long customerId,
    String customerName,

    @NotNull(message = "Vehicle ID is required")
    Long vehicleId,
    String vehicleVin,
    String vehicleName,

    @NotNull(message = "Plan ID is required")
    Long planId,
    String planName,
    PlanType planType,

    LocalDate startDate,
    LocalDate endDate,
    Integer startMileage,
    Integer maxMileage,
    BigDecimal totalPremium,
    BigDecimal monthlyPremium,
    ContractStatus status,
    Integer renewalCount,
    String notes,
    OffsetDateTime createdAt
) {}
