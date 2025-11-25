package com.ticketshall.mq.events;

import java.time.LocalDate;

public record CustomerCreatedEvent(
        Long id,
        String email,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phoneNumber,
        String address,
        String city,
        String country
) {}

