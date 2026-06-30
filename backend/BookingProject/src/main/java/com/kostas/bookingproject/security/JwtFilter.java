package com.kostas.bookingproject.security;

import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.UserRepository;
import com.kostas.bookingproject.security.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final TokenBlacklist tokenBlacklist;

    public JwtFilter(JwtUtil jwtUtil,
            UserRepository userRepository,
            TokenBlacklist tokenBlacklist) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // Skip JWT validation for public endpoints
        if (path.startsWith("/api/auth/")
                || path.startsWith("/api/rooms")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // No token → continue
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // Reject blacklisted tokens
        if (tokenBlacklist.isBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            // Validate token and extract claims
            var claims = jwtUtil.validate(token);
            String userId = claims.getSubject();

            if (userId == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Load user from DB
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Map roles to Spring Security authorities
            var authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();

            // Create authentication object
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    authorities);

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set authentication in context
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            // Invalid token → ignore and continue
        }

        filterChain.doFilter(request, response);
    }
}
