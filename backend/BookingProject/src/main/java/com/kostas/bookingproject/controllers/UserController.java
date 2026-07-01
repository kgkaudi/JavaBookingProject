package com.kostas.bookingproject.controllers;

import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.security.CustomUserDetails;
import com.kostas.bookingproject.services.UserService;

import org.springframework.security.access.prepost.PreAuthorize;
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
    public CustomUserDetails getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userDetails;
    }

    // ---------------------------------------------------------
    // UPDATE USER (ADMIN or SELF)
    // ---------------------------------------------------------
    @PutMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public User updateUser(
            @PathVariable String userId,
            @RequestBody User updatedUser,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return userService.updateUser(userId, updatedUser, currentUser.getUser());
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
