package com.ticketshall.tickets.service;

import com.ticketshall.tickets.dto.ReservationRequest;
import com.ticketshall.tickets.models.nonStoredModels.Reservation;

import java.util.UUID;

public interface ReservationService {
    Reservation reserve(ReservationRequest request);
    void expireReservation(UUID reservationId);
}
