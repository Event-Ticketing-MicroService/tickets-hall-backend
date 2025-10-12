package com.ticketshall.customers;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    @Transactional
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new IllegalStateException("customer by id: " + id + " does not exist"));
    }

    public Customer addCustomer(Customer customer) {
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
            return customerRepository.save(customer);
        }

    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Customer by id " + id + " not found"));

        customer.setName(updatedCustomer.getName());
        customer.setEmail(updatedCustomer.getEmail());
        customer.setPhoneNumber(updatedCustomer.getPhoneNumber());
        customer.setDob(updatedCustomer.getDob());
        customer.setRole(updatedCustomer.getRole());
        return customer;
    }

    public boolean deleteCustomer(Long id) {
        if(!customerRepository.existsById(id)){
            return false;
        }
        customerRepository.deleteById(id);
        return true;
    }

    public boolean checkPassword(String password, String userPassword) {
        return passwordEncoder.matches(password, userPassword);
    }
}
