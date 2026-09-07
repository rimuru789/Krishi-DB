package com.krishidb.krishi_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.krishidb.krishi_api.model.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @Query("SELECT COALESCE(SUM(s.outstandingBalance), 0) FROM Supplier s")
    double sumTotalOutstandingPayable();
}
