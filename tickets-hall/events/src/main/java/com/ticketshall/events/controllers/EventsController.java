package com.ticketshall.events.controllers;

import com.ticketshall.events.dtos.params.CreateEventParams;
import com.ticketshall.events.models.Event;
import com.ticketshall.events.services.EventService;
import com.ticketshall.events.validations.EventValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/events")
@RestController
public class EventsController {
    private final EventService eventService;

    @Autowired
    public EventsController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("")
    ResponseEntity<?> createEvent(@Valid @RequestBody CreateEventParams createEventParams) {
        EventValidator eventValidator = new EventValidator(createEventParams);
        eventValidator.validate();

        Event event = eventService.createEvent(createEventParams);

        return ResponseEntity.ok(event);
    }
}
