package com.kostas.bookingproject.controllers;

import com.kostas.bookingproject.dto.BookingResponse;
import com.kostas.bookingproject.models.Booking;
import com.kostas.bookingproject.services.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return bookingService.createBooking(userDetails.getUsername(), roomId, startDate, endDate);
    }

    // ---------------------------------------------------------
    // GET ALL BOOKINGS (ADMIN)
    // ---------------------------------------------------------
    @GetMapping
    public List<BookingResponse> getAllBookings() {
        return bookingService.getAllBookings();
    }

    // ---------------------------------------------------------
    // GET BOOKINGS FOR AUTHENTICATED USER
    // ---------------------------------------------------------
    @GetMapping("/me")
    public List<BookingResponse> getMyBookings(@AuthenticationPrincipal UserDetails userDetails) {
        return bookingService.getBookingsForUser(userDetails.getUsername());
    }

    // ---------------------------------------------------------
    // GET BOOKINGS FOR SPECIFIC USER (ADMIN)
    // ---------------------------------------------------------
    @GetMapping("/user/{userId}")
    public List<BookingResponse> getBookingsForUser(@PathVariable String userId) {
        return bookingService.getBookingsForUser(userId);
    }

    // ---------------------------------------------------------
    // GET BOOKINGS FOR ROOM (ADMIN)
    // ---------------------------------------------------------
    @GetMapping("/room/{roomId}")
    public List<Booking> getBookingsForRoom(@PathVariable String roomId) {
        return bookingService.getBookingsForRoom(roomId);
    }

    // ---------------------------------------------------------
    // GET BOOKING BY ID (USER + ADMIN)
    // ---------------------------------------------------------
    @GetMapping("/{bookingId}")
    public BookingResponse getBookingById(
            @PathVariable String bookingId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return bookingService.getBookingById(bookingId, userDetails.getUsername());
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
            @AuthenticationPrincipal UserDetails userDetails) {

        bookingService.cancelBooking(bookingId, userDetails.getUsername());
    }
}
