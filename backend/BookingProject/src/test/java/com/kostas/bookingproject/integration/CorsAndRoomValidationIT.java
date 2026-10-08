package com.kostas.bookingproject.integration;

import com.kostas.bookingproject.controllers.RoomController;
import com.kostas.bookingproject.repositories.UserRepository;
import com.kostas.bookingproject.security.JwtFilter;
import com.kostas.bookingproject.security.JwtUtil;
import com.kostas.bookingproject.security.SecurityConfig;
import com.kostas.bookingproject.security.TokenBlacklist;
import com.kostas.bookingproject.services.RoomService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real SecurityConfig (CORS + URL rules) and bean validation on the room endpoints. */
@WebMvcTest(RoomController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class CorsAndRoomValidationIT {

    @Autowired MockMvc mvc;

    @MockBean RoomService roomService;
    @MockBean JwtUtil jwtUtil;
    @MockBean UserRepository userRepository;
    @MockBean TokenBlacklist tokenBlacklist;

    // ---------------- CORS ----------------

    @Test
    void preflight_for_patch_from_frontend_is_allowed() throws Exception {
        mvc.perform(options("/api/bookings/b1/cancel")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "PATCH")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("PATCH")));
    }

    @Test
    void preflight_from_unknown_origin_is_rejected() throws Exception {
        mvc.perform(options("/api/rooms")
                        .header("Origin", "https://evil.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    // ---------------- room validation ----------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_room_with_invalid_fields_returns_400_with_field_errors() throws Exception {
        mvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomNumber\":-1,\"type\":\"\",\"capacity\":0,\"price\":-5,\"available\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.roomNumber").exists())
                .andExpect(jsonPath("$.fieldErrors.type").exists())
                .andExpect(jsonPath("$.fieldErrors.capacity").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists());

        verify(roomService, never()).createRoom(any());
    }
}
