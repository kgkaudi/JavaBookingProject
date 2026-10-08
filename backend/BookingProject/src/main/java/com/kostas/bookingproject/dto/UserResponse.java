package com.kostas.bookingproject.dto;

import com.kostas.bookingproject.models.User;

import java.util.List;

/**
 * Public view of a user. Never includes the password hash.
 */
public record UserResponse(
        String id,
        String name,
        String email,
        String phone,
        List<String> roles) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRoles() == null ? List.of() : List.copyOf(user.getRoles()));
    }
}
