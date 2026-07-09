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
import static org.junit.jupiter.api.Assertions.*;

import com.kostas.bookingproject.config.MockMvcConfig;
import com.kostas.bookingproject.repositories.UserRepository;
import com.kostas.bookingproject.security.JwtUtil;
import com.kostas.bookingproject.security.SignupRequest;
import com.kostas.bookingproject.security.AuthRequest;
import com.kostas.bookingproject.models.User;

import java.util.List;

@SpringBootTest
@Import(MockMvcConfig.class)
class AuthDeepIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired JwtUtil jwt;

    @BeforeEach
    void setup() {
        users.deleteAll();
    }

    // ------------------------------------------------------------
    // FULL AUTH FLOW
    // ------------------------------------------------------------
    @Test
    void signup_then_login_then_access_protected_endpoint() throws Exception {

        // 1) SIGNUP
        SignupRequest req = new SignupRequest("Kostas", "k@k.com", "123456", "6900000000");

        mvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        // 2) LOGIN
        AuthRequest login = new AuthRequest("k@k.com", "123456");

        String tokenJson = mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = mapper.readTree(tokenJson).get("token").asText();
        var claims = jwt.validate(token);

        assertTrue(((List<?>) claims.get("roles")).contains("ROLE_USER"));

        // 3) ACCESS PROTECTED ENDPOINT
        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("k@k.com"));
    }

    // ------------------------------------------------------------
    // WRONG PASSWORD
    // ------------------------------------------------------------
    @Test
    void login_wrong_password() throws Exception {
        users.save(new User(null, "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER")));

        AuthRequest req = new AuthRequest("k@k.com", "wrong");

        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------
    // DUPLICATE EMAIL
    // ------------------------------------------------------------
    @Test
    void signup_duplicate_email() throws Exception {
        users.save(new User(null, "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER")));

        SignupRequest req = new SignupRequest("Kostas", "k@k.com", "123", "6900000000");

        mvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------
    // MALFORMED JSON
    // ------------------------------------------------------------
    @Test
    void login_malformed_json() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{ email: 'missingQuotes', password: 123 }"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------
    // EXPIRED TOKEN
    // ------------------------------------------------------------
    @Test
    void expired_token_denied() throws Exception {
        User u = users.save(new User(null, "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER")));

        String expired = "Bearer " + jwt.generateToken(u.getId(), List.of("ROLE_USER"));

        mvc.perform(get("/api/users/me")
                        .header("Authorization", expired))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // INVALID SIGNATURE TOKEN
    // ------------------------------------------------------------
    @Test
    void invalid_signature_token_denied() throws Exception {
        User u = users.save(new User(null, "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER")));

        String valid = jwt.generateToken(u.getId(), List.of("ROLE_USER"));
        String tampered = valid.substring(0, valid.length() - 10) + "AAAAAA";

        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + tampered))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // TOKEN WITH MISSING CLAIMS
    // ------------------------------------------------------------
    @Test
    void token_missing_roles_claim_denied() throws Exception {
        User u = users.save(new User(null, "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER")));

        // Create token without roles
        String token = jwt.generateTokenWithoutRoles(u.getId());

        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // TOKEN WITH WRONG ROLES
    // ------------------------------------------------------------
    @Test
    void token_with_wrong_roles_denied() throws Exception {
        User u = users.save(new User(null, "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER")));

        String token = "Bearer " + jwt.generateToken(u.getId(), List.of("ROLE_ADMIN"));

        mvc.perform(get("/api/users/me")
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // TOKEN TAMPERING (PAYLOAD)
    // ------------------------------------------------------------
    @Test
    void token_payload_tampered_denied() throws Exception {
        User u = users.save(new User(null, "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER")));

        String token = jwt.generateToken(u.getId(), List.of("ROLE_USER"));

        // Tamper payload
        String[] parts = token.split("\\.");
        String tamperedPayload = parts[1].substring(0, parts[1].length() - 5) + "AAAAA";
        String tampered = parts[0] + "." + tamperedPayload + "." + parts[2];

        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + tampered))
                .andExpect(status().isForbidden());
    }
}
