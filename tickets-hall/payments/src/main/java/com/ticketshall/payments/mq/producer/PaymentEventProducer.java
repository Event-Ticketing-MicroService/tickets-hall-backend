package com.ticketshall.payments.mq.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentEventProducer {
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchanges.payment}")
    private String paymentExchangeName;
    @Value("${app.rabbitmq.routing.paymentSucceeded}")
    private String paymentSucceededRoutingKey;
    @Value("${app.rabbitmq.routing.paymentFailed}")
    private String paymentFailedRoutingKey;

    public void publishPaymentSucceededEvent() {

    }
}
