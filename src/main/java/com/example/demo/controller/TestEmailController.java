package com.example.demo.controller;

import com.example.demo.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal")
public class TestEmailController {

    private static final Logger log = LoggerFactory.getLogger(TestEmailController.class);
    private final EmailService emailService;

    @Value("${app.admin.email:}")
    private String adminEmail;

    public TestEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    // POST /internal/test-email with JSON { "to": "x@y.com", "subject": "test", "html": "<b>hi</b>" }
    @PostMapping("/test-email")
    public ResponseEntity<?> sendTestEmail(@RequestBody(required = false) Map<String, String> body) {
        String to = body != null && body.get("to") != null ? body.get("to") : adminEmail;
        if (to == null || to.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "No recipient configured (provide 'to' or set app.admin.email)"));
        }
        String subject = body != null && body.get("subject") != null ? body.get("subject") : "Test email from Wouhouch Hub";
        String html = body != null && body.get("html") != null ? body.get("html") : "<p>This is a test email from Wouhouch Hub</p>";

        try {
            log.info("Triggering test email to {}", to);
            emailService.sendHtmlMessage(to, subject, html);
            return ResponseEntity.ok(Map.of("ok", true, "to", to));
        } catch (Exception ex) {
            log.error("Test email failed: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body(Map.of("ok", false, "error", ex.getMessage()));
        }
    }
}

