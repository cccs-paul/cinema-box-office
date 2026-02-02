/*
 * myRC - LDAP Security Configuration
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * LDAP authentication configuration for the application.
 * Provides comprehensive LDAP authentication with group-based role mapping,
 * similar to Grafana's LDAP authentication approach.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>User authentication via LDAP bind</li>
 *   <li>Group membership lookup</li>
 *   <li>Group-to-role mapping</li>
 *   <li>Configurable attribute mapping</li>
 *   <li>SSL/TLS support</li>
 * </ul>
 *
 * @author myRC Team
 * @version 2.0.0
 * @since 2026-01-16
 */
@Configuration
@ConditionalOnProperty(
    name = "app.security.ldap.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class LdapSecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(LdapSecurityConfig.class);

    private final LdapProperties ldapProperties;

    public LdapSecurityConfig(LdapProperties ldapProperties) {
        this.ldapProperties = ldapProperties;
        logger.info("LDAP authentication enabled with server: {}", ldapProperties.getUrl());
    }

    /**
     * Configures the LDAP context source with connection pooling and timeout settings.
     *
     * @return configured DefaultSpringSecurityContextSource
     */
    @Bean
    public DefaultSpringSecurityContextSource ldapContextSource() {
        String providerUrl = ldapProperties.getUrl();
        if (!providerUrl.endsWith("/")) {
            providerUrl += "/";
        }
        providerUrl += ldapProperties.getBaseDn();

        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(providerUrl);
        
        // Set manager credentials for search operations
        if (ldapProperties.getManagerDn() != null && !ldapProperties.getManagerDn().isEmpty()) {
            contextSource.setUserDn(ldapProperties.getManagerDn());
            contextSource.setPassword(ldapProperties.getManagerPassword());
        }

        // Configure connection pool and timeouts
        Map<String, Object> baseEnvironment = new HashMap<>();
        baseEnvironment.put("com.sun.jndi.ldap.connect.timeout", 
            String.valueOf(ldapProperties.getConnectTimeout()));
        baseEnvironment.put("com.sun.jndi.ldap.read.timeout", 
            String.valueOf(ldapProperties.getReadTimeout()));
        
        // Configure connection pooling
        baseEnvironment.put("com.sun.jndi.ldap.connect.pool", "true");
        
        contextSource.setBaseEnvironmentProperties(baseEnvironment);
        
        logger.debug("LDAP context source configured with base DN: {}", ldapProperties.getBaseDn());
        return contextSource;
    }

    /**
     * Configures the LDAP user search for finding users.
     *
     * @param contextSource the LDAP context source
     * @return configured FilterBasedLdapUserSearch
     */
    @Bean
    public FilterBasedLdapUserSearch ldapUserSearch(DefaultSpringSecurityContextSource contextSource) {
        return new FilterBasedLdapUserSearch(
            ldapProperties.getUserSearchBase(),
            ldapProperties.getUserSearchFilter(),
            contextSource
        );
    }

    /**
     * Configures the bind authenticator for LDAP authentication.
     *
     * @param contextSource the LDAP context source
     * @param ldapUserSearch the user search configuration
     * @return configured BindAuthenticator
     */
    @Bean
    public BindAuthenticator ldapBindAuthenticator(
            DefaultSpringSecurityContextSource contextSource,
            FilterBasedLdapUserSearch ldapUserSearch) {
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserSearch(ldapUserSearch);
        
        // If user DN pattern is specified, use it for direct binding
        if (ldapProperties.getUserDnPattern() != null && !ldapProperties.getUserDnPattern().isEmpty()) {
            authenticator.setUserDnPatterns(new String[]{ldapProperties.getUserDnPattern()});
        }
        
        return authenticator;
    }

    /**
     * Configures the LDAP authorities populator for group-based role mapping.
     * Maps LDAP groups to application roles based on configuration.
     *
     * @param contextSource the LDAP context source
     * @return configured LdapAuthoritiesPopulator
     */
    @Bean
    public LdapAuthoritiesPopulator ldapAuthoritiesPopulator(DefaultSpringSecurityContextSource contextSource) {
        GroupMappingLdapAuthoritiesPopulator authoritiesPopulator = 
            new GroupMappingLdapAuthoritiesPopulator(
                contextSource,
                ldapProperties.getGroupSearchBase(),
                ldapProperties
            );
        
        authoritiesPopulator.setGroupSearchFilter(ldapProperties.getGroupSearchFilter());
        authoritiesPopulator.setGroupRoleAttribute(ldapProperties.getGroupNameAttribute());
        authoritiesPopulator.setSearchSubtree(true);
        authoritiesPopulator.setConvertToUpperCase(false);
        
        return authoritiesPopulator;
    }

    /**
     * Custom LDAP authorities populator that maps LDAP groups to application roles.
     */
    public static class GroupMappingLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {
        
        private final LdapProperties ldapProperties;
        
        public GroupMappingLdapAuthoritiesPopulator(
                ContextSource contextSource, 
                String groupSearchBase,
                LdapProperties ldapProperties) {
            super(contextSource, groupSearchBase);
            this.ldapProperties = ldapProperties;
        }
        
        @Override
        protected Set<GrantedAuthority> getAdditionalRoles(DirContextOperations userData, String username) {
            Set<GrantedAuthority> additionalRoles = new HashSet<>();
            
            // Get the user's groups from LDAP
            String[] groups = userData.getStringAttributes("memberOf");
            
            // Map LDAP groups to application roles based on configuration
            if (!ldapProperties.isSkipOrgRoleSync() && ldapProperties.getGroupMappings() != null && groups != null) {
                for (LdapProperties.GroupMapping mapping : ldapProperties.getGroupMappings()) {
                    String groupDn = mapping.getGroupDn();
                    
                    for (String userGroup : groups) {
                        // Check if user is member of this group
                        if (userGroup.equalsIgnoreCase(groupDn)) {
                            // Add the mapped role
                            if (mapping.getRole() != null && !mapping.getRole().isEmpty()) {
                                additionalRoles.add(new SimpleGrantedAuthority("ROLE_" + mapping.getRole()));
                                logger.debug("Mapped group {} to role {} for user {}", 
                                    groupDn, mapping.getRole(), username);
                            }
                            
                            // Add admin role if specified
                            if (mapping.isAdmin()) {
                                additionalRoles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                                logger.debug("Granted ADMIN role to user {} via group {}", 
                                    username, groupDn);
                            }
                        }
                    }
                }
            }
            
            // Ensure at least USER role is assigned
            if (additionalRoles.isEmpty()) {
                additionalRoles.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
            
            return additionalRoles;
        }
        
        private static final Logger logger = LoggerFactory.getLogger(GroupMappingLdapAuthoritiesPopulator.class);
    }

    /**
     * Configures the LDAP user details mapper for extracting user information.
     *
     * @return configured LdapUserDetailsMapper
     */
    @Bean
    public LdapUserDetailsMapper ldapUserDetailsMapper() {
        LdapUserDetailsMapper mapper = new LdapUserDetailsMapper();
        // Map password attribute (usually not returned for security)
        mapper.setPasswordAttributeName("userPassword");
        return mapper;
    }

    /**
     * Configures the LDAP authentication provider.
     *
     * @param authenticator the bind authenticator
     * @param authoritiesPopulator the authorities populator
     * @return configured LdapAuthenticationProvider
     */
    @Bean
    public LdapAuthenticationProvider ldapAuthenticationProvider(
            BindAuthenticator authenticator,
            LdapAuthoritiesPopulator authoritiesPopulator) {
        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(
            authenticator, authoritiesPopulator);
        provider.setUserDetailsContextMapper(ldapUserDetailsMapper());
        return provider;
    }

    /**
     * Provides access to LDAP properties for other components.
     *
     * @return the LDAP properties
     */
    public LdapProperties getLdapProperties() {
        return ldapProperties;
    }
}
