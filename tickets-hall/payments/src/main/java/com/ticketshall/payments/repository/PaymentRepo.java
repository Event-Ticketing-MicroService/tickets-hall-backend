package com.ticketshall.payments.repository;

import com.ticketshall.payments.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepo extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByPaymentIntentId(String id);
}
