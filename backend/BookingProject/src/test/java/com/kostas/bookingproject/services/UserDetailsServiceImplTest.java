package com.kostas.bookingproject.security;

import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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

    @Test
    void loadUser_success() {
        User user = new User();
        user.setEmail("k@k.com");
        user.setPassword("ENC");
        user.setRoles(List.of("USER")); // ✔ FIXED

        when(userRepository.findByEmail("k@k.com")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("k@k.com");

        assertEquals("k@k.com", details.getUsername());
        assertEquals("ENC", details.getPassword());

        // ✔ Ensure authorities contain ROLE_USER
        assertTrue(
                details.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
        );
    }

    @Test
    void loadUser_notFound() {
        when(userRepository.findByEmail("missing@k.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                service.loadUserByUsername("missing@k.com")
        );
    }
}
