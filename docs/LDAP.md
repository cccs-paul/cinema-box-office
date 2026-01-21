# LDAP Integration Guide

## Overview

myRC supports LDAP (Lightweight Directory Access Protocol) for enterprise authentication. This guide covers configuring LDAP authentication with your Kubernetes deployment.

## Prerequisites

- Active Directory or OpenLDAP server
- Server hostname/IP and port (typically 389 for unencrypted, 636 for LDAPS)
- LDAP bind user credentials
- User search base DN (Distinguished Name)
- Group search base DN (optional)

## Configuration

### 1. Prepare LDAP Credentials

Create a secret with your LDAP credentials:

```bash
# Base64 encode your LDAP credentials
echo -n "cn=admin,dc=example,dc=com" | base64
echo -n "your-password" | base64

# Update k8s/secrets.yaml with the encoded values
```

### 2. Update Backend Configuration

Edit `k8s/configmap.yaml` to enable LDAP:

```yaml
data:
  SPRING_PROFILES_ACTIVE: "ldap"
  
  # LDAP Server Configuration
  LDAP_URL: "ldap://ldap.example.com:389"
  LDAP_BASE_DN: "dc=example,dc=com"
  LDAP_USER_DN_PATTERN: "uid={0},ou=users,dc=example,dc=com"
  LDAP_GROUP_SEARCH_BASE: "ou=groups,dc=example,dc=com"
  LDAP_GROUP_SEARCH_FILTER: "(memberUid={1})"
```

### 3. Update Secrets

Edit `k8s/secrets.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: cinema-box-office-ldap-secret
  namespace: cinema-box-office
type: Opaque
data:
  ldap-user-dn: Y249YWRtaW4sZGM9ZXhhbXBsZSxkYz1jb20=
  ldap-password: eW91ci1wYXNzd29yZA==
```

### 4. Add Environment Variables to Backend Deployment

Edit `k8s/backend.yaml` deployment spec:

```yaml
containers:
- name: api
  env:
  - name: APP_SECURITY_LDAP_ENABLED
    value: "true"
  - name: APP_SECURITY_LDAP_URL
    valueFrom:
      configMapKeyRef:
        name: cinema-box-office-config
        key: LDAP_URL
  - name: APP_SECURITY_LDAP_BASE_DN
    valueFrom:
      configMapKeyRef:
        name: cinema-box-office-config
        key: LDAP_BASE_DN
  - name: APP_SECURITY_LDAP_USER_DN_PATTERN
    valueFrom:
      configMapKeyRef:
        name: cinema-box-office-config
        key: LDAP_USER_DN_PATTERN
  - name: APP_SECURITY_LDAP_BIND_DN
    valueFrom:
      secretKeyRef:
        name: cinema-box-office-ldap-secret
        key: ldap-user-dn
  - name: APP_SECURITY_LDAP_BIND_PASSWORD
    valueFrom:
      secretKeyRef:
        name: cinema-box-office-ldap-secret
        key: ldap-password
```

## LDAP Configuration Examples

### Active Directory

```yaml
LDAP_URL: "ldap://ad.company.com:389"
LDAP_BASE_DN: "dc=company,dc=com"
LDAP_USER_DN_PATTERN: "{0}@company.com"
LDAP_GROUP_SEARCH_BASE: "ou=Groups,dc=company,dc=com"
LDAP_GROUP_SEARCH_FILTER: "(member={0})"
```

### OpenLDAP

```yaml
LDAP_URL: "ldap://ldap.example.com:389"
LDAP_BASE_DN: "dc=example,dc=com"
LDAP_USER_DN_PATTERN: "uid={0},ou=users,dc=example,dc=com"
LDAP_GROUP_SEARCH_BASE: "ou=groups,dc=example,dc=com"
LDAP_GROUP_SEARCH_FILTER: "(memberUid={1})"
```

### LDAPS (Secure)

For encrypted LDAP connections on port 636:

```yaml
LDAP_URL: "ldaps://ldap.example.com:636"
APP_SECURITY_LDAP_SSL_ENABLED: "true"
# Optional: Trust all certificates (not recommended for production)
APP_SECURITY_LDAP_SSL_TRUST_ALL: "false"
```

## Testing LDAP Configuration

### 1. Verify LDAP Connectivity

```bash
# From within the pod
kubectl exec -it deployment/api -n cinema-box-office -- /bin/bash

# Test LDAP connection
ldapsearch -x -H ldap://ldap.example.com:389 \
  -D "cn=admin,dc=example,dc=com" \
  -w "password" \
  -b "dc=example,dc=com" \
  "uid=testuser"
```

### 2. Check Application Logs

```bash
# View backend logs for LDAP errors
kubectl logs -f deployment/api -n cinema-box-office | grep -i ldap
```

### 3. Test Authentication

```bash
# Test with curl
curl -u username:password http://localhost:8080/api/health

# Check response
# 200 OK = authentication successful
# 401 Unauthorized = authentication failed
```

## LDAP Configuration in Backend

The backend uses Spring Security LDAP authentication. See `backend/src/main/java/com/boxoffice/config/LdapSecurityConfig.java` for implementation details.

### Key Configuration Properties

| Property | Description | Example |
|----------|-------------|---------|
| `app.security.ldap.enabled` | Enable/disable LDAP | `true` |
| `app.security.ldap.url` | LDAP server URL | `ldap://ldap.example.com:389` |
| `app.security.ldap.base-dn` | Base Distinguished Name | `dc=example,dc=com` |
| `app.security.ldap.user-dn-pattern` | User DN pattern | `uid={0},ou=users,dc=example,dc=com` |
| `app.security.ldap.bind-dn` | Bind DN for server connection | `cn=admin,dc=example,dc=com` |
| `app.security.ldap.bind-password` | Bind password | `password` |
| `app.security.ldap.group-search-base` | Group search base | `ou=groups,dc=example,dc=com` |
| `app.security.ldap.group-search-filter` | Group filter | `(memberUid={1})` |

## Deployment

### Apply Configuration

```bash
# Update secrets
kubectl apply -f k8s/secrets.yaml

# Update configmap
kubectl apply -f k8s/configmap.yaml

# Restart backend deployment to pick up changes
kubectl rollout restart deployment/api -n cinema-box-office

# Check rollout status
kubectl rollout status deployment/api -n cinema-box-office
```

### Troubleshooting

**LDAP connection refused:**
- Check server hostname/IP and port
- Verify firewall rules
- Check bind user credentials

**User authentication fails:**
- Verify user exists in LDAP directory
- Check user DN pattern matches your directory structure
- Review backend logs for detailed error messages

**Groups not working:**
- Verify group search base DN is correct
- Check group search filter matches your group structure
- Ensure user is member of appropriate groups

## Local Development with Docker

For testing LDAP locally, you can use a containerized LDAP server:

```bash
# Run OpenLDAP in Docker
docker run -d \
  --name openldap \
  -p 389:389 \
  -e LDAP_ORGANIZATION="Example" \
  -e LDAP_DOMAIN="example.com" \
  -e LDAP_ADMIN_PASSWORD="admin" \
  osixia/openldap:latest

# Run phpLDAPadmin for management
docker run -d \
  --name phpldapadmin \
  -p 6443:443 \
  -e PHPLDAPADMIN_LDAP_HOSTS=openldap \
  osixia/phpldapadmin:latest
```

## Advanced Scenarios

### Multiple LDAP Servers

Configure failover with multiple LDAP servers:

```yaml
LDAP_URL: "ldap://primary.example.com:389 ldap://secondary.example.com:389"
```

### LDAP with Roles/Groups

Map LDAP groups to application roles:

```yaml
APP_SECURITY_LDAP_GROUP_ROLE_MAPPING: |
  cinema-admin=ADMIN
  cinema-users=USER
```

### Custom LDAP Attributes

Map custom LDAP attributes to user details:

```yaml
APP_SECURITY_LDAP_USER_ATTRIBUTES: "mail,phone,department"
```

## Security Best Practices

1. **Use LDAPS (Encrypted)**
   - Always use port 636 with SSL/TLS in production
   - Validate server certificates

2. **Bind User Credentials**
   - Use a dedicated read-only LDAP user
   - Store credentials in Kubernetes Secrets
   - Rotate credentials regularly

3. **User DN Pattern**
   - Ensure pattern is specific to prevent unauthorized access
   - Test pattern with known users before deployment

4. **Network Security**
   - Restrict LDAP server access to authorized clients
   - Use network policies in Kubernetes
   - Monitor LDAP connection logs

## References

- [Spring Security LDAP Documentation](https://spring.io/projects/spring-security-ldap)
- [LDAP RFC 4511](https://tools.ietf.org/html/rfc4511)
- [Active Directory LDAP Guide](https://docs.microsoft.com/en-us/windows/win32/adsi/ldap-provider)
- [OpenLDAP Administrator's Guide](https://www.openldap.org/doc/admin/)

## Support

For issues or questions about LDAP integration:
1. Check the backend logs: `kubectl logs -f deployment/api -n cinema-box-office`
2. Verify LDAP server connectivity
3. Review configuration against this guide
4. Contact support with detailed error messages
