package com.kostas.bookingproject.integration;

import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class UserProfileController {

    private final UserRepository userRepository;

    public UserProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ---------------------------------------------------------
    // GET CURRENT USER
    // ---------------------------------------------------------
    @GetMapping
    public User getCurrentUser(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Unauthorized");
        }

        String email = principal.getUsername();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    // ---------------------------------------------------------
    // UPDATE CURRENT USER
    // ---------------------------------------------------------
    @PutMapping
    public User updateCurrentUser(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody User updated
    ) {
        if (principal == null) {
            throw new IllegalArgumentException("Unauthorized");
        }

        String email = principal.getUsername();

        User existing = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validation
        if (updated == null) {
            throw new IllegalArgumentException("Invalid JSON");
        }
        if (updated.getName() == null || updated.getEmail() == null) {
            throw new IllegalArgumentException("Missing required fields");
        }
        if (!updated.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Duplicate email check
        userRepository.findByEmail(updated.getEmail())
                .filter(u -> !u.getId().equals(existing.getId()))
                .ifPresent(u -> {
                    throw new IllegalArgumentException("Email already exists");
                });

        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());

        return userRepository.save(existing);
    }

    // ---------------------------------------------------------
    // DELETE CURRENT USER
    // ---------------------------------------------------------
    @DeleteMapping
    public void deleteCurrentUser(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Unauthorized");
        }

        String email = principal.getUsername();

        User existing = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userRepository.deleteById(existing.getId());
    }
}
