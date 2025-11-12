package com.ticketshall.attendance.mapper;

import com.ticketshall.attendance.mq.events.EventCreatedEvent;
import com.ticketshall.attendance.mq.events.EventUpdatedEvent;
import com.ticketshall.attendance.entity.Event;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {
    Event toEvent(EventCreatedEvent event);
    Event toEvent(EventUpdatedEvent event);
    EventCreatedEvent toEventCreatedEvent(Event event);
}
