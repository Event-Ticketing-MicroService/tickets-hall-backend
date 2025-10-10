package com.ticketshall.tickets.service;

import com.ticketshall.tickets.dto.ReservationRequest;
import com.ticketshall.tickets.models.nonStoredModels.Reservation;

public interface ReservationService {
    Reservation reserve(ReservationRequest reservation);

}
