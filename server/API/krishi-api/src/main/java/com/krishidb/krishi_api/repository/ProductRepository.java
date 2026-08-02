package com.krishidb.krishi_api.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.krishidb.krishi_api.model.Product;


public interface ProductRepository 
        extends JpaRepository<Product, Long> {

}