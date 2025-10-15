package com.ticketshall.payments.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.ticketshall.payments.dto.CreatePaymentRequest;
import com.ticketshall.payments.dto.CreatePaymentResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", request.getAmount());
        params.put("currency", request.getCurrency());
        params.put("automatic_payment_methods", Map.of("enabled", true));

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        return CreatePaymentResponse.builder()
                .id(paymentIntent.getId())
                .status(paymentIntent.getStatus())
                .clientSecret(paymentIntent.getClientSecret())
                .build();
    }
}
