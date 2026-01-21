/*
 * Cinema Box Office - Authentication Configuration
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
package com.boxoffice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

/**
 * Core authentication configuration for the application.
 * Supports multiple authentication mechanisms including LDAP and OAuth2.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-16
 */
@Configuration
@EnableWebSecurity
public class AuthenticationConfig {

    /**
     * Provides password encoding using BCrypt.
     *
     * @return configured BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provides HTTP session security context repository for session persistence.
     *
     * @return HttpSessionSecurityContextRepository
     */
    @Bean
    public HttpSessionSecurityContextRepository httpSessionSecurityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * Configures the main security filter chain for basic authentication.
     * Used when OAuth2 and LDAP are not enabled.
     *
     * @param http HttpSecurity to configure
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    @ConditionalOnProperty(
        name = "app.security.oauth2.enabled",
        havingValue = "false",
        matchIfMissing = true
    )
    public SecurityFilterChain basicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionFixation().migrateSession())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/health", "/actuator/**").permitAll()
                .requestMatchers("/users/authenticate", "/users/authenticate/ldap").permitAll()
                .anyRequest().permitAll()
            )
            .httpBasic(basic -> {})
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /**
     * Configures CORS for allowing cross-origin requests from the frontend.
     *
     * @return CorsConfigurationSource with CORS settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",
            "http://localhost:3000",
            "http://localhost:80",
            "http://localhost"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
