package com.kostas.bookingproject.controllers;

import com.kostas.bookingproject.dto.BookingResponse;
import com.kostas.bookingproject.models.Booking;
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.services.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // ---------------------------------------------------------
    // CREATE BOOKING
    // ---------------------------------------------------------
    @PostMapping
    public Booking createBooking(
            @AuthenticationPrincipal User user,
            @RequestParam String roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return bookingService.createBooking(
                user.getId(),
                roomId,
                startDate,
                endDate);
    }

    // ---------------------------------------------------------
    // GET BOOKINGS FOR AUTHENTICATED USER
    // ---------------------------------------------------------
    @GetMapping("/me")
    public List<BookingResponse> getMyBookings(@AuthenticationPrincipal User user) {
        return bookingService.getBookingsForUser(user.getId());
    }

    // ---------------------------------------------------------
    // GET BOOKINGS FOR SPECIFIC USER (ADMIN)
    // ---------------------------------------------------------
    @GetMapping("/user/{userId}")
    public List<BookingResponse> getBookingsForUser(@PathVariable String userId) {
        return bookingService.getBookingsForUser(userId);
    }

    // ---------------------------------------------------------
    // GET BOOKINGS FOR ROOM
    // ---------------------------------------------------------
    @GetMapping("/room/{roomId}")
    public List<Booking> getBookingsForRoom(@PathVariable String roomId) {
        return bookingService.getBookingsForRoom(roomId);
    }

    // ---------------------------------------------------------
    // CHECK AVAILABILITY
    // ---------------------------------------------------------
    @GetMapping("/availability")
    public boolean checkAvailability(
            @RequestParam String roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return bookingService.isRoomAvailable(roomId, startDate, endDate);
    }

    // ---------------------------------------------------------
    // CANCEL BOOKING
    // ---------------------------------------------------------
    @DeleteMapping("/{bookingId}")
    public void cancelBooking(
            @PathVariable String bookingId,
            @AuthenticationPrincipal User user) {
        bookingService.cancelBooking(bookingId, user.getId());
    }
}
