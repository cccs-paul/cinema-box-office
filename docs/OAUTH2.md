# OAuth2 Integration Guide

## Overview

myRC supports OAuth2 for enterprise single sign-on (SSO) integration. This guide covers configuring OAuth2 authentication with your Kubernetes deployment.

## Supported OAuth2 Providers

- **Google OAuth2**
- **GitHub OAuth2**
- **Keycloak**
- **Azure AD (Microsoft Entra ID)**
- **Okta**
- **Custom OAuth2 Providers**

## Prerequisites

- OAuth2 provider account and credentials
- Client ID and Client Secret
- Redirect URI configured in OAuth2 provider
- Knowledge of your OAuth2 provider's endpoints

## Configuration

### 1. Prepare OAuth2 Credentials

Create a secret with your OAuth2 credentials:

```bash
# Base64 encode your OAuth2 credentials
echo -n "your-client-id" | base64
echo -n "your-client-secret" | base64

# Update k8s/secrets.yaml with the encoded values
```

### 2. Update Backend Configuration

Edit `k8s/configmap.yaml` to enable OAuth2:

```yaml
data:
  SPRING_PROFILES_ACTIVE: "oauth2"
  
  # OAuth2 Server Configuration
  OAUTH2_CLIENT_ID: "your-client-id"
  OAUTH2_CLIENT_SECRET: "your-client-secret"
  OAUTH2_PROVIDER_URL: "https://provider.example.com"
  OAUTH2_AUTHORIZATION_URI: "https://provider.example.com/oauth/authorize"
  OAUTH2_TOKEN_URI: "https://provider.example.com/oauth/token"
  OAUTH2_USER_INFO_URI: "https://provider.example.com/oauth/userinfo"
  OAUTH2_REDIRECT_URI: "http://localhost:8080/login/oauth2/code/custom"
```

### 3. Update Secrets

Edit `k8s/secrets.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: cinema-box-office-oauth2-secret
  namespace: cinema-box-office
type: Opaque
data:
  client-id: eW91ci1jbGllbnQtaWQ=
  client-secret: eW91ci1jbGllbnQtc2VjcmV0
```

### 4. Add Environment Variables to Backend Deployment

Edit `k8s/backend.yaml` deployment spec:

```yaml
containers:
- name: api
  env:
  - name: APP_SECURITY_OAUTH2_ENABLED
    value: "true"
  - name: SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_CUSTOM_CLIENT_ID
    valueFrom:
      secretKeyRef:
        name: cinema-box-office-oauth2-secret
        key: client-id
  - name: SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_CUSTOM_CLIENT_SECRET
    valueFrom:
      secretKeyRef:
        name: cinema-box-office-oauth2-secret
        key: client-secret
  - name: SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_CUSTOM_AUTHORIZATION_URI
    valueFrom:
      configMapKeyRef:
        name: cinema-box-office-config
        key: OAUTH2_AUTHORIZATION_URI
  - name: SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_CUSTOM_TOKEN_URI
    valueFrom:
      configMapKeyRef:
        name: cinema-box-office-config
        key: OAUTH2_TOKEN_URI
  - name: SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_CUSTOM_USER_INFO_URI
    valueFrom:
      configMapKeyRef:
        name: cinema-box-office-config
        key: OAUTH2_USER_INFO_URI
```

## OAuth2 Configuration Examples

### Google OAuth2

```yaml
OAUTH2_CLIENT_ID: "xxxxxx.apps.googleusercontent.com"
OAUTH2_PROVIDER_URL: "https://accounts.google.com"
OAUTH2_AUTHORIZATION_URI: "https://accounts.google.com/o/oauth2/v2/auth"
OAUTH2_TOKEN_URI: "https://oauth2.googleapis.com/token"
OAUTH2_USER_INFO_URI: "https://openidconnect.googleapis.com/v1/userinfo"
OAUTH2_REDIRECT_URI: "http://cinema-box-office.example.com/login/oauth2/code/google"
```

**Setup Instructions:**
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project
3. Enable OAuth2 API
4. Create OAuth2 credentials (Web application)
5. Add authorized redirect URI
6. Copy Client ID and Secret

### GitHub OAuth2

```yaml
OAUTH2_CLIENT_ID: "your-github-client-id"
OAUTH2_CLIENT_SECRET: "your-github-client-secret"
OAUTH2_AUTHORIZATION_URI: "https://github.com/login/oauth/authorize"
OAUTH2_TOKEN_URI: "https://github.com/login/oauth/access_token"
OAUTH2_USER_INFO_URI: "https://api.github.com/user"
OAUTH2_REDIRECT_URI: "http://cinema-box-office.example.com/login/oauth2/code/github"
```

**Setup Instructions:**
1. Go to GitHub Settings > Developer settings > OAuth Apps
2. Create a new OAuth App
3. Set Authorization callback URL
4. Copy Client ID and Client Secret

### Azure AD (Microsoft Entra ID)

```yaml
OAUTH2_TENANT_ID: "your-tenant-id"
OAUTH2_CLIENT_ID: "your-app-id"
OAUTH2_CLIENT_SECRET: "your-app-secret"
OAUTH2_AUTHORIZATION_URI: "https://login.microsoftonline.com/{tenant}/oauth2/v2.0/authorize"
OAUTH2_TOKEN_URI: "https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token"
OAUTH2_USER_INFO_URI: "https://graph.microsoft.com/oidc/userinfo"
OAUTH2_REDIRECT_URI: "http://cinema-box-office.example.com/login/oauth2/code/azure"
```

**Setup Instructions:**
1. Go to [Azure Portal](https://portal.azure.com/)
2. Register a new application
3. Add a client secret
4. Add Redirect URI
5. Configure API permissions (User.Read)
6. Copy Application ID and Secret

### Keycloak

```yaml
OAUTH2_REALM: "cinema-box-office"
OAUTH2_SERVER_URL: "https://keycloak.example.com"
OAUTH2_CLIENT_ID: "cinema-box-office-client"
OAUTH2_CLIENT_SECRET: "your-client-secret"
OAUTH2_AUTHORIZATION_URI: "https://keycloak.example.com/realms/cinema-box-office/protocol/openid-connect/auth"
OAUTH2_TOKEN_URI: "https://keycloak.example.com/realms/cinema-box-office/protocol/openid-connect/token"
OAUTH2_USER_INFO_URI: "https://keycloak.example.com/realms/cinema-box-office/protocol/openid-connect/userinfo"
OAUTH2_REDIRECT_URI: "http://cinema-box-office.example.com/login/oauth2/code/keycloak"
```

**Setup Instructions:**
1. Create a new realm in Keycloak
2. Create a new client
3. Set Valid Redirect URIs
4. Generate client secret
5. Configure protocol mappers for user info
6. Copy Client ID and Secret

### Okta

```yaml
OAUTH2_DOMAIN: "your-domain.okta.com"
OAUTH2_CLIENT_ID: "your-client-id"
OAUTH2_CLIENT_SECRET: "your-client-secret"
OAUTH2_AUTHORIZATION_URI: "https://your-domain.okta.com/oauth2/v1/authorize"
OAUTH2_TOKEN_URI: "https://your-domain.okta.com/oauth2/v1/token"
OAUTH2_USER_INFO_URI: "https://your-domain.okta.com/oauth2/v1/userinfo"
OAUTH2_REDIRECT_URI: "http://cinema-box-office.example.com/login/oauth2/code/okta"
```

**Setup Instructions:**
1. Go to Okta Admin Console
2. Create a new application (Web)
3. Set Redirect URIs
4. Configure permissions
5. Copy Client ID and Secret

## Testing OAuth2 Configuration

### 1. Verify OAuth2 Configuration

```bash
# Check backend logs for OAuth2 initialization
kubectl logs -f deployment/api -n cinema-box-office | grep -i oauth

# Look for successful OAuth2 provider registration
```

### 2. Test OAuth2 Flow

```bash
# Get authorization code (in browser)
# https://cinema-box-office.example.com/oauth2/authorize?client_id=xxx&redirect_uri=xxx

# Exchange code for token
curl -X POST https://provider.example.com/oauth/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code&code=AUTH_CODE&client_id=CLIENT_ID&client_secret=CLIENT_SECRET&redirect_uri=REDIRECT_URI"

# Get user info
curl -H "Authorization: Bearer ACCESS_TOKEN" \
  https://provider.example.com/oauth/userinfo
```

### 3. Test Login

```bash
# Access the application login page
# The browser should redirect to OAuth2 provider
# After authentication, should redirect back to application
```

## Deployment

### Apply Configuration

```bash
# Update secrets
kubectl apply -f k8s/secrets.yaml

# Update configmap
kubectl apply -f k8s/configmap.yaml

# Restart backend deployment
kubectl rollout restart deployment/api -n cinema-box-office

# Check rollout status
kubectl rollout status deployment/api -n cinema-box-office
```

## Troubleshooting

### OAuth2 Provider Connection Issues

```bash
# Check backend logs
kubectl logs deployment/api -n cinema-box-office | grep -i "oauth\|provider"

# Verify provider URL is accessible
kubectl exec -it deployment/api -n cinema-box-office -- \
  curl -I https://provider.example.com/oauth/authorize
```

### Authentication Failures

Common issues and solutions:

| Issue | Solution |
|-------|----------|
| Invalid client ID/secret | Verify credentials in OAuth2 provider settings |
| Redirect URI mismatch | Ensure exact match in OAuth2 provider configuration |
| Insufficient permissions | Add required scopes: `openid profile email` |
| HTTPS required | Ensure provider uses HTTPS with valid certificate |
| CORS errors | Configure CORS in OAuth2 provider settings |

### User Info Mapping

```bash
# Check user info response
kubectl exec -it deployment/api -n cinema-box-office -- bash
curl -H "Authorization: Bearer TOKEN" https://provider.example.com/oauth/userinfo | jq .
```

## Security Best Practices

1. **Use HTTPS**
   - All communication must be over HTTPS
   - Valid SSL/TLS certificates required
   - Redirect URI must use HTTPS in production

2. **Client Secret Protection**
   - Store client secret in Kubernetes Secret
   - Never commit to version control
   - Rotate regularly
   - Restrict access to secret management

3. **Token Management**
   - Validate token signature
   - Check token expiration
   - Refresh tokens securely
   - Don't expose tokens in logs or URLs

4. **Scope Limitation**
   - Request only necessary scopes
   - Use least privilege principle
   - Document why each scope is needed

5. **Redirect URI Validation**
   - Whitelist all valid redirect URIs
   - Prevent open redirect attacks
   - Use exact matching (no wildcards in production)

## Advanced Configuration

### Custom Claims Mapping

Map OAuth2 claims to application user attributes:

```yaml
OAUTH2_CLAIM_MAPPING: |
  preferred_username=username
  email=email
  given_name=firstName
  family_name=lastName
  roles=authorities
```

### Multi-Provider Setup

Support multiple OAuth2 providers:

```yaml
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID: "xxx"
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET: "xxx"

SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_CLIENT_ID: "xxx"
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_CLIENT_SECRET: "xxx"

SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_AZURE_CLIENT_ID: "xxx"
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_AZURE_CLIENT_SECRET: "xxx"
```

### Token Introspection

Validate tokens with OAuth2 provider:

```yaml
OAUTH2_TOKEN_INTROSPECTION_URI: "https://provider.example.com/oauth/introspect"
OAUTH2_TOKEN_INTROSPECTION_ENABLED: "true"
```

## References

- [Spring Security OAuth2 Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [OAuth 2.0 RFC 6749](https://tools.ietf.org/html/rfc6749)
- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)
- [OAuth2 Best Practices](https://tools.ietf.org/html/draft-ietf-oauth-security-topics)

## Support

For issues or questions about OAuth2 integration:
1. Check the backend logs: `kubectl logs -f deployment/api -n cinema-box-office`
2. Verify OAuth2 provider configuration
3. Review configuration against this guide
4. Contact OAuth2 provider support
5. Review provider's API documentation
