package com.ticketshall.events.services;

import com.ticketshall.events.dtos.params.CreateEventParams;
import com.ticketshall.events.models.Event;

import java.util.UUID;

public interface EventService {
    Event createEvent(CreateEventParams createEventParams);
    Event getEvent(UUID id);
}
