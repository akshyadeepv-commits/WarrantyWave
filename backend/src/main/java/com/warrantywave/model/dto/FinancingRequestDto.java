package com.warrantywave.model.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record FinancingRequestDto(
    @NotNull(message = "Customer ID is required")
    Long customerId,

    @NotNull(message = "Vehicle ID is required")
    Long vehicleId,

    @NotNull(message = "Vehicle price is required")
    @DecimalMin(value = "500.0", message = "Vehicle price must be at least 500")
    BigDecimal vehiclePrice,

    @NotNull(message = "Down payment is required")
    @DecimalMin(value = "0.0", message = "Down payment cannot be negative")
    BigDecimal downPayment,

    @NotNull(message = "Loan term in months is required")
    @Min(value = 12, message = "Minimum loan term is 12 months")
    @Max(value = 84, message = "Maximum loan term is 84 months")
    Integer loanTermMonths
) {}
