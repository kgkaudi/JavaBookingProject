package com.kostas.bookingproject.security;

import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // Public endpoints that must bypass JWT validation
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/request-reset",
            "/api/auth/reset-password",
            "/api/auth/logout"
    );

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ---------------------------------------------------------
        // Skip JWT filter for public endpoints
        // ---------------------------------------------------------
        if (PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ---------------------------------------------------------
        // Skip if no Authorization header
        // ---------------------------------------------------------
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ---------------------------------------------------------
        // Validate JWT token
        // ---------------------------------------------------------
        String token = header.substring(7);

        try {
            Claims claims = jwtUtil.validate(token);
            String email = claims.getSubject();

            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                CustomUserDetails userDetails = new CustomUserDetails(user);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (Exception e) {
            // Invalid token → clear context but DO NOT block request
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
