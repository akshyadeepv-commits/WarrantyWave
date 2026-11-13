package com.warrantywave.controller;

import com.warrantywave.model.entity.AuditLog;
import com.warrantywave.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit Logs", description = "Endpoints for viewing enterprise transaction logs and state changes")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get the latest 50 audit logs across all modules (Admin only)")
    public ResponseEntity<List<AuditLog>> getRecentAuditLogs() {
        return ResponseEntity.ok(auditService.getRecentLogs());
    }
}
