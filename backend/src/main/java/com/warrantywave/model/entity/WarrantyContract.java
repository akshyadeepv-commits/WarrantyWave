package com.warrantywave.model.entity;

import com.warrantywave.model.enums.ContractStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "WARRANTY_CONTRACTS")
public class WarrantyContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CONTRACT_NUMBER", nullable = false, unique = true, length = 50)
    private String contractNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "VEHICLE_ID", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PLAN_ID", nullable = false)
    private WarrantyPlan plan;

    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "END_DATE", nullable = false)
    private LocalDate endDate;

    @Column(name = "START_MILEAGE", nullable = false)
    private Integer startMileage;

    @Column(name = "MAX_MILEAGE", nullable = false)
    private Integer maxMileage;

    @Column(name = "TOTAL_PREMIUM", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPremium;

    @Column(name = "MONTHLY_PREMIUM", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPremium;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ContractStatus status = ContractStatus.ACTIVE;

    @Column(name = "RENEWAL_COUNT", nullable = false)
    private Integer renewalCount = 0;

    @Column(length = 500)
    private String notes;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private OffsetDateTime updatedAt;

    public WarrantyContract() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public WarrantyPlan getPlan() {
        return plan;
    }

    public void setPlan(WarrantyPlan plan) {
        this.plan = plan;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getStartMileage() {
        return startMileage;
    }

    public void setStartMileage(Integer startMileage) {
        this.startMileage = startMileage;
    }

    public Integer getMaxMileage() {
        return maxMileage;
    }

    public void setMaxMileage(Integer maxMileage) {
        this.maxMileage = maxMileage;
    }

    public BigDecimal getTotalPremium() {
        return totalPremium;
    }

    public void setTotalPremium(BigDecimal totalPremium) {
        this.totalPremium = totalPremium;
    }

    public BigDecimal getMonthlyPremium() {
        return monthlyPremium;
    }

    public void setMonthlyPremium(BigDecimal monthlyPremium) {
        this.monthlyPremium = monthlyPremium;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    public Integer getRenewalCount() {
        return renewalCount;
    }

    public void setRenewalCount(Integer renewalCount) {
        this.renewalCount = renewalCount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
