/*
 * Cinema Box Office - Health Check Controller
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
package com.boxoffice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check controller for API endpoints.
 * Provides endpoints to verify API is running and accessible.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-16
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * Health check endpoint.
     *
     * @return HTTP 200 OK with health status
     */
    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP", "Box Office API is running"));
    }

    /**
     * Health response DTO.
     */
    record HealthResponse(String status, String message) {}
}
