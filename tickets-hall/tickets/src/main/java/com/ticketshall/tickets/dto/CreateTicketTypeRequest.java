package com.ticketshall.tickets.dto;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreateTicketTypeRequest(
        @Size(max = 20, min = 3, message = "Size is between 20 and 3") String name,
        @Size(max = 512, min = 10, message = "Description should be between 10 and 512 characters") String description,
        @PositiveOrZero Float price,
        @PositiveOrZero Integer stock,
        UUID eventId){}