package com.warrantywave.model.entity;

import com.warrantywave.model.enums.PlanType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "WARRANTY_PLANS")
public class WarrantyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PLAN_CODE", nullable = false, unique = true, length = 30)
    private String planCode;

    @Column(name = "PLAN_NAME", nullable = false, length = 100)
    private String planName;

    @Enumerated(EnumType.STRING)
    @Column(name = "PLAN_TYPE", nullable = false, length = 40)
    private PlanType planType;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "DURATION_MONTHS", nullable = false)
    private Integer durationMonths;

    @Column(name = "MAX_MILEAGE_COVERAGE", nullable = false)
    private Integer maxMileageCoverage;

    @Column(name = "DEDUCTIBLE_AMOUNT", nullable = false, precision = 10, scale = 2)
    private BigDecimal deductibleAmount;

    @Column(name = "BASE_PREMIUM", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePremium;

    @Column(name = "MAX_CLAIM_LIMIT", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxClaimLimit;

    @Column(name = "IS_ACTIVE", nullable = false)
    private boolean active = true;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public WarrantyPlan() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(PlanType planType) {
        this.planType = planType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }

    public Integer getMaxMileageCoverage() {
        return maxMileageCoverage;
    }

    public void setMaxMileageCoverage(Integer maxMileageCoverage) {
        this.maxMileageCoverage = maxMileageCoverage;
    }

    public BigDecimal getDeductibleAmount() {
        return deductibleAmount;
    }

    public void setDeductibleAmount(BigDecimal deductibleAmount) {
        this.deductibleAmount = deductibleAmount;
    }

    public BigDecimal getBasePremium() {
        return basePremium;
    }

    public void setBasePremium(BigDecimal basePremium) {
        this.basePremium = basePremium;
    }

    public BigDecimal getMaxClaimLimit() {
        return maxClaimLimit;
    }

    public void setMaxClaimLimit(BigDecimal maxClaimLimit) {
        this.maxClaimLimit = maxClaimLimit;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
