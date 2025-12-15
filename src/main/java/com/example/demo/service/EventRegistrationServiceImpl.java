package com.example.demo.service;

import com.example.demo.model.EventRegistration;
import com.example.demo.repository.EventRegistrationRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.model.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class EventRegistrationServiceImpl implements EventRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(EventRegistrationServiceImpl.class);

    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventRepository eventRepository;
    private final EmailService emailService;
    private final MongoTemplate mongoTemplate;
    private final String adminEmail;

    public EventRegistrationServiceImpl(EventRegistrationRepository eventRegistrationRepository, EventRepository eventRepository, EmailService emailService, MongoTemplate mongoTemplate, @Value("${app.admin.email:}") String adminEmail) {
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.eventRepository = eventRepository;
        this.emailService = emailService;
        this.mongoTemplate = mongoTemplate;
        this.adminEmail = adminEmail;
    }

    @Override
    public EventRegistration create(EventRegistration registration) {
        registration.setCreatedAt(Instant.now());
        EventRegistration saved = eventRegistrationRepository.save(registration);

        // obtain event name (fallback to ID if not found)
        String eventName = eventRepository.findById(saved.getEventId()).map(Event::getTitle).orElse("Événement");

        // --- NEW: increment registrationsCount on the Event atomically and set updatedAt ---
        try {
            if (saved.getEventId() != null) {
                Query q = Query.query(Criteria.where("_id").is(saved.getEventId()));
                Update u = new Update().inc("registrationsCount", 1).set("updatedAt", Instant.now());
                var res = mongoTemplate.updateFirst(q, u, Event.class);
                if (res.getMatchedCount() == 0) {
                    log.warn("No Event matched when incrementing registrationsCount for id={}", saved.getEventId());
                } else {
                    log.debug("Incremented registrationsCount for event {} (modified={} matched={})", saved.getEventId(), res.getModifiedCount(), res.getMatchedCount());
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to increment registrationsCount for event {}: {}", saved.getEventId(), ex.getMessage(), ex);
        }

        // send confirmation to the registrant (HTML)
        try {
            String userEmail = saved.getEmail();
            if (userEmail != null && !userEmail.isBlank()) {
                String userSubject = "Confirmation d'inscription à l'événement";
                String userHtml = EmailTemplates.registrationConfirmation(saved.getName(), eventName);
                emailService.sendHtmlMessage(userEmail, userSubject, userHtml);
            }
        } catch (Exception ex) {
            log.warn("Failed sending registration confirmation to user {}: {}", saved.getEmail(), ex.getMessage(), ex);
        }

        // notify admin (HTML)
        try {
            if (adminEmail != null && !adminEmail.isBlank()) {
                String adminSubject = "Nouvelle inscription à un événement";
                String adminHtml = EmailTemplates.adminNewRegistration(eventName, saved.getName(), saved.getEmail(), saved.getPhone(), saved.getEmergencyContact(), saved.getCreatedAt().toString());
                emailService.sendHtmlMessage(adminEmail, adminSubject, adminHtml);
            }
        } catch (Exception ex) {
            log.warn("Failed sending admin notification for registration {}: {}", saved.getId(), ex.getMessage(), ex);
        }

        return saved;
    }

    @Override
    public Optional<EventRegistration> findById(String id) {
        return eventRegistrationRepository.findById(id);
    }

    @Override
    public List<EventRegistration> findAll() {
        return eventRegistrationRepository.findAll();
    }

    @Override
    public List<EventRegistration> findByEventId(String eventId) {
        return eventRegistrationRepository.findByEventId(eventId);
    }

    @Override
    public Optional<EventRegistration> findByEventIdAndEmail(String eventId, String email) {
        return eventRegistrationRepository.findByEventIdAndEmail(eventId, email);
    }

    @Override
    public EventRegistration update(String id, EventRegistration registration) {
        return eventRegistrationRepository.findById(id).map(existingRegistration -> {
            if (registration.getName() != null) existingRegistration.setName(registration.getName());
            if (registration.getEmail() != null) existingRegistration.setEmail(registration.getEmail());
            if (registration.getPhone() != null) existingRegistration.setPhone(registration.getPhone());
            if (registration.getEmergencyContact() != null) existingRegistration.setEmergencyContact(registration.getEmergencyContact());
            if (registration.getAcceptedTerms() != null) existingRegistration.setAcceptedTerms(registration.getAcceptedTerms());
            return eventRegistrationRepository.save(existingRegistration);
        }).orElseThrow(() -> new IllegalArgumentException("EventRegistration not found with id: " + id));
    }

    @Override
    public void delete(String id) {
        eventRegistrationRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        eventRegistrationRepository.deleteAll();
    }

    @Override
    public long countByEventId(String eventId) {
        return eventRegistrationRepository.countByEventId(eventId);
    }
}
