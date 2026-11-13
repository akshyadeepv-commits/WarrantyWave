package com.warrantywave.controller;

import com.warrantywave.model.dto.DashboardKpiDto;
import com.warrantywave.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard & Analytics", description = "Endpoints for high-level business intelligence, loss ratio, and KPIs")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/kpis")
    @Operation(summary = "Get aggregated executive dashboard metrics, recent activities, and plan shares")
    public ResponseEntity<DashboardKpiDto> getDashboardKpis() {
        return ResponseEntity.ok(dashboardService.getDashboardKpis());
    }
}
