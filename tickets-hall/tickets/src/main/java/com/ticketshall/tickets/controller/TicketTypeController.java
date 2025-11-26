package com.ticketshall.tickets.controller;

import com.ticketshall.tickets.dto.request.CreateTicketTypeRequest;
import com.ticketshall.tickets.dto.request.UpdateTicketTypeRequest;
import com.ticketshall.tickets.models.TicketType;
import com.ticketshall.tickets.service.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @PostMapping("/admin")
    public ResponseEntity<TicketType> CreateTicketType(@RequestBody CreateTicketTypeRequest request) {
        var ticketType = ticketTypeService.createTicketType(request);
        return ResponseEntity.created(null).body(ticketType);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<Iterable<TicketType>> ListTicketTypesForEvent(@PathVariable("eventId") UUID eventId) {
        var result = ticketTypeService.listTicketTypesForEvent(eventId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/admin")
    public ResponseEntity<TicketType> UpdateTicketType(@RequestBody UpdateTicketTypeRequest request) {
        var result = ticketTypeService.updateTicketType(request);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/admin/{ticketTypeId}")
    public ResponseEntity<Boolean> DeleteTicketType(@PathVariable("ticketTypeId") UUID ticketTypeId) {
        var result = ticketTypeService.deleteTicketType(ticketTypeId);
        return ResponseEntity.ok(result);
    }
}
