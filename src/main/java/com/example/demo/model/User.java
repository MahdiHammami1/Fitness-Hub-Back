package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User {

    @Id
    private String id; // _id in MongoDB

    private String email;

    private String passwordHash;

    private Role role;

    private String fullName;

    private String phone;

    private Boolean enabled = true;

    private Instant termsAcceptedAt;

    private Instant createdAt;

    private Instant updatedAt;

}
