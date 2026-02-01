/*
 * myRC - Authentication Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * REST API endpoints for authentication configuration and self-registration.
 */

package com.myrc.controller;

import com.myrc.config.LoginMethodsProperties;
import com.myrc.dto.CreateUserRequest;
import com.myrc.dto.ErrorResponse;
import com.myrc.dto.LoginMethodsDTO;
import com.myrc.dto.RegistrationRequest;
import com.myrc.dto.UserDTO;
import com.myrc.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * REST controller for authentication-related endpoints.
 * Provides login methods configuration and self-registration.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-31
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "APIs for authentication configuration and self-registration")
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());
    private final LoginMethodsProperties loginMethodsProperties;
    private final UserService userService;

    public AuthController(LoginMethodsProperties loginMethodsProperties, UserService userService) {
        this.loginMethodsProperties = loginMethodsProperties;
        this.userService = userService;
    }

    /**
     * Get available login methods configuration.
     *
     * @return login methods configuration
     */
    @GetMapping("/login-methods")
    @Operation(summary = "Get login methods", description = "Returns which login methods are enabled")
    @ApiResponse(responseCode = "200", description = "Login methods configuration retrieved successfully")
    public ResponseEntity<LoginMethodsDTO> getLoginMethods() {
        logger.info("GET /auth/login-methods - Fetching login methods configuration");
        
        LoginMethodsDTO.AppAccountConfig appAccountConfig = new LoginMethodsDTO.AppAccountConfig(
            loginMethodsProperties.getAppAccount().isEnabled(),
            loginMethodsProperties.getAppAccount().isAllowRegistration()
        );
        
        LoginMethodsDTO loginMethods = new LoginMethodsDTO(
            appAccountConfig,
            loginMethodsProperties.getLdap().isEnabled(),
            loginMethodsProperties.getOauth2().isEnabled()
        );
        
        return ResponseEntity.ok(loginMethods);
    }

    /**
     * Register a new user account (self-registration).
     *
     * @param request registration request with user details
     * @return created user information
     */
    @PostMapping("/register")
    @Operation(summary = "Register new account", description = "Self-registration endpoint for creating App Account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Account created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or registration disabled"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request) {
        logger.info("POST /auth/register - Registration attempt for username: " + request.getUsername());
        
        // Check if App Account registration is enabled
        if (!loginMethodsProperties.getAppAccount().isEnabled()) {
            logger.warning("Registration failed: App Account login is disabled");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("App Account login is currently disabled"));
        }
        
        if (!loginMethodsProperties.getAppAccount().isAllowRegistration()) {
            logger.warning("Registration failed: Self-registration is disabled");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Self-registration is currently disabled"));
        }
        
        // Validate request
        String validationError = request.getValidationError();
        if (validationError != null) {
            logger.warning("Registration failed: " + validationError);
            return ResponseEntity.badRequest().body(new ErrorResponse(validationError));
        }
        
        try {
            // Check username availability
            if (userService.getUserByUsername(request.getUsername()).isPresent()) {
                logger.warning("Registration failed: Username already exists: " + request.getUsername());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Username is already taken"));
            }
            
            // Create user through service
            Set<String> roles = new HashSet<>();
            roles.add("USER");
            
            CreateUserRequest createRequest = new CreateUserRequest();
            createRequest.setUsername(request.getUsername());
            createRequest.setEmail(request.getEmail());
            createRequest.setPassword(request.getPassword());
            createRequest.setFullName(request.getFullName() != null ? request.getFullName() : request.getUsername());
            createRequest.setAuthProvider("LOCAL");
            createRequest.setRoles(roles);
            
            UserDTO createdUser = userService.createUser(createRequest);
            logger.info("Registration successful for username: " + request.getUsername());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
            
        } catch (IllegalArgumentException e) {
            logger.warning("Registration failed: " + e.getMessage());
            if (e.getMessage().contains("Username already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Username is already taken"));
            }
            if (e.getMessage().contains("Email already registered")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Email is already registered"));
            }
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.severe("Registration failed with unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred during registration"));
        }
    }

    /**
     * Check if a username is available.
     *
     * @param username the username to check
     * @return availability status
     */
    @GetMapping("/check-username/{username}")
    @Operation(summary = "Check username availability", description = "Checks if a username is available for registration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Username availability checked"),
        @ApiResponse(responseCode = "400", description = "Invalid username format")
    })
    public ResponseEntity<?> checkUsernameAvailability(@PathVariable String username) {
        logger.info("GET /auth/check-username/" + username + " - Checking username availability");
        
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Username is required"));
        }
        
        if (username.length() < 3) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Username must be at least 3 characters"));
        }
        
        if (!username.matches("^[a-zA-Z0-9_-]+$")) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Username can only contain letters, numbers, underscores, and hyphens"));
        }
        
        boolean available = userService.getUserByUsername(username).isEmpty();
        
        return ResponseEntity.ok(new UsernameAvailabilityResponse(username, available));
    }

    /**
     * Response DTO for username availability check.
     */
    public static class UsernameAvailabilityResponse {
        private String username;
        private boolean available;

        public UsernameAvailabilityResponse(String username, boolean available) {
            this.username = username;
            this.available = available;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }
    }
}
