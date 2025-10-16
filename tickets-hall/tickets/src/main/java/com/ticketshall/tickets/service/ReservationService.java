package com.ticketshall.tickets.service;

import com.ticketshall.tickets.dto.CreatePaymentResponse;
import com.ticketshall.tickets.dto.request.ReservationRequest;

import java.util.UUID;

public interface ReservationService {
    CreatePaymentResponse reserve(ReservationRequest request);
    void expireReservation(UUID reservationId);
}
