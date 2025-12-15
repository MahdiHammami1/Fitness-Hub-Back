package com.example.demo.repository;

import com.example.demo.model.ProductVariant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends MongoRepository<ProductVariant, String> {
    List<ProductVariant> findByProductId(String productId);
}
