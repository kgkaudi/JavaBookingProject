package com.kostas.bookingproject.services;

import com.kostas.bookingproject.models.ResetPasswordToken;
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.ResetPasswordTokenRepository;
import com.kostas.bookingproject.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

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

    public void requestReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        String token = UUID.randomUUID().toString();

        ResetPasswordToken resetToken = new ResetPasswordToken(
                email,
                token,
                LocalDateTime.now().plusMinutes(30)
        );

        tokenRepository.save(resetToken);

        emailService.sendResetEmail(email, token);
    }

    public void confirmReset(String token, String newPassword) {
        ResetPasswordToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }
}
