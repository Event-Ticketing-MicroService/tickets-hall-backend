package com.ticketshall.payments.service;

import com.stripe.exception.StripeException;
import com.ticketshall.payments.dto.CreatePaymentRequest;
import com.ticketshall.payments.dto.CreatePaymentResponse;

public interface PaymentService {
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) throws StripeException;
    public String handleWebhook(String payload, String sigHeader);
    public void cancelPayment(String paymentIntentId) throws StripeException;
}
