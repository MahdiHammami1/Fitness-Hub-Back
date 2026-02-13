package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "coaches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coach {
    @Id
    private String id;

    private String fullName;
    private String title;
    private String domain;
    private String imageUrl;
    private String bio;
    private String quote;

    private Integer experienceYears;
    private Integer clientsTransformed;
    private Integer eventsHosted;
    private Integer satisfactionRate; // 0-100

    private List<String> certifications; // List of certification names

    private Instant createdAt;
    private Instant updatedAt;
}

