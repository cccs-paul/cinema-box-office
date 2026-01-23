/*
 * myRC - Backend API
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.boxoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application entry point for myRC Management System API.
 * Provides RESTful API endpoints for managing box office operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-16
 */
@SpringBootApplication
public class BoxOfficeApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(BoxOfficeApplication.class, args);
    }
}
