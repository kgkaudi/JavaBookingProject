package com.kostas.bookingproject.services;

import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ---------------------------------------------------------
    // GET ALL USERS (ADMIN)
    // ---------------------------------------------------------
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ---------------------------------------------------------
    // GET USER BY ID
    // ---------------------------------------------------------
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    // ---------------------------------------------------------
    // UPDATE USER (ADMIN or SELF)
    // ---------------------------------------------------------
    public User updateUser(String userId, User updatedUser, User currentUser) {

        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Only admin or the user themselves can update
        if (!currentUser.getId().equals(userId) &&
                !currentUser.getRoles().contains("ADMIN")) {   // ✔ FIXED
            throw new IllegalStateException("Not authorized to update this user");
        }

        existing.setName(updatedUser.getName());
        existing.setEmail(updatedUser.getEmail());
        existing.setPhone(updatedUser.getPhone());

        return userRepository.save(existing);
    }

    // ---------------------------------------------------------
    // DELETE USER (ADMIN)
    // ---------------------------------------------------------
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(userId);
    }

    // ---------------------------------------------------------
    // PROMOTE USER TO ADMIN
    // ---------------------------------------------------------
    public User promoteToAdmin(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setRoles(List.of("ADMIN"));   // ✔ FIXED
        return userRepository.save(user);
    }

    // ---------------------------------------------------------
    // DEMOTE USER TO USER
    // ---------------------------------------------------------
    public User demoteToUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setRoles(List.of("USER"));    // ✔ FIXED
        return userRepository.save(user);
    }
}
