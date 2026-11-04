package com.example.authservice.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
@Import(SecurityConfig.class)
public class SecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh"
    })
    void authEndpoints_shouldBePublic(String path) {
        webTestClient.post().uri(path)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void protectedEndpoint_withoutAuth_shouldReturn401() {
        webTestClient.get().uri("/api/v1/orders")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void csrfIsDisabled_postWithoutCsrfToken_shouldNotReturn403() {
        webTestClient.post().uri("/api/v1/auth/login")
                .exchange()
                .expectStatus().isOk();
    }
}
