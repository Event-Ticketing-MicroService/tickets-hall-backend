package com.ticketshall.events.services.impl;

import com.ticketshall.events.dtos.params.CreateTicketTypeParams;
import com.ticketshall.events.exceptions.BadRequestException;
import com.ticketshall.events.exceptions.ConflictErrorException;
import com.ticketshall.events.exceptions.NotFoundException;
import com.ticketshall.events.mappers.TicketTypeMapper;
import com.ticketshall.events.models.Event;
import com.ticketshall.events.models.TicketType;
import com.ticketshall.events.repositories.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketTypeServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketTypeMapper ticketTypeMapper;

    @InjectMocks
    private TicketTypeServiceImpl ticketTypeService;

    private UUID eventId;
    private Event event;
    private CreateTicketTypeParams params;
    private TicketType ticketType;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        event = new Event();
        event.setId(eventId);
        event.setIsPublished(false);
        event.setTotalAvailableTickets(100);
        event.setTicketTypes(new ArrayList<>());

        params = new CreateTicketTypeParams();
        params.setName("VIP");
        params.setQuantity(50);

        ticketType = new TicketType();
        ticketType.setName("vip");
        ticketType.setQuantity(50);
        ticketType.setEventId(eventId);
    }

    @Test
    void createTicketTypes_ShouldSaveAndReturnTicketTypes() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(ticketTypeMapper.toTicketTypeList(anyList())).thenReturn(List.of(ticketType));

        // Mock save to return event with the new ticket type
        Event savedEvent = new Event();
        savedEvent.setTicketTypes(List.of(ticketType));
        when(eventRepository.save(event)).thenReturn(savedEvent);

        // Act
        List<TicketType> result = ticketTypeService.createTicketTypes(eventId, List.of(params));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("vip", result.get(0).getName());
        verify(eventRepository).save(event);
    }

    @Test
    void createTicketTypes_WhenEventNotFound_ShouldThrowNotFoundException() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> ticketTypeService.createTicketTypes(eventId, List.of(params)));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void createTicketTypes_WhenEventPublished_ShouldThrowConflictException() {
        // Arrange
        event.setIsPublished(true);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act & Assert
        assertThrows(ConflictErrorException.class, () -> ticketTypeService.createTicketTypes(eventId, List.of(params)));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void createTicketTypes_WhenNameExists_ShouldThrowConflictException() {
        // Arrange
        TicketType existing = new TicketType();
        existing.setName("vip");
        event.getTicketTypes().add(existing);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act & Assert
        assertThrows(ConflictErrorException.class, () -> ticketTypeService.createTicketTypes(eventId, List.of(params)));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void createTicketTypes_WhenQuantityExceedsTotal_ShouldThrowBadRequestException() {
        // Arrange
        params.setQuantity(150); // Exceeds 100
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> ticketTypeService.createTicketTypes(eventId, List.of(params)));
        verify(eventRepository, never()).save(any());
    }
}
