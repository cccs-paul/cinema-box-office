/*
 * myRC - Registration Request DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Data Transfer Object for self-registration requests.
 */

package com.boxoffice.dto;

/**
 * DTO for user self-registration requests.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-31
 */
public class RegistrationRequest {

    private String username;
    private String email;
    private String password;
    private String confirmPassword;
    private String fullName;

    public RegistrationRequest() {}

    public RegistrationRequest(String username, String email, String password, String confirmPassword, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.fullName = fullName;
    }

    /**
     * Validate the registration request.
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (username.length() < 3 || username.length() > 50) {
            return false;
        }
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return false;
        }
        if (password == null || password.isEmpty()) {
            return false;
        }
        if (password.length() < 8) {
            return false;
        }
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            return false;
        }
        return true;
    }

    /**
     * Get validation error message.
     *
     * @return error message or null if valid
     */
    public String getValidationError() {
        if (username == null || username.trim().isEmpty()) {
            return "Username is required";
        }
        if (username.length() < 3) {
            return "Username must be at least 3 characters";
        }
        if (username.length() > 50) {
            return "Username must not exceed 50 characters";
        }
        if (!username.matches("^[a-zA-Z0-9_-]+$")) {
            return "Username can only contain letters, numbers, underscores, and hyphens";
        }
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return "Invalid email format";
        }
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            return "Passwords do not match";
        }
        return null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
