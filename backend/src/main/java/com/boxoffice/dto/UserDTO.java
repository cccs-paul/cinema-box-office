package com.boxoffice.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String authProvider;
    private Boolean enabled;
    private Boolean accountLocked;
    private Boolean emailVerified;
    private Set<String> roles;
    private String profileDescription;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String theme;

    public UserDTO() {}

    public UserDTO(Long id, String username, String email, String fullName, String authProvider, 
                   Boolean enabled, Boolean accountLocked, Boolean emailVerified, Set<String> roles,
                   String profileDescription, LocalDateTime lastLoginAt, LocalDateTime createdAt, LocalDateTime updatedAt, String theme) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.authProvider = authProvider;
        this.enabled = enabled;
        this.accountLocked = accountLocked;
        this.emailVerified = emailVerified;
        this.roles = roles;
        this.profileDescription = profileDescription;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.theme = theme;
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
    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Boolean getAccountLocked() { return accountLocked; }
    public void setAccountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; }
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
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
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public static UserDTO fromEntity(com.boxoffice.model.User user) {
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getAuthProvider().toString(),
            user.getEnabled(),
            user.getAccountLocked(),
            user.getEmailVerified(),
            user.getRoles(),
            user.getProfileDescription(),
            user.getLastLoginAt(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getTheme()
        );
    }
}
