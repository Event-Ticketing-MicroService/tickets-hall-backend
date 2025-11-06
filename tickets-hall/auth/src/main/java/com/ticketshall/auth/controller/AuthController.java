package com.ticketshall.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticketshall.auth.DTO.LoginRequestDTO;
import com.ticketshall.auth.config.JwtUtil;
import com.ticketshall.auth.model.UserCredentials;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/validate")
    public ResponseEntity<Object> validateJwt(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @RequestParam(required=true) String role){

        String jwtToken = extractJwtFromHeader(authorizationHeader);

        if(jwtToken == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if(jwtUtil.validateTokenAndRole(jwtToken, role) == false)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        HttpHeaders headers = new HttpHeaders();

        String userId = jwtUtil.extractUserId(jwtToken);

        headers.add("X-User-ID", userId);

        return ResponseEntity.ok().headers(headers).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            UserCredentials user = (UserCredentials) authentication.getPrincipal();

            String token = jwtUtil.generateToken(user.getRole(), user.getUserId());

            return ResponseEntity.ok(Map.of(
                "token", token,
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

    private String extractJwtFromHeader(String headerValue)
    {
        return headerValue != null && headerValue.startsWith("Bearer ") ? 
            headerValue.substring(7) : null;
    }

}
