package com.kostas.bookingproject.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RequestResetDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
