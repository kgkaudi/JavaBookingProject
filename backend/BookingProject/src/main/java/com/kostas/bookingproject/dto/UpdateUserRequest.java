package com.kostas.bookingproject.dto;

/**
 * Fields a user may change on their own profile.
 * Deliberately has no roles/password/id: role changes go through
 * the admin-only promote/demote endpoints.
 */
public record UpdateUserRequest(
        String name,
        String email,
        String phone) {
}
