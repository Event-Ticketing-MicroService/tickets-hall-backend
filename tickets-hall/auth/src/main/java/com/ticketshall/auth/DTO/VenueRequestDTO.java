package com.ticketshall.auth.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record VenueRequestDTO(
        @NotBlank(message = "Venue name is required")
        String venueName,

        @NotBlank(message = "Venue address is required")
        String venueAddress,

        @NotBlank(message = "Phone number is required")
        String venuePhone,

        @NotBlank(message = "Venue email is required")
        @Email(message = "Venue email must be valid")
        String venueEmail,

        @NotBlank(message = "Password cannot be empty")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,

        @Positive(message = "Capacity must be greater than 0")
        Integer venueCapacity,

        @NotBlank(message = "Description is required")
        String venueDescription,

        @NotBlank(message = "Country is required")
        String venueCountry,

        @Size(min = 1, message = "At least one worker is required")
        List<WorkerRequestDTO> venueWorkers
) {
}
