package com.kostas.bookingproject.services;

import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------------------------------------------------------
    // GET ALL USERS
    // ---------------------------------------------------------
    public List<User> getAllUsers() {
        return users.findAll();
    }

    // ---------------------------------------------------------
    // GET USER BY ID
    // ---------------------------------------------------------
    public User getUserById(String id) {
        return users.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    // ---------------------------------------------------------
    // CREATE USER (ADMIN)
    // ---------------------------------------------------------
    public User createUser(User newUser) {

        // Validate email uniqueness
        if (users.findByEmail(newUser.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Encode password or set default
        if (newUser.getPassword() == null || newUser.getPassword().isBlank()) {
            newUser.setPassword(passwordEncoder.encode("default123"));
        } else {
            newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        }

        // Ensure roles exist
        if (newUser.getRoles() == null || newUser.getRoles().isEmpty()) {
            newUser.setRoles(List.of("ROLE_USER"));
        }

        return users.save(newUser);
    }

    // ---------------------------------------------------------
    // UPDATE USER
    // ---------------------------------------------------------
    public User updateUser(String userId, User updatedUser) {
        User existingUser = users.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone(updatedUser.getPhone());

        // FIXED ROLE HANDLING
        if (updatedUser.getRoles() != null && !updatedUser.getRoles().isEmpty()) {
            existingUser.setRoles(updatedUser.getRoles());
        } else if (updatedUser.getRole() != null && !updatedUser.getRole().isBlank()) {
            existingUser.setRoles(List.of(updatedUser.getRole()));
        }

        return users.save(existingUser);
    }

    // ---------------------------------------------------------
    // DELETE USER
    // ---------------------------------------------------------
    public void deleteUser(String id) {
        if (!users.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        users.deleteById(id);
    }

    // ---------------------------------------------------------
    // PROMOTE / DEMOTE
    // ---------------------------------------------------------
    public User promoteToAdmin(String id) {
        User user = getUserById(id);
        user.setRoles(List.of("ROLE_ADMIN"));
        return users.save(user);
    }

    public User demoteToUser(String id) {
        User user = getUserById(id);
        user.setRoles(List.of("ROLE_USER"));
        return users.save(user);
    }
}
