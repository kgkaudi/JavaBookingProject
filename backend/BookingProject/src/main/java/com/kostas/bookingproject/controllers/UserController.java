package com.kostas.bookingproject.controllers;

import com.kostas.bookingproject.dto.CreateUserRequest;
import com.kostas.bookingproject.dto.UpdateUserRequest;
import com.kostas.bookingproject.dto.UserResponse;
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.security.CustomUserDetails;
import com.kostas.bookingproject.services.UserService;

import jakarta.validation.Valid;
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
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers().stream().map(UserResponse::from).toList();
    }

    // ---------------------------------------------------------
    // GET USER BY ID (ADMIN)
    // ---------------------------------------------------------
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserById(@PathVariable String userId) {
        return UserResponse.from(userService.getUserById(userId));
    }

    // ---------------------------------------------------------
    // GET AUTHENTICATED USER (SELF)
    // ---------------------------------------------------------
    @GetMapping("/me")
    public UserResponse getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return UserResponse.from(userDetails.getUser());
    }

    // ---------------------------------------------------------
    // CREATE USER (ADMIN)
    // ---------------------------------------------------------
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        User newUser = new User(null, request.name(), request.email(), request.password(),
                request.phone(), request.roles());
        return UserResponse.from(userService.createUser(newUser));
    }

    // ---------------------------------------------------------
    // UPDATE USER PROFILE (ADMIN or SELF) - name/email/phone only
    // ---------------------------------------------------------
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (principal instanceof T(com.kostas.bookingproject.security.CustomUserDetails) and #userId == principal.id)")
    public UserResponse updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {

        return UserResponse.from(userService.updateUser(userId, request));
    }

    // ---------------------------------------------------------
    // DELETE USER (ADMIN)
    // ---------------------------------------------------------
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
    }

    // ---------------------------------------------------------
    // PROMOTE USER TO ADMIN (ADMIN)
    // ---------------------------------------------------------
    @PutMapping("/{userId}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse promoteToAdmin(@PathVariable String userId) {
        return UserResponse.from(userService.promoteToAdmin(userId));
    }

    // ---------------------------------------------------------
    // DEMOTE USER TO USER (ADMIN)
    // ---------------------------------------------------------
    @PutMapping("/{userId}/demote")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse demoteToUser(@PathVariable String userId) {
        return UserResponse.from(userService.demoteToUser(userId));
    }
}
