package com.ticketshall.auth.controller;

import com.ticketshall.auth.DTO.LoginRequestDTO;
import com.ticketshall.auth.DTO.RefreshResponseDTO;
import com.ticketshall.auth.DTO.SignupLoginResponseDTO;
import com.ticketshall.auth.Enums.Role;
import com.ticketshall.auth.Enums.UserType;
import com.ticketshall.auth.service.AuthService;
import com.ticketshall.auth.service.LogoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private LogoutService logoutService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginReturnsAccessAndRefreshTokens() {
        UUID userId = UUID.randomUUID();
        SignupLoginResponseDTO responseDTO = SignupLoginResponseDTO.builder()
                .token("access-token")
                .refreshToken("refresh-token")
                .userId(userId)
                .email("test@example.com")
                .role(Role.USER)
                .userType(UserType.CUSTOMER)
                .build();

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(responseDTO);

        LoginRequestDTO request = new LoginRequestDTO("test@example.com", "password");
        ResponseEntity<Object> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().get(HttpHeaders.SET_COOKIE));
        assertTrue(response.getHeaders().get(HttpHeaders.SET_COOKIE).toString().contains("refreshToken=refresh-token"));

        SignupLoginResponseDTO body = (SignupLoginResponseDTO) response.getBody();
        assertNotNull(body);
        assertEquals("access-token", body.token());
        assertEquals("refresh-token", body.refreshToken());
    }

    @Test
    void testValidateJwtReturnsHeadersWhenValid() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-ID", UUID.randomUUID().toString());

        when(authService.validate("Bearer token", "USER")).thenReturn(headers);

        ResponseEntity<Object> response = authController.validateJwt("Bearer token", "USER");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(headers.getFirst("X-User-ID"), response.getHeaders().getFirst("X-User-ID"));
    }

    @Test
    void testRefreshTokenReturnsNewTokens() {
        String refreshToken = "valid-refresh-token";
        RefreshResponseDTO responseDTO = RefreshResponseDTO.builder()
                .token("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        when(authService.refreshToken(refreshToken)).thenReturn(responseDTO);

        ResponseEntity<RefreshResponseDTO> response = authController.refreshToken(Map.of("refreshToken", refreshToken));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new-access-token", response.getBody().token());
        assertEquals("new-refresh-token", response.getBody().refreshToken());
    }

    @Test
    void testRefreshTokenThrowsUnauthorizedOnInvalidToken() {
        String refreshToken = "invalid-refresh-token";

        when(authService.refreshToken(refreshToken))
                .thenThrow(new RuntimeException("Invalid refresh token"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authController.refreshToken(Map.of("refreshToken", refreshToken)));

        assertEquals("Invalid refresh token", exception.getMessage());
    }
}
