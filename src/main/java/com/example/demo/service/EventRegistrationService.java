package com.example.demo.service;

import com.example.demo.model.EventRegistration;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationService {
    EventRegistration create(EventRegistration registration);
    Optional<EventRegistration> findById(String id);
    List<EventRegistration> findAll();
    List<EventRegistration> findByEventId(String eventId);
    Optional<EventRegistration> findByEventIdAndEmail(String eventId, String email);
    EventRegistration update(String id, EventRegistration registration);
    void delete(String id);
    void deleteAll();
    long countByEventId(String eventId);
}
