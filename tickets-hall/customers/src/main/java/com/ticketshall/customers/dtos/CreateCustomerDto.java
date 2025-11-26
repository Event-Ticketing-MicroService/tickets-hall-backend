package com.ticketshall.customers.dtos;

public record CreateCustomerDto(
        String email,
        String firstName,
        String lastName,
        String dateOfBirth,
        String phoneNumber,
        String address,
        String city,
        String country
){}
