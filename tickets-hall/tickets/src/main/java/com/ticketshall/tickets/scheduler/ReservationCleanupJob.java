package com.ticketshall.tickets.scheduler;

import com.ticketshall.tickets.models.nonStoredModels.Reservation;
import com.ticketshall.tickets.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReservationCleanupJob {
    private final ReservationService reservationService;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String RESERVATION_PREFIX = "reservation:";

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredReservations() {
        Set<String> keys = redisTemplate.keys("reservation:*");
        for (String key : keys) {
            Reservation reservation = (Reservation) redisTemplate.opsForValue().get(key);
            if (reservation == null) continue;

            if (reservation.getExpiresAtUtc().isBefore(LocalDateTime.now())) {
                // ToDo: Cancel Stripe session
                reservationService.expireReservation(reservation.getId()); // Restore stock
            }
        }
    }
}
