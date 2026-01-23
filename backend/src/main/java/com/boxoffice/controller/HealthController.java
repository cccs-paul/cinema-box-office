/*
 * myRC - Health Check Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.boxoffice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Health check controller for API endpoints.
 * Provides endpoints to verify API is running and accessible.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-16
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * Health check endpoint.
     *
     * @return HTTP 200 OK with health status
     */
    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP", "myRC API is running"));
    }

    /**
     * Database health check endpoint.
     * Attempts to validate a database connection.
     *
     * @return HTTP 200 OK if database is healthy, 503 otherwise
     */
    @GetMapping("/db")
    public ResponseEntity<HealthResponse> databaseHealth() {
        try {
            if (dataSource != null) {
                try (Connection connection = dataSource.getConnection()) {
                    if (connection.isValid(2)) {
                        return ResponseEntity.ok(new HealthResponse("UP", "Database connection is UP"));
                    } else {
                        return ResponseEntity.status(503)
                                .body(new HealthResponse("DOWN", "Database connection validation failed"));
                    }
                }
            } else {
                return ResponseEntity.ok(new HealthResponse("UP", "Database is available"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(new HealthResponse("DOWN", "Database connection failed: " + e.getMessage()));
        }
    }

    /**
     * Health response DTO.
     */
    record HealthResponse(String status, String message) {}
}
