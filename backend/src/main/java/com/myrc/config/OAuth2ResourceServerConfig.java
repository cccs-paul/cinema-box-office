/*
 * myRC - OAuth2 Resource Server Configuration
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;

/**
 * OAuth2 Resource Server configuration for the application.
 * Enables OAuth2 JWT token validation when configured.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-16
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(
    name = "app.security.oauth2.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class OAuth2ResourceServerConfig {

    /**
     * Configures security filter chain for OAuth2 resource server.
     * Validates JWT tokens and secures endpoints.
     *
     * @param http HttpSecurity to configure
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/health", "/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})
            );
        return http.build();
    }
}
