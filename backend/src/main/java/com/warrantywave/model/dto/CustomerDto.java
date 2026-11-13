package com.warrantywave.model.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CustomerDto(
    Long id,

    @NotBlank(message = "First name is required")
    String firstName,

    @NotBlank(message = "Last name is required")
    String lastName,

    String fullName,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Phone number is required")
    String phone,

    String streetAddress,
    String city,
    String stateProvince,
    String postalCode,

    @NotNull(message = "Credit score is required")
    @Min(value = 300, message = "Credit score must be at least 300")
    @Max(value = 850, message = "Credit score cannot exceed 850")
    Integer creditScore,

    @NotNull(message = "Annual income is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Annual income must be greater than zero")
    BigDecimal annualIncome,

    @NotBlank(message = "Employment status is required")
    String employmentStatus,

    Integer vehicleCount
) {}
