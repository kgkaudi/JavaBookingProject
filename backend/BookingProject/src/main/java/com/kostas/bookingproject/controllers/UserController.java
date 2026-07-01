package com.kostas.bookingproject.controllers;

import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.services.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ---------------------------------------------------------
    // GET ALL USERS (ADMIN)
    // ---------------------------------------------------------
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // ---------------------------------------------------------
    // GET USER BY ID (ADMIN)
    // ---------------------------------------------------------
    @GetMapping("/{userId}")
    public User getUserById(@PathVariable String userId) {
        return userService.getUserById(userId);
    }

    // ---------------------------------------------------------
    // GET AUTHENTICATED USER (SELF)
    // ---------------------------------------------------------
    @GetMapping("/me")
    public User getMe(@AuthenticationPrincipal User user) {
        return user;
    }

    // ---------------------------------------------------------
    // UPDATE USER (ADMIN or SELF)
    // ---------------------------------------------------------
    @PutMapping("/{userId}")
    public User updateUser(
            @PathVariable String userId,
            @RequestBody User updatedUser,
            @AuthenticationPrincipal User currentUser) {
        return userService.updateUser(userId, updatedUser, currentUser);
    }

    // ---------------------------------------------------------
    // DELETE USER (ADMIN)
    // ---------------------------------------------------------
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
    }

    // ---------------------------------------------------------
    // PROMOTE USER TO ADMIN (ADMIN)
    // ---------------------------------------------------------
    @PutMapping("/{userId}/promote")
    public User promoteToAdmin(@PathVariable String userId) {
        return userService.promoteToAdmin(userId);
    }

    // ---------------------------------------------------------
    // DEMOTE USER TO USER (ADMIN)
    // ---------------------------------------------------------
    @PutMapping("/{userId}/demote")
    public User demoteToUser(@PathVariable String userId) {
        return userService.demoteToUser(userId);
    }
}
