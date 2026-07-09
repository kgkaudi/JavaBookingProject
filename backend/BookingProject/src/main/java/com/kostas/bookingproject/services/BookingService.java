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
import java.time.temporal.ChronoUnit;
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
    // RESPONSE DTO MAPPER
    // ---------------------------------------------------------
    public BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getRoomId(),
                booking.getStatus(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getTotalPrice()
        );
    }

    // ---------------------------------------------------------
    // CREATE BOOKING
    // ---------------------------------------------------------
    public Booking createBooking(String email, String roomId,
                                 LocalDate startDate, LocalDate endDate) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (startDate == null || endDate == null || !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("Invalid booking dates");
        }

        List<Booking> existing = bookingRepository.findByRoomId(roomId);

        for (Booking b : existing) {
            boolean overlap = !(endDate.isBefore(b.getStartDate()) ||
                                startDate.isAfter(b.getEndDate()));
            if (overlap) {
                throw new IllegalStateException("Room already booked for these dates");
            }
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        double totalPrice = days * room.getPrice();

        Booking booking = new Booking();
        booking.setUserId(user.getId());
        booking.setRoomId(roomId);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setStatus("confirmed");
        booking.setTotalPrice(totalPrice);

        return bookingRepository.save(booking);
    }

    // ---------------------------------------------------------
    // GET ALL BOOKINGS (ADMIN)
    // ---------------------------------------------------------
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // GET BOOKINGS FOR USER
    // ---------------------------------------------------------
    public List<BookingResponse> getBookingsForUser(String userIdOrEmail) {

        // If email is provided, convert to userId
        User user = userRepository.findByEmail(userIdOrEmail)
                .orElseGet(() -> userRepository.findById(userIdOrEmail)
                        .orElseThrow(() -> new IllegalArgumentException("User not found")));

        return bookingRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // GET BOOKINGS FOR ROOM
    // ---------------------------------------------------------
    public List<Booking> getBookingsForRoom(String roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    // ---------------------------------------------------------
    // GET BOOKING BY ID (USER + ADMIN)
    // ---------------------------------------------------------
    public BookingResponse getBookingById(String bookingId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        boolean isAdmin = user.getRoles().contains("ROLE_ADMIN");
        boolean isOwner = booking.getUserId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Not allowed to view this booking");
        }

        return toResponse(booking);
    }

    // ---------------------------------------------------------
    // CHECK AVAILABILITY
    // ---------------------------------------------------------
    public boolean isRoomAvailable(String roomId, LocalDate startDate, LocalDate endDate) {

        List<Booking> existing = bookingRepository.findByRoomId(roomId);

        for (Booking b : existing) {
            boolean overlap = !(endDate.isBefore(b.getStartDate()) ||
                                startDate.isAfter(b.getEndDate()));
            if (overlap) return false;
        }

        return true;
    }

    // ---------------------------------------------------------
    // UPDATE BOOKING (ADMIN)
    // ---------------------------------------------------------
    public BookingResponse updateBooking(String bookingId,
                                         String email,
                                         Booking updatedBooking) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getRoles().contains("ROLE_ADMIN")) {
            throw new RuntimeException("Only admin can update bookings");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        booking.setStatus(updatedBooking.getStatus());
        booking.setStartDate(updatedBooking.getStartDate());
        booking.setEndDate(updatedBooking.getEndDate());
        booking.setTotalPrice(updatedBooking.getTotalPrice());

        bookingRepository.save(booking);

        return toResponse(booking);
    }

    // ---------------------------------------------------------
    // CANCEL BOOKING (USER + ADMIN)
    // ---------------------------------------------------------
    public void cancelBooking(String bookingId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        boolean isAdmin = user.getRoles().contains("ROLE_ADMIN");
        boolean isOwner = booking.getUserId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Not allowed to cancel this booking");
        }

        booking.setStatus("cancelled");
        bookingRepository.save(booking);
    }
}
