package com.epam.training.food.authservice.repository;

import com.epam.training.food.authservice.entity.RefreshToken;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface RefreshTokenRepository extends R2dbcRepository<RefreshToken, Integer> {
}
