package com.example.demo.service.impl;

import com.example.demo.model.Contact;
import com.example.demo.repository.ContactRepository;
import com.example.demo.service.ContactService;
import com.example.demo.service.EmailTemplates;
import com.example.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactServiceImpl.class);

    private final ContactRepository contactRepository;
    private final EmailService emailService;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Override
    public Contact create(Contact contact) {
        contact.setCreatedAt(Instant.now());
        Contact saved = contactRepository.save(contact);

        // send confirmation email to user
        try {
            if (saved.getEmail() != null && !saved.getEmail().isBlank()) {
                String subject = "Nous avons bien reçu votre message";
                String html = "<p>Bonjour " + escape(saved.getName()) + ",</p>"
                        + "<p>Merci pour votre message. Nous reviendrons vers vous prochainement.</p>"
                        + "<div style=\"margin-top:12px;\"><strong>Sujet:</strong> " + escape(saved.getSubject()) + "</div>"
                        + "<div><strong>Message:</strong><br>" + escape(saved.getMessage()) + "</div>";
                emailService.sendHtmlMessage(saved.getEmail(), subject, html);
            }
        } catch (Exception ex) {
            log.error("Failed to send contact confirmation to {}: {}", saved.getEmail(), ex.getMessage(), ex);
        }

        // send admin notification
        try {
            if (adminEmail != null && !adminEmail.isBlank()) {
                String subject = "Nouveau message de contact";
                String html = "<p>Nouveau message reçu:</p>"
                        + "<div><strong>Nom:</strong> " + escape(saved.getName()) + "</div>"
                        + "<div><strong>Email:</strong> " + escape(saved.getEmail()) + "</div>"
                        + "<div><strong>Sujet:</strong> " + escape(saved.getSubject()) + "</div>"
                        + "<div><strong>Message:</strong><br>" + escape(saved.getMessage()) + "</div>";
                emailService.sendHtmlMessage(adminEmail, subject, html);
            }
        } catch (Exception ex) {
            log.error("Failed to send admin notification for contact {}: {}", saved.getId(), ex.getMessage(), ex);
        }

        return saved;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}

