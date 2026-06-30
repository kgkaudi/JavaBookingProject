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

import java.time.LocalDate;
import java.util.List;

import com.kostas.bookingproject.config.MockMvcConfig;
import com.kostas.bookingproject.repositories.BookingRepository;
import com.kostas.bookingproject.repositories.RoomRepository;
import com.kostas.bookingproject.repositories.UserRepository;
import com.kostas.bookingproject.security.jwt.JwtUtil;
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
                List.of("USER")   // ✔ FIXED
        ));

        otherUser = users.save(new User(
                null,
                "Other",
                "other@test.com",
                "ENC",
                "6900000000",
                List.of("USER")   // ✔ FIXED
        ));

        admin = users.save(new User(
                null,
                "Admin",
                "admin@test.com",
                "ENC",
                "6900000000",
                List.of("ADMIN")  // ✔ FIXED
        ));

        userToken      = "Bearer " + jwt.generateToken(user.getId(), List.of("USER"));
        otherUserToken = "Bearer " + jwt.generateToken(otherUser.getId(), List.of("USER"));
        adminToken     = "Bearer " + jwt.generateToken(admin.getId(), List.of("ADMIN"));

        room = rooms.save(new Room(
                null,
                101,
                "single",
                50,
                true
        ));

        otherRoom = rooms.save(new Room(
                null,
                102,
                "double",
                120,
                true
        ));
    }

    // ------------------------------------------------------------
    // SUCCESS CASES
    // ------------------------------------------------------------

    @Test
    void user_can_create_booking() throws Exception {
        Booking req = new Booking(
                null,
                user.getId(),
                room.getId(),
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.roomId").value(room.getId()))
                .andExpect(jsonPath("$.userId").value(user.getId()));
    }

    @Test
    void user_can_list_own_bookings_only() throws Exception {
        bookings.save(new Booking(
                null,
                user.getId(),
                room.getId(),
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                200.0
        ));

        bookings.save(new Booking(
                null,
                otherUser.getId(),
                otherRoom.getId(),
                LocalDate.parse("2026-02-01"),
                LocalDate.parse("2026-02-05"),
                400.0
        ));

        mvc.perform(get("/api/bookings")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].userId", everyItem(equalTo(user.getId()))));
    }

    @Test
    void admin_can_list_all_bookings() throws Exception {
        bookings.save(new Booking(
                null,
                user.getId(),
                room.getId(),
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                200.0
        ));

        bookings.save(new Booking(
                null,
                otherUser.getId(),
                otherRoom.getId(),
                LocalDate.parse("2026-02-01"),
                LocalDate.parse("2026-02-05"),
                400.0
        ));

        mvc.perform(get("/api/bookings/admin")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ------------------------------------------------------------
    // BUSINESS LOGIC EDGE CASES
    // ------------------------------------------------------------

    @Test
    void cannot_book_unavailable_room() throws Exception {
        room.setAvailable(false);
        rooms.save(room);

        Booking req = new Booking(
                null,
                user.getId(),
                room.getId(),
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_book_if_dates_overlap_existing_booking() throws Exception {
        bookings.save(new Booking(
                null,
                user.getId(),
                room.getId(),
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                200.0
        ));

        Booking overlapping = new Booking(
                null,
                user.getId(),
                room.getId(),
                LocalDate.parse("2026-01-03"),
                LocalDate.parse("2026-01-07"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(overlapping)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_book_if_start_after_end() throws Exception {
        Booking req = new Booking(
                null,
                user.getId(),
                room.getId(),
                LocalDate.parse("2026-01-10"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_book_if_start_equals_end() throws Exception {
        Booking req = new Booking(
                null,
                user.getId(),
                room.getId(),
                LocalDate.parse("2026-01-05"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------
    // VALIDATION EDGE CASES
    // ------------------------------------------------------------

    @Test
    void cannot_create_booking_with_missing_roomId() throws Exception {
        Booking req = new Booking(
                null,
                user.getId(),
                null,
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
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
                null,
                null,
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
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
        Booking req = new Booking(
                null,
                user.getId(),
                room.getId(),
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannot_list_bookings_without_token() throws Exception {
        mvc.perform(get("/api/bookings"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannot_create_booking_with_invalid_token() throws Exception {
        Booking req = new Booking(
                null,
                user.getId(),
                room.getId(),
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
    // REPOSITORY EDGE CASES
    // ------------------------------------------------------------

    @Test
    void cannot_create_booking_if_room_does_not_exist() throws Exception {
        Booking req = new Booking(
                null,
                user.getId(),
                "nonexistent-room",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
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
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        mvc.perform(post("/api/bookings")
                        .header("Authorization", userToken)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------
    // CANCELLATION / OWNERSHIP EDGE CASES
    // ------------------------------------------------------------

    @Test
    void user_can_cancel_own_booking() throws Exception {
        Booking b = bookings.save(new Booking(
                null,
                user.getId(),
                room.getId(),
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                200.0
        ));

        mvc.perform(delete("/api/bookings/" + b.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        assert bookings.findById(b.getId()).isEmpty();
    }

    @Test
    void user_cannot_cancel_other_users_booking() throws Exception {
        Booking b = bookings.save(new Booking(
                null,
                otherUser.getId(),
                room.getId(),
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                200.0
        ));

        mvc.perform(delete("/api/bookings/" + b.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_cancel_any_booking() throws Exception {
        Booking b = bookings.save(new Booking(
                null,
                user.getId(),
                room.getId(),
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                200.0
        ));

        mvc.perform(delete("/api/bookings/" + b.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());

        assert bookings.findById(b.getId()).isEmpty();
    }

    @Test
    void cancel_nonexistent_booking_not_found() throws Exception {
        mvc.perform(delete("/api/bookings/nonexistent-id")
                        .header("Authorization", userToken))
                .andExpect(status().isNotFound());
    }
}
