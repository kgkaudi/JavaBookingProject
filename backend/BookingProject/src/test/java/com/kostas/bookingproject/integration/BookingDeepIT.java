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
class BookingDeepIT {

    @Autowired MockMvc mvc;
    @Autowired BookingRepository bookings;
    @Autowired RoomRepository rooms;
    @Autowired UserRepository users;
    @Autowired JwtUtil jwt;
    @Autowired ObjectMapper mapper;

    User user;
    User otherUser;
    User admin;
    Room room;
    Room otherRoom;

    String userToken;
    String otherUserToken;
    String adminToken;

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

        otherUser = users.save(new User(
                null,
                "Other",
                "other@test.com",
                "ENC",
                "6900000000",
                List.of("ROLE_USER")
        ));

        admin = users.save(new User(
                null,
                "Admin",
                "admin@test.com",
                "ENC",
                "6900000000",
                List.of("ROLE_ADMIN")
        ));

        userToken      = "Bearer " + jwt.generateToken(user.getId(), List.of("ROLE_USER"));
        otherUserToken = "Bearer " + jwt.generateToken(otherUser.getId(), List.of("ROLE_USER"));
        adminToken     = "Bearer " + jwt.generateToken(admin.getId(), List.of("ROLE_ADMIN"));

        room = rooms.save(new Room(
                null,
                101,
                "single",
                1,
                50.0,
                true
        ));

        otherRoom = rooms.save(new Room(
                null,
                102,
                "double",
                2,
                120.0,
                true
        ));
    }

    // ------------------------------------------------------------
    // SUCCESS CASES
    // ------------------------------------------------------------

    @Test
    void user_can_create_booking() throws Exception {
        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
                        .param("roomId", room.getId())
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(room.getId()))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.totalPrice").value(200.0));
    }

    @Test
    void user_can_cancel_own_booking() throws Exception {
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
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        Booking updated = bookings.findById(b.getId()).get();
        assert updated.getStatus().equals("cancelled");
    }

    // ------------------------------------------------------------
    // BUSINESS LOGIC EDGE CASES
    // ------------------------------------------------------------

    @Test
    void cannot_book_unavailable_room() throws Exception {
        room.setAvailable(false);
        rooms.save(room);

        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
                        .param("roomId", room.getId())
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_book_if_dates_overlap_existing_booking() throws Exception {
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
                        .header("Authorization", userToken)
                        .param("roomId", room.getId())
                        .param("startDate", "2026-01-03")
                        .param("endDate", "2026-01-07"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_book_if_start_after_end() throws Exception {
        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
                        .param("roomId", room.getId())
                        .param("startDate", "2026-01-10")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_book_if_start_equals_end() throws Exception {
        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
                        .param("roomId", room.getId())
                        .param("startDate", "2026-01-05")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------
    // VALIDATION EDGE CASES
    // ------------------------------------------------------------

    @Test
    void cannot_create_booking_with_missing_roomId() throws Exception {
        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_create_booking_with_missing_dates() throws Exception {
        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
                        .param("roomId", room.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_create_booking_with_malformed_json() throws Exception {
        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
                        .contentType("application/json")
                        .content("{ roomId: 123, startDate: 'bad', endDate: 'bad' }"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------
    // AUTHORIZATION EDGE CASES
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
    // REPOSITORY EDGE CASES
    // ------------------------------------------------------------

    @Test
    void cannot_create_booking_if_room_does_not_exist() throws Exception {
        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
                        .param("roomId", "nonexistent-room")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cannot_create_booking_if_user_does_not_exist() throws Exception {
        users.deleteAll();

        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
                        .param("roomId", room.getId())
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------
    // CANCELLATION EDGE CASES
    // ------------------------------------------------------------

    @Test
    void user_cannot_cancel_other_users_booking() throws Exception {
        Booking b = bookings.save(new Booking(
                null,
                otherUser.getId(),
                room.getId(),
                "confirmed",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                200.0
        ));

        mvc.perform(post("/api/bookings/" + b.getId() + "/cancel")
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_cancel_any_booking() throws Exception {
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
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());

        Booking updated = bookings.findById(b.getId()).get();
        assert updated.getStatus().equals("cancelled");
    }

    @Test
    void cancel_nonexistent_booking_not_found() throws Exception {
        mvc.perform(post("/api/bookings/nonexistent-id/cancel")
                        .header("Authorization", userToken))
                .andExpect(status().isNotFound());
    }
}
