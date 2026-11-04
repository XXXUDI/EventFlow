package com.epam.training.food.authservice.dto;

public record UserAuthenticationResponse(
        String refreshToken,
        String accessToken
) {
}
