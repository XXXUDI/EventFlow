package com.example.authservice.dto;

public record UserAuthenticationResponse(
        String refreshToken,
        String accessToken
) {
}
