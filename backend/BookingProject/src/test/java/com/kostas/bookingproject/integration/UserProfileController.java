package com.kostas.bookingproject.integration;

import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public User getCurrentUser(@AuthenticationPrincipal String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    // ---------------------------------------------------------
    // UPDATE CURRENT USER
    // ---------------------------------------------------------
    @PutMapping
    public User updateCurrentUser(
            @AuthenticationPrincipal String userId,
            @RequestBody User updated
    ) {
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (updated.getName() == null || updated.getEmail() == null) {
            throw new IllegalArgumentException("Missing required fields");
        }

        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());

        return userRepository.save(existing);
    }

    // ---------------------------------------------------------
    // DELETE CURRENT USER
    // ---------------------------------------------------------
    @DeleteMapping
    public void deleteCurrentUser(@AuthenticationPrincipal String userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(userId);
    }
}
