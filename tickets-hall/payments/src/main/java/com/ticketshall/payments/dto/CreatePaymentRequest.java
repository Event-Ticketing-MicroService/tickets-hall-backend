package com.ticketshall.payments.dto;

import lombok.Builder;


@Builder
public record CreatePaymentRequest(Float amount, String currency, String attendeeId, String eventId, String reservationId) {
}
