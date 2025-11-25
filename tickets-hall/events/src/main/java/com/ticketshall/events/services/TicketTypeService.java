package com.ticketshall.events.services;

import com.ticketshall.events.dtos.params.CreateTicketTypeParams;
import com.ticketshall.events.models.TicketType;

import java.util.List;
import java.util.UUID;

public interface TicketTypeService {
    List<TicketType> createTicketTypes(UUID eventId, List<CreateTicketTypeParams> ticketTypesParams);
}
