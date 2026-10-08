package com.kostas.bookingproject.migrations;

import com.kostas.bookingproject.models.Booking;
import com.kostas.bookingproject.models.Room;
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.BookingRepository;
import com.kostas.bookingproject.repositories.RoomRepository;
import com.kostas.bookingproject.repositories.UserRepository;
import com.kostas.bookingproject.security.PasswordEncoderService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Demo data seeder. Disabled unless app.seed.enabled=true (env SEED_ENABLED=true).
 * The admin password must be supplied via app.seed.admin-password (env SEED_ADMIN_PASSWORD);
 * there is no built-in default so no well-known credentials ever exist by accident.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DatabaseSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoderService passwordEncoder;
    private final String adminPassword;
    private final String testUserPassword;

    public DatabaseSeeder(
            UserRepository userRepository,
            RoomRepository roomRepository,
            BookingRepository bookingRepository,
            PasswordEncoderService passwordEncoder,
            @Value("${app.seed.admin-password:}") String adminPassword,
            @Value("${app.seed.test-user-password:}") String testUserPassword) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.testUserPassword = testUserPassword;
    }

    @Override
    public void run(ApplicationArguments args) {

        if (adminPassword.isBlank() || testUserPassword.isBlank()) {
            throw new IllegalStateException(
                    "Seeding is enabled but SEED_ADMIN_PASSWORD / SEED_TEST_USER_PASSWORD are not set");
        }

        System.out.println("Running Database Seeder...");

        // ---------------------------------------------------
        // 1. ADMIN USER
        // ---------------------------------------------------
        if (!userRepository.existsByEmail("admin@booking.com")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@booking.com");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRoles(List.of("ROLE_ADMIN"));
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
            u1.setPassword(passwordEncoder.encode(testUserPassword));
            u1.setRoles(List.of("ROLE_USER"));
            userRepository.save(u1);
            System.out.println("✔ Test User 1 created");
        }

        if (!userRepository.existsByEmail("user2@test.com")) {
            User u2 = new User();
            u2.setName("Test User 2");
            u2.setEmail("user2@test.com");
            u2.setPassword(passwordEncoder.encode(testUserPassword));
            u2.setRoles(List.of("ROLE_USER"));
            userRepository.save(u2);
            System.out.println("✔ Test User 2 created");
        }

        // ---------------------------------------------------
        // 3. ROOMS
        // ---------------------------------------------------
        if (roomRepository.count() == 0) {
            Room r1 = new Room(null, 101, "single", 1, 50.0, true);
            Room r2 = new Room(null, 102, "double", 2, 80.0, true);
            Room r3 = new Room(null, 201, "suite", 4, 120.0, true);

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
                b.setStatus("confirmed");

                bookingRepository.save(b);
                System.out.println("✔ Sample booking created");
            }
        }

        System.out.println("Database seeding completed");
    }
}
