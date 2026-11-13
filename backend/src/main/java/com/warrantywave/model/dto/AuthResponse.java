package com.warrantywave.model.dto;

import com.warrantywave.model.enums.UserRole;

public record AuthResponse(
    String token,
    String type,
    Long id,
    String username,
    String fullName,
    String email,
    UserRole role
) {
    public AuthResponse(String token, Long id, String username, String fullName, String email, UserRole role) {
        this(token, "Bearer", id, username, fullName, email, role);
    }
}
