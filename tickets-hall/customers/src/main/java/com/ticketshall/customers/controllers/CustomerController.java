package com.ticketshall.customers.controllers;

import com.ticketshall.customers.dtos.CreateCustomerDto;
import com.ticketshall.customers.dtos.UpdateCustomerDto;
import com.ticketshall.customers.services.CustomerService;
import com.ticketshall.customers.models.Customer;
import com.ticketshall.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "api/customers")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        try {
            var customer = customerService.getCustomerById(id);
            return ResponseEntity.ok(customer);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound()
                    .build();
        }
    }

    @GetMapping(path = "/email/{email}")
    public ResponseEntity<?> getCustomerByEmail(@PathVariable String email) {
        Result<Customer> result = customerService.findByEmail(email);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound()
                    .build();
        }
    }

    @PostMapping
    public ResponseEntity<?> addCustomer(@RequestBody CreateCustomerDto customerDto) {
        try {
            var addedCustomer = customerService.addCustomer(customerDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(addedCustomer);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody UpdateCustomerDto updatedCustomerDto) {
        try {
            var savedCustomer = customerService.updateCustomer(id, updatedCustomerDto);
            return ResponseEntity.ok(savedCustomer);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        boolean deleted = customerService.deleteCustomer(id);
        if (deleted) {
            return ResponseEntity
                    .noContent()
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Customer with ID " + id + " not found"));
        }
    }
}
