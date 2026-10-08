package com.kostas.bookingproject.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilSecretTest {

    @Test
    void missingOrShortSecret_failsFast() {
        assertThrows(IllegalStateException.class, () -> new JwtUtil(null, 1000));
        assertThrows(IllegalStateException.class, () -> new JwtUtil("too-short", 1000));
    }

    @Test
    void tokensSignedWithDifferentSecrets_areRejected() {
        JwtUtil a = new JwtUtil("a".repeat(40), 60_000);
        JwtUtil b = new JwtUtil("b".repeat(40), 60_000);

        String token = a.generateToken("k@k.com", List.of("ROLE_ADMIN"));

        assertEquals("k@k.com", a.validate(token).getSubject());
        assertThrows(Exception.class, () -> b.validate(token));
    }
}
