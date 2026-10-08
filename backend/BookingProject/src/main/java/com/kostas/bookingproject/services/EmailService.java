package com.kostas.bookingproject.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void sendResetEmail(String to, String token) {
        // DEV STUB: logs the link instead of sending mail.
        // In production replace with JavaMailSender and do NOT log the token.
        log.info("RESET LINK for {}: http://localhost:5173/reset-password?token={}", to, token);
    }
}
