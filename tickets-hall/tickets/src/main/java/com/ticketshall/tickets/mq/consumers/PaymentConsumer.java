package com.ticketshall.tickets.mq.consumers;

import com.ticketshall.tickets.mq.events.PaymentFailedEvent;
import com.ticketshall.tickets.mq.events.PaymentSucceededEvent;
import com.ticketshall.tickets.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentConsumer {
    private final TicketTypeRepository ticketTypeRepository;
    private final RedissonClient redissonClient;

    @RabbitListener(queues = "${app.rabbitmq.queues.paymentSucceeded}")
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {

    }

    @RabbitListener(queues = "${app.rabbitmq.queues.paymentFailed}")
    public void handlePaymentFailed(PaymentFailedEvent event) {

    }
}
