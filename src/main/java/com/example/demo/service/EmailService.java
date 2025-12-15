package com.example.demo.service;

public interface EmailService {
    void sendRegistrationConfirmation(String to, String subject, String text);
    void sendAdminNotification(String to, String subject, String text);

    // Compatibility helper used elsewhere in the project
    void sendSimpleMessage(String to, String subject, String text);

    // New: send HTML content
    void sendHtmlMessage(String to, String subject, String html);
}
