package com.kostas.bookingproject.services;

import com.kostas.bookingproject.dto.UpdateBookingRequest;
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
        admin = new User("a1", "Admin", "admin@test.com", "ENC", "6900000001", List.of("ROLE_ADMIN"));

        room = new Room("r1", 101, "single", 1, 50.0, true);
    }

    // ---------------------------------------------------------
    // CREATE BOOKING — SUCCESS
    // ---------------------------------------------------------
    @Test
    void createBooking_success() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));
        when(bookings.findByRoomId("r1")).thenReturn(List.of());

        Booking saved = new Booking("b1", "u1", "r1", "confirmed",
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-05"), 200.0);

        when(bookings.save(any())).thenReturn(saved);

        Booking result = service.createBooking("k@k.com", "r1",
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-05"));

        assertEquals("u1", result.getUserId());
        assertEquals("r1", result.getRoomId());
        assertEquals(200.0, result.getTotalPrice());
    }

    // ---------------------------------------------------------
    // CREATE BOOKING — VALIDATION FAILURES
    // ---------------------------------------------------------
    @Test
    void createBooking_userNotFound() {
        when(users.findByEmail("missing@test.com")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
                service.createBooking("missing@test.com", "r1",
                        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-05")));
    }

    @Test
    void createBooking_roomNotFound() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("missing")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
                service.createBooking("k@k.com", "missing",
                        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-05")));
    }

    @Test
    void createBooking_invalidDates_reversed() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));
        assertThrows(IllegalArgumentException.class, () ->
                service.createBooking("k@k.com", "r1",
                        LocalDate.parse("2026-01-05"), LocalDate.parse("2026-01-01")));
    }

    @Test
    void createBooking_invalidDates_sameDay() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));
        assertThrows(IllegalArgumentException.class, () ->
                service.createBooking("k@k.com", "r1",
                        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-01")));
    }

    @Test
    void createBooking_roomUnavailable() {
        room.setAvailable(false);
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));
        assertThrows(IllegalArgumentException.class, () ->
                service.createBooking("k@k.com", "r1",
                        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-05")));
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
                        LocalDate.parse("2026-01-03"), LocalDate.parse("2026-01-07")));
    }

    // ---------------------------------------------------------
    // CANCEL BOOKING — SUCCESS & FAILURES
    // ---------------------------------------------------------
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
                service.cancelBooking("b1", "k@k.com"));
    }

    @Test
    void cancelBooking_notFound_throws() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(bookings.findById("missing")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
                service.cancelBooking("missing", "k@k.com"));
    }

    // ---------------------------------------------------------
    // DELETE BOOKING — SUCCESS & FAILURES
    // ---------------------------------------------------------
    @Test
    void admin_can_delete_booking() {
        Booking b = new Booking("b1", "u1", "r1", "confirmed",
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-05"), 0);

        when(users.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(bookings.findById("b1")).thenReturn(Optional.of(b));

        service.deleteBooking("b1", "admin@test.com");
        verify(bookings).delete(b);
    }

    @Test
    void user_cannot_delete_booking() {
        Booking b = new Booking("b1", "u1", "r1", "confirmed",
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-05"), 0);

        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(bookings.findById("b1")).thenReturn(Optional.of(b));

        assertThrows(RuntimeException.class, () ->
                service.deleteBooking("b1", "k@k.com"));
    }

    @Test
    void deleteBooking_notFound_throws() {
        when(users.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(bookings.findById("missing")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
                service.deleteBooking("missing", "admin@test.com"));
    }

    // ---------------------------------------------------------
    // AVAILABILITY CHECKS
    // ---------------------------------------------------------
    @Test
    void availability_true_when_no_bookings() {
        when(rooms.findById("r1")).thenReturn(Optional.of(room));
        when(bookings.findByRoomId("r1")).thenReturn(List.of());

        boolean available = service.isRoomAvailable("r1",
                LocalDate.parse("2026-07-10"),
                LocalDate.parse("2026-07-12"));

        assertTrue(available);
    }

    @Test
    void availability_false_when_overlap() {
        when(rooms.findById("r1")).thenReturn(Optional.of(room));

        Booking existing = new Booking("b1", "u1", "r1", "confirmed",
                LocalDate.parse("2026-07-10"), LocalDate.parse("2026-07-15"), 0);

        when(bookings.findByRoomId("r1")).thenReturn(List.of(existing));

        boolean available = service.isRoomAvailable("r1",
                LocalDate.parse("2026-07-12"),
                LocalDate.parse("2026-07-14"));

        assertFalse(available);
    }

    @Test
    void availability_invalid_date_range_throws() {
        when(rooms.findById("r1")).thenReturn(Optional.of(room));
        assertThrows(IllegalArgumentException.class, () ->
                service.isRoomAvailable("r1",
                        LocalDate.parse("2026-07-20"),
                        LocalDate.parse("2026-07-10")));
    }

    @Test
    void availability_room_not_found_throws() {
        when(rooms.findById("missing")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
                service.isRoomAvailable("missing",
                        LocalDate.parse("2026-07-10"),
                        LocalDate.parse("2026-07-12")));
    }

    @Test
    void availability_room_unavailable_throws() {
        room.setAvailable(false);
        when(rooms.findById("r1")).thenReturn(Optional.of(room));
        assertThrows(IllegalArgumentException.class, () ->
                service.isRoomAvailable("r1",
                        LocalDate.parse("2026-07-10"),
                        LocalDate.parse("2026-07-12")));
    }

    // ---------------------------------------------------------
    // OVERLAP RULES
    // ---------------------------------------------------------
    private Booking booking(String id, String status, String start, String end) {
        return new Booking(id, "u2", "r1", status,
                LocalDate.parse(start), LocalDate.parse(end), 0);
    }

    @Test
    void createBooking_sameDayTurnover_isAllowed() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));
        // existing guest checks out on the 5th; new guest checks in on the 5th
        when(bookings.findByRoomId("r1"))
                .thenReturn(List.of(booking("b1", "confirmed", "2026-01-01", "2026-01-05")));
        when(bookings.save(any())).thenAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setId("b2");
            return b;
        });

        Booking result = service.createBooking("k@k.com", "r1",
                LocalDate.parse("2026-01-05"), LocalDate.parse("2026-01-08"));

        assertEquals("confirmed", result.getStatus());
        assertEquals(150.0, result.getTotalPrice()); // 3 nights x 50
    }

    @Test
    void createBooking_cancelledBookingDoesNotBlockDates() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));
        when(bookings.findByRoomId("r1"))
                .thenReturn(List.of(booking("b1", "cancelled", "2026-01-01", "2026-01-10")));
        when(bookings.save(any())).thenAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setId("b2");
            return b;
        });

        assertDoesNotThrow(() -> service.createBooking("k@k.com", "r1",
                LocalDate.parse("2026-01-03"), LocalDate.parse("2026-01-06")));
    }

    @Test
    void createBooking_pendingBookingStillBlocks() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));
        when(bookings.findByRoomId("r1"))
                .thenReturn(List.of(booking("b1", "pending", "2026-01-01", "2026-01-10")));

        assertThrows(IllegalStateException.class, () -> service.createBooking("k@k.com", "r1",
                LocalDate.parse("2026-01-03"), LocalDate.parse("2026-01-06")));
    }

    @Test
    void createBooking_conflictAppearingAfterInsert_isRolledBack() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));

        // 1st lookup (pre-check): room looks free. 2nd lookup (post-insert verify):
        // a concurrent request has inserted an overlapping booking in the meantime.
        when(bookings.findByRoomId("r1")).thenReturn(
                List.of(),
                List.of(booking("other", "confirmed", "2026-01-02", "2026-01-06")));

        when(bookings.save(any())).thenAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setId("mine");
            return b;
        });

        assertThrows(IllegalStateException.class, () -> service.createBooking("k@k.com", "r1",
                LocalDate.parse("2026-01-03"), LocalDate.parse("2026-01-07")));

        verify(bookings).delete(argThat(b -> "mine".equals(b.getId())));
    }

    @Test
    void createBooking_ownBookingIsIgnoredInPostInsertVerification() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));

        Booking mine = booking("mine", "confirmed", "2026-01-03", "2026-01-07");
        when(bookings.findByRoomId("r1")).thenReturn(List.of(), List.of(mine));
        when(bookings.save(any())).thenReturn(mine);

        assertDoesNotThrow(() -> service.createBooking("k@k.com", "r1",
                LocalDate.parse("2026-01-03"), LocalDate.parse("2026-01-07")));
        verify(bookings, never()).delete(any());
    }

    @Test
    void availability_true_for_sameDayTurnover() {
        when(rooms.findById("r1")).thenReturn(Optional.of(room));
        when(bookings.findByRoomId("r1"))
                .thenReturn(List.of(booking("b1", "confirmed", "2026-07-10", "2026-07-15")));

        assertTrue(service.isRoomAvailable("r1",
                LocalDate.parse("2026-07-15"), LocalDate.parse("2026-07-18")));
    }

    @Test
    void availability_true_when_overlapping_booking_is_cancelled() {
        when(rooms.findById("r1")).thenReturn(Optional.of(room));
        when(bookings.findByRoomId("r1"))
                .thenReturn(List.of(booking("b1", "cancelled", "2026-07-10", "2026-07-15")));

        assertTrue(service.isRoomAvailable("r1",
                LocalDate.parse("2026-07-12"), LocalDate.parse("2026-07-14")));
    }

    // ---------------------------------------------------------
    // ADMIN UPDATE BOOKING
    // ---------------------------------------------------------
    private Booking existingForUpdate() {
        return new Booking("b1", "u1", "r1", "confirmed",
                LocalDate.parse("2026-03-01"), LocalDate.parse("2026-03-05"), 200.0);
    }

    @Test
    void updateBooking_nonAdmin_rejected() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () ->
                service.updateBooking("b1", "k@k.com", new UpdateBookingRequest("confirmed", null, null)));
        verify(bookings, never()).save(any());
    }

    @Test
    void updateBooking_recalculatesPrice_andIgnoresItself() {
        Booking b = existingForUpdate();
        when(users.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(bookings.findById("b1")).thenReturn(Optional.of(b));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));
        // the booking itself is in the room's list; it must not conflict with itself
        when(bookings.findByRoomId("r1")).thenReturn(List.of(b));

        service.updateBooking("b1", "admin@test.com",
                new UpdateBookingRequest(null, LocalDate.parse("2026-03-02"), LocalDate.parse("2026-03-07")));

        assertEquals(LocalDate.parse("2026-03-02"), b.getStartDate());
        assertEquals(LocalDate.parse("2026-03-07"), b.getEndDate());
        assertEquals(250.0, b.getTotalPrice()); // 5 nights x 50, not client supplied
        verify(bookings, atLeastOnce()).save(b);
    }

    @Test
    void updateBooking_invalidDates_rejected() {
        when(users.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(bookings.findById("b1")).thenReturn(Optional.of(existingForUpdate()));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));

        assertThrows(IllegalArgumentException.class, () ->
                service.updateBooking("b1", "admin@test.com",
                        new UpdateBookingRequest(null, LocalDate.parse("2026-03-09"), LocalDate.parse("2026-03-05"))));
        verify(bookings, never()).save(any());
    }

    @Test
    void updateBooking_invalidStatus_rejected() {
        when(users.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(bookings.findById("b1")).thenReturn(Optional.of(existingForUpdate()));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));

        assertThrows(IllegalArgumentException.class, () ->
                service.updateBooking("b1", "admin@test.com", new UpdateBookingRequest("banana", null, null)));
        verify(bookings, never()).save(any());
    }

    @Test
    void updateBooking_overlapWithAnotherBooking_rejected() {
        Booking b = existingForUpdate();
        when(users.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(bookings.findById("b1")).thenReturn(Optional.of(b));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));
        when(bookings.findByRoomId("r1")).thenReturn(List.of(b,
                booking("b9", "confirmed", "2026-03-06", "2026-03-10")));

        assertThrows(IllegalStateException.class, () ->
                service.updateBooking("b1", "admin@test.com",
                        new UpdateBookingRequest(null, null, LocalDate.parse("2026-03-08"))));
        verify(bookings, never()).save(any());
        assertEquals(LocalDate.parse("2026-03-05"), b.getEndDate());
    }

    @Test
    void updateBooking_cancelling_skipsOverlapCheck() {
        Booking b = existingForUpdate();
        when(users.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(bookings.findById("b1")).thenReturn(Optional.of(b));
        when(rooms.findById("r1")).thenReturn(Optional.of(room));

        service.updateBooking("b1", "admin@test.com", new UpdateBookingRequest("cancelled", null, null));

        assertEquals("cancelled", b.getStatus());
        verify(bookings).save(b);
    }
}
