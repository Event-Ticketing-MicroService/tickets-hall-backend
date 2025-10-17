package com.ticketshall.payments.controller;

import com.stripe.exception.StripeException;
import com.ticketshall.payments.dto.CreatePaymentRequest;
import com.ticketshall.payments.dto.CreatePaymentResponse;
import com.ticketshall.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createIntent(@RequestBody CreatePaymentRequest request) throws StripeException {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        return ResponseEntity.ok(paymentService.handleWebhook(payload, sigHeader));
    }

    @PostMapping("/cancel/{paymentIntentId}")
    public ResponseEntity<Void> cancelPayment(@PathVariable String paymentIntentId) throws StripeException {
        paymentService.cancelPayment(paymentIntentId);
        return ResponseEntity.noContent().build();
    }

}
