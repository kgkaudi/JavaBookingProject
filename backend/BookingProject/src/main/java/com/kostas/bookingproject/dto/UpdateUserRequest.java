package com.kostas.bookingproject.dto;

import jakarta.validation.constraints.Email;

/**
 * Fields a user may change on their own profile. Null means "keep current value".
 * Deliberately has no roles/password/id: role changes go through
 * the admin-only promote/demote endpoints.
 */
public record UpdateUserRequest(
        String name,

        @Email(message = "Email must be a valid address")
        String email,

        String phone) {
}
