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
                50,
                true
        ));
    }

    // ------------------------------------------------------------
    // SUCCESS CASES
    // ------------------------------------------------------------

    @Test
    void list_user_bookings() throws Exception {
        mvc.perform(get("/api/bookings")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void user_can_create_booking() throws Exception {
        Booking req = new Booking(
                null,
                user.getId(),
                room.getId(),
                "confirmed",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    // ------------------------------------------------------------
    // EDGE CASES — BUSINESS LOGIC
    // ------------------------------------------------------------

    @Test
    void cannot_create_booking_for_unavailable_room() throws Exception {
        room.setAvailable(false);
        rooms.save(room);

        Booking req = new Booking(
                null,
                user.getId(),
                room.getId(),
                "confirmed",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
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

        Booking req = new Booking(
                null,
                user.getId(),
                room.getId(),
                "confirmed",
                LocalDate.parse("2026-01-03"),
                LocalDate.parse("2026-01-07"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_create_booking_if_start_after_end() throws Exception {
        Booking req = new Booking(
                null,
                user.getId(),
                room.getId(),
                "confirmed",
                LocalDate.parse("2026-01-10"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_create_booking_if_start_equals_end() throws Exception {
        Booking req = new Booking(
                null,
                user.getId(),
                room.getId(),
                "confirmed",
                LocalDate.parse("2026-01-05"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------
    // EDGE CASES — VALIDATION
    // ------------------------------------------------------------

    @Test
    void cannot_create_booking_with_missing_roomId() throws Exception {
        Booking req = new Booking(
                null,
                user.getId(),
                null,
                "confirmed",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_create_booking_with_missing_dates() throws Exception {
        Booking req = new Booking(
                null,
                user.getId(),
                room.getId(),
                "confirmed",
                null,
                null,
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
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
        Booking req = new Booking(
                null,
                user.getId(),
                room.getId(),
                "confirmed",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cannot_list_bookings_without_token() throws Exception {
        mvc.perform(get("/api/bookings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cannot_create_booking_with_invalid_token() throws Exception {
        Booking req = new Booking(
                null,
                user.getId(),
                room.getId(),
                "confirmed",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer invalid")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // EDGE CASES — REPOSITORY FAILURES
    // ------------------------------------------------------------

    @Test
    void cannot_create_booking_if_room_does_not_exist() throws Exception {
        Booking req = new Booking(
                null,
                user.getId(),
                "nonexistent-room",
                "confirmed",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void cannot_create_booking_if_user_does_not_exist() throws Exception {
        users.deleteAll();

        Booking req = new Booking(
                null,
                user.getId(),
                room.getId(),
                "confirmed",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}
