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
import com.kostas.bookingproject.security.JwtUtil;
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

        // ✔ Correct role format
        user = users.save(new User(
                null,
                "Kostas",
                "k@k.com",
                "ENC",
                "6900000000",
                List.of("ROLE_USER")
        ));

        // ✔ Correct JWT role format
        validToken = "Bearer " + jwt.generateToken(user.getId(), List.of("ROLE_USER"));
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
    void missing_token_unauthorized() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized()); // ✔ 401
    }

    @Test
    void invalid_token_forbidden() throws Exception {
        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isForbidden()); // ✔ 403
    }

    @Test
    void malformed_authorization_header_unauthorized() throws Exception {
        mvc.perform(get("/api/users/me")
                        .header("Authorization", "NotBearer token"))
                .andExpect(status().isUnauthorized()); // ✔ 401
    }

    // ------------------------------------------------------------
    // EXPIRED TOKEN
    // ------------------------------------------------------------

    @Test
    void expired_token_forbidden() throws Exception {
        String expired = Jwts.builder()
                .setSubject(user.getId())
                .claim("roles", List.of("ROLE_USER"))
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
                .claim("roles", List.of("ROLE_USER"))
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
        String adminToken = "Bearer " + jwt.generateToken(user.getId(), List.of("ROLE_ADMIN"));

        mvc.perform(get("/api/users/me")
                        .header("Authorization", adminToken))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // USER DELETED AFTER TOKEN ISSUED
    // ------------------------------------------------------------

    @Test
    void token_for_deleted_user_not_found() throws Exception {
        users.deleteAll();

        mvc.perform(get("/api/users/me")
                        .header("Authorization", validToken))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------
    // TOKEN FOR NONEXISTENT USER
    // ------------------------------------------------------------

    @Test
    void token_for_nonexistent_user_not_found() throws Exception {
        String token = "Bearer " + jwt.generateToken("nonexistent-id", List.of("ROLE_USER"));

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
