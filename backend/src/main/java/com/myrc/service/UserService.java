/*
 * myRC User Management System
 * User Service Interface
 * 
 * Author: myRC Team
 * Date: 2026-01-17
 * Version: 1.0.0
 * 
 * License: Apache License 2.0
 * 
 * Description:
 * Service interface for user management including authentication,
 * password management, and user profile operations.
 */

package com.myrc.service;

import com.myrc.dto.ChangePasswordRequest;
import com.myrc.dto.CreateUserRequest;
import com.myrc.dto.UpdateUserRequest;
import com.myrc.dto.UserDTO;
import com.myrc.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    /**
     * Create a new user
     * 
     * @param createUserRequest the user creation request
     * @return created user DTO
     * @throws IllegalArgumentException if request is invalid or user already exists
     */
    UserDTO createUser(CreateUserRequest createUserRequest);

    /**
     * Get a user by ID
     * 
     * @param userId the user ID
     * @return UserDTO if found
     */
    Optional<UserDTO> getUserById(Long userId);

    /**
     * Get a user by username
     * 
     * @param username the username
     * @return UserDTO if found
     */
    Optional<UserDTO> getUserByUsername(String username);

    /**
     * Get a user by email
     * 
     * @param email the email address
     * @return UserDTO if found
     */
    Optional<UserDTO> getUserByEmail(String email);

    /**
     * Get all users
     * 
     * @return list of all users
     */
    List<UserDTO> getAllUsers();

    /**
     * Get all enabled users
     * 
     * @return list of enabled users
     */
    List<UserDTO> getEnabledUsers();

    /**
     * Update user profile
     * 
     * @param userId the user ID to update
     * @param updateRequest the update request
     * @return updated user DTO
     * @throws IllegalArgumentException if user not found or request is invalid
     */
    UserDTO updateUser(Long userId, UpdateUserRequest updateRequest);

    /**
     * Change user password (requires current password verification)
     * 
     * @param userId the user ID
     * @param changePasswordRequest the password change request
     * @throws IllegalArgumentException if user not found or request is invalid
     */
    void changePassword(Long userId, ChangePasswordRequest changePasswordRequest);

    /**
     * Reset user password (admin only - no current password needed)
     * 
     * @param userId the user ID
     * @param newPassword the new password
     * @throws IllegalArgumentException if user not found
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * Verify a password against user's stored hash
     * 
     * @param user the user entity
     * @param rawPassword the raw password to verify
     * @return true if password is correct, false otherwise
     */
    boolean verifyPassword(User user, String rawPassword);

    /**
     * Authenticate a user by username and password
     * 
     * @param username the username
     * @param password the password
     * @return UserDTO if authentication successful
     */
    Optional<UserDTO> authenticate(String username, String password);

    /**
     * Enable a user account
     * 
     * @param userId the user ID
     * @throws IllegalArgumentException if user not found
     */
    void enableUser(Long userId);

    /**
     * Disable a user account
     * 
     * @param userId the user ID
     * @throws IllegalArgumentException if user not found
     */
    void disableUser(Long userId);

    /**
     * Lock a user account (after failed login attempts)
     * 
     * @param userId the user ID
     * @throws IllegalArgumentException if user not found
     */
    void lockAccount(Long userId);

    /**
     * Unlock a user account
     * 
     * @param userId the user ID
     * @throws IllegalArgumentException if user not found
     */
    void unlockAccount(Long userId);

    /**
     * Record a successful login
     * 
     * @param userId the user ID
     * @throws IllegalArgumentException if user not found
     */
    void recordSuccessfulLogin(Long userId);

    /**
     * Record a failed login attempt
     * 
     * @param userId the user ID
     * @throws IllegalArgumentException if user not found
     */
    void recordFailedLoginAttempt(Long userId);

    /**
     * Delete a user by ID
     * 
     * @param userId the user ID
     * @throws IllegalArgumentException if user not found
     */
    void deleteUser(Long userId);

    /**
     * Get user count by authentication provider
     * 
     * @param authProvider the authentication provider
     * @return count of users using that provider
     */
    long getUserCountByProvider(String authProvider);

    /**
     * Create or update an OAuth2 user (upsert)
     * 
     * @param oauthProvider the OAuth2 provider name (e.g., "google", "github")
     * @param externalId the provider's user ID
     * @param email the user's email from provider
     * @param fullName the user's full name from provider
     * @return created or updated user DTO
     */
    UserDTO createOrUpdateOAuth2User(String oauthProvider, String externalId, String email, String fullName);

    /**
     * Create or update an LDAP user
     * 
     * @param username the LDAP username
     * @param email the user's email
     * @param fullName the user's full name
     * @param externalId the LDAP distinguished name
     * @return created or updated user DTO
     */
    UserDTO createOrUpdateLdapUser(String username, String email, String fullName, String externalId);

    /**
     * Verify user email address
     * 
     * @param userId the user ID
     * @throws IllegalArgumentException if user not found
     */
    void verifyEmail(Long userId);

    /**
     * Add a role to a user
     * 
     * @param userId the user ID
     * @param role the role to add
     * @throws IllegalArgumentException if user not found
     */
    void addRole(Long userId, String role);

    /**
     * Remove a role from a user
     * 
     * @param userId the user ID
     * @param role the role to remove
     * @throws IllegalArgumentException if user not found
     */
    void removeRole(Long userId, String role);

    /**
     * Update user theme preference
     * 
     * @param username the username
     * @param theme the theme preference (light or dark)
     * @return updated user DTO wrapped in Optional
     */
    Optional<UserDTO> updateTheme(String username, String theme);
}
