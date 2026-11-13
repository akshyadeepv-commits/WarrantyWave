package com.warrantywave.controller;

import com.warrantywave.model.dto.*;
import com.warrantywave.model.enums.ContractStatus;
import com.warrantywave.service.WarrantyContractService;
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
@RequestMapping("/api/contracts")
@Tag(name = "Warranty Contracts", description = "Endpoints for contract issuance, renewals, status lifecycle, and eligibility verification")
public class WarrantyContractController {

    private final WarrantyContractService contractService;

    public WarrantyContractController(WarrantyContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    @Operation(summary = "Get paginated list of contracts with optional status and text search")
    public ResponseEntity<Page<WarrantyContractDto>> getAllContracts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) ContractStatus status,
        @RequestParam(required = false) String query
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<WarrantyContractDto> contracts = contractService.getAllContracts(status, query, pageRequest);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warranty contract by ID")
    public ResponseEntity<WarrantyContractDto> getContractById(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getContractById(id));
    }

    @PostMapping({"/check-eligibility", "/eligibility"})
    @Operation(summary = "Verify vehicle and plan eligibility prior to contract creation")
    public ResponseEntity<EligibilityCheckResponse> checkEligibility(@Valid @RequestBody EligibilityCheckRequest request) {
        return ResponseEntity.ok(contractService.validateEligibility(request));
    }

    @PostMapping
    @Operation(summary = "Issue a new warranty contract")
    public ResponseEntity<WarrantyContractDto> createContract(@Valid @RequestBody WarrantyContractDto dto, Authentication auth) {
        String username = auth != null ? auth.getName() : "ANONYMOUS";
        WarrantyContractDto created = contractService.createContract(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @RequestMapping(value = "/{id}/renew", method = {RequestMethod.POST, RequestMethod.PUT})
    @Operation(summary = "Renew or extend an existing warranty contract")
    public ResponseEntity<WarrantyContractDto> renewContract(
        @PathVariable Long id,
        @Valid @RequestBody ContractRenewalDto dto,
        Authentication auth
    ) {
        String username = auth != null ? auth.getName() : "ANONYMOUS";
        WarrantyContractDto renewed = contractService.renewContract(id, dto, username);
        return ResponseEntity.ok(renewed);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update contract operational status (e.g. ACTIVE, CANCELLED, CLAIM_SUSPENDED)")
    public ResponseEntity<WarrantyContractDto> updateStatus(
        @PathVariable Long id,
        @RequestParam ContractStatus status,
        Authentication auth
    ) {
        String username = auth != null ? auth.getName() : "ANONYMOUS";
        WarrantyContractDto updated = contractService.updateStatus(id, status, username);
        return ResponseEntity.ok(updated);
    }
}
