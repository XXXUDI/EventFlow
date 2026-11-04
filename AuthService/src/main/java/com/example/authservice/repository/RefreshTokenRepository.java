package com.example.authservice.repository;

import com.example.authservice.entity.RefreshToken;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Mono;

public interface RefreshTokenRepository extends R2dbcRepository<RefreshToken, Integer> {

    @Query("""
            DELETE FROM refresh_tokens
            WHERE token_hash = :tokenHash AND expires_at > CURRENT_TIMESTAMP
            RETURNING id, user_id, token_hash, device, created_at, expires_at
            """)
    Mono<RefreshToken> consumeValidToken(String tokenHash);
}
