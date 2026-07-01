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

    public List<User> getAllUsers() {
        return users.findAll();
    }

    public User getUserById(String id) {
        return users.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User updateUser(String id, User updated, User currentUser) {
        User existing = users.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = currentUser.getRoles().contains("ROLE_ADMIN");
        boolean isSelf = currentUser.getId().equals(id);

        if (!isSelf && !isAdmin) {
            throw new IllegalStateException("Not authorized to update this user");
        }

        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());

        return users.save(existing);
    }

    public void deleteUser(String id) {
        if (!users.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        users.deleteById(id);
    }

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
