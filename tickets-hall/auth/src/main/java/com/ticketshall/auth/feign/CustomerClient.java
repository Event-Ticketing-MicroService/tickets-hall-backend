package com.ticketshall.auth.feign;

import com.ticketshall.auth.DTO.CreateCustomerDTO;
import com.ticketshall.auth.DTO.CustomerResponseDTO;
import com.ticketshall.auth.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "customer-service",
        url = "http://customer-service/api/customers",
        configuration = FeignConfig.class
)
public interface CustomerClient {

    @PostMapping("/register")
    CustomerResponseDTO addCustomer(@RequestBody CreateCustomerDTO customerDto);
}
