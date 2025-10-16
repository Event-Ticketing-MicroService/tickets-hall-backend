package com.ticketshall.payments.service.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.ticketshall.payments.dto.CreatePaymentRequest;
import com.ticketshall.payments.dto.CreatePaymentResponse;
import com.ticketshall.payments.entity.Payment;
import com.ticketshall.payments.repository.PaymentRepo;
import com.ticketshall.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final PaymentRepo paymentRepo;

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", request.getAmount());
        params.put("currency", request.getCurrency());
        params.put("automatic_payment_methods", Map.of("enabled", true));

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        Payment payment = Payment.builder()
                .paymentIntentId(paymentIntent.getId())
                .clientSecret(paymentIntent.getClientSecret())
                .amount(request.getAmount())
                .currency(request.getCurrency())
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
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            if ("payment_intent.succeeded".equals(event.getType())) {
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);

                if (paymentIntent != null) {
                    // set payment in database
                    Payment payment = paymentRepo.findByPaymentIntentId(paymentIntent.getId())
                            .orElseThrow(() -> new RuntimeException("Payment not found for intent: " + paymentIntent.getId()));

                    payment.setStatus(paymentIntent.getStatus());
                    payment.setUpdatedAt(LocalDateTime.now());
                    paymentRepo.save(payment);

                    // ToDo: send a payment success event in rabbitmq

                }
            }
            return "OK";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
