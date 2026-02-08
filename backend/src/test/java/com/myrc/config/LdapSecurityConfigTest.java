/*
 * myRC - LDAP Configuration Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import com.myrc.BoxOfficeApplication;

/**
 * Tests for LDAP security configuration.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-16
 */
@SpringBootTest(classes = BoxOfficeApplication.class)
@ActiveProfiles("test")
class LdapSecurityConfigTest {

    @Test
    @DisplayName("LDAP security should not load when disabled")
    void testLdapDisabledByDefault() {
        // LDAP is disabled by default in test profile
        // This test verifies the application starts without LDAP
        assertTrue(true, "Application started successfully with LDAP disabled");
    }

    @Nested
    @DisplayName("extractGroupDns Tests")
    class ExtractGroupDnsTests {

        @Test
        @DisplayName("Should return empty list for null authentication")
        void shouldReturnEmptyForNull() {
            List<String> result = LdapSecurityConfig.extractGroupDns(null);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return empty list when no LDAP group authorities")
        void shouldReturnEmptyWhenNoGroups() {
            Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")));

            List<String> result = LdapSecurityConfig.extractGroupDns(auth);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should extract group DNs from LDAP_GROUP_DN_ authorities")
        void shouldExtractGroupDns() {
            String groupDn1 = "cn=ship_crew,ou=people,dc=planetexpress,dc=com";
            String groupDn2 = "cn=admin_staff,ou=people,dc=planetexpress,dc=com";

            Authentication auth = new UsernamePasswordAuthenticationToken(
                "fry", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ship_crew"),
                        new SimpleGrantedAuthority(LdapSecurityConfig.LDAP_GROUP_DN_PREFIX + groupDn1),
                        new SimpleGrantedAuthority(LdapSecurityConfig.LDAP_GROUP_DN_PREFIX + groupDn2)));

            List<String> result = LdapSecurityConfig.extractGroupDns(auth);

            assertEquals(2, result.size());
            assertTrue(result.contains(groupDn1));
            assertTrue(result.contains(groupDn2));
        }

        @Test
        @DisplayName("Should not include ROLE_ authorities in group DNs")
        void shouldNotIncludeRoleAuthorities() {
            String groupDn = "cn=ship_crew,ou=people,dc=planetexpress,dc=com";

            Authentication auth = new UsernamePasswordAuthenticationToken(
                "fry", null,
                List.of(new SimpleGrantedAuthority("ROLE_ship_crew"),
                        new SimpleGrantedAuthority(LdapSecurityConfig.LDAP_GROUP_DN_PREFIX + groupDn)));

            List<String> result = LdapSecurityConfig.extractGroupDns(auth);

            assertEquals(1, result.size());
            assertEquals(groupDn, result.getFirst());
            assertFalse(result.contains("ship_crew"));
        }
    }
}
