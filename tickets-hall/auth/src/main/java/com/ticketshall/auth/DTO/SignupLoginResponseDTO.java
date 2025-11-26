package com.ticketshall.auth.DTO;

import com.ticketshall.auth.Enums.Role;
import com.ticketshall.auth.Enums.UserType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record SignupLoginResponseDTO(
        String token,
        String refreshToken,
        UUID userId,
        String email,
        Role role,
        UserType userType
) {
}