package com.ticketshall.events.controllers;

import com.ticketshall.events.dtos.params.CreateEventParams;
import com.ticketshall.events.dtos.responses.EventDTO;
import com.ticketshall.events.mappers.EventMapper;
import com.ticketshall.events.models.Event;
import com.ticketshall.events.services.EventService;
import com.ticketshall.events.validations.EventValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RequestMapping("/events")
@RestController
public class EventsController {
    private final EventService eventService;
    private final EventMapper eventMapper;

    @Autowired
    public EventsController(EventService eventService,
                            EventMapper eventMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
    }

    @PostMapping("")
    ResponseEntity<?> createEvent(@Valid @RequestBody CreateEventParams createEventParams) {
        EventValidator eventValidator = new EventValidator(createEventParams);
        eventValidator.validate();

        Event event = eventService.createEvent(createEventParams);

        return ResponseEntity.status(HttpStatus.CREATED).body(eventMapper.toEventDTO(event));
    }

    // get single event
    @GetMapping("/{id}")
    ResponseEntity<?> getEvent(@PathVariable UUID id) {
        Event event = eventService.getEvent(id);
        return ResponseEntity.ok().body(eventMapper.toEventDTO(event));
    }


    // TODO: get events paginated/filtered/sorted

    // TODO: publish event

    // TODO: update event
}
