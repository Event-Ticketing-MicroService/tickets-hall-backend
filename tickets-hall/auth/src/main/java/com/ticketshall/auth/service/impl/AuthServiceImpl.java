package com.ticketshall.auth.service.impl;

import com.ticketshall.auth.DTO.*;
import com.ticketshall.auth.Enums.Role;
import com.ticketshall.auth.Enums.UserType;
import com.ticketshall.auth.config.JwtUtil;
import com.ticketshall.auth.exceptions_handlers.InvalidJwtException;
import com.ticketshall.auth.exceptions_handlers.MissingImageException;
import com.ticketshall.auth.exceptions_handlers.MissingJwtException;
import com.ticketshall.auth.model.UserCredentials;
import com.ticketshall.auth.repository.UserCredentialsRepo;
import com.ticketshall.auth.service.AuthService;
import com.ticketshall.auth.service.CustomerSignupService;
import com.ticketshall.auth.service.VenueSignupService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserCredentialsRepo userCredentialsRepo;
    private final VenueSignupService  venueSignupService;
    private final CustomerSignupService customerSignupService;

    @Override
    public SignupLoginResponseDTO login(LoginRequestDTO request) throws BadCredentialsException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            UserCredentials user = (UserCredentials) authentication.getPrincipal();

            String token = jwtUtil.generateToken(user.getRole(), user.getUserId(), user.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

            SignupLoginResponseDTO response = SignupLoginResponseDTO.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .userType(user.getUserType())
                    .build();

            return response;
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Override
    @Transactional
    public SignupLoginResponseDTO signup(SignupRequestDTO request, MultipartFile image) {
        UserCredentials user = UserCredentials
                .builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.VENUE)
                .userType(UserType.VENUE)
                .build();

        userCredentialsRepo.save(user);

        switch (request.userType()) {
            case VENUE -> {
                if (image == null || image.isEmpty()) {
                    throw new MissingImageException("Image is required for venue signup");
                }
                venueSignupService.createVenue(request.venueDetails(), image);
            }
            case CUSTOMER -> {
                customerSignupService.createCustomer(request.customerDetails());
            }
        }

        String token = jwtUtil.generateToken(user.getRole(), user.getUserId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        return SignupLoginResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userType(user.getUserType())
                .role(user.getRole())
                .email(user.getEmail())
                .userId(user.getUserId())
                .build();
    }

    @Override
    public HttpHeaders validate(String authorizationHeader, String role) {
        String jwtToken = extractJwtFromHeader(authorizationHeader);

        if(jwtToken == null) {
            throw new MissingJwtException("Missing or improperly formatted Authorization header");
        }

        if(!jwtUtil.validateTokenAndRole(jwtToken, role))
        {
            throw new InvalidJwtException("Invalid token or insufficient role permissions");
        }

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();

        String userId = jwtUtil.extractUserId(jwtToken);

        headers.add("X-User-ID", userId);
        return headers;
    }

    @Override
    public RefreshResponseDTO refreshToken(String refreshToken) {
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        UUID userId = UUID.fromString(jwtUtil.extractUserId(refreshToken));

        UserCredentials user = userCredentialsRepo.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String newAccessToken = jwtUtil.generateToken(user.getRole(), user.getUserId(), user.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        return RefreshResponseDTO
                .builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    private String extractJwtFromHeader(String headerValue)
    {
        return headerValue != null && headerValue.startsWith("Bearer ") ?
                headerValue.substring(7) : null;
    }
}
