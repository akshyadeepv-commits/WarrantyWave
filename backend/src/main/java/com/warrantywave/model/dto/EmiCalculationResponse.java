package com.warrantywave.model.dto;

import com.warrantywave.model.enums.CreditTier;
import java.math.BigDecimal;

public record EmiCalculationResponse(
    BigDecimal vehiclePrice,
    BigDecimal downPayment,
    BigDecimal loanAmount,
    Integer loanTermMonths,
    BigDecimal estimatedInterestRate,
    BigDecimal monthlyEmi,
    BigDecimal totalPayment,
    BigDecimal totalInterest,
    CreditTier creditTier
) {}
