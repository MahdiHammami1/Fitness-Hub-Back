package com.example.demo.controller;

import com.example.demo.DTO.ProductBatchUpdateRequest;
import com.example.demo.DTO.ProductBatchCreateRequest;
import com.example.demo.DTO.ProductBatchDeleteRequest;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        Product created = productService.create(product);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public List<Product> getAll() {
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable String id) {
        return productService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable String id, @RequestBody Product product) {
        Product updated = productService.update(id, product);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/batch/update")
    public ResponseEntity<List<Product>> batchUpdate(@RequestBody ProductBatchUpdateRequest request) {
        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Product> updatedProducts = request.getProducts()
                .stream()
                .map(product -> productService.update(product.getId(), product))
                .collect(Collectors.toList());

        return ResponseEntity.ok(updatedProducts);
    }

    @PostMapping("/batch/create")
    public ResponseEntity<List<Product>> batchCreate(@RequestBody ProductBatchCreateRequest request) {
        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Product> createdProducts = request.getProducts()
                .stream()
                .map(productService::create)
                .collect(Collectors.toList());

        return new ResponseEntity<>(createdProducts, HttpStatus.CREATED);
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> batchDelete(
            @RequestBody(required = false) ProductBatchDeleteRequest request,
            @RequestParam(required = false) List<String> ids) {

        List<String> productIds = null;

        // Get IDs either from request body or query parameters
        if (request != null && request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            productIds = request.getProductIds();
        } else if (ids != null && !ids.isEmpty()) {
            productIds = ids;
        }

        if (productIds == null || productIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }

        productIds.forEach(productService::delete);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch-ids")
    public ResponseEntity<Void> batchDeleteByIds(@RequestParam List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ids.forEach(productService::delete);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch/delete")
    public ResponseEntity<Void> batchDeletePost(@RequestBody ProductBatchDeleteRequest request) {
        if (request == null || request.getProductIds() == null || request.getProductIds().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        request.getProductIds().forEach(productService::delete);

        return ResponseEntity.noContent().build();
    }
}

