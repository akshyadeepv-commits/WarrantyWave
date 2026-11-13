package com.warrantywave.model.dto;

import com.warrantywave.model.enums.CreditTier;
import com.warrantywave.model.enums.FinancingStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record FinancingResponseDto(
    Long id,
    String applicationNumber,
    Long customerId,
    String customerName,
    Long vehicleId,
    String vehicleVin,
    String vehicleName,
    BigDecimal vehiclePrice,
    BigDecimal downPayment,
    BigDecimal loanAmount,
    Integer loanTermMonths,
    BigDecimal interestRate,
    BigDecimal calculatedEmi,
    FinancingStatus status,
    BigDecimal monthlyIncome,
    BigDecimal debtToIncomeRatio,
    CreditTier creditTier,
    String officerNotes,
    String reviewerName,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
