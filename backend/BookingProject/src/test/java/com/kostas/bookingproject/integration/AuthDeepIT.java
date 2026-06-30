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

import com.kostas.bookingproject.config.MockMvcConfig;
import com.kostas.bookingproject.repositories.UserRepository;
import com.kostas.bookingproject.security.jwt.JwtUtil;
import com.kostas.bookingproject.auth.SignupRequest;
import com.kostas.bookingproject.auth.AuthRequest;
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

    @Test
    void signup_then_login_then_access_protected_endpoint() throws Exception {
        SignupRequest req = new SignupRequest("Kostas", "k@k.com", "123456", "6900000000");

        mvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

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

        // ✔ Updated for roles list
        assert ((List<?>) claims.get("roles")).contains("USER");
    }

    @Test
    void login_wrong_password() throws Exception {
        users.save(new User(null, "Kostas", "k@k.com", "ENC", "6900000000", List.of("USER")));

        AuthRequest req = new AuthRequest("k@k.com", "wrong");

        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_duplicate_email() throws Exception {
        users.save(new User(null, "Kostas", "k@k.com", "ENC", "6900000000", List.of("USER")));

        SignupRequest req = new SignupRequest("Kostas", "k@k.com", "123", "6900000000");

        mvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_malformed_json() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{ email: 'missingQuotes', password: 123 }"))
                .andExpect(status().isBadRequest());
    }
}
