package com.kostas.bookingproject.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

/**
 * Test-only helpers that build deliberately unusual tokens (expired, no roles).
 * They sign with the same secret the tests run with (surefire sets the jwt.secret system property).
 */
public final class TestTokens {

    private TestTokens() {
    }

    private static Key key() {
        String secret = System.getProperty("jwt.secret");
        if (secret == null) {
            throw new IllegalStateException("jwt.secret system property is not set for tests");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public static String expiredToken(String subject, List<String> roles) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(subject)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() - 1000))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static String tokenWithoutRoles(String subject) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 3_600_000))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }
}
