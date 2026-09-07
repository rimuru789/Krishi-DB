package com.krishidb.krishi_api.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krishidb.krishi_api.exception.ResourceNotFoundException;
import com.krishidb.krishi_api.model.Expense;
import com.krishidb.krishi_api.model.TransactionRecord;
import com.krishidb.krishi_api.repository.ExpenseRepository;
import com.krishidb.krishi_api.repository.TransactionRepository;

@Service
@Transactional(readOnly = true)
public class ExpenseService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseRepository expenseRepository;
    private final TransactionRepository transactionRepository;

    public ExpenseService(ExpenseRepository expenseRepository, TransactionRepository transactionRepository) {
        this.expenseRepository = expenseRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public Expense getExpenseById(Long id) {
        validateId(id);
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }

    @Transactional
    public Expense createExpense(Expense expense) {
        if (expense == null) {
            throw new IllegalArgumentException("Expense payload cannot be null");
        }
        if (expense.getCategory() == null || expense.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Expense category cannot be empty");
        }
        if (expense.getAmount() <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero");
        }

        expense.setId(null);
        expense.setCategory(expense.getCategory().trim());
        expense.setAmount(Math.round(expense.getAmount() * 100.0) / 100.0);
        if (expense.getPaymentMethod() == null || expense.getPaymentMethod().trim().isEmpty()) {
            expense.setPaymentMethod("CASH");
        } else {
            expense.setPaymentMethod(expense.getPaymentMethod().trim().toUpperCase());
        }

        Expense saved = expenseRepository.save(expense);

        TransactionRecord tx = new TransactionRecord(
                "EXPENSE",
                saved.getId(),
                saved.getAmount(),
                saved.getPaymentMethod(),
                "Expense (" + saved.getCategory() + "): " + (saved.getDescription() != null ? saved.getDescription() : "")
        );
        transactionRepository.save(tx);

        log.info("Created expense: id={}, category={}, amount={}, txId={}",
                saved.getId(), saved.getCategory(), saved.getAmount(), tx.getId());

        return saved;
    }

    @Transactional
    public Expense updateExpense(Long id, Expense updated) {
        validateId(id);
        if (updated == null) {
            throw new IllegalArgumentException("Updated expense payload cannot be null");
        }
        if (updated.getCategory() == null || updated.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Expense category cannot be empty");
        }
        if (updated.getAmount() <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero");
        }

        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        existing.setCategory(updated.getCategory().trim());
        existing.setDescription(updated.getDescription());
        existing.setAmount(Math.round(updated.getAmount() * 100.0) / 100.0);
        if (updated.getPaymentMethod() != null && !updated.getPaymentMethod().trim().isEmpty()) {
            existing.setPaymentMethod(updated.getPaymentMethod().trim().toUpperCase());
        }

        return expenseRepository.save(existing);
    }

    @Transactional
    public void deleteExpense(Long id) {
        validateId(id);
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        expenseRepository.delete(existing);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Expense ID must be a positive number");
        }
    }
}
