package com.example.studylog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PasswordResetMailService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetMailService.class);

    private final Optional<JavaMailSender> mailSender;
    private final boolean enabled;
    private final String fromAddress;

    public PasswordResetMailService(
            Optional<JavaMailSender> mailSender,
            @Value("${app.mail.enabled:true}") boolean enabled,
            @Value("${app.mail.from:noreply@studylog.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.fromAddress = fromAddress;
    }

    public boolean isDeliveryEnabled() {
        return enabled && mailSender.isPresent();
    }

    /** @return true if an email was handed off to the mail server */
    public boolean sendPasswordResetEmail(String toEmail, String resetLink) {
        if (!enabled) {
            log.info("Mail disabled. Password reset link for {}: {}", toEmail, resetLink);
            return false;
        }

        if (mailSender.isEmpty()) {
            log.warn(
                    "No mail server configured (set spring.mail.host). Password reset link for {}: {}",
                    toEmail,
                    resetLink);
            return false;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Reset your Studylog password");
        message.setText(
                """
                You requested a password reset for your Studylog account.

                Open this link to choose a new password:
                %s

                This link expires in 1 hour. If you did not request a reset, you can ignore this email.
                """
                        .formatted(resetLink));

        try {
            mailSender.get().send(message);
            log.info("Password reset email sent to {}", toEmail);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send password reset email to {}", toEmail, ex);
            return false;
        }
    }
}
