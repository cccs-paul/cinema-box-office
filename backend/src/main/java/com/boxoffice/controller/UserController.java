/*
 * myRC User Management System
 * User REST Controller
 * 
 * Author: myRC Team
 * Date: 2026-01-17
 * Version: 1.0.0
 * 
 * License: Apache License 2.0
 * 
 * Description:
 * REST API endpoints for user management operations including
 * user creation, profile updates, password management, and authentication.
 */

package com.boxoffice.controller;

import com.boxoffice.dto.ChangePasswordRequest;
import com.boxoffice.dto.CreateUserRequest;
import com.boxoffice.dto.ErrorResponse;
import com.boxoffice.dto.UpdateUserRequest;
import com.boxoffice.dto.UserDTO;
import com.boxoffice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for managing user accounts, profiles, and authentication")
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class.getName());
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user
     * 
     * @param createUserRequest user creation request with credentials
     * @return created user information
     */
    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user account with the provided credentials")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or user already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createUser(
            @RequestBody CreateUserRequest createUserRequest) {
        logger.info("POST /users - Creating new user: " + createUserRequest.getUsername());
        try {
            UserDTO user = userService.createUser(createUserRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (IllegalArgumentException e) {
            logger.severe("User creation failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get all users
     * 
     * @return list of all users
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all registered users")
    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.info("GET /users - Retrieving all users");
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get the current authenticated user
     * 
     * @return current user information
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieves the currently authenticated user's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<UserDTO> getCurrentUser(
            @Parameter(hidden = true) org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isEmpty()) {
            logger.info("GET /users/me - No authentication found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        logger.info("GET /users/me - Fetching current user: " + authentication.getName());
        Optional<UserDTO> user = userService.getUserByUsername(authentication.getName());
        return user.map(ResponseEntity::ok)
            .orElseGet(() -> {
                logger.warning("GET /users/me - User not found: " + authentication.getName());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            });
    }

    /**
     * Get a user by ID
     * 
     * @param userId the user ID
     * @return user information
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "The user ID", required = true)
            @PathVariable Long userId) {;
        Optional<UserDTO> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok)
            .orElseGet(() -> {
                logger.warning("Warning");
                return ResponseEntity.notFound().build();
            });
    }

    /**
     * Get a user by username
     * 
     * @param username the username
     * @return user information
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Retrieves a user by their username")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDTO> getUserByUsername(
            @Parameter(description = "The username", required = true)
            @PathVariable String username) {;
        Optional<UserDTO> user = userService.getUserByUsername(username);
        return user.map(ResponseEntity::ok)
            .orElseGet(() -> {
                logger.warning("Warning");
                return ResponseEntity.notFound().build();
            });
    }

    /**
     * Get a user by email
     * 
     * @param email the email address
     * @return user information
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieves a user by their email address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDTO> getUserByEmail(
            @Parameter(description = "The email address", required = true)
            @PathVariable String email) {;
        Optional<UserDTO> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok)
            .orElseGet(() -> {
                logger.warning("Warning");
                return ResponseEntity.notFound().build();
            });
    }

    /**
     * Get all enabled users
     * 
     * @return list of enabled users
     */
    @GetMapping("/enabled")
    @Operation(summary = "Get all enabled users", description = "Retrieves a list of all enabled user accounts")
    @ApiResponse(responseCode = "200", description = "List of enabled users retrieved successfully")
    public ResponseEntity<List<UserDTO>> getEnabledUsers() {
        logger.info("GET /users/enabled - Retrieving enabled users");
        List<UserDTO> users = userService.getEnabledUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Update user profile
     * 
     * @param userId the user ID to update
     * @param updateRequest the update request
     * @return updated user information
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Update user profile", description = "Updates user profile information and settings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> updateUser(
            @Parameter(description = "The user ID", required = true)
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest updateRequest) {;
        try {
            UserDTO user = userService.updateUser(userId, updateRequest);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            logger.severe("User update failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Change user password
     * 
     * @param userId the user ID
     * @param changePasswordRequest the password change request
     * @return success response
     */
    @PostMapping("/{userId}/change-password")
    @Operation(summary = "Change user password", description = "Allows user to change their password with current password verification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or password requirements not met"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> changePassword(
            @Parameter(description = "The user ID", required = true)
            @PathVariable Long userId,
            @RequestBody ChangePasswordRequest changePasswordRequest) {;
        try {
            userService.changePassword(userId, changePasswordRequest);
            return ResponseEntity.ok("Password changed successfully");
        } catch (IllegalArgumentException e) {
            logger.severe("Error");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Reset user password (admin only)
     * 
     * @param userId the user ID
     * @param newPassword the new password
     * @return success response
     */
    @PostMapping("/{userId}/reset-password")
    @Operation(summary = "Reset user password (admin only)", description = "Admin endpoint to reset a user's password without verification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid password"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - admin only")
    })
    public ResponseEntity<String> resetPassword(
            @Parameter(description = "The user ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "The new password", required = true)
            @RequestParam String newPassword) {
        logger.warning("Warning");
        try {
            userService.resetPassword(userId, newPassword);
            return ResponseEntity.ok("Password reset successfully");
        } catch (IllegalArgumentException e) {
            logger.severe("Error");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Enable user account
     * 
     * @param userId the user ID
     * @return success response
     */
    @PostMapping("/{userId}/enable")
    @Operation(summary = "Enable user account", description = "Enables a disabled user account allowing login")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User enabled successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> enableUser(
            @Parameter(description = "The user ID", required = true)
            @PathVariable Long userId) {;
        try {
            userService.enableUser(userId);
            return ResponseEntity.ok("User enabled successfully");
        } catch (IllegalArgumentException e) {
            logger.severe("Error");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Disable user account
     * 
     * @param userId the user ID
     * @return success response
     */
    @PostMapping("/{userId}/disable")
    @Operation(summary = "Disable user account", description = "Disables a user account preventing login")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User disabled successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> disableUser(
            @Parameter(description = "The user ID", required = true)
            @PathVariable Long userId) {;
        try {
            userService.disableUser(userId);
            return ResponseEntity.ok("User disabled successfully");
        } catch (IllegalArgumentException e) {
            logger.severe("Error");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Unlock user account
     * 
     * @param userId the user ID
     * @return success response
     */
    @PostMapping("/{userId}/unlock")
    @Operation(summary = "Unlock user account", description = "Unlocks a locked user account due to failed login attempts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User unlocked successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> unlockAccount(
            @Parameter(description = "The user ID", required = true)
            @PathVariable Long userId) {;
        try {
            userService.unlockAccount(userId);
            return ResponseEntity.ok("User account unlocked successfully");
        } catch (IllegalArgumentException e) {
            logger.severe("Error");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Verify user email
     * 
     * @param userId the user ID
     * @return success response
     */
    @PostMapping("/{userId}/verify-email")
    @Operation(summary = "Verify user email", description = "Marks a user's email address as verified")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email verified successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> verifyEmail(
            @Parameter(description = "The user ID", required = true)
            @PathVariable Long userId) {;
        try {
            userService.verifyEmail(userId);
            return ResponseEntity.ok("Email verified successfully");
        } catch (IllegalArgumentException e) {
            logger.severe("Error");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a user
     * 
     * @param userId the user ID
     * @return success response
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete a user", description = "Permanently deletes a user account and all associated data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> deleteUser(
            @Parameter(description = "The user ID", required = true)
            @PathVariable Long userId) {
        logger.warning("Warning");
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("User deleted successfully");
        } catch (IllegalArgumentException e) {
            logger.severe("Error");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Authenticate user and get info (typically called after Spring Security authentication)
     * 
     * @param username the username
     * @param password the password
     * @return authenticated user information
     */
    @PostMapping("/authenticate")
    @Operation(summary = "Authenticate user", description = "Authenticates a user with username and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication failed - invalid credentials or locked account")
    })
    public ResponseEntity<UserDTO> authenticate(
            @Parameter(description = "Username", required = true)
            @RequestParam String username,
            @Parameter(description = "Password", required = true)
            @RequestParam String password,
            HttpServletRequest request,
            HttpServletResponse response) {
        Optional<UserDTO> user = userService.authenticate(username, password);
        if (user.isPresent()) {
            // Create/access session
            HttpSession session = request.getSession(true);
            
            // Establish security context
            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(username, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            // Persist security context to session
            HttpSessionSecurityContextRepository repository = new HttpSessionSecurityContextRepository();
            repository.saveContext(SecurityContextHolder.getContext(), request, response);
            
            logger.info("User authenticated with session: " + username + " (Session ID: " + session.getId() + ")");
            return ResponseEntity.ok(user.get());
        } else {
            logger.warning("Authentication failed for user: " + username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Get user statistics
     * 
     * @return statistics map
     */
    @GetMapping("/stats")
    @Operation(summary = "Get user statistics", description = "Retrieves user management statistics")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public ResponseEntity<java.util.Map<String, Object>> getStats() {
        logger.info("GET /users/stats - Getting user statistics");
        List<UserDTO> allUsers = userService.getAllUsers();
        long totalUsers = allUsers.size();
        long localUsers = userService.getUserCountByProvider("LOCAL");
        long ldapUsers = userService.getUserCountByProvider("LDAP");
        long oauth2Users = userService.getUserCountByProvider("OAUTH2");
        long enabledUsers = userService.getEnabledUsers().size();

        return ResponseEntity.ok(java.util.Map.of(
            "totalUsers", totalUsers,
            "enabledUsers", enabledUsers,
            "disabledUsers", totalUsers - enabledUsers,
            "localUsers", localUsers,
            "ldapUsers", ldapUsers,
            "oauth2Users", oauth2Users
        ));
    }

    /**
     * Update user theme preference
     * 
     * @param username user's username
     * @param theme theme preference (light or dark)
     * @return updated user
     */
    @PutMapping("/{username}/theme")
    @Operation(summary = "Update user theme", description = "Updates user's theme preference (light or dark)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Theme updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> updateTheme(
            @Parameter(description = "Username") @PathVariable String username,
            @Parameter(description = "Theme preference") @RequestParam String theme) {
        logger.info("PUT /users/" + username + "/theme - Updating theme to " + theme);
        
        if (!theme.equals("light") && !theme.equals("dark")) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Theme must be 'light' or 'dark'"));
        }
        
        Optional<UserDTO> updated = userService.updateTheme(username, theme);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get user theme preference
     * 
     * @param username user's username
     * @return user theme
     */
    @GetMapping("/{username}/theme")
    @Operation(summary = "Get user theme", description = "Retrieves user's theme preference")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Theme retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<java.util.Map<String, String>> getTheme(
            @Parameter(description = "Username") @PathVariable String username) {
        logger.info("GET /users/" + username + "/theme - Getting user theme");
        Optional<UserDTO> user = userService.getUserByUsername(username);
        return user.map(u -> ResponseEntity.ok(java.util.Map.of("theme", u.getTheme())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
