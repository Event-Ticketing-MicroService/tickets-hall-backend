package com.ticketshall.events.services;

import com.ticketshall.events.dtos.params.CreateEventParams;
import com.ticketshall.events.dtos.filterparams.EventFilterParams;
import com.ticketshall.events.models.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventService {
    Event createEvent(CreateEventParams createEventParams);
    Event getEvent(UUID id);
    Page<Event> getAllEvents(EventFilterParams eventFilterParams, Pageable pageable);
}
