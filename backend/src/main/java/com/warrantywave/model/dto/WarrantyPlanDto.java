package com.warrantywave.model.dto;

import com.warrantywave.model.enums.PlanType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record WarrantyPlanDto(
    Long id,

    @NotBlank(message = "Plan code is required")
    String planCode,

    @NotBlank(message = "Plan name is required")
    String planName,

    @NotNull(message = "Plan type is required")
    PlanType planType,

    @NotBlank(message = "Description is required")
    String description,

    @NotNull(message = "Duration in months is required")
    @Min(value = 1, message = "Duration must be at least 1 month")
    Integer durationMonths,

    @NotNull(message = "Max mileage coverage is required")
    @Min(value = 1000, message = "Max mileage must be at least 1,000 miles")
    Integer maxMileageCoverage,

    @NotNull(message = "Deductible amount is required")
    @DecimalMin(value = "0.0", message = "Deductible cannot be negative")
    BigDecimal deductibleAmount,

    @NotNull(message = "Base premium is required")
    @DecimalMin(value = "1.0", message = "Base premium must be positive")
    BigDecimal basePremium,

    @NotNull(message = "Max claim limit is required")
    @DecimalMin(value = "100.0", message = "Max claim limit must be at least 100.00")
    BigDecimal maxClaimLimit,

    boolean active
) {}
