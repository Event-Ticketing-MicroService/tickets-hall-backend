package com.ticketshall.tickets.scheduler;

import com.ticketshall.tickets.feign.PaymentServiceClient;
import com.ticketshall.tickets.models.nonStoredModels.Reservation;
import com.ticketshall.tickets.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationCleanupJob {
    private final ReservationService reservationService;
    private static final String RESERVATION_PREFIX = "reservation:";
    private final RedissonClient redissonClient;
    private final PaymentServiceClient paymentServiceClient;

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredReservations() {
        RKeys AllKeys = redissonClient.getKeys();
        var keys = AllKeys.getKeysStream().filter(
                key -> key.startsWith(RESERVATION_PREFIX)).toArray(String[]::new);

        for (String key : keys) {
            RBucket<Reservation> reservationBucket = redissonClient.getBucket(key);
            var reservation = reservationBucket.get();
            if (reservation == null) continue;

            if (reservation.getExpiresAtUtc().isBefore(LocalDateTime.now())) {
                paymentServiceClient.cancelPayment(reservation.getPaymentIntentId());
                reservationService.expireReservation(reservation.getId(), true); // Restore stock
            }
        }
    }
}
