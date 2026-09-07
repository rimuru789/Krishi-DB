package com.krishidb.krishi_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.krishidb.krishi_api.dto.SaleItemRequest;
import com.krishidb.krishi_api.dto.SaleRequest;
import com.krishidb.krishi_api.exception.ResourceNotFoundException;
import com.krishidb.krishi_api.model.Customer;
import com.krishidb.krishi_api.model.Product;
import com.krishidb.krishi_api.model.Sale;
import com.krishidb.krishi_api.model.TransactionRecord;
import com.krishidb.krishi_api.repository.CustomerRepository;
import com.krishidb.krishi_api.repository.ProductRepository;
import com.krishidb.krishi_api.repository.SaleRepository;
import com.krishidb.krishi_api.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private SaleService saleService;

    @BeforeEach
    void setUp() {
        saleService = new SaleService(saleRepository, productRepository, customerRepository, transactionRepository);
    }

    @Test
    void testCreateSale_DecrementsStock_Success() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Wheat");
        p.setUnit("kg");
        p.setSellingPrice(50.0);
        p.setStockQuantity(100.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(saleRepository.save(any(Sale.class))).thenAnswer(i -> {
            Sale saved = i.getArgument(0);
            saved.setId(50L);
            return saved;
        });

        SaleRequest req = new SaleRequest();
        req.setPaymentMethod("CASH");
        SaleItemRequest item = new SaleItemRequest(1L, 20.0, 50.0);
        req.setItems(List.of(item));

        Sale created = saleService.createSale(req);
        assertNotNull(created);
        assertEquals(50L, created.getId());
        assertEquals(1000.0, created.getTotalAmount());
        // Verify inventory decreased
        assertEquals(80.0, p.getStockQuantity());
        verify(productRepository, times(1)).save(p);
        verify(transactionRepository, times(1)).save(any(TransactionRecord.class));
    }

    @Test
    void testCreateSale_InsufficientStock_ThrowsException() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Wheat");
        p.setStockQuantity(10.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        SaleRequest req = new SaleRequest();
        SaleItemRequest item = new SaleItemRequest(1L, 15.0, 50.0);
        req.setItems(List.of(item));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> saleService.createSale(req));
        assertTrue(ex.getMessage().contains("Insufficient stock"));
        verify(saleRepository, never()).save(any());
    }

    @Test
    void testCreateSale_Credit_UpdatesCustomerOutstanding() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Fertilizer");
        p.setStockQuantity(50.0);

        Customer c = new Customer("Farmer John", "9876543210", "Green Valley");
        c.setId(3L);
        c.setOutstandingBalance(200.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(customerRepository.findById(3L)).thenReturn(Optional.of(c));
        when(saleRepository.save(any(Sale.class))).thenAnswer(i -> i.getArgument(0));

        SaleRequest req = new SaleRequest();
        req.setCustomerId(3L);
        req.setPaymentMethod("CREDIT");
        SaleItemRequest item = new SaleItemRequest(1L, 5.0, 100.0);
        req.setItems(List.of(item));

        Sale created = saleService.createSale(req);
        assertEquals(500.0, created.getTotalAmount());
        assertEquals(700.0, c.getOutstandingBalance());
        verify(customerRepository, times(1)).save(c);
    }

    @Test
    void testCreateSale_EmptyCart_ThrowsException() {
        SaleRequest req = new SaleRequest();
        req.setItems(List.of());
        assertThrows(IllegalArgumentException.class, () -> saleService.createSale(req));
    }
}
