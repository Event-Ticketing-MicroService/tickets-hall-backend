package com.ticketshall.tickets.feign;

import com.ticketshall.tickets.dto.CreatePaymentRequest;
import com.ticketshall.tickets.dto.CreatePaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payments-service", url = "${payment.service.url}")
public interface PaymentServiceClient {

    @PostMapping("/api/payments")
    public ResponseEntity<CreatePaymentResponse> createIntent(@RequestBody CreatePaymentRequest request);

    @PostMapping("/api/payments/cancel/{paymentIntentId}")
    public ResponseEntity<Void> cancelPayment(@PathVariable String paymentIntentId);
}
