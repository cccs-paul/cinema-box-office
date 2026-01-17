/*
 * Cinema Box Office - Authentication Controller Tests
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
package com.boxoffice.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.boxoffice.BoxOfficeApplication;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for AuthenticationController.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-16
 */
@SpringBootTest(classes = BoxOfficeApplication.class)
@ActiveProfiles("test")
class AuthenticationControllerTest {

    @Autowired
    private AuthenticationController authenticationController;

    @Test
    @DisplayName("Should return unauthenticated status for anonymous user")
    void testGetAuthStatusUnauthenticated() {
        var response = authenticationController.getAuthStatus();
        
        assertNotNull(response);
        assertFalse(response.getBody().authenticated());
    }

    @Test
    @DisplayName("Should return authentication info for anonymous user")
    void testGetAuthInfo() {
        var response = authenticationController.getAuthInfo();
        
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertFalse(response.getBody().authenticated());
    }
}
