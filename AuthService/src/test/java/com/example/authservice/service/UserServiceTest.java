package com.example.authservice.service;

import com.example.authservice.dto.RefreshTokenRequest;
import com.example.authservice.dto.UserAuthenticationRequest;
import com.example.authservice.dto.UserAuthenticationResponse;
import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AccessTokenService accessTokenService;

    @InjectMocks
    private UserService userService;

    @Test
    void login_withValidCredentials_returnsTokens() {
        // Arrange: define data and responses for it
        UserAuthenticationRequest request = new UserAuthenticationRequest("john", "secret");
        User user = new User(1, "john", "encoded-password", "ROLE_USER", null, null);

        when(userRepository.findByUsername("john")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("secret", "encoded-password")).thenReturn(true);
        when(refreshTokenService.createFor(user)).thenReturn(Mono.just("refresh-token"));
        when(accessTokenService.createFor(user)).thenReturn("access-token");

        // Act: subscribing mono to execute a logic
        UserAuthenticationResponse response = userService.login(request).block();

        // Assert
        assertThat(response).isEqualTo(new UserAuthenticationResponse("refresh-token", "access-token"));
        verify(passwordEncoder).matches("secret", "encoded-password");
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() {
        UserAuthenticationRequest request = new UserAuthenticationRequest("john", "wrong");
        User user = new User(1, "john", "encoded-password", "ROLE_USER", null, null);

        when(userRepository.findByUsername("john")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(request).block())
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(UNAUTHORIZED));
        verifyNoInteractions(refreshTokenService, accessTokenService);
    }

    @Test
    void login_withUnknownUsername_returnsUnauthorized() {
        UserAuthenticationRequest request = new UserAuthenticationRequest("unknown", "secret");
        when(userRepository.findByUsername("unknown")).thenReturn(Mono.empty());

        assertThatThrownBy(() -> userService.login(request).block())
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(UNAUTHORIZED));
        verifyNoInteractions(passwordEncoder, refreshTokenService, accessTokenService);
    }

    @Test
    void register_withNewUsername_savesEncodedPasswordAndReturnsTokens() {
        UserAuthenticationRequest request = new UserAuthenticationRequest("john", "secret");
        User savedUser = new User(1, "john", "encoded-password", "ROLE_USER", null, null);
        when(userRepository.findByUsername("john")).thenReturn(Mono.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));
        when(refreshTokenService.createFor(savedUser)).thenReturn(Mono.just("refresh-token"));
        when(accessTokenService.createFor(savedUser)).thenReturn("access-token");

        UserAuthenticationResponse response = userService.register(request).block();

        assertThat(response).isEqualTo(new UserAuthenticationResponse("refresh-token", "access-token"));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUsername()).isEqualTo("john");
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(userCaptor.getValue().getRoles()).isEqualTo("ROLE_USER");
    }

    @Test
    void refresh_withInvalidToken_returnsUnauthorized() {
        when(refreshTokenService.consume("invalid-token")).thenReturn(Mono.empty());

        assertThatThrownBy(() -> userService.refresh(new RefreshTokenRequest("invalid-token")).block())
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(UNAUTHORIZED));
        verifyNoInteractions(userRepository, accessTokenService);
    }

    @Test
    void login_withBlankUsername_throwsBeforeCreatingMono() {
        UserAuthenticationRequest request = new UserAuthenticationRequest(" ", "secret");

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(UNAUTHORIZED));
        verifyNoInteractions(userRepository, passwordEncoder, refreshTokenService, accessTokenService);
    }


}
