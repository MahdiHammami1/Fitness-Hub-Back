package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "verification_tokens")
public class VerificationToken {
    @Id
    private String id;
    private String userId;
    private String code;
    private TokenType type;
    private Instant expiresAt;
}
