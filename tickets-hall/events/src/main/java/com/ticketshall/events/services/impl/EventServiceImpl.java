package com.ticketshall.events.services.impl;

import com.ticketshall.events.dtos.params.CreateEventParams;
import com.ticketshall.events.exceptions.NotFoundException;
import com.ticketshall.events.mappers.EventMapper;
import com.ticketshall.events.models.Category;
import com.ticketshall.events.models.Event;
import com.ticketshall.events.repositories.CategoryRepository;
import com.ticketshall.events.repositories.EventRepository;
import com.ticketshall.events.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class EventServiceImpl implements EventService {
    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, EventMapper eventMapper, CategoryRepository categoryRepository) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.categoryRepository = categoryRepository;
    }
    @Override
    public Event createEvent(CreateEventParams createEventParams) {
        Optional<Category> category = categoryRepository.findById(createEventParams.getCategoryId());
        if(category.isEmpty()) throw new NotFoundException("No category found with the given id");
        Event event = eventMapper.toEvent(createEventParams);
        event.setCategory(category.get());
        //TODO: push an EVENT_CREATED message to RabbitMQ
        return eventRepository.save(event);
    }

    @Override
    public Event getEvent(UUID id) {
        Optional<Event> event = eventRepository.findByIdWithCategory(id);
        if(event.isEmpty()) throw new NotFoundException("Event with given id is not found");
        return event.get();
    }
}
