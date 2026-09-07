package com.krishidb.krishi_api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.krishidb.krishi_api.model.TransactionRecord;
import com.krishidb.krishi_api.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionRecord> getTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return transactionService.getTransactions(type, startDate, endDate);
    }
}
