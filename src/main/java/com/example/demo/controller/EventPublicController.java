package com.example.demo.controller;

import com.example.demo.DTO.EventRegistrationRequest;
import com.example.demo.model.Event;
import com.example.demo.service.EventService;
import com.example.demo.service.IcsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventPublicController {

    private final EventService eventService;
    private final IcsService icsService;

    @GetMapping("/upcoming")
    public Page<Event> upcoming(@PageableDefault(size = 12) Pageable pageable) {
        return eventService.upcoming(pageable);
    }

    @GetMapping("/past")
    public Page<Event> past(@PageableDefault(size = 12) Pageable pageable) {
        return eventService.past(pageable);
    }

    @GetMapping("/{id}")
    public Event get(@PathVariable String id) {
        return eventService.getEvent(id);
    }

    @GetMapping("/{id}/ics")
    public ResponseEntity<String> downloadIcs(@PathVariable String id) {
        Event event = eventService.getEvent(id);
        String icsContent = icsService.buildIcs(event);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/calendar"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + event.getTitle() + ".ics\"")
                .body(icsContent);
    }

    @PostMapping("/{id}/registrations")
    public ResponseEntity<?> register(@PathVariable String id,
                                      @Valid @RequestBody EventRegistrationRequest req) {
        eventService.register(id, req);
        return ResponseEntity.ok(Map.of("message", "Registered"));
    }
}

