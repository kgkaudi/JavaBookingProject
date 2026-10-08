package com.kostas.bookingproject.repositories;

import com.kostas.bookingproject.models.ResetPasswordToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ResetPasswordTokenRepository extends MongoRepository<ResetPasswordToken, String> {

    /** Looks up a token by the SHA-256 hash that is stored (the raw token is never persisted). */
    Optional<ResetPasswordToken> findByToken(String token);

    void deleteByEmail(String email);
}
