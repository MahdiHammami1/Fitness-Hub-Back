package com.example.demo.controller;

import com.example.demo.model.EventRegistration;
import com.example.demo.service.EventRegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/event-registrations")
public class EventRegistrationController {

    private final EventRegistrationService eventRegistrationService;

    public EventRegistrationController(EventRegistrationService eventRegistrationService) {
        this.eventRegistrationService = eventRegistrationService;
    }

    @PostMapping
    public ResponseEntity<EventRegistration> createRegistration(@RequestBody EventRegistration registration) {
        // Check if already registered
        Optional<EventRegistration> existing = eventRegistrationService
                .findByEventIdAndEmail(registration.getEventId(), registration.getEmail());

        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null); // Or return a custom error message
        }

        EventRegistration createdRegistration = eventRegistrationService.create(registration);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRegistration);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventRegistration> getRegistrationById(@PathVariable String id) {
        Optional<EventRegistration> registration = eventRegistrationService.findById(id);
        return registration.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<EventRegistration>> getAllRegistrations() {
        List<EventRegistration> registrations = eventRegistrationService.findAll();
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<EventRegistration>> getRegistrationsByEventId(@PathVariable String eventId) {
        List<EventRegistration> registrations = eventRegistrationService.findByEventId(eventId);
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/event/{eventId}/email/{email}")
    public ResponseEntity<EventRegistration> getRegistrationByEventIdAndEmail(
            @PathVariable String eventId,
            @PathVariable String email) {
        Optional<EventRegistration> registration = eventRegistrationService
                .findByEventIdAndEmail(eventId, email);
        return registration.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventRegistration> updateRegistration(
            @PathVariable String id,
            @RequestBody EventRegistration registration) {
        try {
            EventRegistration updatedRegistration = eventRegistrationService.update(id, registration);
            return ResponseEntity.ok(updatedRegistration);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegistration(@PathVariable String id) {
        eventRegistrationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllRegistrations() {
        eventRegistrationService.deleteAll();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/event/{eventId}/count")
    public ResponseEntity<Long> countRegistrationsByEventId(@PathVariable String eventId) {
        long count = eventRegistrationService.countByEventId(eventId);
        return ResponseEntity.ok(count);
    }
}
