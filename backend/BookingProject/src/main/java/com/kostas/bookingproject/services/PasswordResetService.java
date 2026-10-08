package com.kostas.bookingproject.services;

import com.kostas.bookingproject.models.ResetPasswordToken;
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.ResetPasswordTokenRepository;
import com.kostas.bookingproject.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final String INVALID_TOKEN_MESSAGE = "Invalid or expired token";

    private final UserRepository userRepository;
    private final ResetPasswordTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(UserRepository userRepository,
                                ResetPasswordTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Always returns normally, whether or not the email is registered, so the endpoint
     * cannot be used to discover which addresses have accounts.
     */
    public void requestReset(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return;
        }

        // only the newest link is valid
        tokenRepository.deleteByEmail(email);

        String rawToken = UUID.randomUUID().toString();

        tokenRepository.save(new ResetPasswordToken(
                email,
                sha256(rawToken),
                LocalDateTime.now().plusMinutes(30)));

        emailService.sendResetEmail(email, rawToken);
    }

    public void confirmReset(String token, String newPassword) {
        ResetPasswordToken resetToken = tokenRepository.findByToken(sha256(token))
                .orElseThrow(() -> new IllegalArgumentException(INVALID_TOKEN_MESSAGE));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException(INVALID_TOKEN_MESSAGE);
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(INVALID_TOKEN_MESSAGE));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // single use: remove this and any other outstanding token for the account
        tokenRepository.deleteByEmail(resetToken.getEmail());
    }

    private static String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
