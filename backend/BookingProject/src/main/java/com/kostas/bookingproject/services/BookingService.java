package com.kostas.bookingproject.services;

import com.kostas.bookingproject.dto.BookingResponse;
import com.kostas.bookingproject.models.Booking;
import com.kostas.bookingproject.models.Room;
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.BookingRepository;
import com.kostas.bookingproject.repositories.RoomRepository;
import com.kostas.bookingproject.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
            RoomRepository roomRepository,
            UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    // ---------------------------------------------------------
    // CREATE BOOKING
    // ---------------------------------------------------------
    public Booking createBooking(String userId, String roomId,
            LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null || !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("Invalid date range");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (!room.isAvailable()) {
            throw new IllegalStateException("Room is not available");
        }

        if (!isRoomAvailable(roomId, startDate, endDate)) {
            throw new IllegalStateException("Room is already booked for the selected dates");
        }

        Booking booking = new Booking();
        booking.setUserId(user.getId());
        booking.setRoomId(room.getId());
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);

        return bookingRepository.save(booking);
    }

    // ---------------------------------------------------------
    // CHECK ROOM AVAILABILITY
    // ---------------------------------------------------------
    public boolean isRoomAvailable(String roomId,
            LocalDate startDate, LocalDate endDate) {

        List<Booking> bookings = bookingRepository.findByRoomId(roomId);

        return bookings.stream().noneMatch(b -> datesOverlap(startDate, endDate, b.getStartDate(), b.getEndDate()));
    }

    private boolean datesOverlap(LocalDate start1, LocalDate end1,
            LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }

    // ---------------------------------------------------------
    // GET BOOKINGS FOR USER
    // ---------------------------------------------------------
    public List<BookingResponse> getBookingsForUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        List<Booking> bookings = bookingRepository.findByUserId(userId);

        return bookings.stream()
                .map(b -> {
                    Room room = roomRepository.findById(b.getRoomId()).orElse(null);

                    String roomName = (room != null)
                            ? "Room " + room.getRoomNumber() + " — " + room.getType()
                            : "Unknown Room";

                    return new BookingResponse(
                            b.getId(),
                            roomName,
                            b.getStartDate(),
                            b.getEndDate(),
                            b.getTotalPrice());
                })
                .toList();
    }

    // ---------------------------------------------------------
    // GET BOOKINGS FOR ROOM
    // ---------------------------------------------------------
    public List<Booking> getBookingsForRoom(String roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new IllegalArgumentException("Room not found");
        }
        return bookingRepository.findByRoomId(roomId);
    }

    // ---------------------------------------------------------
    // CANCEL BOOKING
    // ---------------------------------------------------------
    public void cancelBooking(String bookingId, String userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new IllegalStateException("You cannot cancel someone else's booking");
        }

        bookingRepository.delete(booking);
    }
}
