package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    private String id;

    private String userId; // nullable (guest checkout)
    private String customerName;
    private String email;
    private String phone;

    private Address address;

    private OrderStatus status;
    private BigDecimal total;

    private Instant createdAt;
    private Instant updatedAt;
}

