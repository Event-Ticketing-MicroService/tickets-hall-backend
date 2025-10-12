package com.ticketshall.tickets.scheduler;

import com.ticketshall.tickets.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationCleanupJob {
    private final ReservationService reservationService;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String RESERVATION_PREFIX = "reservation:";

    @Scheduled(fixedRate = 30000)
    public void cleanup() {

    }
}
