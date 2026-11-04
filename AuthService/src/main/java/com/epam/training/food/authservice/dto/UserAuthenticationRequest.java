package com.epam.training.food.authservice.dto;

public record UserAuthenticationRequest(
        String username,
        String password
) {
}
