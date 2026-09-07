package com.krishidb.krishi_api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.krishidb.krishi_api.dto.StockAdjustmentRequest;
import com.krishidb.krishi_api.model.Product;
import com.krishidb.krishi_api.service.ProductService;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        Product saved = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Product updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Product deleted successfully");
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/adjust-stock")
    public ResponseEntity<Product> adjustStock(@PathVariable Long id,
            @RequestBody StockAdjustmentRequest request) {
        Double delta = request != null ? request.getDelta() : null;
        String reason = request != null ? request.getReason() : null;
        Product updated = productService.adjustStock(id, delta, reason);
        return ResponseEntity.ok(updated);
    }
}