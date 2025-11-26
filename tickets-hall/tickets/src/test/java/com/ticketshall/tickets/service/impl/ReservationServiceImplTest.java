package com.ticketshall.tickets.service.impl;

import com.ticketshall.tickets.dto.CreatePaymentRequest;
import com.ticketshall.tickets.dto.CreatePaymentResponse;
import com.ticketshall.tickets.dto.request.ReservationRequest;
import com.ticketshall.tickets.dto.request.ReservationRequestItem;
import com.ticketshall.tickets.exceptions.TicketTypeLockTimeoutException;
import com.ticketshall.tickets.exceptions.TicketTypeNotFoundException;
import com.ticketshall.tickets.exceptions.TicketTypeStockNotEnoughException;
import com.ticketshall.tickets.feign.PaymentServiceClient;
import com.ticketshall.tickets.models.TicketType;
import com.ticketshall.tickets.models.nonStoredModels.Reservation;
import com.ticketshall.tickets.models.nonStoredModels.ReservationItem;
import com.ticketshall.tickets.repository.TicketTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private RLock rLock;

    @Mock
    private RMap<String, String> rMap;

    @Mock
    private RList<TicketType> rList;

    @Mock
    private RBucket<Reservation> rBucket;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private UUID eventId;
    private UUID ticketTypeId;
    private UUID attendeeId;
    private TicketType ticketType;
    private ReservationRequest reservationRequest;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        ticketTypeId = UUID.randomUUID();
        attendeeId = UUID.randomUUID();

        ticketType = new TicketType();
        ticketType.setId(ticketTypeId);
        ticketType.setEventId(eventId);
        ticketType.setName("VIP");
        ticketType.setPrice(100.0f);
        ticketType.setTotalStock(100);
        ticketType.setAvailableStock(50);
        ticketType.setDescription("VIP Access");
        ticketType.setReservationsStartsAtUtc(LocalDateTime.now().minusDays(1));
        ticketType.setReservationsEndsAtUtc(LocalDateTime.now().plusDays(1));
        ticketType.setCreatedAtUtc(LocalDateTime.now().minusDays(2));
        ticketType.setUpdatedAtUtc(LocalDateTime.now().minusDays(2));

        ReservationRequestItem item = new ReservationRequestItem(ticketTypeId, 2);
        reservationRequest = new ReservationRequest(eventId, attendeeId, List.of(item));
    }

    @Test
    void reserve_SuccessfulReservation_ShouldReturnPaymentResponse() throws InterruptedException {
        // Arrange
        ReservationItem resItem = new ReservationItem(ticketTypeId, "VIP", 2, 100.0f);
        CreatePaymentResponse paymentResponse = new CreatePaymentResponse(UUID.randomUUID().toString(), "pending", "client_secret");

        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        doReturn(rMap).when(redissonClient).<String, String>getMap(anyString());
        when(rMap.isEmpty()).thenReturn(true);

        doReturn(rList).when(redissonClient).<TicketType>getList(anyString());
        lenient().when(ticketTypeRepository.getTicketTypesByEventId(eventId)).thenReturn(List.of(ticketType));
        when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));

        when(paymentServiceClient.createIntent(any(CreatePaymentRequest.class))).thenReturn(ResponseEntity.ok(paymentResponse));
        doReturn(rBucket).when(redissonClient).<Reservation>getBucket(anyString());

        // Act
        CreatePaymentResponse result = reservationService.reserve(reservationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(paymentResponse.id(), result.id());
        verify(rLock).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
        verify(ticketTypeRepository).save(any(TicketType.class));
        verify(paymentServiceClient, times(1)).createIntent(any(CreatePaymentRequest.class));
        verify(rLock).unlock();
    }

    @Test
    void reserve_WhenLockTimeout_ShouldThrowException() throws InterruptedException {
        // Arrange
        ReservationRequestItem item = new ReservationRequestItem(ticketTypeId, 2);
        ReservationRequest request = new ReservationRequest(eventId, attendeeId, List.of(item));

        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        // Act & Assert
        assertThrows(TicketTypeLockTimeoutException.class, () -> reservationService.reserve(request));
        verify(rLock, never()).unlock();
    }

    @Test
    void reserve_WhenStockNotEnough_ShouldThrowException() throws InterruptedException {
        // Arrange
        ReservationItem resItem = new ReservationItem(ticketTypeId, "VIP", 60, 100.0f); // Requesting 60, stock is 50
        ReservationRequest invalidRequest = new ReservationRequest(
                eventId,
                attendeeId,
                List.of(new ReservationRequestItem(ticketTypeId, 60))
        );

        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        doReturn(rMap).when(redissonClient).<String, String>getMap(anyString());
        when(rMap.isEmpty()).thenReturn(true);

        doReturn(rList).when(redissonClient).<TicketType>getList(anyString());
        lenient().when(ticketTypeRepository.getTicketTypesByEventId(eventId)).thenReturn(List.of(ticketType));
        lenient().when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));

        // Act & Assert
        assertThrows(TicketTypeStockNotEnoughException.class, () -> reservationService.reserve(invalidRequest));
        verify(rLock).unlock();
    }

    @Test
    void reserve_WhenTicketTypeNotFound_ShouldThrowException() throws InterruptedException {
        // Arrange
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        doReturn(rMap).when(redissonClient).<String, String>getMap(anyString());
        when(rMap.isEmpty()).thenReturn(true);

        doReturn(rList).when(redissonClient).<TicketType>getList(anyString());
        lenient().when(ticketTypeRepository.getTicketTypesByEventId(eventId)).thenReturn(Collections.emptyList());
        lenient().when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TicketTypeNotFoundException.class, () -> reservationService.reserve(reservationRequest));
        verify(rLock).unlock();
    }

    @Test
    void expireReservation_WithStockRecovery_ShouldRecoverStock() throws InterruptedException {
        // Arrange
        UUID reservationId = UUID.randomUUID();
        ReservationItem resItem = new ReservationItem(ticketTypeId, "VIP", 2, 100.0f);
        Reservation reservation = new Reservation(reservationId, attendeeId, eventId, "paymentId", List.of(resItem), 200.0f, LocalDateTime.now());

        doReturn(rBucket).when(redissonClient).<Reservation>getBucket(anyString());
        when(rBucket.get()).thenReturn(reservation);

        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        doReturn(rMap).when(redissonClient).<String, String>getMap(anyString());
        when(rMap.isEmpty()).thenReturn(true);

        doReturn(rList).when(redissonClient).<TicketType>getList(anyString());
        lenient().when(ticketTypeRepository.getTicketTypesByEventId(eventId)).thenReturn(List.of(ticketType));
        lenient().when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));

        // Act
        reservationService.expireReservation(reservationId, true);

        // Assert
        verify(rLock).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
        verify(ticketTypeRepository).save(any(TicketType.class));
        verify(rBucket).delete();
        verify(rLock).unlock();
        // Verify stock was incremented (50 + 2 = 52)
        assertEquals(52, ticketType.getAvailableStock());
    }

    @Test
    void expireReservation_WithoutStockRecovery_ShouldJustDelete() {
        // Arrange
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = new Reservation(reservationId, attendeeId, eventId, "paymentId", Collections.emptyList(), 0.0f, LocalDateTime.now());

        doReturn(rBucket).when(redissonClient).<Reservation>getBucket(anyString());
        when(rBucket.get()).thenReturn(reservation);

        // Act
        reservationService.expireReservation(reservationId, false);

        // Assert
        verify(rBucket).delete();
        verify(redissonClient, never()).getLock(anyString());
    }
}
