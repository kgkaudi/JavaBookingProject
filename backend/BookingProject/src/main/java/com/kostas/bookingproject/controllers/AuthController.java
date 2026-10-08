package com.kostas.bookingproject.controllers;

import com.kostas.bookingproject.security.AuthResponse;
import com.kostas.bookingproject.security.SignupRequest;
import com.kostas.bookingproject.security.AuthService;
import com.kostas.bookingproject.security.AuthRequest;
import com.kostas.bookingproject.security.TokenBlacklist;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Errors are translated centrally by GlobalExceptionHandler
 * (validation -> 400, duplicate email -> 409, bad credentials -> 400, ...).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklist tokenBlacklist;

    public AuthController(AuthService authService, TokenBlacklist tokenBlacklist) {
        this.authService = authService;
        this.tokenBlacklist = tokenBlacklist;
    }

    // ---------------------------------------------------------
    // SIGNUP
    // ---------------------------------------------------------
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ---------------------------------------------------------
    // LOGOUT (JWT BLACKLIST)
    // ---------------------------------------------------------
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("No token provided");
        }

        String token = authHeader.substring(7);
        tokenBlacklist.blacklist(token);

        return ResponseEntity.ok("Logged out successfully");
    }
}
