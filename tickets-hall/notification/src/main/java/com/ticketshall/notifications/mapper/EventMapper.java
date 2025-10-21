package com.ticketshall.notifications.mapper;

import org.mapstruct.Mapper;

import com.ticketshall.notifications.entity.Event;
import com.ticketshall.notifications.mq.events.EventCreatedEvent;
import com.ticketshall.notifications.mq.events.EventUpdatedEvent;

@Mapper(componentModel = "spring")
public interface EventMapper {
    Event toEvent(EventCreatedEvent event);
    Event toEvent(EventUpdatedEvent event);
    EventCreatedEvent toEventCreatedEvent(Event event);
}
