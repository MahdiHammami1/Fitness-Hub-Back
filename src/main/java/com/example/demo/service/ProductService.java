package com.example.demo.service;

import com.example.demo.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product create(Product product);

    Product update(String id, Product product);

    Optional<Product> getById(String id);

    List<Product> getAll();

    void delete(String id);
}

