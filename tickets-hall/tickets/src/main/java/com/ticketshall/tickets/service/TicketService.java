package com.ticketshall.tickets.service;

import com.ticketshall.tickets.dto.CreateTicketTypeRequest;
import com.ticketshall.tickets.models.TicketType;

import java.util.List;
import java.util.UUID;

public interface TicketService {
    TicketType createTicketType(CreateTicketTypeRequest request);
    List<TicketType> listTicketTypesForEvent(UUID eventId);
}
