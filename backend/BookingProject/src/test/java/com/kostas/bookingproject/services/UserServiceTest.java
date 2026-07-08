package com.kostas.bookingproject.services;

import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    User user;
    User admin;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);

        user = new User("u1", "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER"));
        admin = new User("a1", "Admin", "admin@test.com", "ENC", "6900000000", List.of("ROLE_ADMIN"));
    }

    // ---------------------------------------------------------
    // GET ALL USERS
    // ---------------------------------------------------------

    @Test
    void getAllUsers_success() {
        when(userRepository.findAll()).thenReturn(List.of(user, admin));

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
    }

    // ---------------------------------------------------------
    // GET USER BY ID
    // ---------------------------------------------------------

    @Test
    void getUserById_success() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        User result = userService.getUserById("u1");

        assertEquals("u1", result.getId());
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById("u1"));
    }

    // ---------------------------------------------------------
    // UPDATE USER
    // ---------------------------------------------------------

    @Test
    void updateUser_success() {
        User updated = new User();
        updated.setName("New Name");
        updated.setEmail("new@test.com");
        updated.setPhone("6999999999");
        updated.setRole("ROLE_ADMIN");

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUser("u1", updated);

        assertEquals("New Name", result.getName());
        assertEquals("new@test.com", result.getEmail());
        assertEquals("6999999999", result.getPhone());
        assertEquals(List.of("ROLE_ADMIN"), result.getRoles());
    }

    @Test
    void updateUser_notFound() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.updateUser("u1", new User()));
    }

    // ---------------------------------------------------------
    // DELETE USER
    // ---------------------------------------------------------

    @Test
    void deleteUser_success() {
        when(userRepository.existsById("u1")).thenReturn(true);

        userService.deleteUser("u1");

        verify(userRepository).deleteById("u1");
    }

    @Test
    void deleteUser_notFound() {
        when(userRepository.existsById("u1")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser("u1"));
    }

    // ---------------------------------------------------------
    // PROMOTE / DEMOTE
    // ---------------------------------------------------------

    @Test
    void promoteToAdmin_success() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.promoteToAdmin("u1");

        assertEquals(List.of("ROLE_ADMIN"), result.getRoles());
    }

    @Test
    void promoteToAdmin_notFound() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.promoteToAdmin("u1"));
    }

    @Test
    void demoteToUser_success() {
        admin.setRoles(List.of("ROLE_ADMIN"));

        when(userRepository.findById("a1")).thenReturn(Optional.of(admin));
        when(userRepository.save(any(User.class))).thenReturn(admin);

        User result = userService.demoteToUser("a1");

        assertEquals(List.of("ROLE_USER"), result.getRoles());
    }

    @Test
    void demoteToUser_notFound() {
        when(userRepository.findById("a1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.demoteToUser("a1"));
    }
}
