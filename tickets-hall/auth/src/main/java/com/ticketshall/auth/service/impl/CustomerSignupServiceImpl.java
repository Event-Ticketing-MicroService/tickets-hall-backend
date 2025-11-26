package com.ticketshall.auth.service.impl;

import com.ticketshall.auth.DTO.CreateCustomerDTO;
import com.ticketshall.auth.DTO.CustomerResponseDTO;
import com.ticketshall.auth.DTO.VenueRequestDTO;
import com.ticketshall.auth.DTO.VenueResponseDTO;
import com.ticketshall.auth.exceptions_handlers.ExternalServiceException;
import com.ticketshall.auth.feign.CustomerClient;
import com.ticketshall.auth.service.CustomerSignupService;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CustomerSignupServiceImpl implements CustomerSignupService {
    private final CustomerClient customerClient;

    @Override
    @Retry(name = "customerServiceRetry", fallbackMethod = "venueFallback")
    public CustomerResponseDTO createCustomer(CreateCustomerDTO createCustomerDto) {
        return customerClient.addCustomer(createCustomerDto);
    }

    public VenueResponseDTO venueFallback(VenueRequestDTO dto, MultipartFile image, Throwable ex) {
        throw new ExternalServiceException("Venue Service unavailable. Try again later.");
    }
}
