package com.warrantywave.model.dto;

import com.warrantywave.model.enums.UserRole;

public record UserDto(
    Long id,
    String username,
    String fullName,
    String email,
    UserRole role,
    String phoneNumber,
    boolean active
) {}
