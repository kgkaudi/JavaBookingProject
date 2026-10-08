package com.kostas.bookingproject.integration;

import com.kostas.bookingproject.controllers.AuthController;
import com.kostas.bookingproject.repositories.UserRepository;
import com.kostas.bookingproject.security.AuthService;
import com.kostas.bookingproject.security.JwtFilter;
import com.kostas.bookingproject.security.JwtUtil;
import com.kostas.bookingproject.security.SecurityConfig;
import com.kostas.bookingproject.security.TokenBlacklist;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Verifies the real GlobalExceptionHandler + bean validation through the auth endpoints. */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class ErrorHandlingIT {

    @Autowired MockMvc mvc;

    @MockBean AuthService authService;
    @MockBean TokenBlacklist tokenBlacklist;
    @MockBean JwtUtil jwtUtil;
    @MockBean UserRepository userRepository;

    private static final String VALID_SIGNUP =
            "{\"name\":\"Kostas\",\"email\":\"k@k.com\",\"password\":\"Passw0rd123\",\"phone\":\"6900000000\"}";

    @Test
    void signup_with_empty_body_lists_every_missing_field() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.phone").exists());
    }

    @Test
    void signup_with_short_password_is_rejected() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"K\",\"email\":\"k@k.com\",\"password\":\"123\",\"phone\":\"1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void signup_with_bad_email_is_rejected() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"K\",\"email\":\"nope\",\"password\":\"Passw0rd123\",\"phone\":\"1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void duplicate_email_is_409() throws Exception {
        when(authService.signup(any())).thenThrow(new IllegalStateException("Email already in use"));

        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(VALID_SIGNUP))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }

    @Test
    void bad_credentials_is_400_with_generic_message() throws Exception {
        when(authService.login(any())).thenThrow(new IllegalArgumentException("Invalid credentials"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"k@k.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void unexpected_errors_are_500_and_do_not_leak_details() throws Exception {
        when(authService.login(any())).thenThrow(new RuntimeException("mongo password is hunter2"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"k@k.com\",\"password\":\"x\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected server error"))
                .andExpect(content().string(not(containsString("hunter2"))));
    }

    @Test
    void malformed_json_is_400_without_parser_internals() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{ not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed or unreadable request body"));
    }

    @Test
    void wrong_http_method_is_405_json() throws Exception {
        mvc.perform(get("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }
}
