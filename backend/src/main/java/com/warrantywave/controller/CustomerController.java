package com.warrantywave.controller;

import com.warrantywave.model.dto.*;
import com.warrantywave.service.*;
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

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer Management", description = "Endpoints for customer registration, credit profiles, and portfolio lookups")
public class CustomerController {

    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final WarrantyContractService contractService;
    private final ClaimService claimService;
    private final FinancingService financingService;

    public CustomerController(
        CustomerService customerService,
        VehicleService vehicleService,
        WarrantyContractService contractService,
        ClaimService claimService,
        FinancingService financingService
    ) {
        this.customerService = customerService;
        this.vehicleService = vehicleService;
        this.contractService = contractService;
        this.claimService = claimService;
        this.financingService = financingService;
    }

    @GetMapping
    @Operation(summary = "Get paginated list of customers with optional search filtering")
    public ResponseEntity<Page<CustomerDto>> getAllCustomers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String query
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CustomerDto> customers = customerService.getAllCustomers(query, pageRequest);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new customer")
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CustomerDto dto, Authentication auth) {
        String username = auth != null ? auth.getName() : "ANONYMOUS";
        CustomerDto created = customerService.createCustomer(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing customer")
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerDto dto, Authentication auth) {
        String username = auth != null ? auth.getName() : "ANONYMOUS";
        CustomerDto updated = customerService.updateCustomer(id, dto, username);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer by ID")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id, Authentication auth) {
        String username = auth != null ? auth.getName() : "ANONYMOUS";
        customerService.deleteCustomer(id, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/vehicles")
    @Operation(summary = "Get all vehicles registered under a customer")
    public ResponseEntity<List<VehicleDto>> getCustomerVehicles(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehiclesByCustomerId(id));
    }

    @GetMapping("/{id}/contracts")
    @Operation(summary = "Get all warranty contracts held by a customer")
    public ResponseEntity<List<WarrantyContractDto>> getCustomerContracts(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getContractsByCustomerId(id));
    }

    @GetMapping("/{id}/claims")
    @Operation(summary = "Get all warranty claims submitted by a customer")
    public ResponseEntity<List<ClaimResponseDto>> getCustomerClaims(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.getClaimsByCustomerId(id));
    }

    @GetMapping("/{id}/financing")
    @Operation(summary = "Get all financing applications submitted by a customer")
    public ResponseEntity<List<FinancingResponseDto>> getCustomerFinancing(@PathVariable Long id) {
        return ResponseEntity.ok(financingService.getApplicationsByCustomerId(id));
    }
}
