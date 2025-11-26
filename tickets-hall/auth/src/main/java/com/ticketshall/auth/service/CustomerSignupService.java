package com.ticketshall.auth.service;

import com.ticketshall.auth.DTO.CreateCustomerDTO;
import com.ticketshall.auth.DTO.CustomerResponseDTO;

public interface CustomerSignupService {
    CustomerResponseDTO createCustomer(CreateCustomerDTO createCustomerDto);
}
