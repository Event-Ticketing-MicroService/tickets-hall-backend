package com.ticketshall.tickets.mapper;

import com.ticketshall.tickets.models.Ticket;
import com.ticketshall.tickets.mq.events.TicketCreatedEvent;
import com.ticketshall.tickets.mq.events.TicketUpdatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    @Mapping(target = "attendee", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "usedAtUtc", ignore = true)
    Ticket toTicket(TicketCreatedEvent event);

    @Mapping(target = "attendee", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "usedAtUtc", ignore = true)
    Ticket toTicket(TicketUpdatedEvent event);

    @Mapping(source = "attendee.id", target = "userId")
    TicketCreatedEvent toTicketCreatedEvent(Ticket ticket);
}
