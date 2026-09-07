package com.krishidb.krishi_api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krishidb.krishi_api.model.TransactionRecord;
import com.krishidb.krishi_api.repository.TransactionRepository;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionRecord> getTransactions(String type, String startDate, String endDate) {
        boolean hasType = type != null && !type.trim().isEmpty() && !"ALL".equalsIgnoreCase(type.trim());
        boolean hasStart = startDate != null && !startDate.trim().isEmpty();
        boolean hasEnd = endDate != null && !endDate.trim().isEmpty();

        if (hasType && hasStart && hasEnd) {
            return transactionRepository.findByTypeAndDateRange(type.trim().toUpperCase(), startDate.trim(), endDate.trim() + " 23:59:59");
        } else if (hasStart && hasEnd) {
            return transactionRepository.findByDateRange(startDate.trim(), endDate.trim() + " 23:59:59");
        } else if (hasType) {
            return transactionRepository.findByTransactionType(type.trim().toUpperCase());
        } else {
            return transactionRepository.findAll();
        }
    }
}
