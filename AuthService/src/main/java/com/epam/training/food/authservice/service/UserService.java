package com.epam.training.food.authservice.service;

import com.epam.training.food.authservice.dto.UserAuthenticationRequest;
import com.epam.training.food.authservice.dto.UserAuthenticationResponse;
import com.epam.training.food.authservice.dto.RefreshTokenRequest;
import com.epam.training.food.authservice.entity.User;
import com.epam.training.food.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenService accessTokenService;

    public Mono<UserAuthenticationResponse> register(UserAuthenticationRequest request) {
        validate(request);
        return userRepository.findByUsername(request.username())
                .flatMap(existing -> Mono.<User>error(new ResponseStatusException(CONFLICT, "Username is already taken")))
                .switchIfEmpty(userRepository.save(new User(null, request.username(), passwordEncoder.encode(request.password()),
                        "ROLE_USER", null, null)))
                .flatMap(this::createAuthenticationResponse);
    }

    public Mono<UserAuthenticationResponse> login(UserAuthenticationRequest request) {
        validate(request);
        return userRepository.findByUsername(request.username())
                .filter(user -> passwordEncoder.matches(request.password(), user.getPassword()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNAUTHORIZED, "Invalid username or password")))
                .flatMap(this::createAuthenticationResponse);
    }

    public Mono<UserAuthenticationResponse> refresh(RefreshTokenRequest request) {
        if (request == null || request.refreshToken() == null || request.refreshToken().isBlank()) {
            return Mono.error(new ResponseStatusException(UNAUTHORIZED, "Refresh token is required"));
        }
        return refreshTokenService.consume(request.refreshToken())
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNAUTHORIZED, "Refresh token is invalid or expired")))
                .flatMap(token -> userRepository.findById(token.getUserId()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNAUTHORIZED, "Refresh token is invalid or expired")))
                .flatMap(this::createAuthenticationResponse);
    }

    private Mono<UserAuthenticationResponse> createAuthenticationResponse(User user) {
        return refreshTokenService.createFor(user)
                .map(refreshToken -> new UserAuthenticationResponse(refreshToken, accessTokenService.createFor(user)));
    }

    private void validate(UserAuthenticationRequest request) {
        if (request == null || request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Username and password are required");
        }
    }
}
