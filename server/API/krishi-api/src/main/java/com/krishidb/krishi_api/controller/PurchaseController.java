package com.krishidb.krishi_api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.krishidb.krishi_api.dto.PurchaseRequest;
import com.krishidb.krishi_api.model.Purchase;
import com.krishidb.krishi_api.service.PurchaseService;

@RestController
@RequestMapping("/api/purchases")
@CrossOrigin
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping
    public List<Purchase> getPurchases() {
        return purchaseService.getAllPurchases();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Purchase> getPurchaseById(@PathVariable Long id) {
        Purchase purchase = purchaseService.getPurchaseById(id);
        return ResponseEntity.ok(purchase);
    }

    @PostMapping
    public ResponseEntity<Purchase> addPurchase(@RequestBody PurchaseRequest request) {
        Purchase created = purchaseService.createPurchase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePurchase(@PathVariable Long id) {
        purchaseService.deletePurchase(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Purchase deleted successfully");
        response.put("id", id);
        return ResponseEntity.ok(response);
    }
}
