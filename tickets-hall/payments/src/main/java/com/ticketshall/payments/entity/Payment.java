package com.ticketshall.payments.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String paymentIntentId;
    private String status;
    private Float amount;
    private String clientSecret;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
