package com.example.demo.service;

import com.example.demo.model.ProductVariant;

import java.util.List;
import java.util.Optional;

public interface ProductVariantService {
    ProductVariant create(ProductVariant variant);

    ProductVariant update(String id, ProductVariant variant);

    Optional<ProductVariant> getById(String id);

    List<ProductVariant> getAll();

    void delete(String id);
}

