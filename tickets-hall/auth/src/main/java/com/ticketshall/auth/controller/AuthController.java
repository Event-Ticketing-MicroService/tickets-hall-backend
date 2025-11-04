package com.ticketshall.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticketshall.auth.config.JwtUtil;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    @PostMapping("/validate")
    public ResponseEntity<Object> validateJwt(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @RequestParam(required=true) String role){

        String jwtToken = extractJwtFromHeader(authorizationHeader);

        if(jwtToken == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return jwtUtil.validateTokenAndRole(jwtToken, role) ? 
            ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }


    private String extractJwtFromHeader(String headerValue)
    {
        return headerValue != null && headerValue.startsWith("Bearer ") ? 
            headerValue.substring(7) : null;
    }

}
