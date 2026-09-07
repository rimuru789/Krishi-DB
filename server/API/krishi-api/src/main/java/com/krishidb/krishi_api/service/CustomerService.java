package com.krishidb.krishi_api.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krishidb.krishi_api.exception.ResourceNotFoundException;
import com.krishidb.krishi_api.model.Customer;
import com.krishidb.krishi_api.model.TransactionRecord;
import com.krishidb.krishi_api.repository.CustomerRepository;
import com.krishidb.krishi_api.repository.SaleRepository;
import com.krishidb.krishi_api.repository.TransactionRepository;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final TransactionRepository transactionRepository;

    public CustomerService(CustomerRepository customerRepository,
            SaleRepository saleRepository,
            TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        validateId(id);
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer payload cannot be null");
        }
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty");
        }
        customer.setId(null);
        customer.setName(customer.getName().trim());
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer updated) {
        validateId(id);
        if (updated == null) {
            throw new IllegalArgumentException("Updated customer payload cannot be null");
        }
        if (updated.getName() == null || updated.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty");
        }

        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        existing.setName(updated.getName().trim());
        existing.setPhone(updated.getPhone());
        existing.setVillage(updated.getVillage());
        if (updated.getOutstandingBalance() != 0.0 || existing.getOutstandingBalance() == 0.0) {
            existing.setOutstandingBalance(updated.getOutstandingBalance());
        }

        return customerRepository.save(existing);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        validateId(id);
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        long linkedSales = saleRepository.countByCustomerId(id);
        if (linkedSales > 0) {
            throw new IllegalStateException("Cannot delete customer with " + linkedSales + " existing linked sales records");
        }

        customerRepository.delete(existing);
    }

    @Transactional
    public Customer recordPayment(Long id, Double amount, String paymentMethod, String notes) {
        validateId(id);
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        double previousBalance = customer.getOutstandingBalance();
        double newBalance = Math.round((previousBalance - amount) * 100.0) / 100.0;
        customer.setOutstandingBalance(newBalance);
        Customer savedCustomer = customerRepository.save(customer);

        String method = (paymentMethod != null && !paymentMethod.trim().isEmpty()) ? paymentMethod.trim().toUpperCase() : "CASH";
        String description = (notes != null && !notes.trim().isEmpty())
                ? notes.trim()
                : "Payment received from " + customer.getName() + " (Previous: " + previousBalance + ", Current: " + newBalance + ")";

        TransactionRecord tx = new TransactionRecord(
                "CUSTOMER_PAYMENT",
                customer.getId(),
                amount,
                method,
                description);
        transactionRepository.save(tx);

        log.info("Recorded customer payment: id={}, amount={}, previousBalance={}, newBalance={}, txId={}",
                id, amount, previousBalance, newBalance, tx.getId());

        return savedCustomer;
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Customer ID must be a positive number");
        }
    }
}
