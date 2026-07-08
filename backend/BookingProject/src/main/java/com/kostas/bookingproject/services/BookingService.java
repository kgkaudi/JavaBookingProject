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

        if (!isRoomAvailable(roomId, startDate, endDate)) {
            throw new IllegalStateException("Room is already booked for the selected dates");
        }

        Booking booking = new Booking();
        booking.setUserId(user.getId());
        booking.setRoomId(room.getId());
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setStatus("pending");
        booking.setTotalPrice(room.getPrice());

        return bookingRepository.save(booking);
    }

    // ---------------------------------------------------------
    // CHECK ROOM AVAILABILITY
    // ---------------------------------------------------------
    public boolean isRoomAvailable(String roomId, LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findByRoomId(roomId);

        return bookings.stream()
                .noneMatch(b ->
                        !(endDate.isBefore(b.getStartDate()) || startDate.isAfter(b.getEndDate()))
                );
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
    // GET BOOKING BY ID (USER + ADMIN)
    // ---------------------------------------------------------
    public BookingResponse getBookingById(String bookingId, String email) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = user.getRoles().contains("ROLE_ADMIN");

        if (!booking.getUserId().equals(user.getId()) && !isAdmin) {
            throw new RuntimeException("Not authorized to view this booking");
        }

        return toResponse(booking);
    }

    // ---------------------------------------------------------
    // GET BOOKINGS FOR USER
    // ---------------------------------------------------------
    public List<BookingResponse> getBookingsForUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return bookingRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // GET BOOKINGS FOR ROOM (ADMIN)
    // ---------------------------------------------------------
    public List<Booking> getBookingsForRoom(String roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    // ---------------------------------------------------------
    // UPDATE BOOKING (ADMIN)
    // ---------------------------------------------------------
    public BookingResponse updateBooking(String bookingId, String email, Booking updatedBooking) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = user.getRoles().contains("ROLE_ADMIN");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUserId().equals(user.getId()) && !isAdmin) {
            throw new RuntimeException("Not authorized to update this booking");
        }

        booking.setStartDate(updatedBooking.getStartDate());
        booking.setEndDate(updatedBooking.getEndDate());
        booking.setStatus(updatedBooking.getStatus());
        booking.setRoomId(updatedBooking.getRoomId());
        booking.setTotalPrice(updatedBooking.getTotalPrice());

        return toResponse(bookingRepository.save(booking));
    }

    // ---------------------------------------------------------
    // CANCEL BOOKING (USER + ADMIN)
    // ---------------------------------------------------------
    public void cancelBooking(String bookingId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        boolean isAdmin = user.getRoles().contains("ROLE_ADMIN");

        if (!booking.getUserId().equals(user.getId()) && !isAdmin) {
            throw new RuntimeException("Not authorized to cancel this booking");
        }

        booking.setStatus("cancelled");
        bookingRepository.save(booking);
    }

    // ---------------------------------------------------------
    // ENTITY → DTO
    // ---------------------------------------------------------
    public BookingResponse toResponse(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getRoomId(),
                b.getUserId(),
                b.getStatus(),
                b.getStartDate(),
                b.getEndDate(),
                b.getTotalPrice()
        );
    }
}
