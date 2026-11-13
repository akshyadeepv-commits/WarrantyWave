package com.warrantywave.model.dto;

import com.warrantywave.model.enums.ClaimStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ClaimResponseDto(
    Long id,
    String claimNumber,
    Long contractId,
    String contractNumber,
    Long customerId,
    String customerName,
    Long vehicleId,
    String vehicleVin,
    String vehicleName,
    LocalDate incidentDate,
    Integer currentMileage,
    String description,
    String repairShopName,
    BigDecimal repairEstimate,
    BigDecimal approvedAmount,
    BigDecimal deductiblePaid,
    ClaimStatus status,
    String rejectionReason,
    String officerNotes,
    String reviewerName,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
