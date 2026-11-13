package com.warrantywave.controller;

import com.warrantywave.model.dto.VehicleDto;
import com.warrantywave.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicle Management", description = "Endpoints for vehicle inventory, VIN lookups, and owner mapping")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    @Operation(summary = "Get paginated list of vehicles with optional search filtering")
    public ResponseEntity<Page<VehicleDto>> getAllVehicles(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String query
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<VehicleDto> vehicles = vehicleService.getAllVehicles(query, pageRequest);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID")
    public ResponseEntity<VehicleDto> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @PostMapping
    @Operation(summary = "Register a new vehicle under a customer")
    public ResponseEntity<VehicleDto> createVehicle(@Valid @RequestBody VehicleDto dto, Authentication auth) {
        String username = auth != null ? auth.getName() : "ANONYMOUS";
        VehicleDto created = vehicleService.createVehicle(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle specifications or mileage")
    public ResponseEntity<VehicleDto> updateVehicle(@PathVariable Long id, @Valid @RequestBody VehicleDto dto, Authentication auth) {
        String username = auth != null ? auth.getName() : "ANONYMOUS";
        VehicleDto updated = vehicleService.updateVehicle(id, dto, username);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle by ID")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id, Authentication auth) {
        String username = auth != null ? auth.getName() : "ANONYMOUS";
        vehicleService.deleteVehicle(id, username);
        return ResponseEntity.noContent().build();
    }
}
