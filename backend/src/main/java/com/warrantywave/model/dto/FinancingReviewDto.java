package com.warrantywave.model.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FinancingReviewDto(
    @NotNull(message = "Approval status flag is required")
    Boolean approved,

    BigDecimal customInterestRate,
    String officerNotes
) {}
