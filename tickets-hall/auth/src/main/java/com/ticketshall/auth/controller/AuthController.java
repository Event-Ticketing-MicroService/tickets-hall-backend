package com.ticketshall.auth.controller;

import com.ticketshall.auth.DTO.RefreshResponseDTO;
import com.ticketshall.auth.DTO.SignupLoginResponseDTO;
import com.ticketshall.auth.DTO.SignupRequestDTO;
import com.ticketshall.auth.service.AuthService;
import com.ticketshall.auth.service.LogoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.ticketshall.auth.DTO.LoginRequestDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final LogoutService logoutService;

    @PostMapping("/validate")
    public ResponseEntity<Object> validateJwt(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @RequestParam(required=true) String role){
        HttpHeaders headers = authService.validate(authorizationHeader, role);
        return ResponseEntity.ok().headers(headers).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequestDTO request) {
        SignupLoginResponseDTO result = authService.login(request);
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(result);
    }

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> signupWithImage(
            @RequestPart("data") @Validated SignupRequestDTO credentials,
            @RequestPart(value = "image", required = false)MultipartFile image
            ) {
        return ResponseEntity.ok(authService.signup(credentials, image));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponseDTO> refreshToken(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.refreshToken(body.get("refreshToken")));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        logoutService.logout(request, response, auth);
        return ResponseEntity.ok().build();
    }
}
