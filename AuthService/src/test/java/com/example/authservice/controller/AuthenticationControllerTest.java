package com.example.authservice.controller;

import com.example.authservice.config.SecurityConfig;
import com.example.authservice.dto.UserAuthenticationResponse;
import com.example.authservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Slice test: starts only the WebFlux layer (AuthenticationController + SecurityConfig).
 * Services, repositories and the database are NOT loaded, so their collaborators must be mocked.
 */
@WebFluxTest(AuthenticationController.class)
@Import(SecurityConfig.class)
class AuthenticationControllerTest {

    private static final String JSON_BODY =
            """
            {
              "username": "john",
              "password": "secret",
              "refreshToken": "token"
            }
            """;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // The controller depends on UserService; @WebFluxTest does not create service beans,
    // so without this mock the ApplicationContext fails to start.
    @MockitoBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        UserAuthenticationResponse response = new UserAuthenticationResponse("refresh", "access");
        when(userService.login(any())).thenReturn(Mono.just(response));
        when(userService.register(any())).thenReturn(Mono.just(response));
        when(userService.refresh(any())).thenReturn(Mono.just(response));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh"
    })
    void authEndpoints_shouldBePublic(String path) {
        // No credentials and no CSRF token: if security blocked us we'd get 401/403.
        webTestClient.post().uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(JSON_BODY)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void protectedEndpoint_withoutAuth_shouldReturn401() {
        webTestClient.get().uri("/api/v1/orders")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    void protectedEndpoint_withAuth_shouldPassSecurity() {
        // The endpoint doesn't exist, so 404 proves the request got past the security filter.
        webTestClient.get().uri("/api/v1/orders")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void csrfIsDisabled_postWithoutCsrfToken_shouldNotReturn403() {
        webTestClient.post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(JSON_BODY)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void passwordEncoder_shouldBeBCrypt() {
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
        String hash = passwordEncoder.encode("secret");
        assertThat(passwordEncoder.matches("secret", hash)).isTrue();
    }
}
