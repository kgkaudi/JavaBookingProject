package com.kostas.bookingproject.migrations;

import com.kostas.bookingproject.models.Booking;
import com.kostas.bookingproject.models.Room;
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.BookingRepository;
import com.kostas.bookingproject.repositories.RoomRepository;
import com.kostas.bookingproject.repositories.UserRepository;
import com.kostas.bookingproject.security.PasswordEncoderService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class DatabaseSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoderService passwordEncoder;

    public DatabaseSeeder(
            UserRepository userRepository,
            RoomRepository roomRepository,
            BookingRepository bookingRepository,
            PasswordEncoderService passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {

        System.out.println("🔄 Running Database Seeder...");

        // ---------------------------------------------------
        // 1. ADMIN USER
        // ---------------------------------------------------
        if (!userRepository.existsByEmail("admin@booking.com")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@booking.com");
            admin.setPassword(passwordEncoder.encode("Admin123!"));
            admin.setRoles(List.of("ADMIN"));   // ✔ FIXED
            userRepository.save(admin);
            System.out.println("✔ Admin user created");
        }

        // ---------------------------------------------------
        // 2. TEST USERS
        // ---------------------------------------------------
        if (!userRepository.existsByEmail("user1@test.com")) {
            User u1 = new User();
            u1.setName("Test User 1");
            u1.setEmail("user1@test.com");
            u1.setPassword(passwordEncoder.encode("123456"));
            u1.setRoles(List.of("USER"));       // ✔ FIXED
            userRepository.save(u1);
            System.out.println("✔ Test User 1 created");
        }

        if (!userRepository.existsByEmail("user2@test.com")) {
            User u2 = new User();
            u2.setName("Test User 2");
            u2.setEmail("user2@test.com");
            u2.setPassword(passwordEncoder.encode("123456"));
            u2.setRoles(List.of("USER"));       // ✔ FIXED
            userRepository.save(u2);
            System.out.println("✔ Test User 2 created");
        }

        // ---------------------------------------------------
        // 3. ROOMS
        // ---------------------------------------------------
        if (roomRepository.count() == 0) {
            Room r1 = new Room(null, 101, "single", 50, true, 1);
            Room r2 = new Room(null, 102, "double", 80, true, 2);
            Room r3 = new Room(null, 201, "suite", 120, true, 4);

            roomRepository.save(r1);
            roomRepository.save(r2);
            roomRepository.save(r3);

            System.out.println("✔ Rooms seeded");
        }

        // ---------------------------------------------------
        // 4. SAMPLE BOOKING
        // ---------------------------------------------------
        Optional<User> sampleUser = userRepository.findByEmail("user1@test.com");
        Optional<Room> sampleRoom = roomRepository.findByRoomNumber(101);

        if (sampleUser.isPresent() && sampleRoom.isPresent()) {

            boolean exists = bookingRepository.existsByUserId(sampleUser.get().getId());

            if (!exists) {
                Booking b = new Booking();
                b.setUserId(sampleUser.get().getId());
                b.setRoomId(sampleRoom.get().getId());
                b.setStartDate(LocalDate.now().plusDays(3));
                b.setEndDate(LocalDate.now().plusDays(7));

                bookingRepository.save(b);
                System.out.println("✔ Sample booking created");
            }
        }

        System.out.println("🎉 Database seeding completed");
    }
}
