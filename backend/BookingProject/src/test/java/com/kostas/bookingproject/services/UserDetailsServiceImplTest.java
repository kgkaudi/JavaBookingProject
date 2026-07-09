package com.kostas.bookingproject.services;

import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.UserRepository;
import com.kostas.bookingproject.security.UserDetailsServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsServiceImplTest {

    private UserRepository userRepository;
    private UserDetailsServiceImpl service;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        service = new UserDetailsServiceImpl(userRepository);
    }

    // ---------------------------------------------------------
    // LOAD USER SUCCESS
    // ---------------------------------------------------------
    @Test
    void loadUser_success() {
        User user = new User();
        user.setId("u1");
        user.setEmail("k@k.com");
        user.setPassword("ENC");
        user.setRoles(List.of("ROLE_USER", "ROLE_ADMIN"));

        when(userRepository.findByEmail("k@k.com"))
                .thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("k@k.com");

        assertEquals("k@k.com", details.getUsername());
        assertEquals("ENC", details.getPassword());

        // Authorities must contain both roles
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    // ---------------------------------------------------------
    // LOAD USER NOT FOUND
    // ---------------------------------------------------------
    @Test
    void loadUser_notFound() {
        when(userRepository.findByEmail("missing@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing@test.com"));
    }
}
