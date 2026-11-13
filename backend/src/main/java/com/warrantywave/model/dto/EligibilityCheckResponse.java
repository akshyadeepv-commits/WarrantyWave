package com.warrantywave.model.dto;

public record EligibilityCheckResponse(
    boolean eligible,
    String message,
    Integer vehicleMileage,
    Integer maxMileageCoverage,
    Integer vehicleAgeYears,
    Integer maxAgeAllowedYears
) {}
