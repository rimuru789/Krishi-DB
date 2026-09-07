package com.krishidb.krishi_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.krishidb.krishi_api.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT COALESCE(SUM(c.outstandingBalance), 0) FROM Customer c")
    double sumTotalOutstandingBalance();
}
