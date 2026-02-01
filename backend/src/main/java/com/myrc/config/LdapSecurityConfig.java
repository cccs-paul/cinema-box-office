/*
 * myRC - LDAP Security Configuration
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.userdetails.LdapUserDetailsManager;

/**
 * LDAP authentication configuration for the application.
 * Enables LDAP-based user authentication when configured.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-16
 */
@Configuration
@ConditionalOnProperty(
    name = "app.security.ldap.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class LdapSecurityConfig {

    /**
     * Configures LDAP context source.
     *
     * @return configured LdapContextSource
     */
    @Bean
    public LdapContextSource ldapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://localhost:8389");
        contextSource.setBase("dc=example,dc=com");
        contextSource.setUserDn("cn=admin,dc=example,dc=com");
        contextSource.setPassword("password");
        return contextSource;
    }

    /**
     * Configures LDAP user details manager for LDAP authentication.
     *
     * @param contextSource the LDAP context source
     * @return configured LdapUserDetailsManager
     */
    @Bean
    public LdapUserDetailsManager ldapUserDetailsManager(ContextSource contextSource) {
        return new LdapUserDetailsManager(contextSource);
    }
}
