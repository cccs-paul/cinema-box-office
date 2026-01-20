/*
 * Cinema Box Office User Management System
 * User Repository Interface
 * 
 * Author: Box Office Team
 * Date: 2026-01-17
 * Version: 1.0.0
 * 
 * License: Apache License 2.0
 * 
 * Description:
 * Spring Data JPA repository for User entity providing database operations
 * including custom query methods for user lookup and authentication.
 */

package com.boxoffice.repository;

import com.boxoffice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by username (case-sensitive)
     * 
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by email address (case-insensitive)
     * 
     * @param email the email address to search for
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    /**
     * Check if a username already exists
     * 
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email address is already registered
     * 
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    /**
     * Find a user by external ID (for LDAP/OAuth2 users)
     * 
     * @param externalId the external identifier
     * @param authProvider the authentication provider
     * @return Optional containing the user if found
     */
    Optional<User> findByExternalIdAndAuthProvider(String externalId, User.AuthProvider authProvider);

    /**
     * Find a user by OAuth2 provider and provider ID
     * 
     * @param oauthProvider the OAuth2 provider name (e.g., "google", "github")
     * @param externalId the provider's user ID
     * @return Optional containing the user if found
     */
    Optional<User> findByOauthProviderAndExternalId(String oauthProvider, String externalId);

    /**
     * Find all users for a specific authentication provider
     * 
     * @param authProvider the authentication provider
     * @return list of users using that provider
     */
    List<User> findByAuthProvider(User.AuthProvider authProvider);

    /**
     * Find enabled users by authentication provider
     * 
     * @param authProvider the authentication provider
     * @param enabled the enabled status
     * @return list of enabled/disabled users using that provider
     */
    List<User> findByAuthProviderAndEnabled(User.AuthProvider authProvider, Boolean enabled);

    /**
     * Find all enabled users
     * 
     * @return list of enabled users
     */
    List<User> findByEnabledTrue();

    /**
     * Find all disabled users
     * 
     * @return list of disabled users
     */
    List<User> findByEnabledFalse();

    /**
     * Find users with locked accounts
     * 
     * @return list of locked users
     */
    List<User> findByAccountLockedTrue();

    /**
     * Find users with verified email addresses
     * 
     * @return list of users with verified emails
     */
    List<User> findByEmailVerifiedTrue();

    /**
     * Find users with unverified email addresses
     * 
     * @return list of users with unverified emails
     */
    List<User> findByEmailVerifiedFalse();

    /**
     * Count users by authentication provider
     * 
     * @param authProvider the authentication provider
     * @return count of users using that provider
     */
    long countByAuthProvider(User.AuthProvider authProvider);

    /**
     * Count enabled users
     * 
     * @return count of enabled users
     */
    long countByEnabledTrue();
}
