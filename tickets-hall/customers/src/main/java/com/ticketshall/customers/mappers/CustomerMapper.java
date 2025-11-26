package com.ticketshall.customers.mappers;

import com.ticketshall.customers.dtos.CreateCustomerDto;
import com.ticketshall.customers.dtos.UpdateCustomerDto;
import com.ticketshall.customers.models.Customer;

import java.time.LocalDate;

public class CustomerMapper {

    public static Customer toEntity(CreateCustomerDto dto) {
        LocalDate dob = null;
        if (dto.dateOfBirth() != null && !dto.dateOfBirth().isBlank()) {
            dob = LocalDate.parse(dto.dateOfBirth());
        }
        return Customer.builder()
                .email(dto.email())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .dateOfBirth(dob)
                .phoneNumber(dto.phoneNumber())
                .address(dto.address())
                .city(dto.city())
                .country(dto.country())
                .build();
    }

    public static void updateEntity(Customer customer, UpdateCustomerDto dto) {
        if (dto.firstName() != null) {
            customer.setFirstName(dto.firstName());
        }
        if (dto.lastName() != null) {
            customer.setLastName(dto.lastName());
        }
        if (dto.email() != null) {
            customer.setEmail(dto.email());
        }
        if (dto.phoneNumber() != null) {
            customer.setPhoneNumber(dto.phoneNumber());
        }
        if (dto.address() != null) {
            customer.setAddress(dto.address());
        }
        if (dto.city() != null) {
            customer.setCity(dto.city());
        }
        if (dto.country() != null) {
            customer.setCountry(dto.country());
        }
    }
}

