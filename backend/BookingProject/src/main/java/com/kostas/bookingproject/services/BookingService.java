package com.kostas.bookingproject.services;

import com.kostas.bookingproject.dto.BookingResponse;
import com.kostas.bookingproject.dto.UpdateBookingRequest;
import com.kostas.bookingproject.models.Booking;
import com.kostas.bookingproject.models.Room;
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.BookingRepository;
import com.kostas.bookingproject.repositories.RoomRepository;
import com.kostas.bookingproject.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class BookingService {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_CONFIRMED = "confirmed";
    public static final String STATUS_CANCELLED = "cancelled";
    private static final Set<String> VALID_STATUSES =
            Set.of(STATUS_PENDING, STATUS_CONFIRMED, STATUS_CANCELLED);

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
    // OVERLAP HELPERS
    // ---------------------------------------------------------

    /**
     * Stays are [startDate, endDate): the end date is the check-out day, so a guest may check in
     * on the day another checks out. Cancelled bookings never block a room.
     */
    private static boolean blocks(Booking existing, LocalDate start, LocalDate end) {
        if (STATUS_CANCELLED.equals(existing.getStatus())) {
            return false;
        }
        return start.isBefore(existing.getEndDate()) && existing.getStartDate().isBefore(end);
    }

    private boolean hasConflict(String roomId, LocalDate start, LocalDate end, String ignoreBookingId) {
        return bookingRepository.findByRoomId(roomId).stream()
                .filter(b -> ignoreBookingId == null || !ignoreBookingId.equals(b.getId()))
                .anyMatch(b -> blocks(b, start, end));
    }

    private static void validateDates(LocalDate start, LocalDate end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("Invalid date range");
        }
    }

    private static double totalFor(Room room, LocalDate start, LocalDate end) {
        return room.getPrice() * (end.toEpochDay() - start.toEpochDay());
    }

    // ---------------------------------------------------------
    // RESPONSE DTO MAPPER
    // ---------------------------------------------------------
    public BookingResponse toResponse(Booking booking) {

        Room room = roomRepository.findById(booking.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        return new BookingResponse(
                booking.getId(),
                room.getRoomNumber(),
                booking.getUserId(),
                booking.getStatus(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getTotalPrice()
        );
    }

    // ---------------------------------------------------------
    // CREATE BOOKING
    // ---------------------------------------------------------
    public Booking createBooking(String email, String roomId, LocalDate start, LocalDate end) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (!room.isAvailable()) {
            throw new IllegalArgumentException("Room is marked unavailable");
        }

        validateDates(start, end);

        if (hasConflict(roomId, start, end, null)) {
            throw new IllegalStateException("Dates overlap with existing booking");
        }

        Booking booking = new Booking(
                null,
                user.getId(),
                roomId,
                STATUS_CONFIRMED,
                start,
                end,
                totalFor(room, start, end)
        );

        Booking saved = bookingRepository.save(booking);

        // Insert-then-verify: two simultaneous requests can both pass the check above.
        // Re-checking AFTER our insert is race-free: if both inserted, each sees the other and
        // backs off; if one inserted first, the second sees it. So double booking is impossible.
        // (Under a true simultaneous clash both requests may be rejected; the user can retry.)
        if (hasConflict(roomId, start, end, saved.getId())) {
            bookingRepository.delete(saved);
            throw new IllegalStateException("Dates overlap with existing booking");
        }

        return saved;
    }

    // ---------------------------------------------------------
    // DELETE BOOKING (ADMIN)
    // ---------------------------------------------------------
    public void deleteBooking(String bookingId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getRoles().contains("ROLE_ADMIN")) {
            throw new RuntimeException("Only admin can delete bookings");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        bookingRepository.delete(booking);
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

        if (startDate == null || endDate == null || !startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("Invalid date range: startDate must be before endDate");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (!room.isAvailable()) {
            throw new IllegalArgumentException("Room is marked unavailable");
        }

        return !hasConflict(roomId, startDate, endDate, null);
    }

    // ---------------------------------------------------------
    // UPDATE BOOKING (ADMIN)
    // ---------------------------------------------------------
    public BookingResponse updateBooking(String bookingId,
                                         String email,
                                         UpdateBookingRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getRoles().contains("ROLE_ADMIN")) {
            throw new RuntimeException("Only admin can update bookings");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        Room room = roomRepository.findById(booking.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        String status = request.status() != null ? request.status() : booking.getStatus();
        LocalDate start = request.startDate() != null ? request.startDate() : booking.getStartDate();
        LocalDate end = request.endDate() != null ? request.endDate() : booking.getEndDate();

        if (status == null || !VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid status");
        }
        validateDates(start, end);

        // A booking that stays/becomes active must not collide with another active booking
        if (!STATUS_CANCELLED.equals(status) && hasConflict(booking.getRoomId(), start, end, bookingId)) {
            throw new IllegalStateException("Dates overlap with existing booking");
        }

        String oldStatus = booking.getStatus();
        LocalDate oldStart = booking.getStartDate();
        LocalDate oldEnd = booking.getEndDate();
        double oldTotal = booking.getTotalPrice();

        booking.setStatus(status);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setTotalPrice(totalFor(room, start, end));
        bookingRepository.save(booking);

        // Same insert-then-verify protection as createBooking; roll back on conflict
        if (!STATUS_CANCELLED.equals(status) && hasConflict(booking.getRoomId(), start, end, bookingId)) {
            booking.setStatus(oldStatus);
            booking.setStartDate(oldStart);
            booking.setEndDate(oldEnd);
            booking.setTotalPrice(oldTotal);
            bookingRepository.save(booking);
            throw new IllegalStateException("Dates overlap with existing booking");
        }

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

        booking.setStatus(STATUS_CANCELLED);
        bookingRepository.save(booking);
    }
}
