package com.warrantywave.controller;

import com.warrantywave.model.dto.ClaimRequestDto;
import com.warrantywave.model.dto.ClaimResponseDto;
import com.warrantywave.model.dto.ClaimReviewDto;
import com.warrantywave.model.enums.ClaimStatus;
import com.warrantywave.service.ClaimService;
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
@RequestMapping("/api/claims")
@Tag(name = "Warranty Claims", description = "Endpoints for claim submission, repair estimate verification, and officer adjudication")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @GetMapping
    @Operation(summary = "Get paginated list of warranty claims with optional status and text search")
    public ResponseEntity<Page<ClaimResponseDto>> getAllClaims(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) ClaimStatus status,
        @RequestParam(required = false) String query
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ClaimResponseDto> claims = claimService.getAllClaims(status, query, pageRequest);
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warranty claim by ID")
    public ResponseEntity<ClaimResponseDto> getClaimById(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.getClaimById(id));
    }

    @PostMapping
    @Operation(summary = "Submit a new warranty repair claim")
    public ResponseEntity<ClaimResponseDto> submitClaim(@Valid @RequestBody ClaimRequestDto dto, Authentication auth) {
        String username = auth != null ? auth.getName() : "ANONYMOUS";
        ClaimResponseDto created = claimService.submitClaim(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @RequestMapping(value = "/{id}/review", method = {RequestMethod.POST, RequestMethod.PUT})
    @PreAuthorize("hasAnyRole('ADMIN', 'CLAIMS_OFFICER')")
    @Operation(summary = "Adjudicate warranty claim: approve or reject (Claims Officer or Admin only)")
    public ResponseEntity<ClaimResponseDto> reviewClaim(
        @PathVariable Long id,
        @Valid @RequestBody ClaimReviewDto dto,
        Authentication auth
    ) {
        String username = auth != null ? auth.getName() : "CLAIMS_OFFICER";
        ClaimResponseDto reviewed = claimService.reviewClaim(id, dto, username);
        return ResponseEntity.ok(reviewed);
    }

    @RequestMapping(value = {"/{id}/disburse", "/{id}/pay"}, method = {RequestMethod.POST, RequestMethod.PUT})
    @PreAuthorize("hasAnyRole('ADMIN', 'CLAIMS_OFFICER')")
    @Operation(summary = "Mark approved claim as PAID/Disbursed")
    public ResponseEntity<ClaimResponseDto> disburseClaimPayment(@PathVariable Long id, Authentication auth) {
        String username = auth != null ? auth.getName() : "CLAIMS_OFFICER";
        ClaimResponseDto paid = claimService.markClaimPaid(id, username);
        return ResponseEntity.ok(paid);
    }
}
