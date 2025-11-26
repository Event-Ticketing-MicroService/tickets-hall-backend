package com.ticketshall.auth.DTO;

import lombok.Builder;

@Builder
public record RefreshResponseDTO(
        String token,
        String refreshToken
) {
}
