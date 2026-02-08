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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
     * Prefix used for granted authorities that carry full LDAP group DNs.
     * These authorities allow downstream code to extract group DNs from the
     * authentication context without an additional LDAP lookup.
     */
    public static final String LDAP_GROUP_DN_PREFIX = "LDAP_GROUP_DN_";

    /**
     * Extracts LDAP group DNs from an Authentication object's granted authorities.
     * Looks for authorities prefixed with {@link #LDAP_GROUP_DN_PREFIX} and strips the prefix.
     *
     * @param authentication the Spring Security authentication (may be null)
     * @return list of group DNs (empty list if no LDAP group authorities found or auth is null)
     */
    public static List<String> extractGroupDns(org.springframework.security.core.Authentication authentication) {
        if (authentication == null) {
            return List.of();
        }
        return authentication.getAuthorities().stream()
            .map(org.springframework.security.core.GrantedAuthority::getAuthority)
            .filter(a -> a.startsWith(LDAP_GROUP_DN_PREFIX))
            .map(a -> a.substring(LDAP_GROUP_DN_PREFIX.length()))
            .collect(Collectors.toList());
    }

    /**
     * Custom LDAP authorities populator that maps LDAP groups to application roles.
     * <p>
     * In addition to creating ROLE_xxx authorities via the standard group search,
     * this populator also creates LDAP_GROUP_DN_xxx authorities carrying the full
     * group DN so that downstream code (e.g. RC access checks) can match against
     * group-based access records.
     * </p>
     * <p>
     * Group DNs are captured by overriding {@link #getGroupMembershipRoles} to
     * extract the {@code spring.security.ldap.dn} key from each search result,
     * rather than relying on the {@code memberOf} user attribute which may not
     * be returned by all LDAP servers.
     * </p>
     */
    public static class GroupMappingLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {

        private final LdapProperties ldapProperties;

        /**
         * Thread-local storage for group DNs discovered during the group membership
         * search. Populated by {@link #getGroupMembershipRoles} and consumed by
         * {@link #getAdditionalRoles}.
         */
        private final ThreadLocal<Set<String>> discoveredGroupDns = ThreadLocal.withInitial(HashSet::new);

        public GroupMappingLdapAuthoritiesPopulator(
                ContextSource contextSource,
                String groupSearchBase,
                LdapProperties ldapProperties) {
            super(contextSource, groupSearchBase);
            this.ldapProperties = ldapProperties;
        }

        /**
         * Overrides the default group membership search to capture full group DNs
         * from the search results. The DNs are stored in a thread-local set and
         * later consumed by {@link #getAdditionalRoles} to create
         * {@code LDAP_GROUP_DN_xxx} authorities.
         *
         * @param userDn   the user's distinguished name
         * @param username the user's login name
         * @return the set of role authorities from the standard group search
         */
        @Override
        public Set<GrantedAuthority> getGroupMembershipRoles(String userDn, String username) {
            // Clear any leftover state from a previous invocation on this thread
            discoveredGroupDns.get().clear();

            // Perform the standard group search
            String base = getGroupSearchBase();
            if (base != null) {
                try {
                    org.springframework.security.ldap.SpringSecurityLdapTemplate template = getLdapTemplate();
                    Set<java.util.Map<String, java.util.List<String>>> userRoles =
                        template.searchForMultipleAttributeValues(
                            base,
                            getGroupSearchFilter(),
                            new String[]{userDn, username},
                            new String[]{getGroupRoleAttribute()});

                    for (java.util.Map<String, java.util.List<String>> role : userRoles) {
                        java.util.List<String> dns = role.get(
                            org.springframework.security.ldap.SpringSecurityLdapTemplate.DN_KEY);
                        if (dns != null) {
                            for (String dn : dns) {
                                discoveredGroupDns.get().add(dn);
                                logger.debug("Discovered group DN for user {}: {}", username, dn);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error capturing group DNs for user {}: {}", username, e.getMessage());
                }
            }

            // Delegate to super for standard role authority creation
            return super.getGroupMembershipRoles(userDn, username);
        }

        /**
         * Creates additional authorities beyond those produced by the standard
         * group search. This method:
         * <ol>
         *   <li>Creates {@code LDAP_GROUP_DN_xxx} authorities for every group DN
         *       discovered during the group membership search.</li>
         *   <li>Maps configured group DNs to application roles (e.g. ADMIN, USER).</li>
         *   <li>Ensures at least a USER role is assigned.</li>
         * </ol>
         *
         * @param userData the user's LDAP directory context
         * @param username the user's login name
         * @return the set of additional authorities
         */
        @Override
        protected Set<GrantedAuthority> getAdditionalRoles(DirContextOperations userData, String username) {
            Set<GrantedAuthority> additionalRoles = new HashSet<>();

            // Retrieve group DNs captured during getGroupMembershipRoles
            Set<String> groupDns = discoveredGroupDns.get();

            // Also try the memberOf attribute on the user entry as a fallback
            String[] memberOfValues = userData.getStringAttributes("memberOf");
            if (memberOfValues != null) {
                for (String memberOf : memberOfValues) {
                    groupDns.add(memberOf);
                }
            }

            // Create LDAP_GROUP_DN_xxx authorities for each discovered group DN
            for (String groupDn : groupDns) {
                additionalRoles.add(new SimpleGrantedAuthority(LDAP_GROUP_DN_PREFIX + groupDn));
                logger.debug("Added group DN authority for user {}: {}", username, groupDn);
            }

            // Map LDAP groups to application roles based on configuration
            if (!ldapProperties.isSkipOrgRoleSync() && ldapProperties.getGroupMappings() != null) {
                for (LdapProperties.GroupMapping mapping : ldapProperties.getGroupMappings()) {
                    String configuredDn = mapping.getGroupDn();

                    for (String userGroupDn : groupDns) {
                        if (userGroupDn.equalsIgnoreCase(configuredDn)) {
                            if (mapping.getRole() != null && !mapping.getRole().isEmpty()) {
                                additionalRoles.add(new SimpleGrantedAuthority("ROLE_" + mapping.getRole()));
                                logger.debug("Mapped group {} to role {} for user {}",
                                    configuredDn, mapping.getRole(), username);
                            }
                            if (mapping.isAdmin()) {
                                additionalRoles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                                logger.debug("Granted ADMIN role to user {} via group {}",
                                    username, configuredDn);
                            }
                        }
                    }
                }
            }

            // Ensure at least USER role is assigned
            boolean hasApplicationRole = additionalRoles.stream()
                .anyMatch(a -> a.getAuthority().startsWith("ROLE_"));
            if (!hasApplicationRole) {
                additionalRoles.add(new SimpleGrantedAuthority("ROLE_USER"));
            }

            // Clean up thread-local
            discoveredGroupDns.remove();

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
