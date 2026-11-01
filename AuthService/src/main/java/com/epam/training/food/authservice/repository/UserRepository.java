package com.epam.training.food.authservice.repository;

import com.epam.training.food.authservice.entity.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface UserRepository extends R2dbcRepository<User, Integer> {

}
