package com.example.demo.service.impl;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    @Override
    public Product create(Product product) {
        product.setId(null);
        Instant now = Instant.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        if (product.getIsActive() == null) product.setIsActive(true);
        return repository.save(product);
    }

    @Override
    public Product update(String id, Product product) {
        Product existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // copy updatable fields
        existing.setTitle(product.getTitle());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setCollection(product.getCollection());
        existing.setImages(product.getImages());
        existing.setHasVariants(product.getHasVariants());
        existing.setStock(product.getStock());
        existing.setIsActive(product.getIsActive());
        existing.setUpdatedAt(Instant.now());

        return repository.save(existing);
    }

    @Override
    public Optional<Product> getById(String id) {
        return repository.findById(id);
    }

    @Override
    public List<Product> getAll() {
        return repository.findAll();
    }

    @Override
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        repository.deleteById(id);
    }
}
