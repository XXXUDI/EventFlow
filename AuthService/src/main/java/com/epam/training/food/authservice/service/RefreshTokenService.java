package com.epam.training.food.authservice.service;

import com.epam.training.food.authservice.entity.RefreshToken;
import com.epam.training.food.authservice.entity.User;
import com.epam.training.food.authservice.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private static final int REFRESH_TOKEN_BYTES = 32;
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 30;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public Mono<String> createFor(User user) {
        byte[] tokenBytes = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        LocalDateTime now = LocalDateTime.now();
        RefreshToken refreshToken = new RefreshToken(null, user.getId(), sha256(rawToken), null, now,
                now.plus(REFRESH_TOKEN_VALIDITY_DAYS, ChronoUnit.DAYS));
        return refreshTokenRepository.save(refreshToken).thenReturn(rawToken);
    }

    /**
     * Atomically consumes a non-expired token. A consumed token cannot be used again.
     */
    public Mono<RefreshToken> consume(String rawToken) {
        return refreshTokenRepository.consumeValidToken(sha256(rawToken));
    }

    private String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
