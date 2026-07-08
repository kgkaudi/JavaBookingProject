package com.kostas.bookingproject.services;

import com.kostas.bookingproject.models.Booking;
import com.kostas.bookingproject.models.Room;
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.BookingRepository;
import com.kostas.bookingproject.repositories.RoomRepository;
import com.kostas.bookingproject.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    BookingService service;
    BookingRepository bookings;
    RoomRepository rooms;
    UserRepository users;

    User user;
    Room room;

    @BeforeEach
    void setup() {
        bookings = mock(BookingRepository.class);
        rooms = mock(RoomRepository.class);
        users = mock(UserRepository.class);

        service = new BookingService(bookings, rooms, users);

        user = new User("u1", "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER"));
        room = new Room("r1", 101, "single", 50, true);
    }

    @Test
    void create_booking_success() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));
        when(bookings.findByRoomId("r1")).thenReturn(List.of());

        Booking saved = new Booking(
                "b1",
                "u1",
                "r1",
                "confirmed",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        when(bookings.save(any())).thenReturn(saved);

        Booking b = service.createBooking(
                "k@k.com",
                "r1",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05")
        );

        assertNotNull(b);
        assertEquals("u1", b.getUserId());
        assertEquals("r1", b.getRoomId());
    }

    @Test
    void overlapping_booking_throws() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));

        Booking existing = new Booking(
                "b1",
                "u1",
                "r1",
                "confirmed",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        when(bookings.findByRoomId("r1")).thenReturn(List.of(existing));

        assertThrows(IllegalStateException.class, () ->
                service.createBooking(
                        "k@k.com",
                        "r1",
                        LocalDate.parse("2026-01-03"),
                        LocalDate.parse("2026-01-07")
                )
        );
    }

    @Test
    void user_can_cancel_own_booking() {
        Booking b = new Booking(
                "b1",
                "u1",
                "r1",
                "confirmed",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(bookings.findById("b1")).thenReturn(Optional.of(b));

        service.cancelBooking("b1", "k@k.com");

        verify(bookings).delete(b);
    }

    @Test
    void user_cannot_cancel_others_booking() {
        Booking b = new Booking(
                "b1",
                "other",
                "r1",
                "confirmed",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-05"),
                0.0
        );

        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(bookings.findById("b1")).thenReturn(Optional.of(b));

        assertThrows(RuntimeException.class, () ->
                service.cancelBooking("b1", "k@k.com")
        );
    }
}
