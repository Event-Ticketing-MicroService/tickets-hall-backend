package com.ticketshall.payments.dto;

import lombok.Builder;

@Builder
public record CreatePaymentResponse(String id,
        String status,
        String clientSecret) {
}
