package com.example.demo.controller;

import com.example.demo.DTO.EventUpsertRequest;
import com.example.demo.model.Event;
import com.example.demo.model.EventRegistration;
import com.example.demo.model.EventStatus;
import com.example.demo.repository.EventRegistrationRepository;
import com.example.demo.repository.EventRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EventAdminController {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository regRepository;

    @PostMapping
    public Event create(@Valid @RequestBody EventUpsertRequest req) {
        Event e = Event.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .startAt(req.getStartAt())
                .endAt(req.getEndAt())
                .location(req.getLocation())
                .capacity(req.getCapacity())
                .registrationsCount(0)
                .isFree(req.getIsFree())
                .price(Boolean.TRUE.equals(req.getIsFree()) ? BigDecimal.ZERO : req.getPrice())
                .status(EventStatus.DRAFT)
                .coverImageUrl(req.getCoverImageUrl())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return eventRepository.save(e);
    }

    @PutMapping("/{id}")
    public Event update(@PathVariable String id, @Valid @RequestBody EventUpsertRequest req) {
        Event e = eventRepository.findById(id).orElseThrow();
        e.setTitle(req.getTitle());
        e.setDescription(req.getDescription());
        e.setStartAt(req.getStartAt());
        e.setEndAt(req.getEndAt());
        e.setLocation(req.getLocation());
        e.setCapacity(req.getCapacity());
        e.setIsFree(req.getIsFree());
        e.setPrice(Boolean.TRUE.equals(req.getIsFree()) ? BigDecimal.ZERO : req.getPrice());
        e.setCoverImageUrl(req.getCoverImageUrl());
        e.setUpdatedAt(Instant.now());
        return eventRepository.save(e);
    }

    @PatchMapping("/{id}/publish")
    public Event publish(@PathVariable String id) {
        Event e = eventRepository.findById(id).orElseThrow();
        e.setStatus(EventStatus.PUBLISHED);
        e.setUpdatedAt(Instant.now());
        return eventRepository.save(e);
    }

    @PatchMapping("/{id}/cancel")
    public Event cancel(@PathVariable String id) {
        Event e = eventRepository.findById(id).orElseThrow();
        e.setStatus(EventStatus.CANCELLED);
        e.setUpdatedAt(Instant.now());
        return eventRepository.save(e);
    }

    @GetMapping("/{id}/registrations")
    public Page<EventRegistration> regs(@PathVariable String id,
                                        @PageableDefault(size = 50) Pageable pageable) {
        return regRepository.findByEventId(id, pageable);
    }

    @GetMapping("/{id}/registrations/export")
    public void exportCsv(@PathVariable String id, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=registrations-" + id + ".csv");

        var regs = regRepository.findByEventId(id, Pageable.unpaged()).getContent();
        var w = response.getWriter();
        w.println("name,email,phone,emergencyContact,createdAt");
        for (var r : regs) {
            w.printf("%s,%s,%s,%s,%s%n",
                    safe(r.getName()), safe(r.getEmail()), safe(r.getPhone()),
                    safe(r.getEmergencyContact()), r.getCreatedAt()
            );
        }
        w.flush();
    }

    private String safe(String s) {
        return s == null ? "" : s.replace(",", " ");
    }
}

