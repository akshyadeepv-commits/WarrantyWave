package com.warrantywave.model.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ClaimRequestDto(
    @NotNull(message = "Contract ID is required")
    Long contractId,

    @NotNull(message = "Incident date is required")
    LocalDate incidentDate,

    @NotNull(message = "Current vehicle mileage is required")
    @Min(value = 0, message = "Mileage cannot be negative")
    Integer currentMileage,

    @NotBlank(message = "Claim description is required")
    String description,

    @NotBlank(message = "Repair shop name is required")
    String repairShopName,

    @NotNull(message = "Repair estimate is required")
    @DecimalMin(value = "1.0", message = "Repair estimate must be greater than zero")
    BigDecimal repairEstimate
) {}
