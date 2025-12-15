package com.example.demo.repository;

import com.example.demo.model.Product;
import com.example.demo.model.ProductCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Page<Product> findByIsActiveTrueAndCollection(ProductCollection c, Pageable pageable);
    Page<Product> findByIsActiveTrue(Pageable pageable);
}
