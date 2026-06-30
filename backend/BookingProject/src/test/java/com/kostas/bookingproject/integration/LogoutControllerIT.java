package com.kostas.bookingproject.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kostas.bookingproject.config.MockMvcConfig;
import com.kostas.bookingproject.controllers.AuthController;
import com.kostas.bookingproject.security.TokenBlacklist;
import com.kostas.bookingproject.auth.AuthService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(MockMvcConfig.class)
class LogoutControllerIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean AuthService authService;
    @MockBean TokenBlacklist tokenBlacklist;

    // ---------------------------------------------------------
    // LOGOUT SUCCESS
    // ---------------------------------------------------------

    @Test
    @WithMockUser
    void logout_success() throws Exception {
        String token = "Bearer faketoken123";

        mvc.perform(post("/api/auth/logout")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));

        verify(tokenBlacklist).blacklist("faketoken123");
    }

    // ---------------------------------------------------------
    // LOGOUT WITHOUT TOKEN
    // ---------------------------------------------------------

    @Test
    @WithMockUser
    void logout_noToken() throws Exception {
        mvc.perform(post("/api/auth/logout"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No token provided"));

        verifyNoInteractions(tokenBlacklist);
    }

    // ---------------------------------------------------------
    // LOGOUT WITH BLACKLISTED TOKEN (FILTER SHOULD BLOCK)
    // ---------------------------------------------------------

    @Test
    @WithMockUser
    void logout_blacklistedToken() throws Exception {
        when(tokenBlacklist.isBlacklisted("faketoken123")).thenReturn(true);

        mvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer faketoken123"))
                .andExpect(status().isUnauthorized());
    }
}
