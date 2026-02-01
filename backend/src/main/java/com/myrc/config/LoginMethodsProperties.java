/*
 * myRC - Login Methods Configuration Properties
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Configuration properties for enabling/disabling login methods.
 */

package com.myrc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for login methods.
 * Allows enabling/disabling App Account, LDAP, and OAuth2 authentication methods.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-31
 */
@Component
@ConfigurationProperties(prefix = "app.security.login-methods")
public class LoginMethodsProperties {

    private AppAccountConfig appAccount = new AppAccountConfig();
    private LdapConfig ldap = new LdapConfig();
    private OAuth2Config oauth2 = new OAuth2Config();

    /**
     * App Account (LOCAL) authentication configuration.
     */
    public static class AppAccountConfig {
        private boolean enabled = true;
        private boolean allowRegistration = true;

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

    /**
     * LDAP authentication configuration.
     */
    public static class LdapConfig {
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * OAuth2 authentication configuration.
     */
    public static class OAuth2Config {
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public AppAccountConfig getAppAccount() {
        return appAccount;
    }

    public void setAppAccount(AppAccountConfig appAccount) {
        this.appAccount = appAccount;
    }

    public LdapConfig getLdap() {
        return ldap;
    }

    public void setLdap(LdapConfig ldap) {
        this.ldap = ldap;
    }

    public OAuth2Config getOauth2() {
        return oauth2;
    }

    public void setOauth2(OAuth2Config oauth2) {
        this.oauth2 = oauth2;
    }
}
