package com.ticketshall.auth.DTO;

import lombok.Builder;

@Builder
public record CreateCustomerDTO(
        String email,
        String firstName,
        String lastName,
        String dateOfBirth,
        String phoneNumber,
        String address,
        String city,
        String country
) {
}
