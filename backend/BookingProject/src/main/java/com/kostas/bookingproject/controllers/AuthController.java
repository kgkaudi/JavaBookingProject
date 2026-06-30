package com.kostas.bookingproject.controllers;

import com.kostas.bookingproject.auth.AuthRequest;
import com.kostas.bookingproject.auth.AuthResponse;
import com.kostas.bookingproject.auth.SignupRequest;
import com.kostas.bookingproject.auth.AuthService;
import com.kostas.bookingproject.security.TokenBlacklist;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            return ResponseEntity.ok(authService.signup(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // LOGOUT (JWT BLACKLIST)
    // ---------------------------------------------------------

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("No token provided");
        }

        String token = authHeader.substring(7);
        tokenBlacklist.blacklist(token);

        return ResponseEntity.ok("Logged out successfully");
    }
}
