package com.krishidb.krishi_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.krishidb.krishi_api.exception.ResourceNotFoundException;
import com.krishidb.krishi_api.model.Customer;
import com.krishidb.krishi_api.model.TransactionRecord;
import com.krishidb.krishi_api.repository.CustomerRepository;
import com.krishidb.krishi_api.repository.SaleRepository;
import com.krishidb.krishi_api.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, saleRepository, transactionRepository);
    }

    @Test
    void testCreateCustomer_Success() {
        Customer c = new Customer("Ramesh Patil", "9876543210", "Kisanpur");
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> {
            Customer saved = i.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        Customer created = customerService.createCustomer(c);
        assertNotNull(created);
        assertEquals(10L, created.getId());
        assertEquals("Ramesh Patil", created.getName());
    }

    @Test
    void testCreateCustomer_BlankName_ThrowsException() {
        Customer c = new Customer("   ", "9876543210", "Kisanpur");
        assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(c));
    }

    @Test
    void testRecordPayment_Success() {
        Customer c = new Customer("Ramesh", "9876543210", "Village");
        c.setId(10L);
        c.setOutstandingBalance(1000.0);

        when(customerRepository.findById(10L)).thenReturn(Optional.of(c));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        Customer updated = customerService.recordPayment(10L, 400.0, "CASH", "Partial payment");
        assertEquals(600.0, updated.getOutstandingBalance());
        verify(transactionRepository, times(1)).save(any(TransactionRecord.class));
    }

    @Test
    void testRecordPayment_InvalidAmount_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> customerService.recordPayment(10L, -50.0, "CASH", ""));
    }

    @Test
    void testDeleteCustomer_WithLinkedSales_ThrowsIllegalStateException() {
        Customer c = new Customer("Ramesh", "9876543210", "Village");
        c.setId(10L);
        when(customerRepository.findById(10L)).thenReturn(Optional.of(c));
        when(saleRepository.countByCustomerId(10L)).thenReturn(2L);

        assertThrows(IllegalStateException.class, () -> customerService.deleteCustomer(10L));
        verify(customerRepository, never()).delete(any());
    }

    @Test
    void testGetCustomerById_NotFound_ThrowsException() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(999L));
    }
}
