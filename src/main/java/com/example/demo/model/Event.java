package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
@Document(collection = "events")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    private String id;

    private String title;
    private String description;

    private Instant startAt;
    private Instant endAt; // optional

    private String location;

    private Integer capacity;              // required > 0
    private Integer registrationsCount;    // starts at 0

    private Boolean isFree;
    private BigDecimal price;              // 0 if free

    private EventStatus status;            // DRAFT/PUBLISHED/...

    private String coverImageUrl;

    private List<String> images;

    private Instant createdAt;
    private Instant updatedAt;
}
