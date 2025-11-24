package com.ticketshall.auth.config;

import com.ticketshall.auth.Enums.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.security.Key;

@Component
public class JwtUtil {

    private final JwtConfig jwtConfig;
    private final Key key;

    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;

        this.key = Keys.hmacShaKeyFor(this.jwtConfig.getSecret().getBytes());
    }

    public String generateToken(Role role, UUID userId) {
        long expiration = jwtConfig.getExpiration().toMillis();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        long expiration = jwtConfig.getRefreshExpiration().toMillis();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public String extractUserId(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String extractRole(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        try {
            String type = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("type", String.class);
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateTokenAndRole(String token, String requiredRole) {
        if (!validateToken(token)) {
            return false;
        }

        try {
            String tokenRole = extractRole(token);

            return tokenRole != null && tokenRole.equals(requiredRole);
        } catch (Exception e) {
            return false;
        }
    }
}
