package com.kostas.bookingproject.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private static final String SECRET =
            "u8JHk39sdf98JHk39sdf98JHk39sdf98JHk39sdf98JHk39sdf98JHk39sdf98JHk3";

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24h

    // ---------------------------------------------------------
    // GENERATE TOKEN (EMAIL-BASED)
    // ---------------------------------------------------------
    public String generateToken(String email, List<String> roles) {

        List<String> springRoles = roles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .toList();

        return Jwts.builder()
                .setSubject(email) // email is the JWT subject
                .claim("roles", springRoles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
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
    // EXTRACT EMAIL
    // ---------------------------------------------------------
    public String getEmail(String token) {
        return validate(token).getSubject();
    }

    // ---------------------------------------------------------
    // EXTRACT ROLES
    // ---------------------------------------------------------
    public List<String> getRoles(String token) {
        return validate(token).get("roles", List.class);
    }
}
