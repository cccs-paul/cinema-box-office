/*
 * myRC - Login Methods DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Data Transfer Object for login methods configuration.
 */

package com.myrc.dto;

/**
 * DTO representing available login methods configuration.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-31
 */
public class LoginMethodsDTO {

    private AppAccountConfig appAccount;
    private boolean ldapEnabled;
    private boolean oauth2Enabled;

    /**
     * App Account configuration details.
     */
    public static class AppAccountConfig {
        private boolean enabled;
        private boolean allowRegistration;

        public AppAccountConfig() {}

        public AppAccountConfig(boolean enabled, boolean allowRegistration) {
            this.enabled = enabled;
            this.allowRegistration = allowRegistration;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isAllowRegistration() {
            return allowRegistration;
        }

        public void setAllowRegistration(boolean allowRegistration) {
            this.allowRegistration = allowRegistration;
        }
    }

    public LoginMethodsDTO() {}

    public LoginMethodsDTO(AppAccountConfig appAccount, boolean ldapEnabled, boolean oauth2Enabled) {
        this.appAccount = appAccount;
        this.ldapEnabled = ldapEnabled;
        this.oauth2Enabled = oauth2Enabled;
    }

    public AppAccountConfig getAppAccount() {
        return appAccount;
    }

    public void setAppAccount(AppAccountConfig appAccount) {
        this.appAccount = appAccount;
    }

    public boolean isLdapEnabled() {
        return ldapEnabled;
    }

    public void setLdapEnabled(boolean ldapEnabled) {
        this.ldapEnabled = ldapEnabled;
    }

    public boolean isOauth2Enabled() {
        return oauth2Enabled;
    }

    public void setOauth2Enabled(boolean oauth2Enabled) {
        this.oauth2Enabled = oauth2Enabled;
    }
}
