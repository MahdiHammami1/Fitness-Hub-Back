package com.example.demo.repository;

import com.example.demo.model.Event;
import com.example.demo.model.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findByStatus(EventStatus status);
    Optional<Event> findByTitle(String title);

    @Query("{ 'status': 'PUBLISHED', 'startAt': { $gte: ?0 } }")
    Page<Event> findUpcomingEvents(Instant now, Pageable pageable);

    @Query("{ 'status': 'PUBLISHED', 'endAt': { $lt: ?0 } }")
    Page<Event> findPastEvents(Instant now, Pageable pageable);
}

