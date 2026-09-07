package com.krishidb.krishi_api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.krishidb.krishi_api.model.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    long countByCustomerId(Long customerId);

    List<Sale> findBySaleDateStartingWith(String datePrefix);

    @Query("SELECT s FROM Sale s WHERE s.saleDate >= :start AND s.saleDate <= :end ORDER BY s.id DESC")
    List<Sale> findBySaleDateRange(@Param("start") String start, @Param("end") String end);

    List<Sale> findTop10ByOrderByIdDesc();

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s")
    double sumTotalSalesAmount();

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.saleDate LIKE CONCAT(:datePrefix, '%')")
    double sumSalesAmountForDate(@Param("datePrefix") String datePrefix);
}
