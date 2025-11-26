package com.ticketshall.auth.controller;

import com.ticketshall.auth.DTO.LoginRequestDTO;
import com.ticketshall.auth.Enums.Role;
import com.ticketshall.auth.Enums.UserType;
import com.ticketshall.auth.config.JwtUtil;
import com.ticketshall.auth.model.UserCredentials;
import com.ticketshall.auth.repository.UserCredentialsRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserCredentialsRepo userCredentialsRepo;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginReturnsAccessTokenInBodyAndRefreshTokenInCookie() {
        LoginRequestDTO request = new LoginRequestDTO("test@example.com", "password");
        UserCredentials user = UserCredentials.builder()
                .userId(UUID.randomUUID())
                .email("test@example.com")
                .role(Role.USER)
                .userType(UserType.CUSTOMER)
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(any(), any())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh-token");

        ResponseEntity<Object> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify Refresh Token Cookie
        assertNotNull(response.getHeaders().get(HttpHeaders.SET_COOKIE));
        assertTrue(response.getHeaders().get(HttpHeaders.SET_COOKIE).toString().contains("refreshToken=refresh-token"));
        assertFalse(response.getHeaders().get(HttpHeaders.SET_COOKIE).toString().contains("accessToken=access-token"));

        // Verify Access Token in Body
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("access-token", body.get("accessToken"));
    }

    @Test
    void testValidateWithValidAccessTokenInHeader() {
        String accessToken = "valid-access-token";
        String authHeader = "Bearer " + accessToken;
        String role = "USER";
        String userId = UUID.randomUUID().toString();

        when(jwtUtil.validateTokenAndRole(accessToken, role)).thenReturn(true);
        when(jwtUtil.extractUserId(accessToken)).thenReturn(userId);

        ResponseEntity<Object> response = authController.validateJwt(authHeader, role);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userId, response.getHeaders().getFirst("X-User-ID"));
    }

    @Test
    void testValidateWithExpiredAccessTokenReturns401() {
        String accessToken = "expired-access-token";
        String authHeader = "Bearer " + accessToken;
        String role = "USER";

        when(jwtUtil.validateTokenAndRole(accessToken, role)).thenReturn(false);

        ResponseEntity<Object> response = authController.validateJwt(authHeader, role);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testRefreshTokenEndpoint() {
        String refreshToken = "valid-refresh-token";
        UUID userId = UUID.randomUUID();
        UserCredentials user = UserCredentials.builder()
                .userId(userId)
                .role(Role.USER)
                .build();

        when(jwtUtil.validateRefreshToken(refreshToken)).thenReturn(true);
        when(jwtUtil.extractUserId(refreshToken)).thenReturn(userId.toString());
        when(userCredentialsRepo.findById(userId)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(Role.USER, userId)).thenReturn("new-access-token");

        ResponseEntity<Object> response = authController.refreshToken(refreshToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("new-access-token", body.get("accessToken"));
    }

    @Test
    void testRefreshTokenEndpointWithInvalidToken() {
        String refreshToken = "invalid-refresh-token";

        when(jwtUtil.validateRefreshToken(refreshToken)).thenReturn(false);

        ResponseEntity<Object> response = authController.refreshToken(refreshToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
