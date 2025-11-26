package com.ticketshall.tickets.controller;

import com.ticketshall.tickets.dto.CreatePaymentResponse;
import com.ticketshall.tickets.dto.request.ReservationRequest;
import com.ticketshall.tickets.models.nonStoredModels.Reservation;
import com.ticketshall.tickets.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping()
    public ResponseEntity<CreatePaymentResponse> createReservation(@RequestBody ReservationRequest request) {
        var result = reservationService.reserve(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{reservationId}")
    public ResponseEntity<Boolean> cancelReservation(@PathVariable("reservationId") String reservationId) {
        reservationService.expireReservation(UUID.fromString(reservationId), true);
        return ResponseEntity.ok(true);
    }
}
