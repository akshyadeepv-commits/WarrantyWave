package com.warrantywave.controller;

import com.warrantywave.model.dto.*;
import com.warrantywave.model.enums.FinancingStatus;
import com.warrantywave.service.FinancingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/financing")
@Tag(name = "Financing & Leasing", description = "Endpoints for loan applications, instant EMI calculators, and credit risk reviews")
public class FinancingController {

    private final FinancingService financingService;

    public FinancingController(FinancingService financingService) {
        this.financingService = financingService;
    }

    @PostMapping("/calculate-emi")
    @Operation(summary = "Calculate monthly EMI and total interest based on vehicle price, down payment, and loan term")
    public ResponseEntity<EmiCalculationResponse> calculateEmi(@Valid @RequestBody EmiCalculationRequest request) {
        return ResponseEntity.ok(financingService.calculateEmi(request));
    }

    @GetMapping
    @Operation(summary = "Get paginated list of financing applications with optional status and query filtering")
    public ResponseEntity<Page<FinancingResponseDto>> getAllApplications(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) FinancingStatus status,
        @RequestParam(required = false) String query
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FinancingResponseDto> applications = financingService.getAllApplications(status, query, pageRequest);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get financing application by ID")
    public ResponseEntity<FinancingResponseDto> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(financingService.getApplicationById(id));
    }

    @PostMapping
    @Operation(summary = "Submit a new automotive financing/leasing application")
    public ResponseEntity<FinancingResponseDto> submitApplication(
        @Valid @RequestBody FinancingRequestDto dto,
        Authentication auth
    ) {
        String username = auth != null ? auth.getName() : "ANONYMOUS";
        FinancingResponseDto created = financingService.submitApplication(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @RequestMapping(value = "/{id}/review", method = {RequestMethod.POST, RequestMethod.PUT})
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    @Operation(summary = "Review and approve/reject financing application (Finance Officer or Admin only)")
    public ResponseEntity<FinancingResponseDto> reviewApplication(
        @PathVariable Long id,
        @Valid @RequestBody FinancingReviewDto dto,
        Authentication auth
    ) {
        String username = auth != null ? auth.getName() : "FINANCE_OFFICER";
        FinancingResponseDto reviewed = financingService.reviewApplication(id, dto, username);
        return ResponseEntity.ok(reviewed);
    }
}
