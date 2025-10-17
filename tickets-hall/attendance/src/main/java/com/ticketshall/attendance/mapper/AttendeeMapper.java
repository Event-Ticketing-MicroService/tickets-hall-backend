package com.ticketshall.attendance.mapper;

import com.ticketshall.attendance.events.AttendeeCreatedEvent;
import com.ticketshall.attendance.events.AttendeeUpdatedEvent;
import com.ticketshall.attendance.models.Attendee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttendeeMapper {
    Attendee toAttendee(AttendeeCreatedEvent event);
    Attendee toAttendee(AttendeeUpdatedEvent event);
    AttendeeCreatedEvent toAttendeeCreatedEvent(Attendee attendee);
}
