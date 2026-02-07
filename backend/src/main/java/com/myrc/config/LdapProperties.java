/*
 * myRC - LDAP Configuration Properties
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Configuration properties for LDAP authentication and group mapping.
 * Similar to Grafana's LDAP configuration approach.
 */

package com.myrc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for LDAP authentication.
 * Provides comprehensive LDAP settings including server connection,
 * user/group search, attribute mapping, and group-to-role mappings.
 *
 * <p>Example YAML configuration:</p>
 * <pre>
 * app:
 *   security:
 *     ldap:
 *       enabled: true
 *       url: ldap://ldap.example.com:389
 *       base-dn: dc=example,dc=com
 *       user-search-base: ou=users
 *       user-search-filter: "(uid={0})"
 *       group-search-base: ou=groups
 *       group-search-filter: "(member={0})"
 *       manager-dn: cn=admin,dc=example,dc=com
 *       manager-password: admin_password
 *       attributes:
 *         username: uid
 *         email: mail
 *         name: cn
 *         surname: sn
 *         member-of: memberOf
 *       group-mappings:
 *         - group-dn: "cn=admins,ou=groups,dc=example,dc=com"
 *           role: ADMIN
 *         - group-dn: "cn=users,ou=groups,dc=example,dc=com"
 *           role: USER
 * </pre>
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-01
 */
@Component
@ConfigurationProperties(prefix = "app.security.ldap")
public class LdapProperties {

    /**
     * Enable or disable LDAP authentication.
     */
    private boolean enabled = false;

    /**
     * LDAP server URL (e.g., ldap://ldap.example.com:389 or ldaps://ldap.example.com:636).
     * Multiple URLs can be space-separated for failover.
     */
    private String url = "ldap://localhost:389";

    /**
     * Base DN for all LDAP operations.
     */
    private String baseDn = "dc=example,dc=com";

    /**
     * Search base for users, relative to base-dn.
     */
    private String userSearchBase = "ou=users";

    /**
     * Search filter for users. Use {0} as placeholder for username.
     */
    private String userSearchFilter = "(uid={0})";

    /**
     * User DN pattern for direct binding. Use {0} as placeholder for username.
     * If set, this is used instead of search+bind.
     */
    private String userDnPattern;

    /**
     * Search base for groups, relative to base-dn.
     */
    private String groupSearchBase = "ou=groups";

    /**
     * Search filter for groups. Use {0} for user DN, {1} for username.
     */
    private String groupSearchFilter = "(member={0})";

    /**
     * Attribute that contains the group name.
     */
    private String groupNameAttribute = "cn";

    /**
     * LDAP objectClass used for group entries.
     * Common values: "groupOfNames", "groupOfUniqueNames", "Group", "posixGroup".
     * Defaults to a broad filter matching the most common types.
     */
    private String groupObjectClass = "(|(objectClass=groupOfNames)(objectClass=groupOfUniqueNames)(objectClass=Group)(objectClass=posixGroup))";

    /**
     * Search base for distribution lists, relative to base-dn.
     */
    private String distributionListSearchBase = "ou=distribution-lists";

    /**
     * DN of the manager/admin user for binding to LDAP server.
     * Required for search+bind authentication mode.
     */
    private String managerDn;

    /**
     * Password for the manager/admin user.
     */
    private String managerPassword;

    /**
     * Connection timeout in milliseconds.
     */
    private int connectTimeout = 5000;

    /**
     * Read timeout in milliseconds.
     */
    private int readTimeout = 5000;

    /**
     * Allow new users to be created when they authenticate via LDAP.
     */
    private boolean allowSignUp = true;

    /**
     * Skip organization/RC role sync from LDAP groups.
     * When true, group mappings are ignored.
     */
    private boolean skipOrgRoleSync = false;

    /**
     * SSL/TLS configuration.
     */
    private SslConfig ssl = new SslConfig();

    /**
     * Attribute mappings from LDAP to application user fields.
     */
    private AttributeMapping attributes = new AttributeMapping();

    /**
     * Group to role mappings.
     */
    private List<GroupMapping> groupMappings = new ArrayList<>();

    /**
     * SSL/TLS configuration for LDAP connections.
     */
    public static class SslConfig {
        /**
         * Enable SSL (LDAPS).
         */
        private boolean enabled = false;

        /**
         * Use StartTLS for securing connection.
         */
        private boolean startTls = false;

        /**
         * Skip SSL certificate verification (not recommended for production).
         */
        private boolean skipVerify = false;

        /**
         * Minimum TLS version (TLSv1.2 or TLSv1.3).
         */
        private String minTlsVersion = "TLSv1.2";

        /**
         * Path to CA certificate file.
         */
        private String caCertPath;

        /**
         * Path to client certificate file.
         */
        private String clientCertPath;

        /**
         * Path to client key file.
         */
        private String clientKeyPath;

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isStartTls() {
            return startTls;
        }

        public void setStartTls(boolean startTls) {
            this.startTls = startTls;
        }

        public boolean isSkipVerify() {
            return skipVerify;
        }

        public void setSkipVerify(boolean skipVerify) {
            this.skipVerify = skipVerify;
        }

        public String getMinTlsVersion() {
            return minTlsVersion;
        }

        public void setMinTlsVersion(String minTlsVersion) {
            this.minTlsVersion = minTlsVersion;
        }

        public String getCaCertPath() {
            return caCertPath;
        }

        public void setCaCertPath(String caCertPath) {
            this.caCertPath = caCertPath;
        }

        public String getClientCertPath() {
            return clientCertPath;
        }

        public void setClientCertPath(String clientCertPath) {
            this.clientCertPath = clientCertPath;
        }

        public String getClientKeyPath() {
            return clientKeyPath;
        }

        public void setClientKeyPath(String clientKeyPath) {
            this.clientKeyPath = clientKeyPath;
        }
    }

    /**
     * Attribute mapping from LDAP attributes to application user fields.
     */
    public static class AttributeMapping {
        /**
         * LDAP attribute for username.
         */
        private String username = "uid";

        /**
         * LDAP attribute for email address.
         */
        private String email = "mail";

        /**
         * LDAP attribute for full name/display name.
         */
        private String name = "cn";

        /**
         * LDAP attribute for surname/last name.
         */
        private String surname = "sn";

        /**
         * LDAP attribute for given name/first name.
         */
        private String givenName = "givenName";

        /**
         * LDAP attribute for group membership.
         */
        private String memberOf = "memberOf";

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSurname() {
            return surname;
        }

        public void setSurname(String surname) {
            this.surname = surname;
        }

        public String getGivenName() {
            return givenName;
        }

        public void setGivenName(String givenName) {
            this.givenName = givenName;
        }

        public String getMemberOf() {
            return memberOf;
        }

        public void setMemberOf(String memberOf) {
            this.memberOf = memberOf;
        }
    }

    /**
     * Mapping from LDAP group to application role.
     */
    public static class GroupMapping {
        /**
         * Full DN of the LDAP group.
         */
        private String groupDn;

        /**
         * Application role to assign (ADMIN, USER).
         */
        private String role;

        /**
         * Whether members of this group are granted admin privileges.
         */
        private boolean isAdmin = false;

        /**
         * RC access mappings for this group.
         * Maps RC codes to access levels.
         */
        private Map<String, String> rcAccess = new HashMap<>();

        // Getters and setters
        public String getGroupDn() {
            return groupDn;
        }

        public void setGroupDn(String groupDn) {
            this.groupDn = groupDn;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public boolean isAdmin() {
            return isAdmin;
        }

        public void setAdmin(boolean admin) {
            isAdmin = admin;
        }

        public Map<String, String> getRcAccess() {
            return rcAccess;
        }

        public void setRcAccess(Map<String, String> rcAccess) {
            this.rcAccess = rcAccess;
        }
    }

    // Main class getters and setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public String getUserSearchBase() {
        return userSearchBase;
    }

    public void setUserSearchBase(String userSearchBase) {
        this.userSearchBase = userSearchBase;
    }

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public void setUserSearchFilter(String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
    }

    public String getUserDnPattern() {
        return userDnPattern;
    }

    public void setUserDnPattern(String userDnPattern) {
        this.userDnPattern = userDnPattern;
    }

    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    public void setGroupSearchBase(String groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
    }

    public String getGroupSearchFilter() {
        return groupSearchFilter;
    }

    public void setGroupSearchFilter(String groupSearchFilter) {
        this.groupSearchFilter = groupSearchFilter;
    }

    public String getGroupNameAttribute() {
        return groupNameAttribute;
    }

    public void setGroupNameAttribute(String groupNameAttribute) {
        this.groupNameAttribute = groupNameAttribute;
    }

    public String getGroupObjectClass() {
        return groupObjectClass;
    }

    public void setGroupObjectClass(String groupObjectClass) {
        this.groupObjectClass = groupObjectClass;
    }

    public String getDistributionListSearchBase() {
        return distributionListSearchBase;
    }

    public void setDistributionListSearchBase(String distributionListSearchBase) {
        this.distributionListSearchBase = distributionListSearchBase;
    }

    public String getManagerDn() {
        return managerDn;
    }

    public void setManagerDn(String managerDn) {
        this.managerDn = managerDn;
    }

    public String getManagerPassword() {
        return managerPassword;
    }

    public void setManagerPassword(String managerPassword) {
        this.managerPassword = managerPassword;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public boolean isAllowSignUp() {
        return allowSignUp;
    }

    public void setAllowSignUp(boolean allowSignUp) {
        this.allowSignUp = allowSignUp;
    }

    public boolean isSkipOrgRoleSync() {
        return skipOrgRoleSync;
    }

    public void setSkipOrgRoleSync(boolean skipOrgRoleSync) {
        this.skipOrgRoleSync = skipOrgRoleSync;
    }

    public SslConfig getSsl() {
        return ssl;
    }

    public void setSsl(SslConfig ssl) {
        this.ssl = ssl;
    }

    public AttributeMapping getAttributes() {
        return attributes;
    }

    public void setAttributes(AttributeMapping attributes) {
        this.attributes = attributes;
    }

    public List<GroupMapping> getGroupMappings() {
        return groupMappings;
    }

    public void setGroupMappings(List<GroupMapping> groupMappings) {
        this.groupMappings = groupMappings;
    }
}
