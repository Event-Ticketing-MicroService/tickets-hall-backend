package com.ticketshall.events.controllers;

import com.ticketshall.events.dtos.params.CreateEventParams;
import com.ticketshall.events.dtos.responses.ListResponse;
import com.ticketshall.events.exceptions.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RequestMapping("/events")
@RestController
public class EventsController {
    @GetMapping("")
    ResponseEntity<?> find()  {
        List<CreateEventParams> mockEvents = List.of(
                new CreateEventParams(1, "a", "aa"),
                new CreateEventParams(2, "b", "bb"),
                new CreateEventParams(3, "c", "cc")
        );

        return ResponseEntity.ok().body(new ListResponse<>(mockEvents, 180));
    }
}
