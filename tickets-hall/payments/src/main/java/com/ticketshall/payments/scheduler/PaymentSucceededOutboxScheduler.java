package com.ticketshall.payments.scheduler;

import com.ticketshall.payments.constants.GeneralConstants;
import com.ticketshall.payments.entity.OutboxMessage;
import com.ticketshall.payments.helpers.JsonUtil;
import com.ticketshall.payments.mq.events.PaymentSucceededEvent;
import com.ticketshall.payments.mq.producer.PaymentEventProducer;
import com.ticketshall.payments.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentSucceededOutboxScheduler {
    private final JsonUtil jsonUtil;
    private final OutboxRepository outboxRepository;
    private final PaymentEventProducer paymentEventProducer;

    @Scheduled(fixedRate = 2000)
    public void sendPendingPaymentSucceededOutbox() {
        List<OutboxMessage> messages = outboxRepository.findByProcessedFalseAndType(GeneralConstants.PAYMENT_SUCCEEDED_OUTBOX_TYPE);
        for (OutboxMessage message : messages) {
            paymentEventProducer.publishPaymentSucceededEvent(jsonUtil.fromJson(message.getPayload(), PaymentSucceededEvent.class));
            message.setProcessed(true);
            outboxRepository.save(message);
        }
    }
}
