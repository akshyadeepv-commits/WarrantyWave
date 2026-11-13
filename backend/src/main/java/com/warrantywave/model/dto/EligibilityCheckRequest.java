package com.warrantywave.model.dto;

import jakarta.validation.constraints.NotNull;

public record EligibilityCheckRequest(
    @NotNull(message = "Vehicle ID is required")
    Long vehicleId,

    @NotNull(message = "Plan ID is required")
    Long planId,

    Integer currentMileage
) {}
