package com.ticketshall.payments.service.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.ticketshall.payments.dto.CreatePaymentRequest;
import com.ticketshall.payments.dto.CreatePaymentResponse;
import com.ticketshall.payments.entity.Payment;
import com.ticketshall.payments.mq.events.PaymentFailedEvent;
import com.ticketshall.payments.mq.events.PaymentSucceededEvent;
import com.ticketshall.payments.mq.producer.PaymentEventProducer;
import com.ticketshall.payments.repository.PaymentRepo;
import com.ticketshall.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final PaymentRepo paymentRepo;
    private final PaymentEventProducer paymentEventProducer;

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", (long)(request.amount() * 100));
        params.put("currency", request.currency());
        params.put("automatic_payment_methods", Map.of("enabled", true));

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        Payment payment = Payment.builder()
                .paymentIntentId(paymentIntent.getId())
                .clientSecret(paymentIntent.getClientSecret())
                .amount(request.amount())
                .attendeeId(request.attendeeId())
                .eventId(request.eventId())
                .reservationId(request.reservationId())
                .currency(request.currency())
                .status(paymentIntent.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        paymentRepo.save(payment);

        return CreatePaymentResponse.builder()
                .id(paymentIntent.getId())
                .status(paymentIntent.getStatus())
                .clientSecret(paymentIntent.getClientSecret())
                .build();
    }

    @Override
    public String handleWebhook(String payload, String sigHeader) {
        try {
            log.error("Received webhook: {}", payload);
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            switch (event.getType()) {
                case "payment_intent.succeeded" -> handlePaymentEvent(event, true);
                case "payment_intent.failed" -> handlePaymentEvent(event, false);
                default -> log.info("Unhandled event type: {}", event.getType());
            }
            return "OK";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handlePaymentEvent(Event event, boolean succeeded) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (paymentIntent != null) {
            // set payment in database
            Payment payment = paymentRepo.findByPaymentIntentId(paymentIntent.getId())
                    .orElseThrow(() -> new RuntimeException("Payment not found for intent: " + paymentIntent.getId()));

            payment.setStatus(paymentIntent.getStatus());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepo.save(payment);

            // publish payment success event in rabbitmq
            if (succeeded) {
                PaymentSucceededEvent paymentSucceededEvent = new PaymentSucceededEvent(payment.getReservationId());
                paymentEventProducer.publishPaymentSucceededEvent(paymentSucceededEvent);
            } else {
                PaymentFailedEvent paymentFailedEvent = new PaymentFailedEvent(payment.getReservationId());
                paymentEventProducer.publishPaymentFailedEvent(paymentFailedEvent);
            }
        }
    }

    @Override
    public void cancelPayment(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        paymentIntent.cancel();

        paymentRepo.findByPaymentIntentId(paymentIntentId).ifPresent(payment -> {
            payment.setStatus("cancelled");
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepo.save(payment);
        });
    }
}
