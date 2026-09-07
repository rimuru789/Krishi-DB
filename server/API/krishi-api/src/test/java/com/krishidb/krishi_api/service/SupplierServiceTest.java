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
import com.krishidb.krishi_api.model.Supplier;
import com.krishidb.krishi_api.model.TransactionRecord;
import com.krishidb.krishi_api.repository.PurchaseRepository;
import com.krishidb.krishi_api.repository.SupplierRepository;
import com.krishidb.krishi_api.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private SupplierService supplierService;

    @BeforeEach
    void setUp() {
        supplierService = new SupplierService(supplierRepository, purchaseRepository, transactionRepository);
    }

    @Test
    void testCreateSupplier_Success() {
        Supplier s = new Supplier("IFFCO Seeds", "9876543210", "Hubli");
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(i -> {
            Supplier saved = i.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        Supplier created = supplierService.createSupplier(s);
        assertNotNull(created);
        assertEquals(5L, created.getId());
        assertEquals("IFFCO Seeds", created.getName());
    }

    @Test
    void testRecordPayment_Success() {
        Supplier s = new Supplier("IFFCO", "9876543210", "Hubli");
        s.setId(5L);
        s.setOutstandingBalance(5000.0);

        when(supplierRepository.findById(5L)).thenReturn(Optional.of(s));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(i -> i.getArgument(0));

        Supplier updated = supplierService.recordPayment(5L, 2000.0, "UPI", "Bank transfer");
        assertEquals(3000.0, updated.getOutstandingBalance());
        verify(transactionRepository, times(1)).save(any(TransactionRecord.class));
    }

    @Test
    void testDeleteSupplier_WithLinkedPurchases_ThrowsIllegalStateException() {
        Supplier s = new Supplier("IFFCO", "9876543210", "Hubli");
        s.setId(5L);
        when(supplierRepository.findById(5L)).thenReturn(Optional.of(s));
        when(purchaseRepository.countBySupplierId(5L)).thenReturn(3L);

        assertThrows(IllegalStateException.class, () -> supplierService.deleteSupplier(5L));
        verify(supplierRepository, never()).delete(any());
    }
}
