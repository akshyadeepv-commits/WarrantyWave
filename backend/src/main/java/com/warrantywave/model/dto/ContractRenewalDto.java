package com.warrantywave.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ContractRenewalDto(
    @NotNull(message = "Extension months is required")
    @Min(value = 6, message = "Renewal extension must be at least 6 months")
    Integer additionalMonths,

    @NotNull(message = "Extension mileage is required")
    @Min(value = 5000, message = "Renewal mileage must be at least 5,000 miles")
    Integer additionalMileage,

    BigDecimal updatedTotalPremium,
    String notes
) {}
