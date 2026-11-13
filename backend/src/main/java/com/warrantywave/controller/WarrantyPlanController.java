package com.warrantywave.controller;

import com.warrantywave.model.dto.WarrantyPlanDto;
import com.warrantywave.service.WarrantyPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/plans", "/api/warranty-plans"})
@Tag(name = "Warranty Plans", description = "Endpoints for configuring warranty packages, coverage tiers, and deductibles")
public class WarrantyPlanController {

    private final WarrantyPlanService planService;

    public WarrantyPlanController(WarrantyPlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    @Operation(summary = "Get all warranty plans")
    public ResponseEntity<List<WarrantyPlanDto>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @GetMapping("/active")
    @Operation(summary = "Get only active warranty plans available for new contracts")
    public ResponseEntity<List<WarrantyPlanDto>> getActivePlans() {
        return ResponseEntity.ok(planService.getActivePlans());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warranty plan details by ID")
    public ResponseEntity<WarrantyPlanDto> getPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new warranty plan (Admin only)")
    public ResponseEntity<WarrantyPlanDto> createPlan(@Valid @RequestBody WarrantyPlanDto dto, Authentication auth) {
        String username = auth != null ? auth.getName() : "ADMIN";
        WarrantyPlanDto created = planService.createPlan(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update warranty plan terms (Admin only)")
    public ResponseEntity<WarrantyPlanDto> updatePlan(@PathVariable Long id, @Valid @RequestBody WarrantyPlanDto dto, Authentication auth) {
        String username = auth != null ? auth.getName() : "ADMIN";
        WarrantyPlanDto updated = planService.updatePlan(id, dto, username);
        return ResponseEntity.ok(updated);
    }
}
