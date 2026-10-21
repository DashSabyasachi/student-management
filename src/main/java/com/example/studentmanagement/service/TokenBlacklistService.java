package com.example.studentmanagement.service;

import com.example.studentmanagement.model.BlacklistedToken;
import com.example.studentmanagement.repository.BlacklistedTokenRepository;
import org.springframework.stereotype.Service;

/** The token denylist used for logout. */
@Service
public class TokenBlacklistService {

    private final BlacklistedTokenRepository repository;

    public TokenBlacklistService(BlacklistedTokenRepository repository) {
        this.repository = repository;
    }

    public void blacklist(String token) {
        if (!repository.existsByToken(token)) {
            repository.save(new BlacklistedToken(token));
        }
    }

    public boolean isBlacklisted(String token) {
        return repository.existsByToken(token);
    }
}
