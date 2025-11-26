package com.ticketshall.auth.DTO;

public record WorkerResponseDTO(
        Long workerId,
        String fullName,
        String email,
        Long venueId,
        String venueName
) {
}