package com.kostas.bookingproject.dto;

import java.time.LocalDate;

/**
 * Fields an admin may change on a booking. Null means "keep the current value".
 * The total price is never taken from the client; it is recalculated from the room price.
 */
public record UpdateBookingRequest(
        String status,
        LocalDate startDate,
        LocalDate endDate) {
}
