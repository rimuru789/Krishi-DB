package com.krishidb.krishi_api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.krishidb.krishi_api.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e WHERE e.expenseDate >= :start AND e.expenseDate <= :end ORDER BY e.id DESC")
    List<Expense> findByExpenseDateRange(@Param("start") String start, @Param("end") String end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    double sumTotalExpenseAmount();

    @Query("SELECT e.category, SUM(e.amount) FROM Expense e GROUP BY e.category")
    List<Object[]> sumExpensesByCategory();
}
