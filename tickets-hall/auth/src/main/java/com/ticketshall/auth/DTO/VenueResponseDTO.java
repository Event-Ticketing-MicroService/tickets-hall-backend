package com.ticketshall.auth.DTO;

import lombok.*;

import java.util.List;

@Builder
public record VenueResponseDTO(
        Long venueID,
        String venueName,
        String venueAddress,
        String venuePhone,
        String venueEmail,
        Integer venueCapacity,
        String venueDescription,
        String venueCountry,
        String venueImageUrl,
        List<String>venueWorkers
) {
}
