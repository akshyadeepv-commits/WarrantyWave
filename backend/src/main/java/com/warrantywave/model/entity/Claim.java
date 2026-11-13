package com.warrantywave.model.entity;

import com.warrantywave.model.enums.ClaimStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "CLAIMS")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CLAIM_NUMBER", nullable = false, unique = true, length = 50)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CONTRACT_ID", nullable = false)
    private WarrantyContract contract;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "VEHICLE_ID", nullable = false)
    private Vehicle vehicle;

    @Column(name = "INCIDENT_DATE", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "CURRENT_MILEAGE", nullable = false)
    private Integer currentMileage;

    @Column(nullable = false, length = 1500)
    private String description;

    @Column(name = "REPAIR_SHOP_NAME", nullable = false, length = 120)
    private String repairShopName;

    @Column(name = "REPAIR_ESTIMATE", nullable = false, precision = 10, scale = 2)
    private BigDecimal repairEstimate;

    @Column(name = "APPROVED_AMOUNT", nullable = false, precision = 10, scale = 2)
    private BigDecimal approvedAmount = BigDecimal.ZERO;

    @Column(name = "DEDUCTIBLE_PAID", nullable = false, precision = 10, scale = 2)
    private BigDecimal deductiblePaid = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClaimStatus status = ClaimStatus.SUBMITTED;

    @Column(name = "REJECTION_REASON", length = 500)
    private String rejectionReason;

    @Column(name = "OFFICER_NOTES", length = 1000)
    private String officerNotes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "REVIEWER_ID")
    private User reviewer;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private OffsetDateTime updatedAt;

    public Claim() {
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

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public WarrantyContract getContract() {
        return contract;
    }

    public void setContract(WarrantyContract contract) {
        this.contract = contract;
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

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public Integer getCurrentMileage() {
        return currentMileage;
    }

    public void setCurrentMileage(Integer currentMileage) {
        this.currentMileage = currentMileage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRepairShopName() {
        return repairShopName;
    }

    public void setRepairShopName(String repairShopName) {
        this.repairShopName = repairShopName;
    }

    public BigDecimal getRepairEstimate() {
        return repairEstimate;
    }

    public void setRepairEstimate(BigDecimal repairEstimate) {
        this.repairEstimate = repairEstimate;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public BigDecimal getDeductiblePaid() {
        return deductiblePaid;
    }

    public void setDeductiblePaid(BigDecimal deductiblePaid) {
        this.deductiblePaid = deductiblePaid;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getOfficerNotes() {
        return officerNotes;
    }

    public void setOfficerNotes(String officerNotes) {
        this.officerNotes = officerNotes;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
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
