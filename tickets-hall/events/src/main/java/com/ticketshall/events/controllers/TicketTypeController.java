package com.ticketshall.events.controllers;

import com.ticketshall.events.dtos.params.CreateTicketTypeParams;
import com.ticketshall.events.dtos.responses.ListResponse;
import com.ticketshall.events.models.TicketType;
import com.ticketshall.events.services.TicketTypeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @Autowired
    public TicketTypeController(TicketTypeService ticketTypeService) {
        this.ticketTypeService = ticketTypeService;
    }

    @PostMapping("/admin/{eventId}/ticket-types")
    public ResponseEntity<?> createTicketTypes(
            @PathVariable UUID eventId,
            @RequestBody @Valid @NotEmpty(message = "ticketTypes list cannot be empty") List<@Valid CreateTicketTypeParams> createTicketTypeParams
    ) {

        List<TicketType> ticketTypes = ticketTypeService.createTicketTypes(eventId, createTicketTypeParams);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ListResponse<TicketType>(ticketTypes, ticketTypes.size()));
    }
}
