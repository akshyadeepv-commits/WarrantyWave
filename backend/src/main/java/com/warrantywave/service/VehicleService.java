package com.warrantywave.service;

import com.warrantywave.exception.BadRequestException;
import com.warrantywave.exception.ResourceNotFoundException;
import com.warrantywave.model.dto.VehicleDto;
import com.warrantywave.model.entity.Customer;
import com.warrantywave.model.entity.Vehicle;
import com.warrantywave.model.enums.VehicleCondition;
import com.warrantywave.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerService customerService;
    private final AuditService auditService;

    public VehicleService(VehicleRepository vehicleRepository, CustomerService customerService, AuditService auditService) {
        this.vehicleRepository = vehicleRepository;
        this.customerService = customerService;
        this.auditService = auditService;
    }

    public Page<VehicleDto> getAllVehicles(String query, Pageable pageable) {
        Page<Vehicle> page = (query != null && !query.trim().isEmpty())
            ? vehicleRepository.searchVehicles(query.trim(), pageable)
            : vehicleRepository.findAll(pageable);

        return page.map(this::toDto);
    }

    public List<VehicleDto> getVehiclesByCustomerId(Long customerId) {
        return vehicleRepository.findByCustomerId(customerId).stream()
            .map(this::toDto)
            .toList();
    }

    public VehicleDto getVehicleById(Long id) {
        return vehicleRepository.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    public Vehicle getVehicleEntity(Long id) {
        return vehicleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    @Transactional
    public VehicleDto createVehicle(VehicleDto dto, String performedBy) {
        String cleanVin = dto.vin().trim().toUpperCase();
        if (cleanVin.length() != 17) {
            throw new BadRequestException("VIN must be exactly 17 characters in standard ISO 3779 format.");
        }

        if (vehicleRepository.existsByVin(cleanVin)) {
            throw new BadRequestException("A vehicle with VIN " + cleanVin + " is already registered.");
        }

        Customer customer = customerService.getCustomerEntity(dto.customerId());

        Vehicle vehicle = new Vehicle();
        vehicle.setVin(cleanVin);
        vehicle.setMake(dto.make().trim());
        vehicle.setModel(dto.model().trim());
        vehicle.setModelYear(dto.modelYear());
        vehicle.setMileage(dto.mileage());
        vehicle.setPurchasePrice(dto.purchasePrice());
        vehicle.setPurchaseDate(dto.purchaseDate());
        vehicle.setVehicleCondition(dto.vehicleCondition() != null ? dto.vehicleCondition() : VehicleCondition.USED);
        vehicle.setStatus(dto.status() != null ? dto.status() : "ACTIVE");
        vehicle.setCustomer(customer);

        Vehicle saved = vehicleRepository.save(vehicle);

        auditService.log("VEHICLES", saved.getId(), "CREATE", performedBy != null ? performedBy : "SYSTEM",
            "Registered vehicle " + saved.getModelYear() + " " + saved.getMake() + " " + saved.getModel() + " (VIN: " + saved.getVin() + ")");

        return toDto(saved);
    }

    @Transactional
    public VehicleDto updateVehicle(Long id, VehicleDto dto, String performedBy) {
        Vehicle vehicle = getVehicleEntity(id);

        String cleanVin = dto.vin().trim().toUpperCase();
        if (!vehicle.getVin().equalsIgnoreCase(cleanVin) && vehicleRepository.existsByVin(cleanVin)) {
            throw new BadRequestException("VIN " + cleanVin + " is already registered to another vehicle.");
        }

        if (!vehicle.getCustomer().getId().equals(dto.customerId())) {
            Customer newCustomer = customerService.getCustomerEntity(dto.customerId());
            vehicle.setCustomer(newCustomer);
        }

        vehicle.setVin(cleanVin);
        vehicle.setMake(dto.make().trim());
        vehicle.setModel(dto.model().trim());
        vehicle.setModelYear(dto.modelYear());
        vehicle.setMileage(dto.mileage());
        vehicle.setPurchasePrice(dto.purchasePrice());
        vehicle.setPurchaseDate(dto.purchaseDate());
        if (dto.vehicleCondition() != null) {
            vehicle.setVehicleCondition(dto.vehicleCondition());
        }
        if (dto.status() != null) {
            vehicle.setStatus(dto.status());
        }

        Vehicle updated = vehicleRepository.save(vehicle);

        auditService.log("VEHICLES", updated.getId(), "UPDATE", performedBy != null ? performedBy : "SYSTEM",
            "Updated vehicle specs for VIN: " + updated.getVin());

        return toDto(updated);
    }

    @Transactional
    public void deleteVehicle(Long id, String performedBy) {
        Vehicle vehicle = getVehicleEntity(id);
        vehicleRepository.delete(vehicle);
        auditService.log("VEHICLES", id, "DELETE", performedBy != null ? performedBy : "SYSTEM",
            "Deleted vehicle VIN: " + vehicle.getVin());
    }

    public VehicleDto toDto(Vehicle v) {
        return new VehicleDto(
            v.getId(),
            v.getVin(),
            v.getMake(),
            v.getModel(),
            v.getModelYear(),
            v.getMileage(),
            v.getPurchasePrice(),
            v.getPurchaseDate(),
            v.getVehicleCondition(),
            v.getStatus(),
            v.getCustomer() != null ? v.getCustomer().getId() : null,
            v.getCustomer() != null ? v.getCustomer().getFullName() : null
        );
    }
}
