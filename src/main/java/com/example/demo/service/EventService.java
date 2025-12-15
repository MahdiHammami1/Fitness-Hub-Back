package com.example.demo.service;

import com.example.demo.DTO.EventRegistrationRequest;
import com.example.demo.model.Event;
import com.example.demo.model.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface EventService {
    Event create(Event event);
    Optional<Event> findById(String id);
    List<Event> findAll();
    List<Event> findByStatus(EventStatus status);
    Event update(String id, Event event);
    void delete(String id);
    Page<Event> upcoming(Pageable pageable);
    Page<Event> past(Pageable pageable);
    Event getEvent(String id);
    void register(String eventId, EventRegistrationRequest req);
}

