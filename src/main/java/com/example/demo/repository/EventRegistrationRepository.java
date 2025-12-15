package com.example.demo.repository;

import com.example.demo.model.EventRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends MongoRepository<EventRegistration, String> {
    List<EventRegistration> findByEventId(String eventId);
    Page<EventRegistration> findByEventId(String eventId, Pageable pageable);
    Optional<EventRegistration> findByEventIdAndEmail(String eventId, String email);
    long countByEventId(String eventId);
}

