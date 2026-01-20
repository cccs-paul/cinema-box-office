/*
 * Cinema Box Office User Management System
 * Test Configuration
 * 
 * Author: Box Office Team
 * Date: 2026-01-17
 * Version: 1.0.0
 * License: Apache License 2.0
 */

package com.boxoffice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
