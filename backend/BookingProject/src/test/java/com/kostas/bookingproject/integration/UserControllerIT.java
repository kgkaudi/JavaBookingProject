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

    // ---------------------------------------------------------
    // GET ALL USERS (ADMIN)
    // ---------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_success() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(user, admin));

        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ---------------------------------------------------------
    // GET USER BY ID
    // ---------------------------------------------------------

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
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------
    // UPDATE USER (ADMIN)
    // ---------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_success() throws Exception {
        User updated = new User("u1", "Updated", "updated@test.com", "ENC", "6900000000", List.of("ROLE_USER"));

        when(userService.updateUser(eq("u1"), any(), any())).thenReturn(updated);

        mvc.perform(put("/api/users/u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_notFound() throws Exception {
        when(userService.updateUser(eq("u1"), any(), any()))
                .thenThrow(new IllegalArgumentException("User not found"));

        mvc.perform(put("/api/users/u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------
    // DELETE USER
    // ---------------------------------------------------------

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
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------
    // PROMOTE USER
    // ---------------------------------------------------------

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
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------
    // DEMOTE USER
    // ---------------------------------------------------------

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
                .andExpect(status().isBadRequest());
    }
}
