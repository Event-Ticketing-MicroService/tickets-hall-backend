package com.ticketshall.events.services.impl;

import com.ticketshall.events.constants.GeneralConstants;
import com.ticketshall.events.dtos.params.UpsertEventParams;
import com.ticketshall.events.dtos.filterparams.EventFilterParams;
import com.ticketshall.events.dtos.params.PublishEventParams;
import com.ticketshall.events.exceptions.ConflictErrorException;
import com.ticketshall.events.exceptions.GlobalExceptionHandler;
import com.ticketshall.events.exceptions.NotFoundException;
import com.ticketshall.events.helpers.JsonUtil;
import com.ticketshall.events.mappers.EventMapper;
import com.ticketshall.events.models.Category;
import com.ticketshall.events.models.Event;
import com.ticketshall.events.models.OutboxMessage;
import com.ticketshall.events.rabbitmq.producers.EventProducer;
import com.ticketshall.events.repositories.CategoryRepository;
import com.ticketshall.events.repositories.EventRepository;
import com.ticketshall.events.repositories.OutboxRepository;
import com.ticketshall.events.repositories.specifications.EventSpecification;
import com.ticketshall.events.services.EventService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class EventServiceImpl implements EventService {
    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final OutboxRepository outboxRepository;
    private final JsonUtil jsonUtil;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, EventMapper eventMapper, CategoryRepository categoryRepository, OutboxRepository outboxRepository, JsonUtil jsonUtil, GlobalExceptionHandler globalExceptionHandler, CloudinaryService cloudinaryService) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.categoryRepository = categoryRepository;
        this.outboxRepository = outboxRepository;
        this.jsonUtil = jsonUtil;
        this.cloudinaryService = cloudinaryService;
    }


    @Override
    @Transactional
    public Event createEvent(UpsertEventParams UpsertEventParams, MultipartFile image) {
        String imageUrl = "";
        try {
            Optional<Category> category = categoryRepository.findById(UpsertEventParams.getCategoryId());
            if(category.isEmpty()) throw new NotFoundException("No category found with the given id");
            Event event = eventMapper.toEvent(UpsertEventParams);
            event.setCategory(category.get());

            // upload and set imageUrl
            // TODO: This blocks the DB connection for the uploading time, we
            //  can use TransactionalTemplate to only make a transaction for db writes without the img upload time
            imageUrl = cloudinaryService.uploadImage(image);
            event.setBackgroundImageUrl(imageUrl);

            Event savedEvent = eventRepository.save(event);
//        eventProducer.sendEventCreated(eventMapper.toEventUpsertedMessage(event)); I publish the event in the scheduler after checking that it's still pending
            OutboxMessage outboxMessage = OutboxMessage.builder()
                    .type(GeneralConstants.EVENT_CREATED_OUTBOX_TYPE)
                    .payload(jsonUtil.toJson(eventMapper.toEventUpsertedMessage(event)))
                    .createdAt(LocalDateTime.now())
                    .processed(false)
                    .build();
            outboxRepository.save(outboxMessage);
            return savedEvent;
        } catch (Exception e) {
            if(!imageUrl.isEmpty()) cloudinaryService.deleteImage(imageUrl);
            throw e;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = GeneralConstants.EVENTS_CACHE_NAME, key = "#id")
    public void updateEvent(UUID id, UpsertEventParams eventUpdateParams, MultipartFile image) {
        Optional<Event> eventOptional = eventRepository.findById(id);
        if(eventOptional.isEmpty()) throw new NotFoundException("Event with given id is not found");
        Event updatedEvent = eventOptional.get();
        eventMapper.updateEventFromUpsertParams(eventUpdateParams, updatedEvent);

        if(!image.isEmpty()) {
            String oldImageUrl = updatedEvent.getBackgroundImageUrl();
            if(oldImageUrl != null) {
                cloudinaryService.deleteImage(oldImageUrl);
            }
            String imageUrl = cloudinaryService.uploadImage(image);
            updatedEvent.setBackgroundImageUrl(imageUrl);
        }

        eventRepository.save(updatedEvent);

        OutboxMessage outboxMessage = OutboxMessage.builder()
                .type(GeneralConstants.EVENT_UPDATED_OUTBOX_TYPE)
                .payload(jsonUtil.toJson(eventMapper.toEventUpsertedMessage(updatedEvent)))
                .createdAt(LocalDateTime.now())
                .processed(false)
                .build();
        outboxRepository.save(outboxMessage);
//        eventProducer.sendEventUpdated(eventMapper.toEventUpsertedMessage(updatedEvent));
    }

    @Cacheable(value = GeneralConstants.EVENTS_CACHE_NAME, key = "#id")
    @Override
    public Event getEvent(UUID id) {
        Optional<Event> event = eventRepository.findByIdWithCategory(id);
        if(event.isEmpty()) throw new NotFoundException("Event with given id is not found");
        return event.get();
    }

    @Override
    public Page<Event> getAllEvents(EventFilterParams eventFilterParams, Pageable pageable) {
        Specification<Event> spec = EventSpecification.applyFilter(eventFilterParams);
        return eventRepository.findAll(spec, pageable);
    }

    @Override
    @CacheEvict(value = GeneralConstants.EVENTS_CACHE_NAME, key = "#eventId")
    @Transactional
    public void publishEvent(UUID eventId, PublishEventParams publishEventParams) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) throw new NotFoundException("Event with given id is not found");
        Event event = eventOptional.get();

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(event.getEndsAt())) throw new ConflictErrorException("Event has already ended");

        if (publishEventParams.getIsPublished() && !event.getIsPublished()) event.setPublishedAt(now);
        event.setIsPublished(publishEventParams.getIsPublished());
        eventRepository.save(event);
        OutboxMessage outboxMessage = OutboxMessage.builder()
                .type(GeneralConstants.EVENT_UPDATED_OUTBOX_TYPE)
                .payload(jsonUtil.toJson(eventMapper.toEventUpsertedMessage(event)))
                .createdAt(LocalDateTime.now())
                .processed(false)
                .build();
        outboxRepository.save(outboxMessage);
//        eventProducer.sendEventUpdated(eventMapper.toEventUpsertedMessage(event));
    }
}
