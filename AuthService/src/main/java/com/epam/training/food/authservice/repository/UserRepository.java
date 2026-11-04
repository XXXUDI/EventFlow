package com.epam.training.food.authservice.repository;

import com.epam.training.food.authservice.entity.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends R2dbcRepository<User, Integer> {
    Mono<User> findByUsername(String username);
}
