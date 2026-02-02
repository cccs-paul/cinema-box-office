/*
 * myRC User Management System
 * User Service Implementation
 * 
 * Author: myRC Team
 * Date: 2026-01-17
 * Version: 1.0.0
 * 
 * License: Apache License 2.0
 * 
 * Description:
 * Implementation of user management service with password hashing/salting,
 * authentication support for LOCAL/LDAP/OAuth2, and user profile management.
 * Uses BCrypt for password hashing with automatic salt generation.
 */

package com.myrc.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.myrc.dto.ChangePasswordRequest;
import com.myrc.dto.CreateUserRequest;
import com.myrc.dto.UpdateUserRequest;
import com.myrc.dto.UserDTO;
import com.myrc.model.User;
import com.myrc.repository.UserRepository;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Account lockout configuration
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new user
     */
    @Override
    public UserDTO createUser(CreateUserRequest createUserRequest) {
        logger.info(() -> "Creating new user: " + createUserRequest.getUsername());

        // Validate request
        if (!createUserRequest.isValid()) {
            throw new IllegalArgumentException("Invalid user creation request");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(createUserRequest.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + createUserRequest.getUsername());
        }

        // Generate placeholder email if not provided
        String email = createUserRequest.getEmail();
        if (email == null || email.trim().isEmpty()) {
            email = createUserRequest.getUsername() + "@noemail.local";
        }

        // Check if email already exists (only if a real email was provided)
        if (!email.endsWith("@noemail.local") && userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }

        // Determine auth provider
        User.AuthProvider authProvider = User.AuthProvider.valueOf(createUserRequest.getAuthProvider().toUpperCase());

        // Build user entity (use the processed email, not the original)
        User user = User.builder()
            .username(createUserRequest.getUsername())
            .email(email)
            .fullName(createUserRequest.getFullName())
            .authProvider(authProvider)
            .externalId(createUserRequest.getExternalId())
            .oauthProvider(createUserRequest.getOauthProvider())
            .profileDescription(createUserRequest.getProfileDescription())
            .enabled(true)
            .accountLocked(false)
            .emailVerified(false)
            .failedLoginAttempts(0)
            .roles(createUserRequest.getRoles() != null ? createUserRequest.getRoles() : new java.util.HashSet<>(java.util.Set.of("USER")))
            .build();

        // Hash password for LOCAL authentication
        if (authProvider == User.AuthProvider.LOCAL) {
            if (createUserRequest.getPassword() == null || createUserRequest.getPassword().isEmpty()) {
                throw new IllegalArgumentException("Password required for LOCAL authentication");
            }
            user.setPasswordHash(passwordEncoder.encode(createUserRequest.getPassword()));
            user.setPasswordChangedAt(LocalDateTime.now());
        }

        // Save user to database
        User savedUser = userRepository.save(user);

        return UserDTO.fromEntity(savedUser);
    }

    /**
     * Get a user by ID
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long userId) {
        return userRepository.findById(userId)
            .map(UserDTO::fromEntity);
    }

    /**
     * Get a user by username
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(UserDTO::fromEntity);
    }

    /**
     * Get a user by email
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
            .map(UserDTO::fromEntity);
    }

    /**
     * Get all users
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(UserDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get all enabled users
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getEnabledUsers() {
        return userRepository.findByEnabledTrue().stream()
            .map(UserDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Update user profile
     */
    @Override
    public UserDTO updateUser(Long userId, UpdateUserRequest updateRequest) {
        logger.info("Updating user: {}: " + userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Update basic profile information
        if (updateRequest.getFullName() != null) {
            user.setFullName(updateRequest.getFullName());
        }

        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            // Check if new email is already in use
            if (userRepository.existsByEmailIgnoreCase(updateRequest.getEmail())) {
                throw new IllegalArgumentException("Email already registered: " + updateRequest.getEmail());
            }
            user.setEmail(updateRequest.getEmail());
            user.setEmailVerified(false); // Email needs re-verification
        }

        if (updateRequest.getProfileDescription() != null) {
            user.setProfileDescription(updateRequest.getProfileDescription());
        }

        // Admin-only updates
        if (updateRequest.getEnabled() != null) {
            user.setEnabled(updateRequest.getEnabled());
        }

        if (updateRequest.getAccountLocked() != null) {
            user.setAccountLocked(updateRequest.getAccountLocked());
        }

        if (updateRequest.getEmailVerified() != null) {
            user.setEmailVerified(updateRequest.getEmailVerified());
        }

        if (updateRequest.getRoles() != null) {
            user.setRoles(updateRequest.getRoles());
        }

        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully: {}: " + userId);

        return UserDTO.fromEntity(updatedUser);
    }

    /**
     * Change user password (requires current password verification)
     */
    @Override
    public void changePassword(Long userId, ChangePasswordRequest changePasswordRequest) {
        logger.info(() -> "Password change requested for user: " + userId);

        if (!changePasswordRequest.isValid()) {
            throw new IllegalArgumentException("Invalid password change request");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Verify current password
        if (!verifyPassword(user, changePasswordRequest.getCurrentPassword())) {
            logger.warning("Password change failed - invalid current password for user: {}: " + userId);
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0); // Reset failed attempts

        userRepository.save(user);
        logger.info("Password changed successfully for user: {}: " + userId);
    }

    /**
     * Reset user password (admin only)
     */
    @Override
    public void resetPassword(Long userId, String newPassword) {
        logger.warning("Password reset for user: {} (admin action): " + userId);

        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);

        userRepository.save(user);
        logger.info("Password reset successfully for user: {}: " + userId);
    }

    /**
     * Verify a password against user's stored hash
     */
    @Override
    public boolean verifyPassword(User user, String rawPassword) {
        if (user.getPasswordHash() == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    /**
     * Authenticate a user
     */
    @Override
    public Optional<UserDTO> authenticate(String username, String password) {

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            logger.warning("Authentication failed - user not found: {}: " + username);
            return Optional.empty();
        }

        User user = userOpt.get();

        // Check if account is locked
        if (user.getAccountLocked()) {
            // Check if lockout has expired
            if (user.getAccountLockedUntil() != null &&
                LocalDateTime.now().isAfter(user.getAccountLockedUntil())) {
                // Unlock account
                user.setAccountLocked(false);
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            } else {
                logger.warning("Authentication failed - account locked: {}: " + username);
                return Optional.empty();
            }
        }

        // Check if account is enabled
        if (!user.getEnabled()) {
            logger.warning("Authentication failed - account disabled: {}: " + username);
            return Optional.empty();
        }

        // Verify password
        if (!verifyPassword(user, password)) {
            recordFailedLoginAttempt(user.getId());
            logger.warning("Authentication failed - invalid password for user: {}: " + username);
            return Optional.empty();
        }

        // Successful authentication
        recordSuccessfulLogin(user.getId());
        logger.info("User authenticated successfully: {}: " + username);

        return Optional.of(UserDTO.fromEntity(user));
    }

    /**
     * Enable a user account
     */
    @Override
    public void enableUser(Long userId) {
        logger.info("Enabling user: {}: " + userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setEnabled(true);
        userRepository.save(user);
    }

    /**
     * Disable a user account
     */
    @Override
    public void disableUser(Long userId) {
        logger.info("Disabling user: {}: " + userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setEnabled(false);
        userRepository.save(user);
    }

    /**
     * Lock a user account
     */
    @Override
    public void lockAccount(Long userId) {
        logger.warning("Locking account for user: {}: " + userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setAccountLocked(true);
        user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
        userRepository.save(user);
    }

    /**
     * Unlock a user account
     */
    @Override
    public void unlockAccount(Long userId) {
        logger.info("Unlocking account for user: {}: " + userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setAccountLocked(false);
        user.setAccountLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    /**
     * Record a successful login
     */
    @Override
    public void recordSuccessfulLogin(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setLastLoginAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);

        userRepository.save(user);
    }

    /**
     * Record a failed login attempt
     */
    @Override
    public void recordFailedLoginAttempt(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        int newAttempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
        user.setFailedLoginAttempts(newAttempts);

        // Lock account after max failed attempts
        if (newAttempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLocked(true);
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
            logger.warning("Account locked due to failed login attempts: {}: " + userId);
        }

        userRepository.save(user);
    }

    /**
     * Delete a user
     */
    @Override
    public void deleteUser(Long userId) {
        logger.warning("Deleting user: {}: " + userId);

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        userRepository.deleteById(userId);
        logger.info("User deleted: {}: " + userId);
    }

    /**
     * Get user count by authentication provider
     */
    @Override
    @Transactional(readOnly = true)
    public long getUserCountByProvider(String authProvider) {
        try {
            User.AuthProvider provider = User.AuthProvider.valueOf(authProvider.toUpperCase());
            return userRepository.countByAuthProvider(provider);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * Create or update an OAuth2 user
     */
    @Override
    public UserDTO createOrUpdateOAuth2User(String oauthProvider, String externalId, String email, String fullName) {
        logger.info("Creating/updating OAuth2 user: {} from provider: " + email + " (ID: " + oauthProvider + ")");

        // Try to find existing user
        Optional<User> existingUser = userRepository.findByOauthProviderAndExternalId(oauthProvider, externalId);

        if (existingUser.isPresent()) {
            // Update existing user
            User user = existingUser.get();
            user.setLastLoginAt(LocalDateTime.now());
            User updated = userRepository.save(user);
            logger.info("OAuth2 user updated: {}: " + email);
            return UserDTO.fromEntity(updated);
        }

        // Create new user
        User user = User.builder()
            .username(email.split("@")[0] + "_" + oauthProvider) // Generate username from email
            .email(email)
            .fullName(fullName)
            .authProvider(User.AuthProvider.OAUTH2)
            .externalId(externalId)
            .oauthProvider(oauthProvider)
            .enabled(true)
            .accountLocked(false)
            .emailVerified(true) // OAuth2 emails are verified by provider
            .failedLoginAttempts(0)
            .roles(new java.util.HashSet<>(java.util.Set.of("USER")))
            .lastLoginAt(LocalDateTime.now())
            .build();

        User saved = userRepository.save(user);
        logger.info("New OAuth2 user created: {}: " + email);

        return UserDTO.fromEntity(saved);
    }

    /**
     * Create or update an LDAP user
     */
    @Override
    public UserDTO createOrUpdateLdapUser(String username, String email, String fullName, String externalId) {
        logger.info("Creating/updating LDAP user: {}: " + username);

        // Try to find existing user
        Optional<User> existingUser = userRepository.findByExternalIdAndAuthProvider(externalId, User.AuthProvider.LDAP);

        if (existingUser.isPresent()) {
            // Update existing user
            User user = existingUser.get();
            user.setLastLoginAt(LocalDateTime.now());
            if (email != null) {
                user.setEmail(email);
            }
            if (fullName != null) {
                user.setFullName(fullName);
            }
            User updated = userRepository.save(user);
            logger.info("LDAP user updated: {}: " + username);
            return UserDTO.fromEntity(updated);
        }

        // Create new user
        User user = User.builder()
            .username(username)
            .email(email != null ? email : username + "@ldap.local")
            .fullName(fullName)
            .authProvider(User.AuthProvider.LDAP)
            .externalId(externalId)
            .enabled(true)
            .accountLocked(false)
            .emailVerified(email != null)
            .failedLoginAttempts(0)
            .roles(new java.util.HashSet<>(java.util.Set.of("USER")))
            .lastLoginAt(LocalDateTime.now())
            .build();

        User saved = userRepository.save(user);
        logger.info("New LDAP user created: {}: " + username);

        return UserDTO.fromEntity(saved);
    }

    /**
     * Verify user email address
     */
    @Override
    public void verifyEmail(Long userId) {
        logger.info("Verifying email for user: {}: " + userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setEmailVerified(true);
        userRepository.save(user);
    }

    /**
     * Add a role to a user
     */
    @Override
    public void addRole(Long userId, String role) {
        logger.info("Adding role '{}' to user: " + role + " (ID: " + userId + ")");

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.getRoles().add(role);
        userRepository.save(user);
    }

    /**
     * Remove a role from a user
     */
    @Override
    public void removeRole(Long userId, String role) {
        logger.info("Removing role '{}' from user: " + role + " (ID: " + userId + ")");

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.getRoles().remove(role);
        userRepository.save(user);
    }

    /**
     * Update user theme preference
     */
    @Override
    public Optional<UserDTO> updateTheme(String username, String theme) {
        logger.info("Updating theme for user: " + username + " to: " + theme);

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            User userEntity = user.get();
            userEntity.setTheme(theme);
            User updated = userRepository.save(userEntity);
            return Optional.of(UserDTO.fromEntity(updated));
        }
        
        return Optional.empty();
    }
}
