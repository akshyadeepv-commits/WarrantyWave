package com.warrantywave.model.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ClaimReviewDto(
    @NotNull(message = "Approval status flag is required")
    Boolean approved,

    BigDecimal approvedAmount,
    BigDecimal deductiblePaid,
    String officerNotes,
    String rejectionReason
) {}
