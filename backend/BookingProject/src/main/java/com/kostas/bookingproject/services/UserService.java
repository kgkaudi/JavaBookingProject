package com.kostas.bookingproject.services;

import com.kostas.bookingproject.exceptions.ResourceNotFoundException;
import com.kostas.bookingproject.dto.UpdateUserRequest;
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ---------------------------------------------------------
    // GET USER BY EMAIL
    // ---------------------------------------------------------
    public User getUserByEmail(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ---------------------------------------------------------
    // CREATE USER (ADMIN)
    // ---------------------------------------------------------
    public User createUser(User newUser) {

        newUser.setEmail(normalize(newUser.getEmail()));

        if (users.findByEmail(newUser.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (newUser.getPassword() == null || newUser.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        if (newUser.getRoles() == null || newUser.getRoles().isEmpty()) {
            newUser.setRoles(List.of("ROLE_USER"));
        }

        return users.save(newUser);
    }

    // ---------------------------------------------------------
    // UPDATE USER (profile fields only - roles/password are never touched)
    // ---------------------------------------------------------
    public User updateUser(String userId, UpdateUserRequest request) {
        User existingUser = users.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.name() != null) {
            existingUser.setName(request.name());
        }

        String newEmail = normalize(request.email());
        if (newEmail != null && !newEmail.equals(existingUser.getEmail())) {
            users.findByEmail(newEmail)
                    .filter(other -> !other.getId().equals(existingUser.getId()))
                    .ifPresent(other -> {
                        throw new IllegalArgumentException("Email already exists");
                    });
            existingUser.setEmail(newEmail);
        }

        if (request.phone() != null) {
            existingUser.setPhone(request.phone());
        }

        return users.save(existingUser);
    }

    // ---------------------------------------------------------
    // DELETE USER
    // ---------------------------------------------------------
    public void deleteUser(String id) {
        if (!users.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
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

    private static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
