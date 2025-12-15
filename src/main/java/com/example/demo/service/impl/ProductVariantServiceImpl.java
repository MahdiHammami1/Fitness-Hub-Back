package com.example.demo.service.impl;

import com.example.demo.model.ProductVariant;
import com.example.demo.repository.ProductVariantRepository;
import com.example.demo.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository repository;

    @Override
    public ProductVariant create(ProductVariant variant) {
        variant.setId(null);
        Instant now = Instant.now();
        variant.setCreatedAt(now);
        variant.setUpdatedAt(now);
        if (variant.getStock() == null) variant.setStock(0);
        return repository.save(variant);
    }

    @Override
    public ProductVariant update(String id, ProductVariant variant) {
        ProductVariant existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ProductVariant not found"));

        existing.setProductId(variant.getProductId());
        existing.setVariantType(variant.getVariantType());
        existing.setValue(variant.getValue());
        existing.setStock(variant.getStock());
        existing.setUpdatedAt(Instant.now());

        return repository.save(existing);
    }

    @Override
    public Optional<ProductVariant> getById(String id) {
        return repository.findById(id);
    }

    @Override
    public List<ProductVariant> getAll() {
        return repository.findAll();
    }

    @Override
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ProductVariant not found");
        }
        repository.deleteById(id);
    }
}

