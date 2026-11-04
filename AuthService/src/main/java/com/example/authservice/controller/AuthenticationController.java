package com.example.authservice.controller;

import com.example.authservice.dto.UserAuthenticationRequest;
import com.example.authservice.dto.UserAuthenticationResponse;
import com.example.authservice.dto.RefreshTokenRequest;
import com.example.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserAuthenticationResponse> register(@RequestBody UserAuthenticationRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public Mono<UserAuthenticationResponse> login(@RequestBody UserAuthenticationRequest request) {
        return userService.login(request);
    }

    @PostMapping("/refresh")
    public Mono<UserAuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return userService.refresh(request);
    }
}
