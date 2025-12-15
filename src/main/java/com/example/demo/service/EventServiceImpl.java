package com.example.demo.service;

import com.example.demo.DTO.EventRegistrationRequest;
import com.example.demo.model.Event;
import com.example.demo.model.EventRegistration;
import com.example.demo.model.EventStatus;
import com.example.demo.repository.EventRegistrationRepository;
import com.example.demo.repository.EventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EmailService emailService;

    @Value("${app.admin.email:admin@wouhouch.com}")
    private String adminEmail;

    public EventServiceImpl(EventRepository eventRepository,
                          EventRegistrationRepository eventRegistrationRepository,
                          EmailService emailService) {
        this.eventRepository = eventRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.emailService = emailService;
    }

    @Override
    public Event create(Event event) {
        event.setCreatedAt(Instant.now());
        event.setUpdatedAt(Instant.now());
        event.setRegistrationsCount(0);
        return eventRepository.save(event);
    }

    @Override
    public Optional<Event> findById(String id) {
        return eventRepository.findById(id);
    }

    @Override
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    @Override
    public List<Event> findByStatus(EventStatus status) {
        return eventRepository.findByStatus(status);
    }

    @Override
    public Event update(String id, Event event) {
        return eventRepository.findById(id).map(existingEvent -> {
            if (event.getTitle() != null) existingEvent.setTitle(event.getTitle());
            if (event.getDescription() != null) existingEvent.setDescription(event.getDescription());
            if (event.getStartAt() != null) existingEvent.setStartAt(event.getStartAt());
            if (event.getEndAt() != null) existingEvent.setEndAt(event.getEndAt());
            if (event.getLocation() != null) existingEvent.setLocation(event.getLocation());
            if (event.getCapacity() != null) existingEvent.setCapacity(event.getCapacity());
            if (event.getIsFree() != null) existingEvent.setIsFree(event.getIsFree());
            if (event.getPrice() != null) existingEvent.setPrice(event.getPrice());
            if (event.getStatus() != null) existingEvent.setStatus(event.getStatus());
            if (event.getCoverImageUrl() != null) existingEvent.setCoverImageUrl(event.getCoverImageUrl());
            existingEvent.setUpdatedAt(Instant.now());
            return eventRepository.save(existingEvent);
        }).orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + id));
    }

    @Override
    public void delete(String id) {
        eventRepository.deleteById(id);
    }

    @Override
    public Page<Event> upcoming(Pageable pageable) {
        return eventRepository.findUpcomingEvents(Instant.now(), pageable);
    }

    @Override
    public Page<Event> past(Pageable pageable) {
        return eventRepository.findPastEvents(Instant.now(), pageable);
    }

    @Override
    public Event getEvent(String id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + id));
    }

    @Override
    public void register(String eventId, EventRegistrationRequest req) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Check if already registered
        Optional<EventRegistration> existing = eventRegistrationRepository
                .findByEventIdAndEmail(eventId, req.getEmail());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("User already registered for this event");
        }

        // Check capacity
        long registrationCount = eventRegistrationRepository.countByEventId(eventId);
        if (registrationCount >= event.getCapacity()) {
            throw new IllegalArgumentException("Event is full");
        }

        // Create registration
        EventRegistration registration = EventRegistration.builder()
                .eventId(eventId)
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .emergencyContact(req.getEmergencyContact())
                .acceptedTerms(req.getAcceptedTerms())
                .createdAt(Instant.now())
                .build();

        eventRegistrationRepository.save(registration);

        // Update registrations count
        event.setRegistrationsCount((int) (registrationCount + 1));
        eventRepository.save(event);

        // Send confirmation email to user
        sendUserConfirmationEmail(registration, event);

        // Send notification email to admin
        sendAdminNotificationEmail(registration, event);
    }

    private void sendUserConfirmationEmail(EventRegistration registration, Event event) {
        try {
            String to = registration.getEmail();
            if (to == null || to.isBlank()) {
                System.err.println("User email is empty - skipping user confirmation email for registration id=" + registration.getId());
                return;
            }
            String subject = "Inscription confirmée - " + event.getTitle();
            String html = EmailTemplates.registrationConfirmation(registration.getName(), event.getTitle());
            emailService.sendHtmlMessage(to, subject, html);
        } catch (Exception e) {
            // Log but don't fail the registration if email fails
            System.err.println("Failed to send confirmation email to user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendAdminNotificationEmail(EventRegistration registration, Event event) {
        try {
            if (adminEmail == null || adminEmail.isBlank()) {
                System.err.println("Admin email not configured - skipping admin notification");
                return;
            }
            String subject = "Nouvelle inscription - " + event.getTitle();
            String html = EmailTemplates.adminNewRegistration(event.getTitle(), registration.getName(), registration.getEmail(), registration.getPhone(), registration.getEmergencyContact(), registration.getCreatedAt().toString());
            emailService.sendHtmlMessage(adminEmail, subject, html);
        } catch (Exception e) {
            // Log but don't fail the registration if email fails
            System.err.println("Failed to send notification email to admin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String safe(String s) { return s == null ? "" : s; }
}
