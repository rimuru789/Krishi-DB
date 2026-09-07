package com.krishidb.krishi_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.krishidb.krishi_api.model.PurchaseItem;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
}
