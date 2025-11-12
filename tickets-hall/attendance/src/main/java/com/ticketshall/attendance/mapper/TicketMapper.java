package com.ticketshall.attendance.mapper;

import com.ticketshall.attendance.mq.events.TicketCreatedEvent;
import com.ticketshall.attendance.mq.events.TicketUpdatedEvent;
import com.ticketshall.attendance.entity.Ticket;
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
