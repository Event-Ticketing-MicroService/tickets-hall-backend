package com.ticketshall.customers;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

    @Transactional
    public void updateCustomerPartially(Long id, Map<String, Object> changes) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new IllegalStateException("Customer with id: " + id + " not found."));

        changes.forEach((key, value) -> {
            try{
                Field field = Customer.class.getDeclaredField(key);
                field.setAccessible(true);

                if (field.getType().equals(LocalDate.class) && value instanceof String) {
                    value = LocalDate.parse((String) value);
                }

                field.set(customer, value);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException("Invalid field: " + key);
            }
        });
    }

    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        Customer customer = customerRepository.findById(id).orElseThrow(()-> new IllegalStateException("User with id " + id + " not found."));
        if(!passwordEncoder.matches(oldPassword, customer.getPassword())){
            throw new IllegalStateException("Invalid password entered.");
        }
        customer.setPassword(passwordEncoder.encode(newPassword));
    }

}
