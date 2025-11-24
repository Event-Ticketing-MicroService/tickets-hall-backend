package com.ticketshall.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticketshall.auth.DTO.LoginRequestDTO;
import com.ticketshall.auth.config.JwtUtil;
import com.ticketshall.auth.model.UserCredentials;
import com.ticketshall.auth.repository.UserCredentialsRepo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserCredentialsRepo userCredentialsRepo;

    @PostMapping("/validate")
    public ResponseEntity<Object> validateJwt(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestParam(required = true) String role) {

        String accessToken = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
        }

        if (accessToken != null && jwtUtil.validateTokenAndRole(accessToken, role)) {
            String userId = jwtUtil.extractUserId(accessToken);
            return ResponseEntity.ok()
                    .header("X-User-ID", userId)
                    .build();
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        if (refreshToken != null && jwtUtil.validateRefreshToken(refreshToken)) {
            String userIdStr = jwtUtil.extractUserId(refreshToken);
            UUID userId = UUID.fromString(userIdStr);

            UserCredentials user = userCredentialsRepo.findById(userId).orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String newAccessToken = jwtUtil.generateToken(user.getRole(), user.getUserId());

            return ResponseEntity.ok()
                    .body(Map.of("accessToken", newAccessToken));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            UserCredentials user = (UserCredentials) authentication.getPrincipal();

            String accessToken = jwtUtil.generateToken(user.getRole(), user.getUserId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false) // Set to true in production
                    .path("/")
                    .maxAge(Duration.ofDays(30))
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(Map.of(
                            "accessToken", accessToken,
                            "userId", user.getUserId(),
                            "email", user.getEmail(),
                            "role", user.getRole().name(),
                            "userType", user.getUserType().name()
                    ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        }
    }
}
