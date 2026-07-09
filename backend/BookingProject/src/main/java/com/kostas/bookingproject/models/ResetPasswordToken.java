package com.kostas.bookingproject.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "password_tokens")
public class ResetPasswordToken {

    @Id
    private String id;

    private String email;
    private String token;
    private LocalDateTime expiresAt;

    public ResetPasswordToken() {}

    public ResetPasswordToken(String email, String token, LocalDateTime expiresAt) {
        this.email = email;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getEmail() { return email; }
    public String getToken() { return token; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
