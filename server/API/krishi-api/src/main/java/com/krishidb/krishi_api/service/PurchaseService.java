package com.krishidb.krishi_api.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krishidb.krishi_api.dto.PurchaseItemRequest;
import com.krishidb.krishi_api.dto.PurchaseRequest;
import com.krishidb.krishi_api.exception.ResourceNotFoundException;
import com.krishidb.krishi_api.model.*;
import com.krishidb.krishi_api.repository.*;

@Service
@Transactional(readOnly = true)
public class PurchaseService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseService.class);

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final TransactionRepository transactionRepository;

    public PurchaseService(PurchaseRepository purchaseRepository,
            ProductRepository productRepository,
            SupplierRepository supplierRepository,
            TransactionRepository transactionRepository) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Purchase> getAllPurchases() {
        return purchaseRepository.findAll();
    }

    public Purchase getPurchaseById(Long id) {
        validateId(id);
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));
    }

    @Transactional
    public Purchase createPurchase(PurchaseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Purchase request cannot be null");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Purchase must contain at least one item");
        }

        Purchase purchase = new Purchase();
        purchase.setNotes(request.getNotes());
        purchase.setInvoiceNumber(request.getInvoiceNumber());

        String paymentMethod = request.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "CASH";
        } else {
            paymentMethod = paymentMethod.trim().toUpperCase();
        }
        purchase.setPaymentMethod(paymentMethod);

        Supplier supplier = null;
        if (request.getSupplierId() != null && request.getSupplierId() > 0) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));
            purchase.setSupplierId(supplier.getId());
            purchase.setSupplierName(supplier.getName());
        } else if (request.getSupplierName() != null && !request.getSupplierName().trim().isEmpty()) {
            purchase.setSupplierName(request.getSupplierName().trim());
        }

        boolean isCredit = "CREDIT".equalsIgnoreCase(paymentMethod);
        if (isCredit && supplier == null) {
            throw new IllegalArgumentException("Credit purchase requires a registered supplier ID");
        }

        double totalCalculated = 0.0;
        List<PurchaseItem> items = new ArrayList<>();

        for (PurchaseItemRequest itemReq : request.getItems()) {
            if (itemReq.getProductId() == null || itemReq.getProductId() <= 0) {
                throw new IllegalArgumentException("Valid product ID is required for each purchase item");
            }
            if (itemReq.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero for product ID: " + itemReq.getProductId());
            }
            if (itemReq.getPricePerUnit() < 0) {
                throw new IllegalArgumentException("Price per unit cannot be negative for product ID: " + itemReq.getProductId());
            }

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemReq.getProductId()));

            PurchaseItem item = new PurchaseItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setUnit(product.getUnit());
            item.setQuantity(itemReq.getQuantity());
            item.setPricePerUnit(itemReq.getPricePerUnit());
            double subtotal = Math.round(itemReq.getQuantity() * itemReq.getPricePerUnit() * 100.0) / 100.0;
            item.setSubtotal(subtotal);
            items.add(item);

            totalCalculated += subtotal;

            // Atomically increment product inventory stock
            double updatedStock = product.getStockQuantity() + itemReq.getQuantity();
            product.setStockQuantity(Math.round(updatedStock * 100.0) / 100.0);
            productRepository.save(product);

            log.info("Purchase stock increment for product {}: previous={}, added={}, new={}",
                    product.getId(), product.getStockQuantity() - itemReq.getQuantity(), itemReq.getQuantity(), product.getStockQuantity());
        }

        totalCalculated = Math.round(totalCalculated * 100.0) / 100.0;
        purchase.setTotalAmount(totalCalculated);
        purchase.setItems(items);

        Purchase savedPurchase = purchaseRepository.save(purchase);

        // Update supplier payable balance if CREDIT
        if (isCredit && supplier != null) {
            double newBalance = Math.round((supplier.getOutstandingBalance() + totalCalculated) * 100.0) / 100.0;
            supplier.setOutstandingBalance(newBalance);
            supplierRepository.save(supplier);
            log.info("Updated supplier credit payable: id={}, added={}, newBalance={}",
                    supplier.getId(), totalCalculated, newBalance);
        }

        // Record in transactions ledger
        TransactionRecord tx = new TransactionRecord(
                "PURCHASE",
                savedPurchase.getId(),
                totalCalculated,
                paymentMethod,
                "Inward purchase #" + savedPurchase.getId() + (savedPurchase.getSupplierName() != null ? " from " + savedPurchase.getSupplierName() : "")
        );
        transactionRepository.save(tx);

        return savedPurchase;
    }

    @Transactional
    public void deletePurchase(Long id) {
        validateId(id);
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));

        // Revert product inventory
        for (PurchaseItem item : purchase.getItems()) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                double newStock = product.getStockQuantity() - item.getQuantity();
                product.setStockQuantity(Math.max(0.0, Math.round(newStock * 100.0) / 100.0));
                productRepository.save(product);
            });
        }

        // Revert supplier balance if credit
        if ("CREDIT".equalsIgnoreCase(purchase.getPaymentMethod()) && purchase.getSupplierId() != null) {
            supplierRepository.findById(purchase.getSupplierId()).ifPresent(supplier -> {
                double newBalance = supplier.getOutstandingBalance() - purchase.getTotalAmount();
                supplier.setOutstandingBalance(Math.round(newBalance * 100.0) / 100.0);
                supplierRepository.save(supplier);
            });
        }

        purchaseRepository.delete(purchase);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Purchase ID must be a positive number");
        }
    }
}
