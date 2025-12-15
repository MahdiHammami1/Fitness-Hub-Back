package com.example.demo.repository;

import com.example.demo.model.VerificationToken;
import com.example.demo.model.TokenType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends MongoRepository<VerificationToken, String> {
    Optional<VerificationToken> findByCodeAndType(String code, TokenType type);
    Optional<VerificationToken> findByUserIdAndType(String userId, TokenType type);
}

