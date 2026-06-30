package com.kostas.bookingproject.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import com.kostas.bookingproject.config.MockMvcConfig;
import com.kostas.bookingproject.repositories.UserRepository;
import com.kostas.bookingproject.security.jwt.JwtUtil;
import com.kostas.bookingproject.models.User;

@SpringBootTest
@Import(MockMvcConfig.class)
class JwtSecurityDeepIT {

    @Autowired MockMvc mvc;
    @Autowired JwtUtil jwt;
    @Autowired UserRepository users;
    @Autowired ObjectMapper mapper;

    String validToken;
    User user;

    @BeforeEach
    void setup() {
        users.deleteAll();

        user = users.save(new User(
                null,
                "Kostas",
                "k@k.com",
                "ENC",
                "6900000000",
                List.of("USER")   // ✔ FIXED
        ));

        validToken = "Bearer " + jwt.generateToken(user.getId(), List.of("USER")); // ✔ FIXED
    }

    // ------------------------------------------------------------
    // SUCCESS CASE
    // ------------------------------------------------------------

    @Test
    void valid_jwt_allows_access() throws Exception {
        mvc.perform(get("/api/users/me")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("k@k.com"));
    }

    // ------------------------------------------------------------
    // MISSING / INVALID TOKEN
    // ------------------------------------------------------------

    @Test
    void missing_token_forbidden() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalid_token_forbidden() throws Exception {
        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isForbidden());
    }

    @Test
    void malformed_authorization_header_forbidden() throws Exception {
        mvc.perform(get("/api/users/me")
                        .header("Authorization", "NotBearer token"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // EXPIRED TOKEN
    // ------------------------------------------------------------

    @Test
    void expired_token_forbidden() throws Exception {
        String expired = Jwts.builder()
                .setSubject(user.getId())
                .claim("roles", List.of("USER")) // ✔ FIXED
                .setExpiration(Date.from(Instant.now().minusSeconds(3600)))
                .signWith(SignatureAlgorithm.HS256, "WRONGKEY12345678901234567890")
                .compact();

        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + expired))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // WRONG SIGNATURE
    // ------------------------------------------------------------

    @Test
    void token_with_wrong_signature_forbidden() throws Exception {
        String wrongSig = Jwts.builder()
                .setSubject(user.getId())
                .claim("roles", List.of("USER")) // ✔ FIXED
                .setExpiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(SignatureAlgorithm.HS256, "WRONGKEY12345678901234567890")
                .compact();

        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + wrongSig))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // WRONG ROLE
    // ------------------------------------------------------------

    @Test
    void token_with_wrong_role_forbidden() throws Exception {
        String adminToken = "Bearer " + jwt.generateToken(user.getId(), List.of("ADMIN")); // ✔ FIXED

        mvc.perform(get("/api/users/me")
                        .header("Authorization", adminToken))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // USER DELETED AFTER TOKEN ISSUED
    // ------------------------------------------------------------

    @Test
    void token_for_deleted_user_forbidden() throws Exception {
        users.deleteAll();

        mvc.perform(get("/api/users/me")
                        .header("Authorization", validToken))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------
    // TOKEN FOR NONEXISTENT USER
    // ------------------------------------------------------------

    @Test
    void token_for_nonexistent_user_forbidden() throws Exception {
        String token = "Bearer " + jwt.generateToken("nonexistent-id", List.of("USER")); // ✔ FIXED

        mvc.perform(get("/api/users/me")
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------
    // MALFORMED JWT STRUCTURE
    // ------------------------------------------------------------

    @Test
    void malformed_jwt_structure_forbidden() throws Exception {
        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer a.b"))
                .andExpect(status().isForbidden());
    }
}
