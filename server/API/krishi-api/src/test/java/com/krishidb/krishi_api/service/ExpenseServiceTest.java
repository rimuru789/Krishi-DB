package com.krishidb.krishi_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.krishidb.krishi_api.model.Expense;
import com.krishidb.krishi_api.model.TransactionRecord;
import com.krishidb.krishi_api.repository.ExpenseRepository;
import com.krishidb.krishi_api.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseService = new ExpenseService(expenseRepository, transactionRepository);
    }

    @Test
    void testCreateExpense_Success() {
        Expense exp = new Expense("Electricity", "Shop electricity bill", 1200.0, "CASH");
        when(expenseRepository.save(any(Expense.class))).thenAnswer(i -> {
            Expense saved = i.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Expense created = expenseService.createExpense(exp);
        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals("Electricity", created.getCategory());
        assertEquals(1200.0, created.getAmount());
        verify(transactionRepository, times(1)).save(any(TransactionRecord.class));
    }

    @Test
    void testCreateExpense_InvalidAmount_ThrowsException() {
        Expense exp = new Expense("Rent", "Shop rent", -500.0, "CASH");
        assertThrows(IllegalArgumentException.class, () -> expenseService.createExpense(exp));
    }
}
