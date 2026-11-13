package com.warrantywave.model.dto;

import com.warrantywave.model.enums.VehicleCondition;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record VehicleDto(
    Long id,

    @NotBlank(message = "VIN is required")
    @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters")
    String vin,

    @NotBlank(message = "Vehicle make is required")
    String make,

    @NotBlank(message = "Vehicle model is required")
    String model,

    @NotNull(message = "Model year is required")
    @Min(value = 1990, message = "Model year must be 1990 or newer")
    Integer modelYear,

    @NotNull(message = "Mileage is required")
    @Min(value = 0, message = "Mileage cannot be negative")
    Integer mileage,

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "1.0", message = "Purchase price must be positive")
    BigDecimal purchasePrice,

    @NotNull(message = "Purchase date is required")
    LocalDate purchaseDate,

    VehicleCondition vehicleCondition,
    String status,

    @NotNull(message = "Customer ID is required")
    Long customerId,

    String customerName
) {}
