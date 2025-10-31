package com.ticketshall.events.services;

import com.ticketshall.events.dtos.params.UpsertEventParams;
import com.ticketshall.events.dtos.filterparams.EventFilterParams;
import com.ticketshall.events.dtos.params.PublishEventParams;
import com.ticketshall.events.models.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventService {
    Event createEvent(UpsertEventParams UpsertEventParams);
    Event getEvent(UUID id);
    Page<Event> getAllEvents(EventFilterParams eventFilterParams, Pageable pageable);
    void publishEvent(UUID eventId, PublishEventParams publishEventParams);
    public void updateEvent(UUID id, UpsertEventParams upsertEventParams);
}
