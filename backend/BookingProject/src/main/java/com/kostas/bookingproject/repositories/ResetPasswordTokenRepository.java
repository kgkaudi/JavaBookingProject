package com.kostas.bookingproject.repositories;

import com.kostas.bookingproject.models.ResetPasswordToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ResetPasswordTokenRepository extends MongoRepository<ResetPasswordToken, String> {
    Optional<ResetPasswordToken> findByToken(String token);
}
