package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "event_registrations")
@CompoundIndex(name="uniq_event_email", def="{'eventId': 1, 'email': 1}", unique=true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistration {
    @Id
    private String id;

    private String eventId;

    // optional link later if you add customer accounts:
    private String userId;

    private String name;
    private String email;
    private String phone;

    private String emergencyContact; // optional

    private Boolean acceptedTerms;

    private Instant createdAt;
}
