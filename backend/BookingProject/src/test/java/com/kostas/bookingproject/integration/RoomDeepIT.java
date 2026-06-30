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
import static org.hamcrest.Matchers.*;

import java.util.List;

import com.kostas.bookingproject.config.MockMvcConfig;
import com.kostas.bookingproject.repositories.RoomRepository;
import com.kostas.bookingproject.repositories.UserRepository;
import com.kostas.bookingproject.security.jwt.JwtUtil;
import com.kostas.bookingproject.models.Room;
import com.kostas.bookingproject.models.User;

@SpringBootTest
@Import(MockMvcConfig.class)
class RoomDeepIT {

    @Autowired MockMvc mvc;
    @Autowired RoomRepository rooms;
    @Autowired UserRepository users;
    @Autowired JwtUtil jwt;
    @Autowired ObjectMapper mapper;

    String adminToken;
    String userToken;

    @BeforeEach
    void setup() {
        rooms.deleteAll();
        users.deleteAll();

        User admin = users.save(new User(
                null,
                "Admin",
                "admin@test.com",
                "ENC",
                "6900000000",
                List.of("ADMIN")   // ✔ FIXED
        ));

        User user = users.save(new User(
                null,
                "User",
                "user@test.com",
                "ENC",
                "6900000000",
                List.of("USER")    // ✔ FIXED
        ));

        adminToken = "Bearer " + jwt.generateToken(admin.getId(), List.of("ADMIN")); // ✔ FIXED
        userToken  = "Bearer " + jwt.generateToken(user.getId(), List.of("USER"));   // ✔ FIXED
    }

    // ------------------------------------------------------------
    // SUCCESS CASES
    // ------------------------------------------------------------

    @Test
    void admin_can_create_room() throws Exception {
        Room r = new Room(null, 101, "single", 50, true);

        mvc.perform(post("/api/rooms")
                        .header("Authorization", adminToken)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(r)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void search_rooms_by_price_range() throws Exception {
        rooms.save(new Room(null, 101, "single", 50, true));
        rooms.save(new Room(null, 102, "double", 120, true));

        mvc.perform(get("/api/rooms/price?min=40&max=60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ------------------------------------------------------------
    // AUTHORIZATION EDGE CASES
    // ------------------------------------------------------------

    @Test
    void user_cannot_create_room() throws Exception {
        Room r = new Room(null, 101, "single", 50, true);

        mvc.perform(post("/api/rooms")
                        .header("Authorization", userToken)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(r)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannot_create_room_without_token() throws Exception {
        Room r = new Room(null, 101, "single", 50, true);

        mvc.perform(post("/api/rooms")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(r)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannot_create_room_with_invalid_token() throws Exception {
        Room r = new Room(null, 101, "single", 50, true);

        mvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer invalid")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(r)))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // VALIDATION EDGE CASES
    // ------------------------------------------------------------

    @Test
    void cannot_create_room_with_missing_number() throws Exception {
        Room r = new Room(null, 0, "single", 50, true);

        mvc.perform(post("/api/rooms")
                        .header("Authorization", adminToken)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(r)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_create_room_with_missing_type() throws Exception {
        Room r = new Room(null, 101, null, 50, true);

        mvc.perform(post("/api/rooms")
                        .header("Authorization", adminToken)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(r)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_create_room_with_negative_price() throws Exception {
        Room r = new Room(null, 101, "single", -10, true);

        mvc.perform(post("/api/rooms")
                        .header("Authorization", adminToken)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(r)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_create_room_with_malformed_json() throws Exception {
        mvc.perform(post("/api/rooms")
                        .header("Authorization", adminToken)
                        .contentType("application/json")
                        .content("{ number: 'bad', type: 123 }"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------
    // BUSINESS LOGIC EDGE CASES
    // ------------------------------------------------------------

    @Test
    void cannot_create_duplicate_room_number() throws Exception {
        rooms.save(new Room(null, 101, "single", 50, true));

        Room r = new Room(null, 101, "double", 120, true);

        mvc.perform(post("/api/rooms")
                        .header("Authorization", adminToken)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(r)))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------
    // REPOSITORY EDGE CASES
    // ------------------------------------------------------------

    @Test
    void search_price_range_returns_empty_list_if_no_matches() throws Exception {
        rooms.save(new Room(null, 101, "single", 200, true));

        mvc.perform(get("/api/rooms/price?min=10&max=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
