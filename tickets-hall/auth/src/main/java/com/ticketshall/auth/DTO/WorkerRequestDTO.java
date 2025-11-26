package com.ticketshall.auth.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record WorkerRequestDTO(
        @NotBlank(message = "Full name cannot be empty")
        String fullName,

        @NotBlank(message = "Email cannot be empty")
        @Email
        String email
) {
}
