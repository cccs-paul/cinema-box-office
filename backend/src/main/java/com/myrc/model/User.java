/*
 * myRC User Management System
 * User Entity Class
 * 
 * Author: myRC Team
 * Date: 2026-01-17
 * Version: 1.0.0
 * 
 * License: Apache License 2.0
 */

package com.myrc.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username", name = "uk_users_username"),
    @UniqueConstraint(columnNames = "email", name = "uk_users_email")
})
public class User {

    public enum AuthProvider {
        LOCAL, LDAP, OAUTH2
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 100)
    private String fullName;

    @Column(length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @Column(length = 255)
    private String externalId;

    @Column(length = 50)
    private String oauthProvider;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(nullable = false)
    private Boolean accountLocked;

    @Column(nullable = false)
    private Boolean emailVerified;

    @Column(nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column
    private LocalDateTime accountLockedUntil;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    @Column(length = 500)
    private String profileDescription;

    @Column
    private LocalDateTime lastLoginAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime passwordChangedAt;

    @Column(length = 20, nullable = false)
    private String theme = "light";

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (enabled == null) enabled = true;
        if (accountLocked == null) accountLocked = false;
        if (emailVerified == null) emailVerified = false;
        if (failedLoginAttempts == null) failedLoginAttempts = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public AuthProvider getAuthProvider() { return authProvider; }
    public void setAuthProvider(AuthProvider authProvider) { this.authProvider = authProvider; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getOauthProvider() { return oauthProvider; }
    public void setOauthProvider(String oauthProvider) { this.oauthProvider = oauthProvider; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Boolean getAccountLocked() { return accountLocked; }
    public void setAccountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public LocalDateTime getAccountLockedUntil() { return accountLockedUntil; }
    public void setAccountLockedUntil(LocalDateTime accountLockedUntil) { this.accountLockedUntil = accountLockedUntil; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public String getProfileDescription() { return profileDescription; }
    public void setProfileDescription(String profileDescription) { this.profileDescription = profileDescription; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    // Builder pattern support
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String passwordHash;
        private AuthProvider authProvider;
        private String externalId;
        private String oauthProvider;
        private Boolean enabled = true;
        private Boolean accountLocked = false;
        private Boolean emailVerified = false;
        private Integer failedLoginAttempts = 0;
        private LocalDateTime accountLockedUntil;
        private Set<String> roles = new HashSet<>();
        private String profileDescription;
        private LocalDateTime lastLoginAt;
        private String theme = "light";

        public UserBuilder id(Long id) { this.id = id; return this; }
        public UserBuilder username(String username) { this.username = username; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public UserBuilder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public UserBuilder authProvider(AuthProvider authProvider) { this.authProvider = authProvider; return this; }
        public UserBuilder externalId(String externalId) { this.externalId = externalId; return this; }
        public UserBuilder oauthProvider(String oauthProvider) { this.oauthProvider = oauthProvider; return this; }
        public UserBuilder enabled(Boolean enabled) { this.enabled = enabled; return this; }
        public UserBuilder accountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; return this; }
        public UserBuilder emailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; return this; }
        public UserBuilder failedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; return this; }
        public UserBuilder accountLockedUntil(LocalDateTime accountLockedUntil) { this.accountLockedUntil = accountLockedUntil; return this; }
        public UserBuilder roles(Set<String> roles) { this.roles = roles; return this; }
        public UserBuilder profileDescription(String profileDescription) { this.profileDescription = profileDescription; return this; }
        public UserBuilder lastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; return this; }
        public UserBuilder theme(String theme) { this.theme = theme; return this; }

        public User build() {
            User user = new User();
            user.id = this.id;
            user.username = this.username;
            user.email = this.email;
            user.fullName = this.fullName;
            user.passwordHash = this.passwordHash;
            user.authProvider = this.authProvider;
            user.externalId = this.externalId;
            user.oauthProvider = this.oauthProvider;
            user.enabled = this.enabled;
            user.accountLocked = this.accountLocked;
            user.emailVerified = this.emailVerified;
            user.failedLoginAttempts = this.failedLoginAttempts;
            user.accountLockedUntil = this.accountLockedUntil;
            user.roles = this.roles;
            user.profileDescription = this.profileDescription;
            user.lastLoginAt = this.lastLoginAt;
            user.theme = this.theme;
            return user;
        }
    }
}
