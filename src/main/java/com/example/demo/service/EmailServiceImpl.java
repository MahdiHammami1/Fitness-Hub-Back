package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Primary
public class EmailServiceImpl implements EmailService, com.example.demo.service.email.EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String from;

    public EmailServiceImpl(JavaMailSender mailSender, @Value("${spring.mail.username:}") String from) {
        this.mailSender = mailSender;
        this.from = from;
        log.info("EmailServiceImpl initialized with JavaMailSender={} and from={}", mailSender != null ? mailSender.getClass().getName() : "<null>", from);
    }

    @Override
    public void sendRegistrationConfirmation(String to, String subject, String text) {
        // default: send as HTML
        sendHtmlMessage(to, subject, text);
    }

    @Override
    public void sendAdminNotification(String to, String subject, String text) {
        // default: send as HTML
        sendHtmlMessage(to, subject, text);
    }

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        sendHtmlMessage(to, subject, text);
    }

    @Override
    public void sendHtmlMessage(String to, String subject, String html) {
        if (to == null || to.isBlank()) {
            log.warn("Email not sent: empty recipient");
            return;
        }
        try {
            log.debug("Preparing email to='{}' subject='{}' from='{}'", to, subject, from);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            if (from != null && !from.isBlank()) {
                helper.setFrom(from);
            } else {
                log.warn("No 'from' address configured (spring.mail.username is empty). Some SMTP servers may reject the message.");
            }
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to {} (subject={})", to, subject);
        } catch (MessagingException ex) {
            log.error("Failed to send email to {} (subject={}) due to MessagingException: {}", to, subject, ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Failed to send email to {} (subject={}) due to unexpected exception: {}", to, subject, ex.getMessage(), ex);
        }
    }

    @Override
    public void sendHtml(String to, String subject, String html) {
        sendHtmlMessage(to, subject, html);
    }
}
