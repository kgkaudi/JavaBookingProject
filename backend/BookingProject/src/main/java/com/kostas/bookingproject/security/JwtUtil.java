package com.kostas.bookingproject.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    // 256-bit secret key (must be 32+ bytes)
    private static final String SECRET = "u8JHk39sdf98JHk39sdf98JHk39sdf98JHk39sdf98JHk39sdf98JHk39sdf98JHk3";

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Token validity: 24 hours
    private static final long EXPIRATION = 1000 * 60 * 60 * 24;

    // ---------------------------------------------------------
    // GENERATE TOKEN
    // ---------------------------------------------------------
    public String generateToken(String userId, List<String> roles) {

        // Prefix roles with ROLE_ for Spring Security compatibility
        List<String> springRoles = roles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .toList();

        return Jwts.builder()
                .setSubject(userId)
                .claim("roles", springRoles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ---------------------------------------------------------
    // VALIDATE TOKEN & RETURN CLAIMS
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
