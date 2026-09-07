package com.krishidb.krishi_api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.krishidb.krishi_api.model.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    long countBySupplierId(Long supplierId);

    @Query("SELECT p FROM Purchase p WHERE p.purchaseDate >= :start AND p.purchaseDate <= :end ORDER BY p.id DESC")
    List<Purchase> findByPurchaseDateRange(@Param("start") String start, @Param("end") String end);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Purchase p")
    double sumTotalPurchaseAmount();
}
