package com.kostas.bookingproject.services;

import com.kostas.bookingproject.models.ResetPasswordToken;
import com.kostas.bookingproject.models.User;
import com.kostas.bookingproject.repositories.ResetPasswordTokenRepository;
import com.kostas.bookingproject.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    UserRepository users;
    ResetPasswordTokenRepository tokens;
    PasswordEncoder encoder;
    EmailService email;
    PasswordResetService service;

    User user;

    @BeforeEach
    void setup() {
        users = mock(UserRepository.class);
        tokens = mock(ResetPasswordTokenRepository.class);
        encoder = mock(PasswordEncoder.class);
        email = mock(EmailService.class);
        service = new PasswordResetService(users, tokens, encoder, email);

        user = new User("u1", "K", "k@k.com", "OLD", "1", List.of("ROLE_USER"));
    }

    @Test
    void requestReset_unknownEmail_doesNothingAndDoesNotThrow() {
        when(users.findByEmail("ghost@x.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.requestReset("ghost@x.com"));

        verifyNoInteractions(email);
        verify(tokens, never()).save(any());
    }

    @Test
    void requestReset_knownEmail_storesOnlyHashOfToken_andEmailsRawToken() {
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));

        service.requestReset("k@k.com");

        verify(tokens).deleteByEmail("k@k.com"); // older links are invalidated

        ArgumentCaptor<ResetPasswordToken> saved = ArgumentCaptor.forClass(ResetPasswordToken.class);
        verify(tokens).save(saved.capture());
        ArgumentCaptor<String> rawToken = ArgumentCaptor.forClass(String.class);
        verify(email).sendResetEmail(eq("k@k.com"), rawToken.capture());

        assertNotEquals(rawToken.getValue(), saved.getValue().getToken());
        assertEquals(64, saved.getValue().getToken().length()); // SHA-256 hex
        assertTrue(saved.getValue().getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void confirmReset_unknownToken_rejected() {
        when(tokens.findByToken(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.confirmReset("nope", "NewPassw0rd"));
        verify(users, never()).save(any());
    }

    @Test
    void confirmReset_expiredToken_deletedAndRejected() {
        ResetPasswordToken expired = new ResetPasswordToken("k@k.com", "h", LocalDateTime.now().minusMinutes(1));
        when(tokens.findByToken(any())).thenReturn(Optional.of(expired));

        assertThrows(IllegalArgumentException.class, () -> service.confirmReset("t", "NewPassw0rd"));

        verify(tokens).delete(expired);
        verify(users, never()).save(any());
    }

    @Test
    void confirmReset_validToken_changesPassword_andConsumesAllTokens() {
        ResetPasswordToken valid = new ResetPasswordToken("k@k.com", "h", LocalDateTime.now().plusMinutes(5));
        when(tokens.findByToken(any())).thenReturn(Optional.of(valid));
        when(users.findByEmail("k@k.com")).thenReturn(Optional.of(user));
        when(encoder.encode("NewPassw0rd")).thenReturn("NEW_ENC");

        service.confirmReset("t", "NewPassw0rd");

        assertEquals("NEW_ENC", user.getPassword());
        verify(users).save(user);
        verify(tokens).deleteByEmail("k@k.com");
    }
}
