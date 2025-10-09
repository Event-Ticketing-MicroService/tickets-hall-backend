package com.ticketshall.tickets.service;

import com.ticketshall.tickets.dto.ReservationRequest;

public interface ReservationService {
    boolean reserve(ReservationRequest reservation);

}
