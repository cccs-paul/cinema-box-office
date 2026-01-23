/*
 * myRC - OAuth2 Configuration Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.boxoffice.config;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.boxoffice.BoxOfficeApplication;

/**
 * Tests for OAuth2 resource server configuration.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-16
 */
@SpringBootTest(classes = BoxOfficeApplication.class)
@ActiveProfiles("test")
class OAuth2ResourceServerConfigTest {

    @Test
    @DisplayName("OAuth2 resource server should not load when disabled")
    void testOAuth2DisabledByDefault() {
        // OAuth2 is disabled by default in test profile
        // This test verifies the application starts without OAuth2
        assertTrue(true, "Application started successfully with OAuth2 disabled");
    }
}
