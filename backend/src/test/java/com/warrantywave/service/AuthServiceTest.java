package com.warrantywave.service;

import com.warrantywave.config.JwtUtil;
import com.warrantywave.exception.BadRequestException;
import com.warrantywave.model.dto.AuthRequest;
import com.warrantywave.model.dto.AuthResponse;
import com.warrantywave.model.entity.User;
import com.warrantywave.model.enums.UserRole;
import com.warrantywave.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("admin", "hash123", "Admin User", "admin@warrantywave.com", UserRole.ROLE_ADMIN);
        sampleUser.setId(1L);
        sampleUser.setActive(true);
    }

    @Test
    @DisplayName("Should successfully authenticate valid user and return JWT token")
    void testSuccessfulLogin() {
        AuthRequest request = new AuthRequest("admin", "Password@123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(sampleUser);
        when(jwtUtil.generateToken(eq(sampleUser), any())).thenReturn("mock-jwt-token-xyz");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token-xyz", response.token());
        assertEquals("Bearer", response.type());
        assertEquals("admin", response.username());
        assertEquals(UserRole.ROLE_ADMIN, response.role());
    }

    @Test
    @DisplayName("Should throw BadRequestException if user is deactivated")
    void testDeactivatedUserLogin() {
        sampleUser.setActive(false);
        AuthRequest request = new AuthRequest("admin", "Password@123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(sampleUser);

        assertThrows(BadRequestException.class, () -> authService.login(request));
        verify(jwtUtil, never()).generateToken(any(), any());
    }
}
