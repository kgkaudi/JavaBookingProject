package com.kostas.bookingproject.services;

import static org.mockito.ArgumentMatchers.any;
import com.kostas.bookingproject.dto.UpdateUserRequest;
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository users;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    User user;
    User admin;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        users = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        userService = new UserService(users, passwordEncoder);

        user = new User("u1", "Kostas", "k@k.com", "ENC", "6900000000", List.of("ROLE_USER"));
        admin = new User("a1", "Admin", "admin@test.com", "ENC", "6900000000", List.of("ROLE_ADMIN"));
    }

    // ---------------------------------------------------------
    // CREATE USER
    // ---------------------------------------------------------

    @Test
    void createUser_success_withPassword() {
        User newUser = new User();
        newUser.setEmail("new@test.com");
        newUser.setPassword("plain123");
        newUser.setRoles(List.of("ROLE_USER"));

        when(users.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain123")).thenReturn("ENCODED");
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser(newUser);

        assertEquals("ENCODED", result.getPassword());
        assertEquals(List.of("ROLE_USER"), result.getRoles());
    }

    @Test
    void createUser_blankPassword_rejected() {
        User newUser = new User();
        newUser.setEmail("new@test.com");
        newUser.setPassword(""); // blank

        when(users.findByEmail("new@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(newUser));
        verify(users, never()).save(any());
    }

    @Test
    void createUser_emailAlreadyExists() {
        User newUser = new User();
        newUser.setEmail("k@k.com");

        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(newUser));
    }

    @Test
    void createUser_nullPassword_rejected() {
        User newUser = new User();
        newUser.setEmail("new@test.com");
        newUser.setPassword(null);

        when(users.findByEmail("new@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(newUser));
    }

    @Test
    void createUser_nullRoles_assignDefault() {
        User newUser = new User();
        newUser.setEmail("new@test.com");
        newUser.setPassword("plain123");
        newUser.setRoles(null);

        when(users.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain123")).thenReturn("ENCODED");
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser(newUser);

        assertEquals(List.of("ROLE_USER"), result.getRoles());
    }

    // ---------------------------------------------------------
    // GET ALL USERS
    // ---------------------------------------------------------

    @Test
    void getAllUsers_success() {
        when(users.findAll()).thenReturn(List.of(user, admin));

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
    }

    // ---------------------------------------------------------
    // GET USER BY ID
    // ---------------------------------------------------------

    @Test
    void getUserById_success() {
        when(users.findById("u1")).thenReturn(Optional.of(user));

        User result = userService.getUserById("u1");

        assertEquals("u1", result.getId());
    }

    @Test
    void getUserById_notFound() {
        when(users.findById("u1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById("u1"));
    }

    // ---------------------------------------------------------
    // GET USER BY EMAIL
    // ---------------------------------------------------------

    @Test
    void getUserByEmail_success() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail("k@k.com");

        assertEquals("k@k.com", result.getEmail());
    }

    @Test
    void getUserByEmail_notFound() {
        when(users.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserByEmail("missing@test.com"));
    }

    // ---------------------------------------------------------
    // UPDATE USER
    // ---------------------------------------------------------

    @Test
    void updateUser_success_profileFields() {
        UpdateUserRequest request = new UpdateUserRequest("New Name", "new@test.com", "6999999999");

        when(users.findById("u1")).thenReturn(Optional.of(user));
        when(users.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser("u1", request);

        assertEquals("New Name", result.getName());
        assertEquals("new@test.com", result.getEmail());
        assertEquals("6999999999", result.getPhone());
    }

    @Test
    void updateUser_neverChangesRolesOrPassword() {
        List<String> rolesBefore = List.copyOf(user.getRoles());
        String passwordBefore = user.getPassword();

        when(users.findById("u1")).thenReturn(Optional.of(user));
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser("u1", new UpdateUserRequest("X", null, null));

        assertEquals(rolesBefore, result.getRoles());
        assertEquals(passwordBefore, result.getPassword());
    }

    @Test
    void updateUser_emailTakenByAnotherUser_rejected() {
        when(users.findById("u1")).thenReturn(Optional.of(user));
        when(users.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser("u1", new UpdateUserRequest(null, "admin@test.com", null)));
        verify(users, never()).save(any());
    }

    @Test
    void updateUser_notFound() {
        when(users.findById("u1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser("u1", new UpdateUserRequest(null, null, null)));
    }

    // ---------------------------------------------------------
    // DELETE USER
    // ---------------------------------------------------------

    @Test
    void deleteUser_success() {
        when(users.existsById("u1")).thenReturn(true);

        userService.deleteUser("u1");

        verify(users).deleteById("u1");
    }

    @Test
    void deleteUser_notFound() {
        when(users.existsById("u1")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser("u1"));
    }

    // ---------------------------------------------------------
    // PROMOTE / DEMOTE
    // ---------------------------------------------------------

    @Test
    void promoteToAdmin_success() {
        when(users.findById("u1")).thenReturn(Optional.of(user));
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.promoteToAdmin("u1");

        assertEquals(List.of("ROLE_ADMIN"), result.getRoles());
    }

    @Test
    void promoteToAdmin_notFound() {
        when(users.findById("u1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.promoteToAdmin("u1"));
    }

    @Test
    void demoteToUser_success() {
        admin.setRoles(List.of("ROLE_ADMIN"));

        when(users.findById("a1")).thenReturn(Optional.of(admin));
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.demoteToUser("a1");

        assertEquals(List.of("ROLE_USER"), result.getRoles());
    }

    @Test
    void demoteToUser_notFound() {
        when(users.findById("a1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.demoteToUser("a1"));
    }

    // ---------------------------------------------------------
    // EMAIL NORMALISATION
    // ---------------------------------------------------------
    @Test
    void createUser_emailIsTrimmedAndLowerCased() {
        User newUser = new User();
        newUser.setEmail("  New@Test.COM ");
        newUser.setPassword("plain123");

        when(users.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain123")).thenReturn("ENCODED");
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser(newUser);

        assertEquals("new@test.com", result.getEmail());
    }
}
