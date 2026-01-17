# Security Best Practices Guide

## Overview

This guide covers security best practices for deploying and running Cinema Box Office in production environments.

## Authentication & Authorization

### LDAP Security

- Use LDAPS (port 636) with SSL/TLS in production
- Validate LDAP server certificates
- Use dedicated read-only bind user
- Rotate LDAP credentials regularly
- Implement connection pooling with connection limits
- Log all authentication attempts

### OAuth2 Security

- Use HTTPS for all OAuth2 communications
- Validate redirect URIs strictly
- Store client secrets in Kubernetes Secrets
- Rotate secrets quarterly
- Use short-lived access tokens (15-60 minutes)
- Implement token refresh mechanism
- Validate token signatures
- Check token expiration before use

### JWT Token Security

- Use strong signing algorithms (RS256 or better)
- Validate token signatures
- Check expiration time
- Validate issuer (iss claim)
- Validate audience (aud claim)
- Store JWT signing key securely in Secrets
- Rotate signing keys annually

## Database Security

### Connection Security

```yaml
# Use SSL for database connections
SPRING_DATASOURCE_URL: "jdbc:postgresql://prod-db.rds.amazonaws.com:5432/boxoffice?sslmode=require"
```

### Access Control

- Use strong database user passwords (20+ characters)
- Create separate database users for different applications
- Grant minimal required privileges
- Use role-based access control (RBAC)
- Audit database access logs

### Data Protection

```bash
# Enable encryption at rest
# For AWS RDS
aws rds modify-db-instance --db-instance-identifier boxoffice-db --storage-encrypted

# Enable encryption in transit
SPRING_DATASOURCE_URL: "jdbc:postgresql://...?sslmode=require"
```

### Backup Security

- Encrypt database backups
- Store backups in separate secure location
- Test backup restoration regularly
- Limit backup access to authorized personnel
- Maintain backup retention policy

## Network Security

### Kubernetes Network Policies

```yaml
# Deny all ingress by default
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny-ingress
  namespace: cinema-box-office
spec:
  podSelector: {}
  policyTypes:
  - Ingress

# Allow specific traffic
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-frontend-to-backend
  namespace: cinema-box-office
spec:
  podSelector:
    matchLabels:
      component: backend
  policyTypes:
  - Ingress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          component: frontend
    ports:
    - protocol: TCP
      port: 8080
```

### Firewall Configuration

- Restrict ingress to HTTPS (port 443) only
- Restrict egress to necessary external services
- Whitelist allowed domains and IP ranges
- Log firewall violations
- Regular firewall rule audits

### TLS/SSL Configuration

```yaml
# Use TLS 1.2 minimum
SPRING_MVC_SSL_ENABLED: "true"
SERVER_SSL_KEY_STORE: "/path/to/keystore.jks"
SERVER_SSL_KEY_STORE_PASSWORD: "password"
SERVER_SSL_KEY_STORE_TYPE: "PKCS12"
SERVER_SSL_PROTOCOL: "TLSv1.2"

# Ingress TLS configuration
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: cinema-box-office-ingress
spec:
  tls:
  - hosts:
    - cinema-box-office.example.com
    secretName: cinema-box-office-tls
```

## Container Security

### Image Security

```dockerfile
# Don't run as root
USER cinema-box-office

# Use read-only root filesystem where possible
RUN chmod a-w /

# Minimize image layers
# Use multi-stage builds

# Scan images for vulnerabilities
# docker scan cinema-box-office-api:latest
```

### Pod Security

```yaml
# Run with read-only root filesystem
securityContext:
  readOnlyRootFilesystem: true
  runAsNonRoot: true
  runAsUser: 1001
  allowPrivilegeEscalation: false
  capabilities:
    drop:
      - ALL
```

### Image Registry Security

```bash
# Use private image registry
# Enable image signing
# Implement image scanning policies
# Audit image pull history
```

## Secret Management

### Kubernetes Secrets

```bash
# Never commit secrets to version control
# Use Secret encryption at rest
kubectl apply -f k8s/secrets.yaml

# Rotate secrets regularly
kubectl delete secret cinema-box-office-oauth2-secret
kubectl create secret generic cinema-box-office-oauth2-secret \
  --from-literal=client-secret="new-secret"

# Audit secret access
# Enable audit logging for secret access
```

### External Secret Management

```bash
# Use HashiCorp Vault
helm repo add hashicorp https://helm.releases.hashicorp.com
helm install vault hashicorp/vault -n vault --create-namespace

# Or AWS Secrets Manager
# Or Azure Key Vault
```

## RBAC & Access Control

### Service Account Permissions

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: cinema-box-office
  namespace: cinema-box-office
rules:
# Minimal permissions required
- apiGroups: [""]
  resources: ["secrets", "configmaps"]
  verbs: ["get"]
  resourceNames: ["cinema-box-office-config"]
```

### User Access Control

- Enable RBAC for all cluster access
- Use role-based access control
- Implement principle of least privilege
- Regular access reviews
- Audit user actions

## API Security

### Input Validation

```java
// Validate all inputs
@NotBlank
@Size(min = 1, max = 100)
private String username;

@Email
private String email;

@Pattern(regexp = "^[0-9]{10}$")
private String phoneNumber;
```

### SQL Injection Prevention

```java
// Use parameterized queries / JPA
@Query("SELECT u FROM User u WHERE u.username = :username")
User findByUsername(@Param("username") String username);

// Avoid string concatenation
// Don't use: "SELECT * FROM users WHERE username = '" + username + "'"
```

### Cross-Site Scripting (XSS) Prevention

```java
// Encode output
String encoded = HtmlUtils.htmlEscape(userInput);

// Use Content Security Policy headers
response.setHeader("Content-Security-Policy", "default-src 'self'");
```

### Cross-Site Request Forgery (CSRF) Prevention

```yaml
# Enable CSRF protection
SPRING_SECURITY_CSRF_ENABLED: "true"

# CORS configuration
CORS_ALLOWED_ORIGINS: "https://cinema-box-office.example.com"
CORS_ALLOWED_CREDENTIALS: "true"
```

### Rate Limiting

```java
// Implement rate limiting
@RestController
@RateLimiter(name = "api", fallbackMethod = "rateLimitFallback")
public class ApiController {
    // Implementation
}
```

## Logging and Monitoring

### Security Logging

```yaml
# Log security events
LOGGING_LEVEL_SPRING_SECURITY: "DEBUG"

# Log authentication attempts
LOGGING_LEVEL_COM_BOXOFFICE_SECURITY: "INFO"
```

### Audit Logging

```java
// Log sensitive operations
@Audited
@Entity
public class User {
    // Audit all changes
}

// Custom audit events
applicationContext.publishEvent(new AuditEvent(
    SecurityContextHolder.getContext().getAuthentication().getName(),
    "USER_LOGIN",
    "User logged in successfully"
));
```

### Monitoring Alerts

- Alert on failed authentication attempts (>5 in 5 minutes)
- Alert on database access anomalies
- Alert on unauthorized API access
- Alert on certificate expiration (>30 days)
- Alert on SSL/TLS configuration changes

## Compliance and Regulations

### Data Protection

- GDPR compliance (if EU users)
- CCPA compliance (if California users)
- Data retention policies
- Data deletion procedures
- Privacy policy and terms of service

### Audit and Compliance

- Maintain audit logs for 1 year minimum
- Regular security assessments
- Penetration testing annually
- Vulnerability scanning
- Compliance reporting

### Incident Response

- Have incident response plan
- Document security incidents
- Notify affected users within 72 hours (GDPR)
- Conduct root cause analysis
- Implement preventive measures

## Security Checklist

- [ ] All passwords meet complexity requirements (20+ chars)
- [ ] TLS 1.2+ enabled for all connections
- [ ] Secrets stored in Kubernetes Secrets or vault
- [ ] RBAC configured with least privilege
- [ ] Network policies restrict traffic
- [ ] Audit logging enabled
- [ ] Backup encryption enabled
- [ ] Regular penetration testing scheduled
- [ ] Security headers configured
- [ ] Rate limiting implemented
- [ ] SQL injection prevention in place
- [ ] XSS/CSRF protections enabled
- [ ] Dependencies scanned for vulnerabilities
- [ ] Container images scanned for vulnerabilities
- [ ] Certificate expiration monitored
- [ ] Incident response plan documented
- [ ] Regular security training for team
- [ ] Third-party dependencies assessed
- [ ] API rate limiting configured
- [ ] DDoS protection configured

## Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Kubernetes Security Best Practices](https://kubernetes.io/docs/concepts/security/pod-security-standards/)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [NIST Cybersecurity Framework](https://www.nist.gov/cyberframework)
- [CIS Benchmarks](https://www.cisecurity.org/cis-benchmarks/)

## Support

For security questions or to report vulnerabilities:
- Submit security vulnerability reports responsibly
- Follow responsible disclosure practices
- Contact security team with detailed information
