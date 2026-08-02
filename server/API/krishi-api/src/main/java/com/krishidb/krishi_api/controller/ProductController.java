package com.krishidb.krishi_api.controller;


import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.krishidb.krishi_api.model.Product;
import com.krishidb.krishi_api.repository.ProductRepository;


@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {


    private final ProductRepository repository;


    public ProductController(ProductRepository repository){
        this.repository = repository;
    }


    @GetMapping
    public List<Product> getProducts(){
        return repository.findAll();
    }


    @PostMapping
    public Product addProduct(@RequestBody Product product){

        System.out.println("Received: " + product.getName());

        product.setId(null);

        return repository.save(product);
    }

}