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

import com.krishidb.krishi_api.dto.PurchaseItemRequest;
import com.krishidb.krishi_api.dto.PurchaseRequest;
import com.krishidb.krishi_api.exception.ResourceNotFoundException;
import com.krishidb.krishi_api.model.Product;
import com.krishidb.krishi_api.model.Purchase;
import com.krishidb.krishi_api.model.Supplier;
import com.krishidb.krishi_api.model.TransactionRecord;
import com.krishidb.krishi_api.repository.ProductRepository;
import com.krishidb.krishi_api.repository.PurchaseRepository;
import com.krishidb.krishi_api.repository.SupplierRepository;
import com.krishidb.krishi_api.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private PurchaseService purchaseService;

    @BeforeEach
    void setUp() {
        purchaseService = new PurchaseService(purchaseRepository, productRepository, supplierRepository, transactionRepository);
    }

    @Test
    void testCreatePurchase_IncrementsStock_Success() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Fertilizer");
        p.setUnit("bag");
        p.setStockQuantity(10.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(i -> {
            Purchase saved = i.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        PurchaseRequest req = new PurchaseRequest();
        req.setPaymentMethod("CASH");
        PurchaseItemRequest item = new PurchaseItemRequest(1L, 25.0, 500.0);
        req.setItems(List.of(item));

        Purchase created = purchaseService.createPurchase(req);
        assertNotNull(created);
        assertEquals(100L, created.getId());
        assertEquals(12500.0, created.getTotalAmount());
        // Verify product stock incremented
        assertEquals(35.0, p.getStockQuantity());
        verify(productRepository, times(1)).save(p);
        verify(transactionRepository, times(1)).save(any(TransactionRecord.class));
    }

    @Test
    void testCreatePurchase_Credit_UpdatesSupplierBalance() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Seeds");
        p.setStockQuantity(5.0);

        Supplier s = new Supplier("Supplier A", "12345", "Village");
        s.setId(2L);
        s.setOutstandingBalance(100.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(supplierRepository.findById(2L)).thenReturn(Optional.of(s));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(i -> i.getArgument(0));

        PurchaseRequest req = new PurchaseRequest();
        req.setSupplierId(2L);
        req.setPaymentMethod("CREDIT");
        PurchaseItemRequest item = new PurchaseItemRequest(1L, 10.0, 50.0);
        req.setItems(List.of(item));

        Purchase created = purchaseService.createPurchase(req);
        assertEquals(500.0, created.getTotalAmount());
        assertEquals(600.0, s.getOutstandingBalance());
        verify(supplierRepository, times(1)).save(s);
    }

    @Test
    void testCreatePurchase_ProductNotFound_ThrowsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        PurchaseRequest req = new PurchaseRequest();
        PurchaseItemRequest item = new PurchaseItemRequest(999L, 10.0, 50.0);
        req.setItems(List.of(item));

        assertThrows(ResourceNotFoundException.class, () -> purchaseService.createPurchase(req));
    }
}
