package com.kostas.bookingproject.controllers;

import com.kostas.bookingproject.dto.BookingResponse;
import com.kostas.bookingproject.models.Booking;
import com.kostas.bookingproject.services.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // ---------------------------------------------------------
    // CREATE BOOKING (USER)
    // ---------------------------------------------------------
    @PostMapping
    public BookingResponse createBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Booking booking = bookingService.createBooking(
                userDetails.getUsername(),
                roomId,
                startDate,
                endDate);

        return bookingService.toResponse(booking);
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
    public List<BookingResponse> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails) {

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
    public List<BookingResponse> getBookingsForRoom(@PathVariable String roomId) {
        return bookingService.getBookingsForRoom(roomId)
                .stream()
                .map(bookingService::toResponse)
                .toList();
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
    public ResponseEntity<?> checkAvailability(
            @RequestParam String roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            boolean available = bookingService.isRoomAvailable(roomId, startDate, endDate);

            if (!available) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "available", false,
                        "reason", "Room is already booked for the selected dates"));
            }

            return ResponseEntity.ok(Map.of("available", true));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "available", false,
                    "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "available", false,
                    "error", "Unexpected server error"));
        }
    }

    // ---------------------------------------------------------
    // UPDATE BOOKING (ADMIN)
    // ---------------------------------------------------------
    @PutMapping("/{bookingId}")
    public BookingResponse updateBooking(
            @PathVariable String bookingId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Booking updatedBooking) {

        return bookingService.updateBooking(
                bookingId,
                userDetails.getUsername(),
                updatedBooking);
    }

    // ---------------------------------------------------------
    // DELETE BOOKING (ADMIN)
    // ---------------------------------------------------------
    @DeleteMapping("/{bookingId}")
    public void deleteBooking(
            @PathVariable String bookingId,
            @AuthenticationPrincipal UserDetails userDetails) {

        bookingService.deleteBooking(bookingId, userDetails.getUsername());
    }

    // ---------------------------------------------------------
    // CANCEL BOOKING (USER + ADMIN)
    // ---------------------------------------------------------
    @PatchMapping("/{bookingId}/cancel")
    public BookingResponse cancelBooking(
            @PathVariable String bookingId,
            @AuthenticationPrincipal UserDetails userDetails) {

        bookingService.cancelBooking(bookingId, userDetails.getUsername());
        return bookingService.getBookingById(bookingId, userDetails.getUsername());
    }
}