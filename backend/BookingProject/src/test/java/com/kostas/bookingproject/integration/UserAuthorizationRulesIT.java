package com.kostas.bookingproject.integration;

import com.kostas.bookingproject.controllers.UserController;
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.UserRepository;
import com.kostas.bookingproject.security.JwtFilter;
import com.kostas.bookingproject.security.JwtUtil;
import com.kostas.bookingproject.security.SecurityConfig;
import com.kostas.bookingproject.security.TokenBlacklist;
import com.kostas.bookingproject.services.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Exercises the REAL SecurityConfig (URL rules + method security).
 * Intentionally does NOT import MockMvcConfig, so Spring Security's filter chain is applied.
 */
@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class UserAuthorizationRulesIT {

    @Autowired MockMvc mvc;

    @MockBean UserService userService;
    @MockBean JwtUtil jwtUtil;
    @MockBean UserRepository userRepository;
    @MockBean TokenBlacklist tokenBlacklist;

    private User sample() {
        return new User("u1", "Kostas", "k@k.com", "$2a$HASH", "6900000000", List.of("ROLE_USER"));
    }

    // --- privilege escalation endpoints ---------------------------------

    @Test
    @WithMockUser(roles = "USER")
    void user_cannot_promote() throws Exception {
        mvc.perform(put("/api/users/u1/promote")).andExpect(status().isForbidden());
        verify(userService, never()).promoteToAdmin(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void user_cannot_demote() throws Exception {
        mvc.perform(put("/api/users/a1/demote")).andExpect(status().isForbidden());
        verify(userService, never()).demoteToUser(any());
    }

    @Test
    void anonymous_cannot_promote() throws Exception {
        mvc.perform(put("/api/users/u1/promote")).andExpect(status().is4xxClientError());
        verify(userService, never()).promoteToAdmin(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_can_promote() throws Exception {
        when(userService.promoteToAdmin("u1")).thenReturn(
                new User("u1", "Kostas", "k@k.com", "$2a$HASH", "6900000000", List.of("ROLE_ADMIN")));

        mvc.perform(put("/api/users/u1/promote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    // --- other admin-only endpoints -------------------------------------

    @Test
    @WithMockUser(roles = "USER")
    void user_cannot_list_users() throws Exception {
        mvc.perform(get("/api/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void user_cannot_create_users() throws Exception {
        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@x.com\",\"password\":\"p\",\"roles\":[\"ROLE_ADMIN\"]}"))
                .andExpect(status().isForbidden());
        verify(userService, never()).createUser(any());
    }

    // --- password hash must never be serialized -------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_users_does_not_expose_password() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(sample()));

        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("k@k.com"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void get_user_does_not_expose_password() throws Exception {
        when(userService.getUserById("u1")).thenReturn(sample());

        mvc.perform(get("/api/users/u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
