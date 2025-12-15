package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    private String id;

    private String orderId;

    private String productId;
    private String productTitleSnapshot;

    private String variantId;             // nullable
    private String variantSnapshot;       // e.g. "SIZE:XL / COLOR:Red"

    private Integer qty;
    private BigDecimal unitPrice;
}

