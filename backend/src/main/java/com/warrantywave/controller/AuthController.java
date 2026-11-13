package com.warrantywave.controller;

import com.warrantywave.model.dto.AuthRequest;
import com.warrantywave.model.dto.AuthResponse;
import com.warrantywave.model.dto.UserDto;
import com.warrantywave.model.entity.User;
import com.warrantywave.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user login, JWT tokens, and identity")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get profile information for currently authenticated user")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        UserDto dto = new UserDto(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.getPhoneNumber(),
            user.isActive()
        );
        return ResponseEntity.ok(dto);
    }
}
