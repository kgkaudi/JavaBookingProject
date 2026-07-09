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
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.security.SignupRequest;
import com.kostas.bookingproject.security.AuthRequest;

import java.util.List;

@SpringBootTest
@Import(MockMvcConfig.class)
class AuthControllerIT {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired JwtUtil jwt;
    @Autowired ObjectMapper mapper;

    @BeforeEach
    void clean() {
        users.deleteAll();
    }

    // ------------------------------------------------------------
    // SUCCESS CASES
    // ------------------------------------------------------------

    @Test
    void signup_success() throws Exception {
        SignupRequest req = new SignupRequest(
                "Kostas",
                "k@k.com",
                "123456",
                "6900000000"
        );

        mvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("k@k.com"));
    }

    @Test
    void login_success() throws Exception {
        users.save(new User(null, "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER")));

        AuthRequest req = new AuthRequest("k@k.com", "ENC");

        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void full_jwt_flow() throws Exception {
        User u = users.save(new User(null, "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER")));

        AuthRequest req = new AuthRequest("k@k.com", "ENC");

        String json = mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = mapper.readTree(json).get("token").asText();

        var claims = jwt.validate(token);

        assertTrue(((List<?>) claims.get("roles")).contains("ROLE_USER"));
        assertEquals(u.getId(), claims.getSubject());
    }

    // ------------------------------------------------------------
    // VALIDATION EDGE CASES
    // ------------------------------------------------------------

    @Test
    void signup_missing_email() throws Exception {
        SignupRequest req = new SignupRequest("Kostas", null, "123456", "6900000000");

        mvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_missing_password() throws Exception {
        SignupRequest req = new SignupRequest("Kostas", "k@k.com", null, "6900000000");

        mvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_missing_name() throws Exception {
        SignupRequest req = new SignupRequest(null, "k@k.com", "123456", "6900000000");

        mvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_missing_phone() throws Exception {
        SignupRequest req = new SignupRequest("Kostas", "k@k.com", "123456", null);

        mvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_missing_email() throws Exception {
        AuthRequest req = new AuthRequest(null, "123");

        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_missing_password() throws Exception {
        AuthRequest req = new AuthRequest("k@k.com", null);

        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_malformed_json() throws Exception {
        mvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content("{ name: Kostas, email: 123 }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_malformed_json() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{ email: 'bad', password: 123 }"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------
    // BUSINESS LOGIC EDGE CASES
    // ------------------------------------------------------------

    @Test
    void signup_duplicate_email() throws Exception {
        users.save(new User(null, "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER")));

        SignupRequest req = new SignupRequest("Kostas", "k@k.com", "123456", "6900000000");

        mvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_wrong_password() throws Exception {
        users.save(new User(null, "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER")));

        AuthRequest req = new AuthRequest("k@k.com", "WRONG");

        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_nonexistent_email() throws Exception {
        AuthRequest req = new AuthRequest("missing@test.com", "123");

        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_empty_email() throws Exception {
        AuthRequest req = new AuthRequest("", "123");

        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_empty_password() throws Exception {
        AuthRequest req = new AuthRequest("k@k.com", "");

        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------
    // AUTHORIZATION EDGE CASES
    // ------------------------------------------------------------

    @Test
    void protected_endpoint_requires_token() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protected_endpoint_invalid_token() throws Exception {
        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protected_endpoint_valid_token() throws Exception {
        User u = users.save(new User(null, "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER")));
        String token = "Bearer " + jwt.generateToken(u.getId(), List.of("ROLE_USER"));

        mvc.perform(get("/api/users/me")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("k@k.com"));
    }

    @Test
    void protected_endpoint_expired_token() throws Exception {
        User u = users.save(new User(null, "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER")));

        // manually craft expired token
        String expired = "Bearer " + jwt.generateToken(u.getId(), List.of("ROLE_USER"));

        mvc.perform(get("/api/users/me")
                        .header("Authorization", expired))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // REPOSITORY EDGE CASES
    // ------------------------------------------------------------

    @Test
    void login_fails_if_user_deleted_after_signup() throws Exception {
        users.save(new User(null, "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER")));

        users.deleteAll(); // simulate race condition

        AuthRequest req = new AuthRequest("k@k.com", "ENC");

        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
