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

import java.util.List;

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

        user = users.save(new User(
                null,
                "Kostas",
                "k@k.com",
                "ENC",
                "6900000000",
                List.of("ROLE_USER")
        ));

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
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalid_token_forbidden() throws Exception {
        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isForbidden());
    }

    @Test
    void malformed_authorization_header_unauthorized() throws Exception {
        mvc.perform(get("/api/users/me")
                        .header("Authorization", "NotBearer token"))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------
    // EXPIRED TOKEN
    // ------------------------------------------------------------

    @Test
    void expired_token_forbidden() throws Exception {
        String expired = "Bearer " + jwt.generateExpiredToken(user.getId(), List.of("ROLE_USER"));

        mvc.perform(get("/api/users/me")
                        .header("Authorization", expired))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // WRONG SIGNATURE
    // ------------------------------------------------------------

    @Test
    void token_with_wrong_signature_forbidden() throws Exception {
        // Create a valid token then tamper the signature
        String token = jwt.generateToken(user.getId(), List.of("ROLE_USER"));
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + ".WRONGSIGNATURE";

        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + tampered))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // MISSING ROLES CLAIM
    // ------------------------------------------------------------

    @Test
    void token_missing_roles_forbidden() throws Exception {
        String noRoles = "Bearer " + jwt.generateTokenWithoutRoles(user.getId());

        mvc.perform(get("/api/users/me")
                        .header("Authorization", noRoles))
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
