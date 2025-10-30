package com.ticketshall.payments.dto;

import lombok.Builder;

@Builder
public record ErrorResponse(String message,
        String error) {
}
