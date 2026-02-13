package com.example.demo.controller;

import com.example.demo.DTO.ReorderProductRequest;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products/reorder")
@RequiredArgsConstructor
public class ProductReorderController {

    private final ProductService productService;

    /**
     * Reorder products based on a provided list of product IDs
     * This endpoint saves the new order to the database by updating displayOrder for each product
     * @param request contains the list of product IDs in the desired order
     * @return list of products in the new order
     */
    @PostMapping
    public ResponseEntity<List<Product>> reorderProducts(@RequestBody ReorderProductRequest request) {
        if (request.getProductIds() == null || request.getProductIds().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<String> orderedIds = request.getProductIds();
        List<Product> reorderedProducts = new ArrayList<>();

        // Update displayOrder for each product and save to database
        for (int i = 0; i < orderedIds.size(); i++) {
            String productId = orderedIds.get(i);
            Product product = productService.getById(productId).orElse(null);

            if (product != null) {
                product.setDisplayOrder(i);
                product.setUpdatedAt(Instant.now());
                Product updatedProduct = productService.update(productId, product);
                reorderedProducts.add(updatedProduct);
            }
        }

        return ResponseEntity.ok(reorderedProducts);
    }

    /**
     * Move a product to a specific position in the list and save the order
     * @param productId the ID of the product to move
     * @param newPosition the desired position (0-based index)
     * @return the updated list of products in the new order
     */
    @PutMapping("/{productId}/move")
    public ResponseEntity<List<Product>> moveProduct(
            @PathVariable String productId,
            @RequestParam int newPosition) {

        List<Product> allProducts = productService.getAll()
                .stream()
                .sorted((p1, p2) -> {
                    Integer order1 = p1.getDisplayOrder() != null ? p1.getDisplayOrder() : 0;
                    Integer order2 = p2.getDisplayOrder() != null ? p2.getDisplayOrder() : 0;
                    return order1.compareTo(order2);
                })
                .collect(Collectors.toList());

        // Find the product
        Product productToMove = allProducts.stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (productToMove == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Remove the product from its current position
        allProducts.remove(productToMove);

        // Ensure the new position is within bounds
        if (newPosition < 0) {
            newPosition = 0;
        } else if (newPosition > allProducts.size()) {
            newPosition = allProducts.size();
        }

        // Insert at the new position
        allProducts.add(newPosition, productToMove);

        // Update displayOrder for all products and save
        List<Product> reorderedProducts = new ArrayList<>();
        for (int i = 0; i < allProducts.size(); i++) {
            Product p = allProducts.get(i);
            p.setDisplayOrder(i);
            p.setUpdatedAt(Instant.now());
            Product updatedProduct = productService.update(p.getId(), p);
            reorderedProducts.add(updatedProduct);
        }

        return ResponseEntity.ok(reorderedProducts);
    }

    /**
     * Swap the positions of two products and save the new order
     * @param productId1 the ID of the first product
     * @param productId2 the ID of the second product
     * @return the updated list of products
     */
    @PutMapping("/swap")
    public ResponseEntity<List<Product>> swapProducts(
            @RequestParam String productId1,
            @RequestParam String productId2) {

        List<Product> allProducts = productService.getAll()
                .stream()
                .sorted((p1, p2) -> {
                    Integer order1 = p1.getDisplayOrder() != null ? p1.getDisplayOrder() : 0;
                    Integer order2 = p2.getDisplayOrder() != null ? p2.getDisplayOrder() : 0;
                    return order1.compareTo(order2);
                })
                .collect(Collectors.toList());

        int index1 = -1;
        int index2 = -1;

        // Find the indices of both products
        for (int i = 0; i < allProducts.size(); i++) {
            if (allProducts.get(i).getId().equals(productId1)) {
                index1 = i;
            }
            if (allProducts.get(i).getId().equals(productId2)) {
                index2 = i;
            }
        }

        if (index1 == -1 || index2 == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Swap the products
        Product temp = allProducts.get(index1);
        allProducts.set(index1, allProducts.get(index2));
        allProducts.set(index2, temp);

        // Update displayOrder for both products and save
        List<Product> reorderedProducts = new ArrayList<>();
        for (int i = 0; i < allProducts.size(); i++) {
            Product p = allProducts.get(i);
            p.setDisplayOrder(i);
            p.setUpdatedAt(Instant.now());
            Product updatedProduct = productService.update(p.getId(), p);
            reorderedProducts.add(updatedProduct);
        }

        return ResponseEntity.ok(reorderedProducts);
    }

    /**
     * Sort products by a specific field and save the order
     * @param sortBy the field to sort by (e.g., "title", "price", "createdAt")
     * @param order the sort order ("asc" for ascending, "desc" for descending)
     * @return the sorted list of products with updated displayOrder
     */
    @GetMapping("/sort")
    public ResponseEntity<List<Product>> sortProducts(
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "asc") String order) {

        List<Product> allProducts = productService.getAll();

        switch (sortBy.toLowerCase()) {
            case "title":
                allProducts.sort((p1, p2) -> p1.getTitle().compareTo(p2.getTitle()));
                break;
            case "price":
                allProducts.sort((p1, p2) -> p1.getPrice().compareTo(p2.getPrice()));
                break;
            case "createdat":
            case "created_at":
                allProducts.sort((p1, p2) -> p1.getCreatedAt().compareTo(p2.getCreatedAt()));
                break;
            case "updatedat":
            case "updated_at":
                allProducts.sort((p1, p2) -> p1.getUpdatedAt().compareTo(p2.getUpdatedAt()));
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        if ("desc".equalsIgnoreCase(order)) {
            java.util.Collections.reverse(allProducts);
        }

        // Update displayOrder for all products and save
        List<Product> sortedProducts = new ArrayList<>();
        for (int i = 0; i < allProducts.size(); i++) {
            Product p = allProducts.get(i);
            p.setDisplayOrder(i);
            p.setUpdatedAt(Instant.now());
            Product updatedProduct = productService.update(p.getId(), p);
            sortedProducts.add(updatedProduct);
        }

        return ResponseEntity.ok(sortedProducts);
    }
}

