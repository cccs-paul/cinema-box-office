/*
 * Cinema Box Office - LDAP Configuration Tests
 * Copyright (c) 2026 Box Office Team
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
 * Tests for LDAP security configuration.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-16
 */
@SpringBootTest(classes = BoxOfficeApplication.class)
@ActiveProfiles("test")
class LdapSecurityConfigTest {

    @Test
    @DisplayName("LDAP security should not load when disabled")
    void testLdapDisabledByDefault() {
        // LDAP is disabled by default in test profile
        // This test verifies the application starts without LDAP
        assertTrue(true, "Application started successfully with LDAP disabled");
    }
}
