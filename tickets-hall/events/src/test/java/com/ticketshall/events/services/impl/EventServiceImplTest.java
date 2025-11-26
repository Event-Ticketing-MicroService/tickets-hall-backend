package com.ticketshall.events.services.impl;

import com.ticketshall.events.constants.GeneralConstants;
import com.ticketshall.events.dtos.messages.EventUpsertedMessage;
import com.ticketshall.events.dtos.params.PublishEventParams;
import com.ticketshall.events.dtos.params.UpsertEventParams;
import com.ticketshall.events.exceptions.ConflictErrorException;
import com.ticketshall.events.exceptions.NotFoundException;
import com.ticketshall.events.helpers.JsonUtil;
import com.ticketshall.events.mappers.EventMapper;
import com.ticketshall.events.models.Category;
import com.ticketshall.events.models.Event;
import com.ticketshall.events.models.OutboxMessage;
import com.ticketshall.events.repositories.CategoryRepository;
import com.ticketshall.events.repositories.EventRepository;
import com.ticketshall.events.repositories.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private JsonUtil jsonUtil;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private EventServiceImpl eventService;

    private UpsertEventParams upsertEventParams;
    private MultipartFile image;
    private Category category;
    private Event event;
    private UUID eventId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        upsertEventParams = new UpsertEventParams();
        upsertEventParams.setCategoryId(categoryId);
        upsertEventParams.setName("Test Event");

        image = mock(MultipartFile.class);
        category = new Category();
        category.setId(categoryId);

        event = new Event();
        event.setId(eventId);
        event.setName("Test Event");
        event.setCategory(category);
        event.setEndsAt(LocalDateTime.now().plusDays(1));
    }

    @Test
    void createEvent_ShouldSaveAndReturnEvent() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(eventMapper.toEvent(upsertEventParams)).thenReturn(event);
        when(cloudinaryService.uploadImage(image)).thenReturn("http://image.url");
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toEventUpsertedMessage(event)).thenReturn(new EventUpsertedMessage(eventId, "Test Event", "Desc", "Loc", LocalDateTime.now(), LocalDateTime.now()));
        when(jsonUtil.toJson(any())).thenReturn("{}");

        // Act
        Event result = eventService.createEvent(upsertEventParams, image);

        // Assert
        assertNotNull(result);
        assertEquals(event.getName(), result.getName());
        verify(categoryRepository).findById(categoryId);
        verify(eventRepository).save(event);
        verify(outboxRepository).save(any(OutboxMessage.class));
        verify(cloudinaryService).uploadImage(image);
    }

    @Test
    void createEvent_WhenCategoryNotFound_ShouldThrowException() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> eventService.createEvent(upsertEventParams, image));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void createEvent_WhenUploadFails_ShouldThrowException() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(eventMapper.toEvent(upsertEventParams)).thenReturn(event);
        when(cloudinaryService.uploadImage(image)).thenThrow(new RuntimeException("Upload failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> eventService.createEvent(upsertEventParams, image));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateEvent_ShouldUpdateAndSaveEvent() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(cloudinaryService.uploadImage(image)).thenReturn("http://new-image.url");
        when(eventMapper.toEventUpsertedMessage(event)).thenReturn(new EventUpsertedMessage(eventId, "Test Event", "Desc", "Loc", LocalDateTime.now(), LocalDateTime.now()));
        when(jsonUtil.toJson(any())).thenReturn("{}");

        // Act
        eventService.updateEvent(eventId, upsertEventParams, image);

        // Assert
        verify(eventRepository).findById(eventId);
        verify(eventMapper).updateEventFromUpsertParams(upsertEventParams, event);
        verify(cloudinaryService).uploadImage(image);
        verify(eventRepository).save(event);
        verify(outboxRepository).save(any(OutboxMessage.class));
    }

    @Test
    void updateEvent_WhenEventNotFound_ShouldThrowException() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> eventService.updateEvent(eventId, upsertEventParams, image));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void getEvent_ShouldReturnEvent() {
        // Arrange
        when(eventRepository.findByIdWithCategory(eventId)).thenReturn(Optional.of(event));

        // Act
        Event result = eventService.getEvent(eventId);

        // Assert
        assertNotNull(result);
        assertEquals(eventId, result.getId());
        verify(eventRepository).findByIdWithCategory(eventId);
    }

    @Test
    void getEvent_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(eventRepository.findByIdWithCategory(eventId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> eventService.getEvent(eventId));
    }

    @Test
    void publishEvent_ShouldUpdateStatusAndSave() {
        // Arrange
        PublishEventParams params = new PublishEventParams();
        params.setIsPublished(true);
        event.setIsPublished(false);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventMapper.toEventUpsertedMessage(event)).thenReturn(new EventUpsertedMessage(eventId, "Test Event", "Desc", "Loc", LocalDateTime.now(), LocalDateTime.now()));
        when(jsonUtil.toJson(any())).thenReturn("{}");

        // Act
        eventService.publishEvent(eventId, params);

        // Assert
        assertTrue(event.getIsPublished());
        assertNotNull(event.getPublishedAt());
        verify(eventRepository).save(event);
        verify(outboxRepository).save(any(OutboxMessage.class));
    }

    @Test
    void publishEvent_WhenEventEnded_ShouldThrowConflictException() {
        // Arrange
        PublishEventParams params = new PublishEventParams();
        event.setEndsAt(LocalDateTime.now().minusDays(1));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act & Assert
        assertThrows(ConflictErrorException.class, () -> eventService.publishEvent(eventId, params));
        verify(eventRepository, never()).save(any());
    }
}
