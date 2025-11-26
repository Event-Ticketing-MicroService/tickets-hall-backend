package com.ticketshall.tickets.service.impl;

import com.ticketshall.tickets.dto.request.CreateTicketTypeRequest;
import com.ticketshall.tickets.dto.request.UpdateTicketTypeRequest;
import com.ticketshall.tickets.exceptions.TicketTypeNotFoundException;
import com.ticketshall.tickets.mapper.TicketTypeMapper;
import com.ticketshall.tickets.models.Event;
import com.ticketshall.tickets.models.TicketType;
import com.ticketshall.tickets.repository.EventRepository;
import com.ticketshall.tickets.repository.TicketTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketTypeServiceImplTest {

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketTypeMapper ticketTypeMapper;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RList<TicketType> rList;

    @InjectMocks
    private TicketTypeServiceImpl ticketTypeService;

    private UUID eventId;
    private UUID ticketTypeId;
    private TicketType ticketType;
    private Event event;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        ticketTypeId = UUID.randomUUID();

        event = new Event();
        event.setId(eventId);

        ticketType = new TicketType();
        ticketType.setId(ticketTypeId);
        ticketType.setEventId(eventId);
        ticketType.setName("VIP");
        ticketType.setPrice(100.0f);
        ticketType.setTotalStock(100);
        ticketType.setAvailableStock(100);
        ticketType.setReservationsStartsAtUtc(LocalDateTime.now().plusDays(1)); // Future
    }

    @Test
    void createTicketType_WhenEventExists_ShouldCreateAndCache() {
        // Arrange
        CreateTicketTypeRequest request = new CreateTicketTypeRequest(
                "VIP", "Desc", 100.0f, 100, eventId,
                java.time.LocalDate.now().plusDays(1), java.time.LocalDate.now().plusDays(2)
        );

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(ticketTypeMapper.toTicketType(request)).thenReturn(ticketType);
        when(eventRepository.getReferenceById(eventId)).thenReturn(event);
        doReturn(rList).when(redissonClient).<TicketType>getList(anyString());
        when(rList.isExists()).thenReturn(true);
        when(rList.isEmpty()).thenReturn(false);

        // Act
        TicketType created = ticketTypeService.createTicketType(request);

        // Assert
        assertNotNull(created);
        verify(ticketTypeRepository).save(ticketType);
        verify(rList).add(ticketType);
    }

    @Test
    void createTicketType_WhenEventNotFound_ShouldThrowException() {
        // Arrange
        CreateTicketTypeRequest request = new CreateTicketTypeRequest(
                "VIP", "Desc", 100.0f, 100, eventId,
                java.time.LocalDate.now().plusDays(1), java.time.LocalDate.now().plusDays(2)
        );

        when(eventRepository.existsById(eventId)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ticketTypeService.createTicketType(request));
        verify(ticketTypeRepository, never()).save(any());
    }

    @Test
    void updateTicketType_WhenFoundInCache_ShouldUpdateCacheAndDb() {
        // Arrange
        UpdateTicketTypeRequest request = new UpdateTicketTypeRequest(
                ticketTypeId, "VIP Updated", "Desc", 150.0f, 120, eventId
        );

        doReturn(rList).when(redissonClient).<TicketType>getList(anyString());
        when(rList.isExists()).thenReturn(true);
        when(rList.isEmpty()).thenReturn(false);
        when(rList.readAll()).thenReturn(List.of(ticketType));

        // Act
        TicketType updated = ticketTypeService.updateTicketType(request);

        // Assert
        assertEquals("VIP Updated", updated.getName());
        assertEquals(150.0f, updated.getPrice());
        assertEquals(120, updated.getTotalStock());
        assertEquals(120, updated.getAvailableStock()); // 100 + (120 - 100)
        verify(rList).set(eq(0), any(TicketType.class));
        verify(ticketTypeRepository).save(any(TicketType.class));
    }

    @Test
    void updateTicketType_WhenReservationsStarted_ShouldThrowException() {
        // Arrange
        ticketType.setReservationsStartsAtUtc(LocalDateTime.now().minusDays(1)); // Started
        UpdateTicketTypeRequest request = new UpdateTicketTypeRequest(
                ticketTypeId, "VIP Updated", "Desc", 150.0f, 120, eventId
        );

        doReturn(rList).when(redissonClient).<TicketType>getList(anyString());
        when(rList.isExists()).thenReturn(true);
        when(rList.isEmpty()).thenReturn(false);
        when(rList.readAll()).thenReturn(List.of(ticketType));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ticketTypeService.updateTicketType(request));
        verify(ticketTypeRepository, never()).save(any());
    }

    @Test
    void deleteTicketType_WhenReservationsNotStarted_ShouldDelete() {
        // Arrange
        when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));
        doReturn(rList).when(redissonClient).<TicketType>getList(anyString());
        when(rList.isExists()).thenReturn(true);
        when(rList.isEmpty()).thenReturn(false);
        when(rList.readAll()).thenReturn(List.of(ticketType));

        // Act
        boolean result = ticketTypeService.deleteTicketType(ticketTypeId);

        // Assert
        assertTrue(result);
        verify(ticketTypeRepository).delete(ticketType);
        verify(rList).clear();
        verify(rList).addAll(anyList());
    }

    @Test
    void deleteTicketType_WhenReservationsStarted_ShouldThrowException() {
        // Arrange
        ticketType.setReservationsStartsAtUtc(LocalDateTime.now().minusDays(1));
        when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ticketTypeService.deleteTicketType(ticketTypeId));
        verify(ticketTypeRepository, never()).delete(any());
    }

    @Test
    void listTicketTypesForEvent_WhenCacheMiss_ShouldFetchFromDbAndCache() {
        // Arrange
        doReturn(rList).when(redissonClient).<TicketType>getList(anyString());
        when(rList.isExists()).thenReturn(false);
        when(ticketTypeRepository.getTicketTypesByEventId(eventId)).thenReturn(List.of(ticketType));

        // Act
        List<TicketType> result = ticketTypeService.listTicketTypesForEvent(eventId);

        // Assert
        assertEquals(1, result.size());
        verify(rList).addAll(List.of(ticketType));
    }
}
