package com.ticketshall.customers.services;

import com.ticketshall.customers.dtos.CreateCustomerDto;
import com.ticketshall.customers.dtos.UpdateCustomerDto;
import com.ticketshall.customers.mappers.CustomerMapper;
import com.ticketshall.customers.repositories.CustomerRepository;
import com.ticketshall.customers.models.Customer;
import com.ticketshall.mq.events.CustomerCreatedEvent;
import com.ticketshall.mq.events.CustomerDeletedEvent;
import com.ticketshall.mq.events.CustomerUpdatedEvent;
import com.ticketshall.outbox.OutboxService;
import com.ticketshall.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final OutboxService outboxService;

    @Value("${app.rabbitmq.exchanges.customer}")
    private String customerExchange;

    @Value("${app.rabbitmq.routing.customer-created}")
    private String customerCreatedRouting;

    @Value("${app.rabbitmq.routing.customer-updated}")
    private String customerUpdatedRouting;

    @Value("${app.rabbitmq.routing.customer-deleted}")
    private String customerDeletedRouting;

    public CustomerService(CustomerRepository customerRepository, OutboxService outboxService) {
        this.customerRepository = customerRepository;
        this.outboxService = outboxService;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Result<Customer> getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Customer with id " + id + " not found"));
        return Result.success(customer);
    }

    @Transactional(readOnly = true)
    public Result<Customer> findByEmail(String email) {
        var CustomerOpt = customerRepository.findByEmail(email);
        return CustomerOpt.map(Result::success)
                .orElseGet(()
                        -> Result.failure("Customer with email " + email + " not found"));
    }

    @Transactional
    public Customer addCustomer(CreateCustomerDto customerDto) {
        if (customerRepository.findByEmail(customerDto.email()).isPresent()) {
            throw new IllegalStateException("Customer with email " + customerDto.email() + " already exists");
        }

        Customer customer = CustomerMapper.toEntity(customerDto);
        Customer savedCustomer = customerRepository.save(customer);

        CustomerCreatedEvent event = new CustomerCreatedEvent(
                savedCustomer.getId(),
                savedCustomer.getEmail(),
                savedCustomer.getFirstName(),
                savedCustomer.getLastName(),
                savedCustomer.getDateOfBirth(),
                savedCustomer.getPhoneNumber(),
                savedCustomer.getAddress(),
                savedCustomer.getCity(),
                savedCustomer.getCountry()
        );
        // minimal outbox signature: eventType, aggregateId, payload, routingKey, exchange
        outboxService.saveEvent(
                "CustomerCreated",
                savedCustomer.getId(),
                event,
                customerCreatedRouting,
                customerExchange
        );

        return savedCustomer;
    }

    @Transactional
    public Customer updateCustomer(Long id, UpdateCustomerDto updatedCustomerDto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Customer with id " + id + " not found"));

        if (updatedCustomerDto.email() != null &&
                !updatedCustomerDto.email().equals(customer.getEmail()) &&
                customerRepository.findByEmail(updatedCustomerDto.email()).isPresent()) {
            throw new IllegalStateException("Email " + updatedCustomerDto.email() + " is already taken");
        }

        CustomerMapper.updateEntity(customer, updatedCustomerDto);

        Customer saved = customerRepository.save(customer);

        CustomerUpdatedEvent event = new CustomerUpdatedEvent(
                saved.getId(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getDateOfBirth(),
                saved.getPhoneNumber(),
                saved.getAddress(),
                saved.getCity(),
                saved.getCountry()
        );
        outboxService.saveEvent(
                "CustomerUpdated",
                saved.getId(),
                event,
                customerUpdatedRouting,
                customerExchange
        );

        return saved;
    }

    @Transactional
    public boolean deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            return false;
        }

        customerRepository.deleteById(id);

        CustomerDeletedEvent event = new CustomerDeletedEvent(id);
        outboxService.saveEvent(
                "CustomerDeleted",
                id,
                event,
                customerDeletedRouting,
                customerExchange
        );

        return true;
    }

}
