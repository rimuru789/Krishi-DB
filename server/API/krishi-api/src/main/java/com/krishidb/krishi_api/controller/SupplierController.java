package com.krishidb.krishi_api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.krishidb.krishi_api.dto.SupplierPaymentRequest;
import com.krishidb.krishi_api.model.Supplier;
import com.krishidb.krishi_api.service.SupplierService;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public List<Supplier> getSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplier);
    }

    @PostMapping
    public ResponseEntity<Supplier> addSupplier(@RequestBody Supplier supplier) {
        Supplier created = supplierService.createSupplier(supplier);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
        Supplier updated = supplierService.updateSupplier(id, supplier);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Supplier deleted successfully");
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<Supplier> recordPayment(@PathVariable Long id,
            @RequestBody SupplierPaymentRequest request) {
        Double amount = request != null ? request.getAmount() : null;
        String method = request != null ? request.getPaymentMethod() : null;
        String notes = request != null ? request.getNotes() : null;
        Supplier updated = supplierService.recordPayment(id, amount, method, notes);
        return ResponseEntity.ok(updated);
    }
}
