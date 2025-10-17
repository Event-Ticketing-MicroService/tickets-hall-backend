package com.ticketshall.tickets.mapper;

import com.ticketshall.tickets.models.Event;
import com.ticketshall.tickets.mq.events.EventCreatedEvent;
import com.ticketshall.tickets.mq.events.EventUpdatedEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {
    Event toEvent(EventCreatedEvent event);
    Event toEvent(EventUpdatedEvent event);
    EventCreatedEvent toEventCreatedEvent(Event event);
}
