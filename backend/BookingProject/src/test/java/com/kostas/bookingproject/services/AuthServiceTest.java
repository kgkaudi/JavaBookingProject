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

    @Test
    void signup_success() {
        SignupRequest req = new SignupRequest("Kostas", "k@k.com", "123", "6900000000");

        when(userRepository.existsByEmail("k@k.com")).thenReturn(false);
        when(passwordEncoder.encode("123")).thenReturn("ENC");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = authService.signup(req);

        assertEquals("ENC", result.getPassword());
        assertTrue(result.getRoles().contains("ROLE_USER"));
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
    void login_success() {
        AuthRequest req = new AuthRequest("k@k.com", "123");

        User user = new User();
        user.setPassword("ENC");
        user.setId("u1");
        user.setRoles(List.of("USER"));

        when(userRepository.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123", "ENC")).thenReturn(true);
        when(jwtUtil.generateToken("u1", List.of("USER"))).thenReturn("TOKEN");

        AuthResponse res = authService.login(req);

        assertEquals("TOKEN", res.getToken());
        assertEquals(List.of("USER"), res.getRoles());
    }

    @Test
    void login_wrongPassword() {
        AuthRequest req = new AuthRequest("k@k.com", "wrong");

        User user = new User();
        user.setPassword("ENC");
        user.setRoles(List.of("USER"));

        when(userRepository.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "ENC")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                authService.login(req)
        );
    }
}
