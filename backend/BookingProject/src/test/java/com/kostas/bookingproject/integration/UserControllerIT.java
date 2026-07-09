package com.kostas.bookingproject.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kostas.bookingproject.config.MockMvcConfig;
import com.kostas.bookingproject.controllers.UserController;
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.services.UserService;

import org.junit.jupiter.api.BeforeEach;
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

@WebMvcTest(UserController.class)
@Import(MockMvcConfig.class)
class UserControllerIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean UserService userService;

    User user;
    User admin;

    @BeforeEach
    void setup() {
        user = new User("u1", "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER"));
        admin = new User("a1", "Admin", "admin@test.com", "ENC", "6900000000", List.of("ROLE_ADMIN"));
    }

    // ------------------------------------------------------------
    // GET ALL USERS (ADMIN)
    // ------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_success() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(user, admin));

        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_forbidden_for_non_admin() throws Exception {
        mvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // GET USER BY ID
    // ------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_success() throws Exception {
        when(userService.getUserById("u1")).thenReturn(user);

        mvc.perform(get("/api/users/u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("u1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_notFound() throws Exception {
        when(userService.getUserById("u1"))
                .thenThrow(new IllegalArgumentException("User not found"));

        mvc.perform(get("/api/users/u1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserById_forbidden_for_non_admin() throws Exception {
        mvc.perform(get("/api/users/u1"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // UPDATE USER (ADMIN)
    // ------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_success() throws Exception {
        User updated = new User("u1", "Updated", "updated@test.com", "ENC", "6900000000", List.of("ROLE_USER"));

        when(userService.updateUser(eq("u1"), any())).thenReturn(updated);

        mvc.perform(put("/api/users/u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_notFound() throws Exception {
        when(userService.updateUser(eq("u1"), any()))
                .thenThrow(new IllegalArgumentException("User not found"));

        mvc.perform(put("/api/users/u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_invalid_payload() throws Exception {
        mvc.perform(put("/api/users/u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ name: 123 }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUser_forbidden_for_non_admin() throws Exception {
        mvc.perform(put("/api/users/u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // DELETE USER (ADMIN)
    // ------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_success() throws Exception {
        doNothing().when(userService).deleteUser("u1");

        mvc.perform(delete("/api/users/u1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_notFound() throws Exception {
        doThrow(new IllegalArgumentException("User not found"))
                .when(userService).deleteUser("u1");

        mvc.perform(delete("/api/users/u1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_forbidden_for_non_admin() throws Exception {
        mvc.perform(delete("/api/users/u1"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // PROMOTE USER (ADMIN)
    // ------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void promoteUser_success() throws Exception {
        User promoted = new User("u1", "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_ADMIN"));

        when(userService.promoteToAdmin("u1")).thenReturn(promoted);

        mvc.perform(put("/api/users/u1/promote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void promoteUser_notFound() throws Exception {
        when(userService.promoteToAdmin("u1"))
                .thenThrow(new IllegalArgumentException("User not found"));

        mvc.perform(put("/api/users/u1/promote"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void promoteUser_forbidden_for_non_admin() throws Exception {
        mvc.perform(put("/api/users/u1/promote"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // DEMOTE USER (ADMIN)
    // ------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void demoteUser_success() throws Exception {
        User demoted = new User("u1", "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER"));

        when(userService.demoteToUser("u1")).thenReturn(demoted);

        mvc.perform(put("/api/users/u1/demote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void demoteUser_notFound() throws Exception {
        when(userService.demoteToUser("u1"))
                .thenThrow(new IllegalArgumentException("User not found"));

        mvc.perform(put("/api/users/u1/demote"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void demoteUser_forbidden_for_non_admin() throws Exception {
        mvc.perform(put("/api/users/u1/demote"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // GET AUTHENTICATED USER (/me)
    // ------------------------------------------------------------

    @Test
    @WithMockUser(username = "k@k.com", roles = "USER")
    void getAuthenticatedUser_success() throws Exception {
        when(userService.getUserByEmail("k@k.com")).thenReturn(user);

        mvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("k@k.com"));
    }

    @Test
    @WithMockUser(username = "missing@test.com", roles = "USER")
    void getAuthenticatedUser_notFound() throws Exception {
        when(userService.getUserByEmail("missing@test.com"))
                .thenThrow(new IllegalArgumentException("User not found"));

        mvc.perform(get("/api/users/me"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------
    // MALFORMED JSON IN UPDATE
    // ------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_malformedJson() throws Exception {
        mvc.perform(put("/api/users/u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ bad json }"))
                .andExpect(status().isBadRequest());
    }
}
