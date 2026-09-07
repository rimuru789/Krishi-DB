package com.krishidb.krishi_api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.krishidb.krishi_api.dto.CustomerPaymentRequest;
import com.krishidb.krishi_api.model.Customer;
import com.krishidb.krishi_api.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<Customer> getCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    @PostMapping
    public ResponseEntity<Customer> addCustomer(@RequestBody Customer customer) {
        Customer created = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        Customer updated = customerService.updateCustomer(id, customer);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Customer deleted successfully");
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<Customer> recordPayment(@PathVariable Long id,
            @RequestBody CustomerPaymentRequest request) {
        Double amount = request != null ? request.getAmount() : null;
        String method = request != null ? request.getPaymentMethod() : null;
        String notes = request != null ? request.getNotes() : null;
        Customer updated = customerService.recordPayment(id, amount, method, notes);
        return ResponseEntity.ok(updated);
    }
}
