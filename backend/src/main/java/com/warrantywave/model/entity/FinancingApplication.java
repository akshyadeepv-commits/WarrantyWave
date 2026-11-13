package com.warrantywave.model.entity;

import com.warrantywave.model.enums.CreditTier;
import com.warrantywave.model.enums.FinancingStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "FINANCING_APPLICATIONS")
public class FinancingApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "APPLICATION_NUMBER", nullable = false, unique = true, length = 50)
    private String applicationNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "VEHICLE_ID", nullable = false)
    private Vehicle vehicle;

    @Column(name = "VEHICLE_PRICE", nullable = false, precision = 12, scale = 2)
    private BigDecimal vehiclePrice;

    @Column(name = "DOWN_PAYMENT", nullable = false, precision = 12, scale = 2)
    private BigDecimal downPayment;

    @Column(name = "LOAN_AMOUNT", nullable = false, precision = 12, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "LOAN_TERM_MONTHS", nullable = false)
    private Integer loanTermMonths;

    @Column(name = "INTEREST_RATE", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "CALCULATED_EMI", nullable = false, precision = 10, scale = 2)
    private BigDecimal calculatedEmi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FinancingStatus status = FinancingStatus.SUBMITTED;

    @Column(name = "MONTHLY_INCOME", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "DEBT_TO_INCOME_RATIO", nullable = false, precision = 5, scale = 2)
    private BigDecimal debtToIncomeRatio;

    @Enumerated(EnumType.STRING)
    @Column(name = "CREDIT_TIER", nullable = false, length = 30)
    private CreditTier creditTier;

    @Column(name = "OFFICER_NOTES", length = 1000)
    private String officerNotes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "REVIEWER_ID")
    private User reviewer;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private OffsetDateTime updatedAt;

    public FinancingApplication() {
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

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
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

    public BigDecimal getVehiclePrice() {
        return vehiclePrice;
    }

    public void setVehiclePrice(BigDecimal vehiclePrice) {
        this.vehiclePrice = vehiclePrice;
    }

    public BigDecimal getDownPayment() {
        return downPayment;
    }

    public void setDownPayment(BigDecimal downPayment) {
        this.downPayment = downPayment;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public Integer getLoanTermMonths() {
        return loanTermMonths;
    }

    public void setLoanTermMonths(Integer loanTermMonths) {
        this.loanTermMonths = loanTermMonths;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal getCalculatedEmi() {
        return calculatedEmi;
    }

    public void setCalculatedEmi(BigDecimal calculatedEmi) {
        this.calculatedEmi = calculatedEmi;
    }

    public FinancingStatus getStatus() {
        return status;
    }

    public void setStatus(FinancingStatus status) {
        this.status = status;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getDebtToIncomeRatio() {
        return debtToIncomeRatio;
    }

    public void setDebtToIncomeRatio(BigDecimal debtToIncomeRatio) {
        this.debtToIncomeRatio = debtToIncomeRatio;
    }

    public CreditTier getCreditTier() {
        return creditTier;
    }

    public void setCreditTier(CreditTier creditTier) {
        this.creditTier = creditTier;
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
