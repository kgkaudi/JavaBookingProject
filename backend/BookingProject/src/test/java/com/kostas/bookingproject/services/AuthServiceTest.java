package com.kostas.bookingproject.services;

import com.kostas.bookingproject.security.SignupRequest;
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.UserRepository;
import com.kostas.bookingproject.security.AuthRequest;
import com.kostas.bookingproject.security.AuthResponse;
import com.kostas.bookingproject.security.AuthService;
import com.kostas.bookingproject.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private AuthService authService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtUtil = mock(JwtUtil.class);
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil);
    }

    // ---------------------------------------------------------
    // SIGNUP TESTS
    // ---------------------------------------------------------

    @Test
    void signup_success() {
        SignupRequest req = new SignupRequest("Kostas", "k@k.com", "123", "6900000000");

        when(userRepository.existsByEmail("k@k.com")).thenReturn(false);
        when(passwordEncoder.encode("123")).thenReturn("ENC");

        User saved = new User();
        saved.setId("u1");
        saved.setName("Kostas");
        saved.setEmail("k@k.com");
        saved.setPhone("6900000000");
        saved.setPassword("ENC");
        saved.setRoles(List.of("ROLE_USER"));

        when(userRepository.save(any())).thenReturn(saved);
        when(jwtUtil.generateToken("k@k.com", List.of("ROLE_USER")))
                .thenReturn("jwt-token");

        AuthResponse response = authService.signup(req);

        assertEquals("jwt-token", response.getToken());
        assertEquals(List.of("ROLE_USER"), response.getRoles());
    }

    @Test
    void signup_duplicateEmail() {
        when(userRepository.existsByEmail("k@k.com")).thenReturn(true);

        SignupRequest req = new SignupRequest("Kostas", "k@k.com", "123", "6900000000");

        assertThrows(IllegalStateException.class, () ->
                authService.signup(req)
        );
    }

    @Test
    void signup_blankPassword_assignDefault() {
        SignupRequest req = new SignupRequest("Kostas", "new@test.com", "", "6900000000");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("default123")).thenReturn("DEFAULT_ENC");

        User saved = new User();
        saved.setId("u1");
        saved.setEmail("new@test.com");
        saved.setPassword("DEFAULT_ENC");
        saved.setRoles(List.of("ROLE_USER"));

        when(userRepository.save(any())).thenReturn(saved);
        when(jwtUtil.generateToken("new@test.com", List.of("ROLE_USER")))
                .thenReturn("jwt-token");

        AuthResponse response = authService.signup(req);

        assertEquals("jwt-token", response.getToken());
        assertEquals("DEFAULT_ENC", saved.getPassword());
    }

    @Test
    void signup_nullEmail_allowed() {
        SignupRequest req = new SignupRequest("Kostas", null, "123", "6900000000");

        when(userRepository.existsByEmail(null)).thenReturn(false);
        when(passwordEncoder.encode("123")).thenReturn("ENC");

        User saved = new User();
        saved.setId("u1");
        saved.setEmail(null);
        saved.setPassword("ENC");
        saved.setRoles(List.of("ROLE_USER"));

        when(userRepository.save(any())).thenReturn(saved);
        when(jwtUtil.generateToken(null, List.of("ROLE_USER")))
                .thenReturn("jwt-token");

        AuthResponse response = authService.signup(req);

        assertEquals("jwt-token", response.getToken());
        assertEquals(List.of("ROLE_USER"), response.getRoles());
    }

    // ---------------------------------------------------------
    // LOGIN TESTS
    // ---------------------------------------------------------

    @Test
    void login_success() {
        AuthRequest req = new AuthRequest("k@k.com", "123");

        User user = new User();
        user.setId("u1");
        user.setEmail("k@k.com");
        user.setPassword("ENC");
        user.setRoles(List.of("ROLE_ADMIN"));

        when(userRepository.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123", "ENC")).thenReturn(true);
        when(jwtUtil.generateToken("k@k.com", List.of("ROLE_ADMIN")))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login(req);

        assertEquals("jwt-token", response.getToken());
        assertEquals(List.of("ROLE_ADMIN"), response.getRoles());
    }

    @Test
    void login_wrongPassword() {
        AuthRequest req = new AuthRequest("k@k.com", "wrong");

        User user = new User();
        user.setEmail("k@k.com");
        user.setPassword("ENC");
        user.setRoles(List.of("ROLE_USER"));

        when(userRepository.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "ENC")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                authService.login(req)
        );
    }

    @Test
    void login_userNotFound() {
        AuthRequest req = new AuthRequest("missing@test.com", "123");

        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                authService.login(req)
        );
    }
}
