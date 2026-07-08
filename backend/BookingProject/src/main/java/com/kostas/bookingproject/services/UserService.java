package com.kostas.bookingproject.services;

import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
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
    // UPDATE USER (INCLUDING ROLE)
    // ---------------------------------------------------------
    public User updateUser(String userId, User updatedUser) {
        User existingUser = users.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone(updatedUser.getPhone());

        // ✅ Handle single role or list
        if (updatedUser.getRole() != null) {
            existingUser.setRoles(List.of(updatedUser.getRole()));
        } else if (updatedUser.getRoles() != null && !updatedUser.getRoles().isEmpty()) {
            existingUser.setRoles(updatedUser.getRoles());
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
        User user = users.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setRoles(List.of("ROLE_ADMIN"));
        return users.save(user);
    }

    public User demoteToUser(String id) {
        User user = users.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setRoles(List.of("ROLE_USER"));
        return users.save(user);
    }
}
