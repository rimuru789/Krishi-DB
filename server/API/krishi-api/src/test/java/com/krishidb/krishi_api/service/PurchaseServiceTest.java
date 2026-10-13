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

    @Test
    void testCreatePurchase_SubtotalCalculation_50Qty_1350Price_Produces_67500() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Urea 45kg");
        p.setUnit("Bag");
        p.setStockQuantity(10.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(i -> {
            Purchase saved = i.getArgument(0);
            saved.setId(201L);
            return saved;
        });

        PurchaseRequest req = new PurchaseRequest();
        req.setPaymentMethod("CASH");
        PurchaseItemRequest item = new PurchaseItemRequest(1L, 50.0, 1350.0);
        req.setItems(List.of(item));

        Purchase created = purchaseService.createPurchase(req);
        assertNotNull(created);
        assertEquals(201L, created.getId());
        assertEquals(67500.0, created.getTotalAmount(), "Total amount must be exactly 67,500.00");
        assertEquals(60.0, p.getStockQuantity(), "Inventory must increase by exactly 50 units");
        assertEquals(1, created.getItems().size());
        assertEquals(1350.0, created.getItems().get(0).getPricePerUnit(), "Saved item must retain unit price");
        assertEquals(67500.0, created.getItems().get(0).getSubtotal(), "Line subtotal must be 67,500.00");
        verify(productRepository, times(1)).save(p);
        verify(transactionRepository, times(1)).save(argThat(tx ->
            tx.getAmount() == 67500.0 && "PURCHASE".equals(tx.getTransactionType())
        ));
    }

    @Test
    void testCreatePurchase_NullOrInvalidProductId_ThrowsException() {
        PurchaseRequest req1 = new PurchaseRequest();
        req1.setItems(List.of(new PurchaseItemRequest(null, 10.0, 50.0)));
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> purchaseService.createPurchase(req1));
        assertTrue(ex1.getMessage().contains("Valid product ID is required"));

        PurchaseRequest req2 = new PurchaseRequest();
        req2.setItems(List.of(new PurchaseItemRequest(0L, 10.0, 50.0)));
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> purchaseService.createPurchase(req2));
        assertTrue(ex2.getMessage().contains("Valid product ID is required"));
    }

    @Test
    void testCreatePurchase_CreditWithoutSupplier_ThrowsException() {
        PurchaseRequest req = new PurchaseRequest();
        req.setPaymentMethod("CREDIT");
        req.setSupplierId(null);
        req.setItems(List.of(new PurchaseItemRequest(1L, 10.0, 50.0)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> purchaseService.createPurchase(req));
        assertTrue(ex.getMessage().contains("Credit purchase requires a registered supplier ID"));
    }

    @Test
    void testCreatePurchase_CashWithSupplier_DoesNotUpdateSupplierBalance() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Seeds");
        p.setStockQuantity(5.0);

        Supplier s = new Supplier("Supplier A", "12345", "Village");
        s.setId(2L);
        s.setOutstandingBalance(500.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(supplierRepository.findById(2L)).thenReturn(Optional.of(s));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(i -> i.getArgument(0));

        PurchaseRequest req = new PurchaseRequest();
        req.setSupplierId(2L);
        req.setPaymentMethod("CASH");
        PurchaseItemRequest item = new PurchaseItemRequest(1L, 10.0, 50.0);
        req.setItems(List.of(item));

        Purchase created = purchaseService.createPurchase(req);
        assertEquals(500.0, created.getTotalAmount());
        assertEquals(500.0, s.getOutstandingBalance(), "Supplier balance must not change on cash purchase");
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void testPurchaseItemRequest_UnitPriceCompatibilityGettersAndSetters() {
        PurchaseItemRequest req = new PurchaseItemRequest();
        req.setUnitPrice(1350.0);
        assertEquals(1350.0, req.getPricePerUnit());
        assertEquals(1350.0, req.getUnitPrice());
    }

    @Test
    void testPurchaseRequest_PaymentMethodNormalizationInService() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Seeds");
        p.setStockQuantity(5.0);

        Supplier s = new Supplier("Supplier B", "99999", "Pune");
        s.setId(3L);
        s.setOutstandingBalance(1000.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(supplierRepository.findById(3L)).thenReturn(Optional.of(s));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(i -> i.getArgument(0));

        PurchaseRequest req = new PurchaseRequest();
        req.setSupplierId(3L);
        req.setPaymentMethod("Credit Due"); // UI passes "Credit Due"
        req.setItems(List.of(new PurchaseItemRequest(1L, 2.0, 500.0)));

        Purchase created = purchaseService.createPurchase(req);
        assertEquals("CREDIT", created.getPaymentMethod(), "Normalized payment method must be CREDIT");
        assertEquals(2000.0, s.getOutstandingBalance(), "Supplier balance must increase on Credit Due");
    }
}
