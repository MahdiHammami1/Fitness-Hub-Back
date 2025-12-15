package com.example.demo.service;

import com.example.demo.model.Event;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class IcsService {
    public String buildIcs(Event e) {
        // Minimal RFC5545-ish ICS; good enough for MVP
        String uid = java.util.UUID.randomUUID() + "@wouhouchhub";
        return "BEGIN:VCALENDAR\n" +
                "VERSION:2.0\n" +
                "PRODID:-//Wouhouch Hub//EN\n" +
                "BEGIN:VEVENT\n" +
                "UID:" + uid + "\n" +
                "DTSTAMP:" + fmt(Instant.now()) + "\n" +
                "DTSTART:" + fmt(e.getStartAt()) + "\n" +
                (e.getEndAt()!=null ? "DTEND:" + fmt(e.getEndAt()) + "\n" : "") +
                "SUMMARY:" + escape(e.getTitle()) + "\n" +
                "LOCATION:" + escape(e.getLocation()) + "\n" +
                "DESCRIPTION:" + escape(e.getDescription()==null?"":e.getDescription()) + "\n" +
                "END:VEVENT\n" +
                "END:VCALENDAR\n";
    }

    private String fmt(Instant i){
        return java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(java.time.ZoneOffset.UTC).format(i);
    }
    private String escape(String s){ return s.replace("\n","\\n").replace(",","\\,"); }
}
