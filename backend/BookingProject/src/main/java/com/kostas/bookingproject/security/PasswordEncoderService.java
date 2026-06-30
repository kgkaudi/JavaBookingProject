package com.kostas.bookingproject.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncoderService {

    private final PasswordEncoder encoder;

    public PasswordEncoderService(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
}
