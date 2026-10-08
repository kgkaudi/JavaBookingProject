package com.kostas.bookingproject.security;

import com.kostas.bookingproject.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtFilterBlacklistTest {

    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final TokenBlacklist blacklist = new TokenBlacklist(60_000);
    private final JwtFilter filter = new JwtFilter(jwtUtil, userRepository, blacklist);

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void blacklistedToken_isNotAuthenticated_butRequestContinues() throws Exception {
        blacklist.blacklist("logged-out-token");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/bookings/me");
        request.setRequestURI("/api/bookings/me");
        request.addHeader("Authorization", "Bearer logged-out-token");
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtUtil, never()).validate(any());
        verify(chain).doFilter(any(), any());
    }

    @Test
    void validToken_notBlacklisted_isAuthenticated() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("k@k.com");
        when(jwtUtil.validate("good-token")).thenReturn(claims);
        when(userRepository.findByEmail("k@k.com")).thenReturn(java.util.Optional.of(
                new com.kostas.bookingproject.models.User("u1", "K", "k@k.com", "ENC", "1",
                        java.util.List.of("ROLE_USER"))));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/bookings/me");
        request.setRequestURI("/api/bookings/me");
        request.addHeader("Authorization", "Bearer good-token");

        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void tokenBlacklist_entriesExpire() throws Exception {
        TokenBlacklist shortLived = new TokenBlacklist(1);
        shortLived.blacklist("t");
        Thread.sleep(10);
        assertFalse(shortLived.isBlacklisted("t"));
    }
}
