package com.kostas.bookingproject.services;

import com.kostas.bookingproject.models.Booking;
import com.kostas.bookingproject.models.Room;
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.BookingRepository;
import com.kostas.bookingproject.repositories.RoomRepository;
import com.kostas.bookingproject.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
    User admin;
    Room room;

    @BeforeEach
    void setup() {
        bookings = mock(BookingRepository.class);
        rooms = mock(RoomRepository.class);
        users = mock(UserRepository.class);

        service = new BookingService(bookings, rooms, users);

        user = new User("u1", "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER"));
        admin = new User("a1", "Admin", "admin@test.com", "ENC", "6900000000", List.of("ROLE_ADMIN"));

        room = new Room();
        room.setId("r1");
        room.setRoomNumber(101);
        room.setType("single");
        room.setCapacity(2);
        room.setPrice(50);
        room.setAvailable(true);
    }

    @Test
    void createBooking_success() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));
        when(bookings.findByRoomId("r1")).thenReturn(List.of());

        Booking saved = new Booking("b1", "u1", "r1", "confirmed",
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-05"), 200);

        when(bookings.save(any())).thenReturn(saved);

        Booking b = service.createBooking("k@k.com", "r1",
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-05"));

        assertEquals("u1", b.getUserId());
        assertEquals("r1", b.getRoomId());
        assertEquals(200, b.getTotalPrice());
    }

    @Test
    void createBooking_userNotFound() {
        when(users.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.createBooking("missing@test.com", "r1",
                        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-05"))
        );
    }

    @Test
    void createBooking_roomNotFound() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("missing")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.createBooking("k@k.com", "missing",
                        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-05"))
        );
    }

    @Test
    void createBooking_invalidDates_reversed() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));

        assertThrows(IllegalArgumentException.class, () ->
                service.createBooking("k@k.com", "r1",
                        LocalDate.parse("2026-01-05"), LocalDate.parse("2026-01-01"))
        );
    }

    @Test
    void createBooking_invalidDates_sameDay() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));

        assertThrows(IllegalArgumentException.class, () ->
                service.createBooking("k@k.com", "r1",
                        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-01"))
        );
    }

    @Test
    void createBooking_overlappingDates_throws() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));

        Booking existing = new Booking("b1", "u1", "r1", "confirmed",
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-05"), 0);

        when(bookings.findByRoomId("r1")).thenReturn(List.of(existing));

        assertThrows(IllegalStateException.class, () ->
                service.createBooking("k@k.com", "r1",
                        LocalDate.parse("2026-01-03"), LocalDate.parse("2026-01-07"))
        );
    }

    @Test
    void user_can_cancel_own_booking() {
        Booking b = new Booking("b1", "u1", "r1", "confirmed",
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-05"), 0);

        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(bookings.findById("b1")).thenReturn(Optional.of(b));

        service.cancelBooking("b1", "k@k.com");

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookings).save(captor.capture());

        assertEquals("cancelled", captor.getValue().getStatus());
    }

    @Test
    void admin_can_cancel_any_booking() {
        Booking b = new Booking("b1", "u2", "r1", "confirmed",
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-05"), 0);

        when(users.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(bookings.findById("b1")).thenReturn(Optional.of(b));

        service.cancelBooking("b1", "admin@test.com");

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookings).save(captor.capture());

        assertEquals("cancelled", captor.getValue().getStatus());
    }

    @Test
    void user_cannot_cancel_others_booking() {
        Booking b = new Booking("b1", "other", "r1", "confirmed",
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-05"), 0);

        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(bookings.findById("b1")).thenReturn(Optional.of(b));

        assertThrows(RuntimeException.class, () ->
                service.cancelBooking("b1", "k@k.com")
        );
    }
}
