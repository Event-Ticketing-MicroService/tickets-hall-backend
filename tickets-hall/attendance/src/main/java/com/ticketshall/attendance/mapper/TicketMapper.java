package com.ticketshall.attendance.mapper;

import com.ticketshall.attendance.events.TicketCreatedEvent;
import com.ticketshall.attendance.models.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    @Mapping(target = "attendee", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "usedAtUtc", constant = "null")
    Ticket toTicket(TicketCreatedEvent event);

    @Mapping(source = "attendee.id", target = "userId")
    @Mapping(source = "event.id", target = "eventId")
    TicketCreatedEvent toTicketCreatedEvent(Ticket ticket);
}
