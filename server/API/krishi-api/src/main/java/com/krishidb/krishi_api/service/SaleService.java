package com.krishidb.krishi_api.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krishidb.krishi_api.dto.SaleItemRequest;
import com.krishidb.krishi_api.dto.SaleRequest;
import com.krishidb.krishi_api.exception.ResourceNotFoundException;
import com.krishidb.krishi_api.model.*;
import com.krishidb.krishi_api.repository.*;

@Service
@Transactional(readOnly = true)
public class SaleService {

    private static final Logger log = LoggerFactory.getLogger(SaleService.class);

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public SaleService(SaleRepository saleRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            TransactionRepository transactionRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public Sale getSaleById(Long id) {
        validateId(id);
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
    }

    @Transactional
    public Sale createSale(SaleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Sale request cannot be null");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot complete sale: Cart is empty");
        }

        Sale sale = new Sale();
        sale.setNotes(request.getNotes());

        String paymentMethod = request.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "CASH";
        } else {
            paymentMethod = paymentMethod.trim().toUpperCase();
        }
        sale.setPaymentMethod(paymentMethod);

        Customer customer = null;
        if (request.getCustomerId() != null && request.getCustomerId() > 0) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
            sale.setCustomerId(customer.getId());
            sale.setCustomerName(customer.getName());
        } else if (request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty()) {
            sale.setCustomerName(request.getCustomerName().trim());
        }

        boolean isCredit = "CREDIT".equalsIgnoreCase(paymentMethod) || "KHATA".equalsIgnoreCase(paymentMethod);
        if (isCredit && customer == null) {
            throw new IllegalArgumentException("Credit / Khata payment requires a registered customer ID");
        }

        double totalCalculated = 0.0;
        List<SaleItem> items = new ArrayList<>();

        for (SaleItemRequest itemReq : request.getItems()) {
            if (itemReq.getProductId() == null || itemReq.getProductId() <= 0) {
                throw new IllegalArgumentException("Valid product ID is required for each sale item");
            }
            if (itemReq.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero for product ID: " + itemReq.getProductId());
            }

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemReq.getProductId()));

            // Guarded inventory check
            double availableStock = product.getStockQuantity();
            if (availableStock < itemReq.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product '" + product.getName()
                        + "'. Requested: " + itemReq.getQuantity() + ", Available: " + availableStock);
            }

            double pricePerUnit = (itemReq.getPricePerUnit() != null && itemReq.getPricePerUnit() >= 0)
                    ? itemReq.getPricePerUnit()
                    : product.getSellingPrice();

            SaleItem item = new SaleItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setUnit(product.getUnit());
            item.setQuantity(itemReq.getQuantity());
            item.setPricePerUnit(pricePerUnit);
            double subtotal = Math.round(itemReq.getQuantity() * pricePerUnit * 100.0) / 100.0;
            item.setSubtotal(subtotal);
            items.add(item);

            totalCalculated += subtotal;

            // Atomically decrement inventory
            double updatedStock = availableStock - itemReq.getQuantity();
            product.setStockQuantity(Math.round(updatedStock * 100.0) / 100.0);
            productRepository.save(product);

            log.info("Sale inventory decrement for product {}: previous={}, deducted={}, new={}",
                    product.getId(), availableStock, itemReq.getQuantity(), product.getStockQuantity());
        }

        totalCalculated = Math.round(totalCalculated * 100.0) / 100.0;
        sale.setTotalAmount(totalCalculated);
        sale.setItems(items);

        Sale savedSale = saleRepository.save(sale);

        // Update customer outstanding balance if CREDIT
        if (isCredit && customer != null) {
            double newBalance = Math.round((customer.getOutstandingBalance() + totalCalculated) * 100.0) / 100.0;
            customer.setOutstandingBalance(newBalance);
            customerRepository.save(customer);
            log.info("Updated customer credit outstanding: id={}, added={}, newBalance={}",
                    customer.getId(), totalCalculated, newBalance);
        }

        // Record in transactions ledger
        TransactionRecord tx = new TransactionRecord(
                "SALE",
                savedSale.getId(),
                totalCalculated,
                paymentMethod,
                "POS sale #" + savedSale.getId() + (savedSale.getCustomerName() != null ? " to " + savedSale.getCustomerName() : "")
        );
        transactionRepository.save(tx);

        return savedSale;
    }

    @Transactional
    public void deleteSale(Long id) {
        validateId(id);
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        // Revert product inventory
        for (SaleItem item : sale.getItems()) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                double newStock = product.getStockQuantity() + item.getQuantity();
                product.setStockQuantity(Math.round(newStock * 100.0) / 100.0);
                productRepository.save(product);
            });
        }

        // Revert customer balance if credit
        if (("CREDIT".equalsIgnoreCase(sale.getPaymentMethod()) || "KHATA".equalsIgnoreCase(sale.getPaymentMethod()))
                && sale.getCustomerId() != null) {
            customerRepository.findById(sale.getCustomerId()).ifPresent(customer -> {
                double newBalance = customer.getOutstandingBalance() - sale.getTotalAmount();
                customer.setOutstandingBalance(Math.round(newBalance * 100.0) / 100.0);
                customerRepository.save(customer);
            });
        }

        saleRepository.delete(sale);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Sale ID must be a positive number");
        }
    }
}
