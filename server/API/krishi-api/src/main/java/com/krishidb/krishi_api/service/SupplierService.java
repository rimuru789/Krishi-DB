package com.krishidb.krishi_api.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krishidb.krishi_api.exception.ResourceNotFoundException;
import com.krishidb.krishi_api.model.Supplier;
import com.krishidb.krishi_api.model.TransactionRecord;
import com.krishidb.krishi_api.repository.PurchaseRepository;
import com.krishidb.krishi_api.repository.SupplierRepository;
import com.krishidb.krishi_api.repository.TransactionRepository;

@Service
@Transactional(readOnly = true)
public class SupplierService {

    private static final Logger log = LoggerFactory.getLogger(SupplierService.class);

    private final SupplierRepository supplierRepository;
    private final PurchaseRepository purchaseRepository;
    private final TransactionRepository transactionRepository;

    public SupplierService(SupplierRepository supplierRepository,
            PurchaseRepository purchaseRepository,
            TransactionRepository transactionRepository) {
        this.supplierRepository = supplierRepository;
        this.purchaseRepository = purchaseRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Supplier getSupplierById(Long id) {
        validateId(id);
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    @Transactional
    public Supplier createSupplier(Supplier supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier payload cannot be null");
        }
        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name cannot be empty");
        }
        supplier.setId(null);
        supplier.setName(supplier.getName().trim());
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier updateSupplier(Long id, Supplier updated) {
        validateId(id);
        if (updated == null) {
            throw new IllegalArgumentException("Updated supplier payload cannot be null");
        }
        if (updated.getName() == null || updated.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name cannot be empty");
        }

        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        existing.setName(updated.getName().trim());
        existing.setPhone(updated.getPhone());
        existing.setVillage(updated.getVillage());
        if (updated.getOutstandingBalance() != 0.0 || existing.getOutstandingBalance() == 0.0) {
            existing.setOutstandingBalance(updated.getOutstandingBalance());
        }

        return supplierRepository.save(existing);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        validateId(id);
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        long linkedPurchases = purchaseRepository.countBySupplierId(id);
        if (linkedPurchases > 0) {
            throw new IllegalStateException("Cannot delete supplier with " + linkedPurchases + " existing linked purchases");
        }

        supplierRepository.delete(existing);
    }

    @Transactional
    public Supplier recordPayment(Long id, Double amount, String paymentMethod, String notes) {
        validateId(id);
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        double previousBalance = supplier.getOutstandingBalance();
        double newBalance = Math.round((previousBalance - amount) * 100.0) / 100.0;
        supplier.setOutstandingBalance(newBalance);
        Supplier savedSupplier = supplierRepository.save(supplier);

        String method = (paymentMethod != null && !paymentMethod.trim().isEmpty()) ? paymentMethod.trim().toUpperCase() : "CASH";
        String description = (notes != null && !notes.trim().isEmpty())
                ? notes.trim()
                : "Payment made to " + supplier.getName() + " (Previous: " + previousBalance + ", Current: " + newBalance + ")";

        TransactionRecord tx = new TransactionRecord(
                "SUPPLIER_PAYMENT",
                supplier.getId(),
                amount,
                method,
                description);
        transactionRepository.save(tx);

        log.info("Recorded supplier payment: id={}, amount={}, previousBalance={}, newBalance={}, txId={}",
                id, amount, previousBalance, newBalance, tx.getId());

        return savedSupplier;
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Supplier ID must be a positive number");
        }
    }
}
