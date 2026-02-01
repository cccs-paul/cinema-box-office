/*
 * myRC - Authentication Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication and user information controller.
 * Provides endpoints for authentication status and user details.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-16
 */
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    /**
     * Authentication info DTO.
     *
     * @param authenticated whether user is authenticated
     * @param principal user principal name
     * @param authorities user authorities/roles
     */
    record AuthInfo(boolean authenticated, String principal, String authorities) {}

    /**
     * Get current authentication information.
     *
     * @return authentication details for current user
     */
    @GetMapping("/info")
    public ResponseEntity<AuthInfo> getAuthInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        AuthInfo info = new AuthInfo(
            auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser"),
            auth != null ? auth.getName() : "anonymous",
            auth != null ? auth.getAuthorities().toString() : "NONE"
        );
        
        return ResponseEntity.ok(info);
    }

    /**
     * Check if user is authenticated.
     *
     * @return authentication status
     */
    @GetMapping("/status")
    public ResponseEntity<StatusResponse> getAuthStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");
        
        return ResponseEntity.ok(new StatusResponse(isAuthenticated));
    }

    /**
     * Status response DTO.
     *
     * @param authenticated whether user is authenticated
     */
    record StatusResponse(boolean authenticated) {}
}
