package com.ticketshall.auth.controller;

import com.ticketshall.auth.DTO.LoginRequestDTO;
import com.ticketshall.auth.DTO.RefreshResponseDTO;
import com.ticketshall.auth.Enums.Role;
import com.ticketshall.auth.Enums.UserType;
import com.ticketshall.auth.config.JwtUtil;
import com.ticketshall.auth.model.UserCredentials;
import com.ticketshall.auth.service.AuthService;
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
    private AuthService authService;

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
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh-token");

        ResponseEntity<Object> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());


        String cookies = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(cookies);
        assertTrue(cookies.contains("refreshToken=refresh-token"));
        assertFalse(cookies.contains("accessToken=access-token"));


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

        RefreshResponseDTO expected =
                new RefreshResponseDTO("new-access-token", "new-refresh-token");

        when(authService.refreshToken(refreshToken)).thenReturn(expected);

        ResponseEntity<RefreshResponseDTO> response =
                authController.refreshToken(Map.of("refreshToken", refreshToken));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new-access-token", response.getBody().token());
    }

    @Test
    void testRefreshTokenEndpointWithInvalidToken() {
        String refreshToken = "invalid-refresh-token";

        when(authService.refreshToken(refreshToken))
                .thenThrow(new RuntimeException("Invalid refresh token"));

        ResponseEntity<RefreshResponseDTO> response =
                authController.refreshToken(Map.of("refreshToken", refreshToken));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
