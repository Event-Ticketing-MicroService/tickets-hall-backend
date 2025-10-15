package com.ticketshall.payments.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CreatePaymentRequest {
    private Float amount;
    private String currency;
}
