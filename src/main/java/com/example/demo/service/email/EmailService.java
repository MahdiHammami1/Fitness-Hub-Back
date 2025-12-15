package com.example.demo.service.email;

public interface EmailService {
    void sendHtml(String to, String subject, String html);

    // Compatibility with service.EmailService
    void sendHtmlMessage(String to, String subject, String html);
}

