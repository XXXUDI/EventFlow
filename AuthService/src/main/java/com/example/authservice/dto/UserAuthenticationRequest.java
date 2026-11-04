package com.example.authservice.dto;

public record UserAuthenticationRequest(
        String username,
        String password
) {
}
