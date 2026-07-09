package com.kostas.bookingproject.services;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendResetEmail(String to, String token) {
        System.out.println("RESET LINK: http://localhost:5173/reset-password?token=" + token);
        // In production: use JavaMailSender
    }
}
