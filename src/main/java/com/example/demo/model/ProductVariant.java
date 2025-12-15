package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "product_variants")
@CompoundIndex(name = "uniq_product_variant", def = "{'productId': 1, 'variantType': 1, 'value': 1}", unique = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {
    @Id
    private String id;

    private String productId;

    private VariantType variantType;
    private String value;   // "XL", "Red", "Vanilla", etc.

    private Integer stock;

    private Instant createdAt;
    private Instant updatedAt;
}

