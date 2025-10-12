package com.ticketshall.tickets.mapper;

import com.ticketshall.tickets.models.Attendee;
import com.ticketshall.tickets.mq.events.AttendeeCreatedEvent;
import com.ticketshall.tickets.mq.events.AttendeeUpdatedEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttendeeMapper {
    Attendee toAttendee(AttendeeCreatedEvent event);
    Attendee toAttendee(AttendeeUpdatedEvent event);
    AttendeeCreatedEvent toAttendeeCreatedEvent(Attendee attendee);
}
