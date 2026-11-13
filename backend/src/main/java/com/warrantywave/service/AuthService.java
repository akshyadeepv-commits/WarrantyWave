package com.warrantywave.service;

import com.warrantywave.config.JwtUtil;
import com.warrantywave.exception.BadRequestException;
import com.warrantywave.model.dto.AuthRequest;
import com.warrantywave.model.dto.AuthResponse;
import com.warrantywave.model.entity.User;
import com.warrantywave.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = (User) authentication.getPrincipal();
        if (!user.isActive()) {
            throw new BadRequestException("User account is inactive. Please contact your system administrator.");
        }

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("fullName", user.getFullName());

        String token = jwtUtil.generateToken(user, extraClaims);

        return new AuthResponse(
            token,
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getRole()
        );
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}
