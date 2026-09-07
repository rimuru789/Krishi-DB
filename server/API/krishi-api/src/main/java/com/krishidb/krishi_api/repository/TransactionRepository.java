package com.krishidb.krishi_api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.krishidb.krishi_api.model.TransactionRecord;

public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {

    List<TransactionRecord> findByTransactionType(String transactionType);

    @Query("SELECT t FROM TransactionRecord t WHERE t.transactionDate >= :start AND t.transactionDate <= :end ORDER BY t.id DESC")
    List<TransactionRecord> findByDateRange(@Param("start") String start, @Param("end") String end);

    @Query("SELECT t FROM TransactionRecord t WHERE t.transactionType = :type AND t.transactionDate >= :start AND t.transactionDate <= :end ORDER BY t.id DESC")
    List<TransactionRecord> findByTypeAndDateRange(@Param("type") String type, @Param("start") String start, @Param("end") String end);

    List<TransactionRecord> findTop10ByOrderByIdDesc();
}
