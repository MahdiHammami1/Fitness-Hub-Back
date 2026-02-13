package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    private String id;

    private String title;
    private String description;

    private BigDecimal price;
    private ProductCollection collection;

    private List<ProductImage> images;

    private Boolean hasVariants; // true if using ProductVariant
    private Integer stock;       // used only if hasVariants=false

    private Boolean isActive;

    private Integer displayOrder; // order position for display/sorting

    private Instant createdAt;
    private Instant updatedAt;
}
