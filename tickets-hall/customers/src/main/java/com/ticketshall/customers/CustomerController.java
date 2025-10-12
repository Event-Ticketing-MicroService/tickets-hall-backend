package com.ticketshall.customers;

import com.ticketshall.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<Customer> getAllCustomers(){
        return customerService.getAllCustomers();
    }

    @PostMapping
    public ResponseEntity<?> addCustomer(@RequestBody Customer customer){
        Optional<Customer> exists = customerService.findByEmail(customer.getEmail());
        if (exists.isPresent()){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Email already in use."));
        }
        Customer addedCustomer = customerService.addCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedCustomer);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id){
        try {
            Customer customer = customerService.getCustomerById(id);
            return ResponseEntity.ok(customer);
        } catch (IllegalStateException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody Customer updatedCustomer){
        try{
            Customer savedCustomer = customerService.updateCustomer(id, updatedCustomer);
            return ResponseEntity.ok(savedCustomer);
        } catch (IllegalStateException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }


//    @PatchMapping(path = "/{id}")
//    public void updateCustomerPartially(@PathVariable Long id, Map<String, Object> changes){
//     need to implement
//    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id){
        boolean deleted = customerService.deleteCustomer(id);
        if(deleted){
            return ResponseEntity.noContent().build();
        } else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Customer with ID " + id + " not found"));
        }
    }

    @PostMapping(path = "/auth/login")
    public ResponseEntity<?> loginCustomer(@RequestBody LoginRequest request){
        Optional<Customer> customerOpt = customerService.findByEmail(request.getEmail());

        if (customerOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }

        Customer customer = customerOpt.get();

        boolean passwordMatch = customerService.checkPassword(request.getPassword(), customer.getPassword());

        if(!passwordMatch){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }

        return  ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "customerId", customer.getId(),
                "email", customer.getEmail(),
                "name", customer.getName()
        ));
    }

}
