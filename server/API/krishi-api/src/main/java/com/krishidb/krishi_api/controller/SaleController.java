package com.krishidb.krishi_api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.krishidb.krishi_api.dto.SaleRequest;
import com.krishidb.krishi_api.model.Sale;
import com.krishidb.krishi_api.service.SaleService;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public List<Sale> getSales() {
        return saleService.getAllSales();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sale> getSaleById(@PathVariable Long id) {
        Sale sale = saleService.getSaleById(id);
        return ResponseEntity.ok(sale);
    }

    @PostMapping
    public ResponseEntity<Sale> addSale(@RequestBody SaleRequest request) {
        Sale created = saleService.createSale(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSale(@PathVariable Long id) {
        saleService.deleteSale(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Sale deleted successfully");
        response.put("id", id);
        return ResponseEntity.ok(response);
    }
}
