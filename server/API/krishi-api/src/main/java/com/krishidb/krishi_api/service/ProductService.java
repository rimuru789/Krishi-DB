package com.krishidb.krishi_api.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krishidb.krishi_api.exception.ResourceNotFoundException;
import com.krishidb.krishi_api.model.Product;
import com.krishidb.krishi_api.repository.ProductRepository;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    public Product getProductById(Long id) {
        validateId(id);
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public Product createProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product data cannot be null");
        }
        validateProductFields(product);
        product.setId(null);
        return repository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product updated) {
        validateId(id);
        if (updated == null) {
            throw new IllegalArgumentException("Updated product data cannot be null");
        }
        validateProductFields(updated);

        Product existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        existing.setName(updated.getName().trim());
        existing.setCategory(updated.getCategory());
        existing.setUnit(updated.getUnit());
        existing.setSellingPrice(updated.getSellingPrice());
        existing.setStockQuantity(updated.getStockQuantity());
        existing.setLowStockLevel(updated.getLowStockLevel());

        return repository.save(existing);
    }

    @Transactional
    public void deleteProduct(Long id) {
        validateId(id);
        Product existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        repository.delete(existing);
    }

    @Transactional
    public Product adjustStock(Long id, Double delta, String reason) {
        validateId(id);
        if (delta == null) {
            throw new IllegalArgumentException("Adjustment delta cannot be null");
        }

        Product existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        double currentStock = existing.getStockQuantity();
        double newStock = currentStock + delta;

        if (newStock < 0) {
            throw new IllegalArgumentException("Insufficient stock: resulting stock quantity cannot be negative (current: "
                    + currentStock + ", delta: " + delta + ")");
        }

        log.info("Stock adjusted for product id {}: current={}, delta={}, new={}, reason='{}'",
                id, currentStock, delta, newStock, reason);

        existing.setStockQuantity(newStock);
        return repository.save(existing);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }
    }

    private void validateProductFields(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (product.getSellingPrice() < 0) {
            throw new IllegalArgumentException("Selling price cannot be negative");
        }
        if (product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        if (product.getLowStockLevel() < 0) {
            throw new IllegalArgumentException("Low stock level cannot be negative");
        }
    }
}
