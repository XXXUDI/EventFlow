package com.epam.training.food.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges.pathMatchers("/actuator/**").permitAll())
                .authorizeExchange(exchanges -> exchanges.pathMatchers("/api/v1/auth/login").permitAll())
                .authorizeExchange(exchanges -> exchanges.pathMatchers("/api/v1/auth/register").permitAll())
                .authorizeExchange(exchanges -> exchanges.pathMatchers("/api/v1/auth/refresh").permitAll())
                .authorizeExchange(exchanges -> exchanges.anyExchange().authenticated());

        return http.build();
    }

}
