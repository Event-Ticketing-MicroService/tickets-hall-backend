package com.ticketshall.customers.dtos;

public record UpdateCustomerDto(
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String address,
        String city,
        String country
){ }
