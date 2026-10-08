package com.kostas.bookingproject.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final Key key;
    private final long expiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms:86400000}") long expiration) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "jwt.secret must be set (env JWT_SECRET) and be at least 32 bytes long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    // ---------------------------------------------------------
    // GENERATE TOKEN (USER-ID BASED)
    // ---------------------------------------------------------
    public String generateToken(String userId, List<String> roles) {

        List<String> springRoles = roles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .toList();

        return Jwts.builder()
                .setSubject(userId) // userId is the JWT subject
                .claim("roles", springRoles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ---------------------------------------------------------
    // VALIDATE TOKEN
    // ---------------------------------------------------------
    public Claims validate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ---------------------------------------------------------
    // EXTRACT USER ID
    // ---------------------------------------------------------
    public String getUserId(String token) {
        return validate(token).getSubject();
    }

    // ---------------------------------------------------------
    // EXTRACT ROLES
    // ---------------------------------------------------------
    public List<String> getRoles(String token) {
        return validate(token).get("roles", List.class);
    }
}
