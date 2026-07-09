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

import java.time.LocalDate;
import java.util.List;

import com.kostas.bookingproject.config.MockMvcConfig;
import com.kostas.bookingproject.repositories.BookingRepository;
import com.kostas.bookingproject.repositories.RoomRepository;
import com.kostas.bookingproject.repositories.UserRepository;
import com.kostas.bookingproject.security.JwtUtil;
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.models.Room;
import com.kostas.bookingproject.models.Booking;

@SpringBootTest
@Import(MockMvcConfig.class)
class BookingControllerIT {

    @Autowired MockMvc mvc;
    @Autowired BookingRepository bookings;
    @Autowired RoomRepository rooms;
    @Autowired UserRepository users;
    @Autowired JwtUtil jwt;
    @Autowired ObjectMapper mapper;

    String token;
    User user;
    Room room;

    @BeforeEach
    void setup() {
        bookings.deleteAll();
        rooms.deleteAll();
        users.deleteAll();

        user = users.save(new User(
                null,
                "Kostas",
                "k@k.com",
                "ENC",
                "6900000000",
                List.of("ROLE_USER")
        ));

        token = "Bearer " + jwt.generateToken(user.getId(), List.of("ROLE_USER"));

        room = rooms.save(new Room(
                null,
                101,
                "single",
                1,
                50.0,
                true
        ));
    }

    // ------------------------------------------------------------
    // SUCCESS CASES
    // ------------------------------------------------------------

    @Test
    void user_can_create_booking() throws Exception {
        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .param("roomId", room.getId())
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.totalPrice").value(200.0));
    }

    @Test
    void user_can_cancel_booking() throws Exception {
        Booking b = bookings.save(new Booking(
                null,
                user.getId(),
                room.getId(),
                "confirmed",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                200.0
        ));

        mvc.perform(post("/api/bookings/" + b.getId() + "/cancel")
                        .header("Authorization", token))
                .andExpect(status().isOk());

        Booking updated = bookings.findById(b.getId()).get();
        assert updated.getStatus().equals("cancelled");
    }

    // ------------------------------------------------------------
    // EDGE CASES — BUSINESS LOGIC
    // ------------------------------------------------------------

    @Test
    void cannot_create_booking_for_unavailable_room() throws Exception {
        room.setAvailable(false);
        rooms.save(room);

        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .param("roomId", room.getId())
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_create_booking_if_dates_overlap_existing_booking() throws Exception {
        bookings.save(new Booking(
                null,
                user.getId(),
                room.getId(),
                "confirmed",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                200.0
        ));

        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .param("roomId", room.getId())
                        .param("startDate", "2026-01-03")
                        .param("endDate", "2026-01-07"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_create_booking_if_start_after_end() throws Exception {
        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .param("roomId", room.getId())
                        .param("startDate", "2026-01-10")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_create_booking_if_start_equals_end() throws Exception {
        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .param("roomId", room.getId())
                        .param("startDate", "2026-01-05")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------
    // EDGE CASES — VALIDATION
    // ------------------------------------------------------------

    @Test
    void cannot_create_booking_with_missing_roomId() throws Exception {
        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_create_booking_with_missing_dates() throws Exception {
        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .param("roomId", room.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_create_booking_with_malformed_json() throws Exception {
        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content("{ roomId: 123, startDate: 'bad', endDate: 'bad' }"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------
    // EDGE CASES — AUTHORIZATION
    // ------------------------------------------------------------

    @Test
    void cannot_create_booking_without_token() throws Exception {
        mvc.perform(post("/api/bookings")
                        .param("roomId", room.getId())
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cannot_create_booking_with_invalid_token() throws Exception {
        mvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer invalid")
                        .param("roomId", room.getId())
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // EDGE CASES — REPOSITORY FAILURES
    // ------------------------------------------------------------

    @Test
    void cannot_create_booking_if_room_does_not_exist() throws Exception {
        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .param("roomId", "nonexistent-room")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cannot_create_booking_if_user_does_not_exist() throws Exception {
        users.deleteAll();

        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .param("roomId", room.getId())
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isNotFound());
    }
}
