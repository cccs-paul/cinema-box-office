package com.myrc.dto;

import java.util.Set;

public class UpdateUserRequest {
    private String fullName;
    private String email;
    private String profileDescription;
    private Set<String> roles;
    private Boolean enabled;
    private Boolean accountLocked;
    private Boolean emailVerified;

    public UpdateUserRequest() {}

    public UpdateUserRequest(String fullName, String email, String profileDescription, Set<String> roles,
                            Boolean enabled, Boolean accountLocked, Boolean emailVerified) {
        this.fullName = fullName;
        this.email = email;
        this.profileDescription = profileDescription;
        this.roles = roles;
        this.enabled = enabled;
        this.accountLocked = accountLocked;
        this.emailVerified = emailVerified;
    }

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getProfileDescription() { return profileDescription; }
    public void setProfileDescription(String profileDescription) { this.profileDescription = profileDescription; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Boolean getAccountLocked() { return accountLocked; }
    public void setAccountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; }
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
}
