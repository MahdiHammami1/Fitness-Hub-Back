package com.example.demo.controller;

import com.example.demo.model.ProductVariant;
import com.example.demo.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService service;

    @PostMapping
    public ResponseEntity<ProductVariant> create(@RequestBody ProductVariant variant) {
        ProductVariant created = service.create(variant);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public List<ProductVariant> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductVariant> getById(@PathVariable String id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductVariant> update(@PathVariable String id, @RequestBody ProductVariant variant) {
        ProductVariant updated = service.update(id, variant);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

