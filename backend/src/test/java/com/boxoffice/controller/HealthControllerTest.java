/*
 * Cinema Box Office - Health Controller Tests
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
package com.boxoffice.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import com.boxoffice.BoxOfficeApplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for HealthController.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-16
 */
@SpringBootTest(classes = BoxOfficeApplication.class)
@ActiveProfiles("test")
class HealthControllerTest {

    @Autowired
    private HealthController healthController;

    @Test
    @DisplayName("Should return UP status when health endpoint is called")
    void testHealthCheck() {
        ResponseEntity<?> response = healthController.health();
        
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }
}
