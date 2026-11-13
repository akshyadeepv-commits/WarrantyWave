package com.warrantywave.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.warrantywave.model.enums.VehicleCondition;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "VEHICLES")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 17)
    private String vin;

    @Column(nullable = false, length = 60)
    private String make;

    @Column(nullable = false, length = 60)
    private String model;

    @Column(name = "MODEL_YEAR", nullable = false)
    private Integer modelYear;

    @Column(nullable = false)
    private Integer mileage;

    @Column(name = "PURCHASE_PRICE", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "PURCHASE_DATE", nullable = false)
    private LocalDate purchaseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "VEHICLE_CONDITION", nullable = false, length = 30)
    private VehicleCondition vehicleCondition = VehicleCondition.USED;

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, TRANSFERRED

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    @JsonIgnoreProperties("vehicles")
    private Customer customer;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private OffsetDateTime updatedAt;

    public Vehicle() {
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

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public void setModelYear(Integer modelYear) {
        this.modelYear = modelYear;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public VehicleCondition getVehicleCondition() {
        return vehicleCondition;
    }

    public void setVehicleCondition(VehicleCondition vehicleCondition) {
        this.vehicleCondition = vehicleCondition;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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
