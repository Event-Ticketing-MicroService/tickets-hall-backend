package com.ticketshall.events.controllers;

import com.ticketshall.events.dtos.params.UpsertEventParams;
import com.ticketshall.events.dtos.params.PublishEventParams;
import com.ticketshall.events.dtos.responses.EventDTO;
import com.ticketshall.events.dtos.responses.ListResponse;
import com.ticketshall.events.dtos.filterparams.EventFilterParams;
import com.ticketshall.events.mappers.EventMapper;
import com.ticketshall.events.models.Event;
import com.ticketshall.events.services.EventService;
import com.ticketshall.events.validations.EventValidator;
import jakarta.servlet.ServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    ResponseEntity<?> createEvent(@Valid @RequestBody UpsertEventParams UpsertEventParams) {
        EventValidator eventValidator = new EventValidator(UpsertEventParams);
        eventValidator.validate();

        Event event = eventService.createEvent(UpsertEventParams);

        return ResponseEntity.status(HttpStatus.CREATED).body(eventMapper.toEventDTO(event));
    }

    // get single event
    @GetMapping("/{id}")
    ResponseEntity<?> getEvent(@PathVariable UUID id) {
        Event event = eventService.getEvent(id);
        return ResponseEntity.ok().body(eventMapper.toEventDTO(event));
    }


    // TODO: get events paginated/filtered/sorted
    @GetMapping("")
    ResponseEntity<?> getAllEvents(
            EventFilterParams eventFilterParams,
            @PageableDefault(size = 10, page = 0, sort = "startsAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<Event> eventsPage = eventService.getAllEvents(eventFilterParams, pageable);
        Page<EventDTO> eventDTOS = eventsPage.map(eventMapper::toEventDTO);
        return ResponseEntity.ok().body(new ListResponse(eventDTOS.getContent(), eventDTOS.getNumberOfElements()));
    }

    // TODO: publish event
    @PatchMapping("/{id}/publish")
    ResponseEntity<?> publishEvent(
            @PathVariable UUID id,
            @RequestBody PublishEventParams publishEventParams
    ) {
        eventService.publishEvent(id, publishEventParams);
        return ResponseEntity.noContent().build();
    }

    // TODO: update event

    @PutMapping("/{id}")
    ResponseEntity<?> updateEvent(@PathVariable UUID id, @RequestBody UpsertEventParams upsertEventParams, ServletRequest servletRequest) {
        eventService.updateEvent(id, upsertEventParams);
        return ResponseEntity.noContent().build();
    }
}
