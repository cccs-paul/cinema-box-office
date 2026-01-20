package com.boxoffice.dto;

public class CreateUserRequest {
    private String username;
    private String email;
    private String fullName;
    private String password;
    private String authProvider;
    private String externalId;
    private String oauthProvider;
    private java.util.Set<String> roles;
    private String profileDescription;

    public CreateUserRequest() {}

    public CreateUserRequest(String username, String email, String fullName, String password, 
                            String authProvider, String externalId, String oauthProvider,
                            java.util.Set<String> roles, String profileDescription) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.authProvider = authProvider;
        this.externalId = externalId;
        this.oauthProvider = oauthProvider;
        this.roles = roles;
        this.profileDescription = profileDescription;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getOauthProvider() { return oauthProvider; }
    public void setOauthProvider(String oauthProvider) { this.oauthProvider = oauthProvider; }
    public java.util.Set<String> getRoles() { return roles; }
    public void setRoles(java.util.Set<String> roles) { this.roles = roles; }
    public String getProfileDescription() { return profileDescription; }
    public void setProfileDescription(String profileDescription) { this.profileDescription = profileDescription; }

    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               authProvider != null && !authProvider.trim().isEmpty();
    }
}
