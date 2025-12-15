package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    private String id;

    private String orderId;

    private PaymentProvider provider; // TEST or COD
    private PaymentStatus status;

    private String transactionId; // optional

    private Instant createdAt;
}

