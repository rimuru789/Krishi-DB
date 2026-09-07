package com.krishidb.krishi_api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.krishidb.krishi_api.model.SaleItem;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    @Query("SELECT si.productId, si.productName, SUM(si.quantity), SUM(si.subtotal) " +
           "FROM SaleItem si GROUP BY si.productId, si.productName ORDER BY SUM(si.quantity) DESC")
    List<Object[]> findTopSellingProducts();
}
