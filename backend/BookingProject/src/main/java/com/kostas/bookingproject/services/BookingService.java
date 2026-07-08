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
    public Booking createBooking(String email, String roomId,
                                 LocalDate startDate, LocalDate endDate) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

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
    public boolean isRoomAvailable(String roomId, LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findByRoomId(roomId);
        return bookings.stream().noneMatch(b ->
                !startDate.isAfter(b.getEndDate()) && !b.getStartDate().isAfter(endDate)
        );
    }

    // ---------------------------------------------------------
    // GET ALL BOOKINGS (ADMIN)
    // ---------------------------------------------------------
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapBookingToResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // GET BOOKING BY ID (USER + ADMIN)
    // ---------------------------------------------------------
    public BookingResponse getBookingById(String bookingId, String email) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.equals("ROLE_ADMIN"));

        if (!booking.getUserId().equals(user.getId()) && !isAdmin) {
            throw new RuntimeException("Not authorized to view this booking");
        }

        return mapBookingToResponse(booking);
    }

    // ---------------------------------------------------------
    // GET BOOKINGS FOR USER
    // ---------------------------------------------------------
    public List<BookingResponse> getBookingsForUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings = bookingRepository.findByUserId(user.getId());

        return bookings.stream()
                .map(this::mapBookingToResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // GET BOOKINGS FOR ROOM
    // ---------------------------------------------------------
    public List<Booking> getBookingsForRoom(String roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    // ---------------------------------------------------------
    // CANCEL BOOKING
    // ---------------------------------------------------------
    public void cancelBooking(String bookingId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUserId().equals(user.getId())) {
            throw new RuntimeException("You cannot cancel someone else's booking");
        }

        bookingRepository.delete(booking);
    }

    // ---------------------------------------------------------
    // MAPPING
    // ---------------------------------------------------------
    private BookingResponse mapBookingToResponse(Booking b) {
        Room room = roomRepository.findById(b.getRoomId()).orElse(null);

        String roomName = (room != null)
                ? "Room " + room.getRoomNumber() + " — " + room.getType()
                : "Unknown Room";

        return new BookingResponse(
                b.getId(),
                roomName,
                b.getStartDate(),
                b.getEndDate(),
                b.getTotalPrice()
        );
    }
}
