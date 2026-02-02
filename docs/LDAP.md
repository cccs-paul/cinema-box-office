# LDAP Authentication Guide

## Overview

myRC supports LDAP (Lightweight Directory Access Protocol) for enterprise authentication. This guide provides comprehensive instructions for configuring LDAP authentication with group-based permission management, similar to how Grafana handles LDAP authentication.

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration Reference](#configuration-reference)
- [Group Mapping](#group-mapping)
- [LDAP Server Examples](#ldap-server-examples)
- [SSL/TLS Configuration](#ssltls-configuration)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Docker Development](#docker-development)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Security Best Practices](#security-best-practices)

## Features

- **User Authentication**: Authenticate users against LDAP/Active Directory
- **Group-Based Permissions**: Map LDAP groups to application roles
- **RC Access Control**: Grant access to Responsibility Centres via group membership
- **Automatic User Creation**: Create application users on first LDAP login
- **Attribute Mapping**: Map LDAP attributes to user profile fields
- **SSL/TLS Support**: Secure connections with LDAPS or StartTLS
- **Failover Support**: Configure multiple LDAP servers for high availability

## Prerequisites

- LDAP server (Active Directory, OpenLDAP, 389 Directory Server, etc.)
- Server hostname/IP and port (typically 389 for LDAP, 636 for LDAPS)
- LDAP bind/manager account credentials
- Knowledge of your directory structure (base DN, user/group locations)

## Quick Start

### 1. Enable LDAP Authentication

Add the following to your \`application.yml\`:

\`\`\`yaml
app:
  security:
    login-methods:
      ldap:
        enabled: true
    ldap:
      enabled: true
      url: ldap://your-ldap-server:389
      base-dn: dc=example,dc=com
      manager-dn: cn=admin,dc=example,dc=com
      manager-password: \${LDAP_MANAGER_PASSWORD}
      user-search-base: ou=users
      user-search-filter: "(uid={0})"
      group-search-base: ou=groups
      group-search-filter: "(member={0})"
\`\`\`

### 2. Set Environment Variable

\`\`\`bash
export LDAP_MANAGER_PASSWORD=your-secure-password
\`\`\`

### 3. Test Authentication

\`\`\`bash
curl -X POST http://localhost:8080/api/users/authenticate/ldap \\
  -H "Content-Type: application/json" \\
  -d '{"username": "testuser", "password": "testpassword"}'
\`\`\`

## Configuration Reference

### Server Connection

| Property | Description | Default | Example |
|----------|-------------|---------|---------|
| \`enabled\` | Enable LDAP authentication | \`false\` | \`true\` |
| \`url\` | LDAP server URL(s) | \`ldap://localhost:389\` | \`ldap://ldap.example.com:389\` |
| \`base-dn\` | Base Distinguished Name | \`dc=example,dc=com\` | \`dc=company,dc=com\` |
| \`manager-dn\` | Manager/admin bind DN | - | \`cn=admin,dc=example,dc=com\` |
| \`manager-password\` | Manager password | - | \`\${LDAP_MANAGER_PASSWORD}\` |
| \`connect-timeout\` | Connection timeout (ms) | \`5000\` | \`10000\` |
| \`read-timeout\` | Read timeout (ms) | \`5000\` | \`10000\` |

### User Search

| Property | Description | Default | Example |
|----------|-------------|---------|---------|
| \`user-search-base\` | User search base (relative to base-dn) | \`ou=users\` | \`ou=People\` |
| \`user-search-filter\` | User search filter ({0}=username) | \`(uid={0})\` | \`(sAMAccountName={0})\` |
| \`user-dn-pattern\` | Direct bind pattern (optional) | - | \`uid={0},ou=users,dc=example,dc=com\` |

### Group Search

| Property | Description | Default | Example |
|----------|-------------|---------|---------|
| \`group-search-base\` | Group search base | \`ou=groups\` | \`ou=Groups\` |
| \`group-search-filter\` | Group search filter ({0}=user DN, {1}=username) | \`(member={0})\` | \`(memberUid={1})\` |
| \`group-name-attribute\` | Attribute for group name | \`cn\` | \`cn\` |

### User Behavior

| Property | Description | Default |
|----------|-------------|---------|
| \`allow-sign-up\` | Auto-create users on first login | \`true\` |
| \`skip-org-role-sync\` | Skip role sync from groups | \`false\` |

### Attribute Mapping

| Property | LDAP Attribute | Description |
|----------|---------------|-------------|
| \`attributes.username\` | \`uid\` | User identifier |
| \`attributes.email\` | \`mail\` | Email address |
| \`attributes.name\` | \`cn\` | Full name |
| \`attributes.surname\` | \`sn\` | Last name |
| \`attributes.given-name\` | \`givenName\` | First name |
| \`attributes.member-of\` | \`memberOf\` | Group membership |

### SSL/TLS Configuration

| Property | Description | Default |
|----------|-------------|---------|
| \`ssl.enabled\` | Enable SSL (use with ldaps://) | \`false\` |
| \`ssl.start-tls\` | Use StartTLS | \`false\` |
| \`ssl.skip-verify\` | Skip certificate verification | \`false\` |
| \`ssl.min-tls-version\` | Minimum TLS version | \`TLSv1.2\` |
| \`ssl.ca-cert-path\` | CA certificate file path | - |
| \`ssl.client-cert-path\` | Client certificate path | - |
| \`ssl.client-key-path\` | Client key path | - |

## Group Mapping

Group mapping allows you to automatically assign application roles and RC permissions based on LDAP group membership.

### Basic Role Mapping

\`\`\`yaml
app:
  security:
    ldap:
      group-mappings:
        # Map LDAP admins group to ADMIN role
        - group-dn: "cn=myrc-admins,ou=groups,dc=example,dc=com"
          role: ADMIN
          is-admin: true
        
        # Map LDAP users group to USER role
        - group-dn: "cn=myrc-users,ou=groups,dc=example,dc=com"
          role: USER
          is-admin: false
\`\`\`

### RC Access Mapping

Grant Responsibility Centre access based on group membership:

\`\`\`yaml
app:
  security:
    ldap:
      group-mappings:
        # Finance team gets read/write access to Finance RC
        - group-dn: "cn=finance-team,ou=groups,dc=example,dc=com"
          role: USER
          is-admin: false
          rc-access:
            FINANCE-RC: READ_WRITE
            
        # Auditors get read-only access to multiple RCs
        - group-dn: "cn=auditors,ou=groups,dc=example,dc=com"
          role: USER
          is-admin: false
          rc-access:
            FINANCE-RC: READ_ONLY
            OPERATIONS-RC: READ_ONLY
            HR-RC: READ_ONLY
\`\`\`

### Access Levels

- \`OWNER\`: Full control - manage permissions, delete RC
- \`READ_WRITE\`: Create and edit items
- \`READ_ONLY\`: View only

## LDAP Server Examples

### Active Directory

\`\`\`yaml
app:
  security:
    ldap:
      enabled: true
      url: ldap://ad.company.com:389
      base-dn: dc=company,dc=com
      manager-dn: cn=ldap-bind,ou=Service Accounts,dc=company,dc=com
      manager-password: \${LDAP_MANAGER_PASSWORD}
      
      # AD uses sAMAccountName for username
      user-search-base: ou=Users
      user-search-filter: "(sAMAccountName={0})"
      
      # AD group membership
      group-search-base: ou=Groups
      group-search-filter: "(member={0})"
      
      # AD attribute mapping
      attributes:
        username: sAMAccountName
        email: mail
        name: displayName
        surname: sn
        given-name: givenName
        member-of: memberOf
      
      group-mappings:
        - group-dn: "cn=MyRC-Admins,ou=Groups,dc=company,dc=com"
          role: ADMIN
          is-admin: true
        - group-dn: "cn=MyRC-Users,ou=Groups,dc=company,dc=com"
          role: USER
\`\`\`

### OpenLDAP

\`\`\`yaml
app:
  security:
    ldap:
      enabled: true
      url: ldap://openldap.company.com:389
      base-dn: dc=company,dc=com
      manager-dn: cn=admin,dc=company,dc=com
      manager-password: \${LDAP_MANAGER_PASSWORD}
      
      # OpenLDAP uses uid
      user-search-base: ou=people
      user-search-filter: "(uid={0})"
      # Optional: direct bind pattern
      user-dn-pattern: "uid={0},ou=people,dc=company,dc=com"
      
      # OpenLDAP group membership (may use memberUid)
      group-search-base: ou=groups
      group-search-filter: "(memberUid={1})"
      
      attributes:
        username: uid
        email: mail
        name: cn
        surname: sn
        given-name: givenName
\`\`\`

### 389 Directory Server

\`\`\`yaml
app:
  security:
    ldap:
      enabled: true
      url: ldap://389ds.company.com:389
      base-dn: dc=company,dc=com
      manager-dn: cn=Directory Manager
      manager-password: \${LDAP_MANAGER_PASSWORD}
      
      user-search-base: ou=People
      user-search-filter: "(uid={0})"
      
      group-search-base: ou=Groups
      group-search-filter: "(member={0})"
\`\`\`

## SSL/TLS Configuration

### LDAPS (SSL on port 636)

\`\`\`yaml
app:
  security:
    ldap:
      enabled: true
      url: ldaps://ldap.company.com:636
      base-dn: dc=company,dc=com
      
      ssl:
        enabled: true
        min-tls-version: TLSv1.2
        # Path to CA certificate (required if using self-signed certs)
        ca-cert-path: /etc/ssl/certs/ldap-ca.crt
\`\`\`

### StartTLS (upgrade connection on port 389)

\`\`\`yaml
app:
  security:
    ldap:
      enabled: true
      url: ldap://ldap.company.com:389
      base-dn: dc=company,dc=com
      
      ssl:
        enabled: false
        start-tls: true
        min-tls-version: TLSv1.2
\`\`\`

### Mutual TLS (Client Certificates)

\`\`\`yaml
app:
  security:
    ldap:
      ssl:
        enabled: true
        ca-cert-path: /etc/ssl/certs/ldap-ca.crt
        client-cert-path: /etc/ssl/certs/client.crt
        client-key-path: /etc/ssl/private/client.key
\`\`\`

## Kubernetes Deployment

### 1. Create LDAP Secret

\`\`\`bash
# Create secret with LDAP credentials
kubectl create secret generic myrc-ldap-secret \\
  --namespace=myrc \\
  --from-literal=manager-dn='cn=admin,dc=example,dc=com' \\
  --from-literal=manager-password='your-secure-password'
\`\`\`

Or use a YAML file:

\`\`\`yaml
apiVersion: v1
kind: Secret
metadata:
  name: myrc-ldap-secret
  namespace: myrc
type: Opaque
stringData:
  manager-dn: cn=admin,dc=example,dc=com
  manager-password: your-secure-password
\`\`\`

### 2. Update ConfigMap

\`\`\`yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: myrc-config
  namespace: myrc
data:
  SPRING_PROFILES_ACTIVE: "ldap"
  APP_SECURITY_LDAP_ENABLED: "true"
  APP_SECURITY_LDAP_URL: "ldap://ldap.company.com:389"
  APP_SECURITY_LDAP_BASE_DN: "dc=company,dc=com"
  APP_SECURITY_LDAP_USER_SEARCH_BASE: "ou=users"
  APP_SECURITY_LDAP_USER_SEARCH_FILTER: "(uid={0})"
  APP_SECURITY_LDAP_GROUP_SEARCH_BASE: "ou=groups"
  APP_SECURITY_LDAP_GROUP_SEARCH_FILTER: "(member={0})"
\`\`\`

### 3. Update Backend Deployment

\`\`\`yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api
  namespace: myrc
spec:
  template:
    spec:
      containers:
      - name: api
        env:
        - name: APP_SECURITY_LDAP_MANAGER_DN
          valueFrom:
            secretKeyRef:
              name: myrc-ldap-secret
              key: manager-dn
        - name: APP_SECURITY_LDAP_MANAGER_PASSWORD
          valueFrom:
            secretKeyRef:
              name: myrc-ldap-secret
              key: manager-password
        envFrom:
        - configMapRef:
            name: myrc-config
\`\`\`

### 4. Deploy

\`\`\`bash
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/configmap.yaml
kubectl rollout restart deployment/api -n myrc
\`\`\`

## Docker Development

### Using Docker Compose with OpenLDAP

The project includes a Docker Compose file for local LDAP testing:

\`\`\`bash
# Start with LDAP support
docker compose -f docker-compose.dev.yml -f docker-compose.oauth2.yml up -d
\`\`\`

### Test Users (from ldap-init.ldif)

| Username | Password | Groups |
|----------|----------|--------|
| \`testuser\` | \`testpassword123\` | users |
| \`adminuser\` | \`adminpassword123\` | users, admins |
| \`movieuser\` | \`moviepassword123\` | users, managers, staff |
| \`ticketuser\` | \`ticketpassword123\` | users |

### Manual OpenLDAP Setup

\`\`\`bash
# Run OpenLDAP in Docker
docker run -d \\
  --name openldap \\
  -p 389:389 \\
  -e LDAP_ORGANISATION="myRC" \\
  -e LDAP_DOMAIN="myrc.local" \\
  -e LDAP_ADMIN_PASSWORD="admin" \\
  osixia/openldap:latest

# Run phpLDAPadmin for management
docker run -d \\
  --name phpldapadmin \\
  -p 6443:443 \\
  --link openldap \\
  -e PHPLDAPADMIN_LDAP_HOSTS=openldap \\
  osixia/phpldapadmin:latest
\`\`\`

Access phpLDAPadmin at https://localhost:6443

## Testing

### 1. Verify LDAP Connectivity

\`\`\`bash
# Using ldapsearch
ldapsearch -x -H ldap://localhost:389 \\
  -D "cn=admin,dc=myrc,dc=local" \\
  -w "admin" \\
  -b "dc=myrc,dc=local" \\
  "objectClass=*"
\`\`\`

### 2. Test User Authentication

\`\`\`bash
# Bind as a user
ldapwhoami -x -H ldap://localhost:389 \\
  -D "uid=testuser,ou=users,dc=myrc,dc=local" \\
  -w "testpassword123"
\`\`\`

### 3. Test via API

\`\`\`bash
# Authenticate via myRC API
curl -X POST http://localhost:8080/api/users/authenticate/ldap \\
  -H "Content-Type: application/json" \\
  -d '{"username": "testuser", "password": "testpassword123"}'
\`\`\`

### 4. View Application Logs

\`\`\`bash
# Docker
docker logs -f myrc-api-dev | grep -i ldap

# Kubernetes
kubectl logs -f deployment/api -n myrc | grep -i ldap
\`\`\`

## Troubleshooting

### Connection Refused

**Symptoms**: LDAP connection fails immediately

**Solutions**:
1. Verify hostname/IP and port
2. Check firewall rules
3. Ensure LDAP server is running
4. Test with \`telnet ldap-server 389\`

### Authentication Failed

**Symptoms**: User credentials are rejected

**Solutions**:
1. Verify user exists: \`ldapsearch -x -b "base-dn" "(uid=username)"\`
2. Check user-search-filter matches your directory
3. Verify password is correct
4. Check if user account is enabled/not locked

### Groups Not Found

**Symptoms**: User authenticates but has no roles

**Solutions**:
1. Verify group-search-base is correct
2. Check group-search-filter matches your group structure
3. Ensure user is actually a member of the group
4. Check group-mappings configuration

### SSL/TLS Errors

**Symptoms**: Certificate validation failures

**Solutions**:
1. Verify certificate is valid and not expired
2. Check CA certificate is trusted
3. Use \`openssl s_client -connect ldap-server:636\` to debug
4. Temporarily set \`ssl.skip-verify: true\` for testing (not production!)

### Timeout Errors

**Symptoms**: Slow or hanging connections

**Solutions**:
1. Increase \`connect-timeout\` and \`read-timeout\`
2. Check network latency to LDAP server
3. Verify LDAP server performance

## Security Best Practices

### 1. Use LDAPS or StartTLS

Always encrypt LDAP connections in production:

\`\`\`yaml
# Option 1: LDAPS
url: ldaps://ldap.company.com:636
ssl:
  enabled: true

# Option 2: StartTLS
url: ldap://ldap.company.com:389
ssl:
  start-tls: true
\`\`\`

### 2. Use a Dedicated Bind Account

- Create a dedicated service account for myRC
- Grant minimal permissions (read-only to users/groups)
- Use a strong, unique password
- Rotate credentials regularly

### 3. Protect Credentials

- Store manager password in Kubernetes Secrets or environment variables
- Never commit passwords to version control
- Use secret management tools (Vault, AWS Secrets Manager, etc.)

### 4. Network Security

- Restrict LDAP server access to authorized clients
- Use network policies in Kubernetes
- Consider VPN or private network for LDAP access

### 5. Audit and Monitor

- Enable LDAP server audit logging
- Monitor for failed authentication attempts
- Set up alerts for suspicious activity

## References

- [Spring Security LDAP Documentation](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/ldap.html)
- [LDAP RFC 4511](https://tools.ietf.org/html/rfc4511)
- [Active Directory LDAP Guide](https://docs.microsoft.com/en-us/windows/win32/adsi/ldap-provider)
- [OpenLDAP Administrator's Guide](https://www.openldap.org/doc/admin/)
- [Grafana LDAP Configuration](https://grafana.com/docs/grafana/latest/setup-grafana/configure-security/configure-authentication/ldap/)

## Support

For issues or questions:
1. Check application logs for detailed error messages
2. Verify LDAP server connectivity
3. Review this guide and configuration reference
4. Open an issue on GitHub with:
   - Configuration (without passwords)
   - Error messages
   - LDAP server type
