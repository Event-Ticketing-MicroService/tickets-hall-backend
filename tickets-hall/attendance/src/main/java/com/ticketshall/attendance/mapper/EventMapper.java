package com.ticketshall.attendance.mapper;

import com.ticketshall.attendance.events.EventCreatedEvent;
import com.ticketshall.attendance.events.EventUpdatedEvent;
import com.ticketshall.attendance.models.Event;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {
    Event toEvent(EventCreatedEvent event);
    Event toEvent(EventUpdatedEvent event);
    EventCreatedEvent toEventCreatedEvent(Event event);
}
