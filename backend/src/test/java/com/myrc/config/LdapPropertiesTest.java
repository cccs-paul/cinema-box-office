/*
 * myRC - LDAP Properties Test
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LdapProperties configuration class.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-01
 */
@DisplayName("LdapProperties Tests")
class LdapPropertiesTest {

    private LdapProperties properties;

    @BeforeEach
    void setUp() {
        properties = new LdapProperties();
    }

    @Nested
    @DisplayName("Default Values")
    class DefaultValuesTests {

        @Test
        @DisplayName("Should have LDAP disabled by default")
        void shouldHaveLdapDisabledByDefault() {
            assertFalse(properties.isEnabled());
        }

        @Test
        @DisplayName("Should have default URL")
        void shouldHaveDefaultUrl() {
            assertEquals("ldap://localhost:389", properties.getUrl());
        }

        @Test
        @DisplayName("Should have default base DN")
        void shouldHaveDefaultBaseDn() {
            assertEquals("dc=example,dc=com", properties.getBaseDn());
        }

        @Test
        @DisplayName("Should have default user search settings")
        void shouldHaveDefaultUserSearchSettings() {
            assertEquals("ou=users", properties.getUserSearchBase());
            assertEquals("(uid={0})", properties.getUserSearchFilter());
        }

        @Test
        @DisplayName("Should have default group search settings")
        void shouldHaveDefaultGroupSearchSettings() {
            assertEquals("ou=groups", properties.getGroupSearchBase());
            assertEquals("(member={0})", properties.getGroupSearchFilter());
            assertEquals("cn", properties.getGroupNameAttribute());
        }

        @Test
        @DisplayName("Should have default timeouts")
        void shouldHaveDefaultTimeouts() {
            assertEquals(5000, properties.getConnectTimeout());
            assertEquals(5000, properties.getReadTimeout());
        }

        @Test
        @DisplayName("Should allow sign up by default")
        void shouldAllowSignUpByDefault() {
            assertTrue(properties.isAllowSignUp());
        }

        @Test
        @DisplayName("Should not skip org role sync by default")
        void shouldNotSkipOrgRoleSyncByDefault() {
            assertFalse(properties.isSkipOrgRoleSync());
        }
    }

    @Nested
    @DisplayName("Server Connection Settings")
    class ServerConnectionTests {

        @Test
        @DisplayName("Should set and get enabled flag")
        void shouldSetAndGetEnabled() {
            properties.setEnabled(true);
            assertTrue(properties.isEnabled());
            
            properties.setEnabled(false);
            assertFalse(properties.isEnabled());
        }

        @Test
        @DisplayName("Should set and get URL")
        void shouldSetAndGetUrl() {
            String url = "ldap://ldap.example.com:389";
            properties.setUrl(url);
            assertEquals(url, properties.getUrl());
        }

        @Test
        @DisplayName("Should set and get base DN")
        void shouldSetAndGetBaseDn() {
            String baseDn = "dc=company,dc=com";
            properties.setBaseDn(baseDn);
            assertEquals(baseDn, properties.getBaseDn());
        }

        @Test
        @DisplayName("Should set and get manager DN")
        void shouldSetAndGetManagerDn() {
            String managerDn = "cn=admin,dc=example,dc=com";
            properties.setManagerDn(managerDn);
            assertEquals(managerDn, properties.getManagerDn());
        }

        @Test
        @DisplayName("Should set and get manager password")
        void shouldSetAndGetManagerPassword() {
            String password = "secret123";
            properties.setManagerPassword(password);
            assertEquals(password, properties.getManagerPassword());
        }

        @Test
        @DisplayName("Should set and get connect timeout")
        void shouldSetAndGetConnectTimeout() {
            properties.setConnectTimeout(10000);
            assertEquals(10000, properties.getConnectTimeout());
        }

        @Test
        @DisplayName("Should set and get read timeout")
        void shouldSetAndGetReadTimeout() {
            properties.setReadTimeout(15000);
            assertEquals(15000, properties.getReadTimeout());
        }
    }

    @Nested
    @DisplayName("User Search Settings")
    class UserSearchTests {

        @Test
        @DisplayName("Should set and get user search base")
        void shouldSetAndGetUserSearchBase() {
            String searchBase = "ou=people";
            properties.setUserSearchBase(searchBase);
            assertEquals(searchBase, properties.getUserSearchBase());
        }

        @Test
        @DisplayName("Should set and get user search filter")
        void shouldSetAndGetUserSearchFilter() {
            String filter = "(sAMAccountName={0})";
            properties.setUserSearchFilter(filter);
            assertEquals(filter, properties.getUserSearchFilter());
        }

        @Test
        @DisplayName("Should set and get user DN pattern")
        void shouldSetAndGetUserDnPattern() {
            String pattern = "uid={0},ou=users,dc=example,dc=com";
            properties.setUserDnPattern(pattern);
            assertEquals(pattern, properties.getUserDnPattern());
        }
    }

    @Nested
    @DisplayName("Group Search Settings")
    class GroupSearchTests {

        @Test
        @DisplayName("Should set and get group search base")
        void shouldSetAndGetGroupSearchBase() {
            String searchBase = "ou=Groups";
            properties.setGroupSearchBase(searchBase);
            assertEquals(searchBase, properties.getGroupSearchBase());
        }

        @Test
        @DisplayName("Should set and get group search filter")
        void shouldSetAndGetGroupSearchFilter() {
            String filter = "(memberUid={1})";
            properties.setGroupSearchFilter(filter);
            assertEquals(filter, properties.getGroupSearchFilter());
        }

        @Test
        @DisplayName("Should set and get group name attribute")
        void shouldSetAndGetGroupNameAttribute() {
            String attribute = "name";
            properties.setGroupNameAttribute(attribute);
            assertEquals(attribute, properties.getGroupNameAttribute());
        }

        @Test
        @DisplayName("Should have default group object class filter")
        void shouldHaveDefaultGroupObjectClass() {
            String defaultValue = properties.getGroupObjectClass();
            assertNotNull(defaultValue);
            assertTrue(defaultValue.contains("objectClass=groupOfNames"));
            assertTrue(defaultValue.contains("objectClass=Group"));
        }

        @Test
        @DisplayName("Should set and get group object class")
        void shouldSetAndGetGroupObjectClass() {
            properties.setGroupObjectClass("(objectClass=Group)");
            assertEquals("(objectClass=Group)", properties.getGroupObjectClass());
        }

        @Test
        @DisplayName("Should accept simple group object class value")
        void shouldAcceptSimpleGroupObjectClassValue() {
            properties.setGroupObjectClass("posixGroup");
            assertEquals("posixGroup", properties.getGroupObjectClass());
        }

        @Test
        @DisplayName("Should accept complex filter expression for group object class")
        void shouldAcceptComplexFilterExpression() {
            String complexFilter = "(|(objectClass=groupOfNames)(objectClass=Group))";
            properties.setGroupObjectClass(complexFilter);
            assertEquals(complexFilter, properties.getGroupObjectClass());
        }
    }

    @Nested
    @DisplayName("SSL Configuration")
    class SslConfigTests {

        @Test
        @DisplayName("Should have SSL disabled by default")
        void shouldHaveSslDisabledByDefault() {
            assertFalse(properties.getSsl().isEnabled());
        }

        @Test
        @DisplayName("Should have StartTLS disabled by default")
        void shouldHaveStartTlsDisabledByDefault() {
            assertFalse(properties.getSsl().isStartTls());
        }

        @Test
        @DisplayName("Should not skip verify by default")
        void shouldNotSkipVerifyByDefault() {
            assertFalse(properties.getSsl().isSkipVerify());
        }

        @Test
        @DisplayName("Should have default min TLS version")
        void shouldHaveDefaultMinTlsVersion() {
            assertEquals("TLSv1.2", properties.getSsl().getMinTlsVersion());
        }

        @Test
        @DisplayName("Should set and get SSL enabled")
        void shouldSetAndGetSslEnabled() {
            properties.getSsl().setEnabled(true);
            assertTrue(properties.getSsl().isEnabled());
        }

        @Test
        @DisplayName("Should set and get StartTLS")
        void shouldSetAndGetStartTls() {
            properties.getSsl().setStartTls(true);
            assertTrue(properties.getSsl().isStartTls());
        }

        @Test
        @DisplayName("Should set and get skip verify")
        void shouldSetAndGetSkipVerify() {
            properties.getSsl().setSkipVerify(true);
            assertTrue(properties.getSsl().isSkipVerify());
        }

        @Test
        @DisplayName("Should set and get min TLS version")
        void shouldSetAndGetMinTlsVersion() {
            properties.getSsl().setMinTlsVersion("TLSv1.3");
            assertEquals("TLSv1.3", properties.getSsl().getMinTlsVersion());
        }

        @Test
        @DisplayName("Should set and get certificate paths")
        void shouldSetAndGetCertificatePaths() {
            properties.getSsl().setCaCertPath("/etc/ssl/ca.crt");
            properties.getSsl().setClientCertPath("/etc/ssl/client.crt");
            properties.getSsl().setClientKeyPath("/etc/ssl/client.key");

            assertEquals("/etc/ssl/ca.crt", properties.getSsl().getCaCertPath());
            assertEquals("/etc/ssl/client.crt", properties.getSsl().getClientCertPath());
            assertEquals("/etc/ssl/client.key", properties.getSsl().getClientKeyPath());
        }

        @Test
        @DisplayName("Should set SSL config object")
        void shouldSetSslConfigObject() {
            LdapProperties.SslConfig sslConfig = new LdapProperties.SslConfig();
            sslConfig.setEnabled(true);
            sslConfig.setMinTlsVersion("TLSv1.3");

            properties.setSsl(sslConfig);

            assertTrue(properties.getSsl().isEnabled());
            assertEquals("TLSv1.3", properties.getSsl().getMinTlsVersion());
        }
    }

    @Nested
    @DisplayName("Attribute Mapping")
    class AttributeMappingTests {

        @Test
        @DisplayName("Should have default attribute mappings")
        void shouldHaveDefaultAttributeMappings() {
            LdapProperties.AttributeMapping attrs = properties.getAttributes();
            assertEquals("uid", attrs.getUsername());
            assertEquals("mail", attrs.getEmail());
            assertEquals("cn", attrs.getName());
            assertEquals("sn", attrs.getSurname());
            assertEquals("givenName", attrs.getGivenName());
            assertEquals("memberOf", attrs.getMemberOf());
        }

        @Test
        @DisplayName("Should set and get username attribute")
        void shouldSetAndGetUsernameAttribute() {
            properties.getAttributes().setUsername("sAMAccountName");
            assertEquals("sAMAccountName", properties.getAttributes().getUsername());
        }

        @Test
        @DisplayName("Should set and get email attribute")
        void shouldSetAndGetEmailAttribute() {
            properties.getAttributes().setEmail("emailAddress");
            assertEquals("emailAddress", properties.getAttributes().getEmail());
        }

        @Test
        @DisplayName("Should set and get name attribute")
        void shouldSetAndGetNameAttribute() {
            properties.getAttributes().setName("displayName");
            assertEquals("displayName", properties.getAttributes().getName());
        }

        @Test
        @DisplayName("Should set and get surname attribute")
        void shouldSetAndGetSurnameAttribute() {
            properties.getAttributes().setSurname("lastName");
            assertEquals("lastName", properties.getAttributes().getSurname());
        }

        @Test
        @DisplayName("Should set and get given name attribute")
        void shouldSetAndGetGivenNameAttribute() {
            properties.getAttributes().setGivenName("firstName");
            assertEquals("firstName", properties.getAttributes().getGivenName());
        }

        @Test
        @DisplayName("Should set and get member of attribute")
        void shouldSetAndGetMemberOfAttribute() {
            properties.getAttributes().setMemberOf("groups");
            assertEquals("groups", properties.getAttributes().getMemberOf());
        }

        @Test
        @DisplayName("Should set attributes config object")
        void shouldSetAttributesConfigObject() {
            LdapProperties.AttributeMapping attrs = new LdapProperties.AttributeMapping();
            attrs.setUsername("sAMAccountName");
            attrs.setEmail("mail");

            properties.setAttributes(attrs);

            assertEquals("sAMAccountName", properties.getAttributes().getUsername());
            assertEquals("mail", properties.getAttributes().getEmail());
        }
    }

    @Nested
    @DisplayName("Group Mapping")
    class GroupMappingTests {

        @Test
        @DisplayName("Should have empty group mappings by default")
        void shouldHaveEmptyGroupMappingsByDefault() {
            assertNotNull(properties.getGroupMappings());
            assertTrue(properties.getGroupMappings().isEmpty());
        }

        @Test
        @DisplayName("Should set and get group mappings")
        void shouldSetAndGetGroupMappings() {
            LdapProperties.GroupMapping adminMapping = new LdapProperties.GroupMapping();
            adminMapping.setGroupDn("cn=admins,ou=groups,dc=example,dc=com");
            adminMapping.setRole("ADMIN");
            adminMapping.setAdmin(true);

            LdapProperties.GroupMapping userMapping = new LdapProperties.GroupMapping();
            userMapping.setGroupDn("cn=users,ou=groups,dc=example,dc=com");
            userMapping.setRole("USER");
            userMapping.setAdmin(false);

            properties.setGroupMappings(List.of(adminMapping, userMapping));

            assertEquals(2, properties.getGroupMappings().size());
            assertEquals("cn=admins,ou=groups,dc=example,dc=com", 
                properties.getGroupMappings().get(0).getGroupDn());
            assertEquals("ADMIN", properties.getGroupMappings().get(0).getRole());
            assertTrue(properties.getGroupMappings().get(0).isAdmin());
        }

        @Test
        @DisplayName("Should set and get RC access in group mapping")
        void shouldSetAndGetRcAccessInGroupMapping() {
            LdapProperties.GroupMapping mapping = new LdapProperties.GroupMapping();
            mapping.setGroupDn("cn=finance,ou=groups,dc=example,dc=com");
            mapping.setRole("USER");

            Map<String, String> rcAccess = new HashMap<>();
            rcAccess.put("FINANCE-RC", "READ_WRITE");
            rcAccess.put("HR-RC", "READ_ONLY");
            mapping.setRcAccess(rcAccess);

            properties.setGroupMappings(List.of(mapping));

            assertEquals(2, properties.getGroupMappings().get(0).getRcAccess().size());
            assertEquals("READ_WRITE", properties.getGroupMappings().get(0).getRcAccess().get("FINANCE-RC"));
            assertEquals("READ_ONLY", properties.getGroupMappings().get(0).getRcAccess().get("HR-RC"));
        }

        @Test
        @DisplayName("Should have empty RC access by default in group mapping")
        void shouldHaveEmptyRcAccessByDefault() {
            LdapProperties.GroupMapping mapping = new LdapProperties.GroupMapping();
            assertNotNull(mapping.getRcAccess());
            assertTrue(mapping.getRcAccess().isEmpty());
        }
    }

    @Nested
    @DisplayName("User Behavior Settings")
    class UserBehaviorTests {

        @Test
        @DisplayName("Should set and get allow sign up")
        void shouldSetAndGetAllowSignUp() {
            properties.setAllowSignUp(false);
            assertFalse(properties.isAllowSignUp());

            properties.setAllowSignUp(true);
            assertTrue(properties.isAllowSignUp());
        }

        @Test
        @DisplayName("Should set and get skip org role sync")
        void shouldSetAndGetSkipOrgRoleSync() {
            properties.setSkipOrgRoleSync(true);
            assertTrue(properties.isSkipOrgRoleSync());

            properties.setSkipOrgRoleSync(false);
            assertFalse(properties.isSkipOrgRoleSync());
        }
    }
}
