# Docker LDAP and OAuth2 Integration Guide

## Overview

This guide covers integrating myRC with corporate LDAP directories and OAuth2 providers when deploying on Docker. It includes Docker Compose configurations, environment setup, and testing procedures.

## Table of Contents

1. [LDAP Integration with Docker](#ldap-integration-with-docker)
2. [OAuth2 Integration with Docker](#oauth2-integration-with-docker)
3. [Development Setup](#development-setup)
4. [Testing and Verification](#testing-and-verification)
5. [Troubleshooting](#troubleshooting)

---

## LDAP Integration with Docker

### Prerequisites

- Docker and Docker Compose installed
- LDAP server details (hostname, port, base DN, bind credentials)
- Basic understanding of LDAP directory structure

### Option 1: Using Docker LDAP Server (Development)

For development and testing, create a Docker-based LDAP server:

#### Docker Compose with OpenLDAP

```yaml
version: '3.9'

services:
  # OpenLDAP Server for testing
  ldap:
    image: osixia/openldap:latest
    container_name: myrc-ldap
    ports:
      - "389:389"
      - "636:636"
    environment:
      LDAP_ORGANISATION: "myRC"
      LDAP_DOMAIN: "myrc.local"
      LDAP_BASE_DN: "dc=myrc,dc=local"
      LDAP_ADMIN_PASSWORD: "admin-password-123"
      LDAP_READONLY_USER: "true"
      LDAP_READONLY_USER_USERNAME: "readonly"
      LDAP_READONLY_USER_PASSWORD: "readonly-password"
    volumes:
      - ldap_data:/var/lib/ldap
      - ldap_config:/etc/ldap/slapd.d
    networks:
      - myrc-network

  # LDAP Admin UI (optional, for easy management)
  phpldapadmin:
    image: osixia/phpldapadmin:latest
    container_name: myrc-phpldapadmin
    ports:
      - "6443:443"
    environment:
      PHPLDAPADMIN_LDAP_HOSTS: "ldap"
      PHPLDAPADMIN_LDAP_CLIENT_ADD: "true"
    depends_on:
      - ldap
    networks:
      - myrc-network

  # Backend API with LDAP support
  api:
    image: myrc-api:latest
    container_name: myrc-api
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: "dev,ldap"
      SPRING_LDAP_URLS: "ldap://ldap:389"
      SPRING_LDAP_BASE: "dc=myrc,dc=local"
      SPRING_LDAP_USERNAME: "cn=admin,dc=myrc,dc=local"
      SPRING_LDAP_PASSWORD: "admin-password-123"
      SPRING_LDAP_USER_SEARCH_FILTER: "(uid={0})"
      SPRING_LDAP_USER_SEARCH_BASE: "ou=users"
      SPRING_LDAP_GROUP_SEARCH_FILTER: "(member={0})"
      SPRING_LDAP_GROUP_SEARCH_BASE: "ou=groups"
    depends_on:
      - ldap
    networks:
      - myrc-network

volumes:
  ldap_data:
  ldap_config:

networks:
  myrc-network:
    driver: bridge
```

#### Create Test LDAP Users

```bash
# Save as init-ldap.ldif
dn: ou=users,dc=myrc,dc=local
objectClass: organizationalUnit
ou: users

dn: cn=Test User,ou=users,dc=myrc,dc=local
objectClass: person
objectClass: inetOrgPerson
cn: Test User
sn: User
uid: testuser
userPassword: testpassword123
mail: testuser@myrc.local

dn: cn=Admin User,ou=users,dc=myrc,dc=local
objectClass: person
objectClass: inetOrgPerson
cn: Admin User
sn: User
uid: adminuser
userPassword: adminpassword123
mail: admin@myrc.local

dn: ou=groups,dc=myrc,dc=local
objectClass: organizationalUnit
ou: groups

dn: cn=users,ou=groups,dc=myrc,dc=local
objectClass: groupOfNames
cn: users
member: cn=Test User,ou=users,dc=myrc,dc=local
member: cn=Admin User,ou=users,dc=myrc,dc=local

dn: cn=admins,ou=groups,dc=myrc,dc=local
objectClass: groupOfNames
cn: admins
member: cn=Admin User,ou=users,dc=myrc,dc=local
```

Load LDAP data:

```bash
# Copy init-ldap.ldif into container
docker cp init-ldap.ldif myrc-ldap:/tmp/

# Load into LDAP
docker exec myrc-ldap ldapadd -x -D "cn=admin,dc=myrc,dc=local" \
  -w "admin-password-123" -f /tmp/init-ldap.ldif
```

### Option 2: Corporate LDAP/Active Directory

For production with corporate LDAP:

#### Docker Compose Environment

```yaml
version: '3.9'

services:
  api:
    image: myrc-api:latest
    container_name: myrc-api
    ports:
      - "8080:8080"
    environment:
      # LDAP Configuration
      SPRING_PROFILES_ACTIVE: "prod,ldap"
      SPRING_LDAP_URLS: "ldap://corp-ldap.company.com:389"
      SPRING_LDAP_BASE: "dc=company,dc=com"
      SPRING_LDAP_USERNAME: "cn=service_account,dc=company,dc=com"
      SPRING_LDAP_PASSWORD: "${LDAP_PASSWORD}"
      
      # User search configuration
      SPRING_LDAP_USER_SEARCH_FILTER: "(sAMAccountName={0})"  # For Active Directory
      # Or for OpenLDAP: "(uid={0})"
      SPRING_LDAP_USER_SEARCH_BASE: "ou=Users,dc=company,dc=com"
      
      # Group search configuration (optional)
      SPRING_LDAP_GROUP_SEARCH_FILTER: "(member={0})"
      SPRING_LDAP_GROUP_SEARCH_BASE: "ou=Groups,dc=company,dc=com"
      
      # Active Directory Specific (if using AD)
      SPRING_LDAP_ACTIVE_DIRECTORY_DOMAIN: "company.com"
      
      # Database
      SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres:5432/myrc"
      SPRING_DATASOURCE_USERNAME: "myrc"
      SPRING_DATASOURCE_PASSWORD: "${DB_PASSWORD}"
      
    depends_on:
      - postgres
    networks:
      - myrc-network

  postgres:
    image: postgres:16-alpine
    container_name: myrc-postgres
    environment:
      POSTGRES_DB: myrc
      POSTGRES_USER: myrc
      POSTGRES_PASSWORD: "${DB_PASSWORD}"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - myrc-network

  frontend:
    image: myrc-frontend:latest
    container_name: myrc-web
    ports:
      - "4200:80"
    depends_on:
      - api
    networks:
      - myrc-network

volumes:
  postgres_data:

networks:
  myrc-network:
    driver: bridge
```

#### Environment Variables (.env file)

```bash
# .env for Docker Compose
LDAP_PASSWORD=your-secure-ldap-password
DB_PASSWORD=your-secure-db-password
```

Deploy:

```bash
# Create .env file with sensitive credentials
echo "LDAP_PASSWORD=your-password" > .env
echo "DB_PASSWORD=your-password" >> .env

# Deploy with environment
docker-compose -f docker-compose.ldap.yml up -d
```

---

## OAuth2 Integration with Docker

### Prerequisites

- OAuth2 provider account (Google, GitHub, Azure AD, etc.)
- OAuth2 application credentials (client ID, client secret)
- Redirect URI configured in provider

### Google OAuth2 Docker Setup

#### 1. Register Application in Google Cloud Console

```
1. Go to https://console.cloud.google.com/
2. Create new project: "myRC"
3. Enable OAuth 2.0 API
4. Create OAuth 2.0 credentials (Web application)
5. Authorized redirect URIs:
   - http://localhost:4200/auth/callback
   - http://localhost:8080/login/oauth2/code/google
   - https://your-domain.com/auth/callback
```

#### 2. Docker Compose Configuration

```yaml
version: '3.9'

services:
  api:
    image: myrc-api:latest
    container_name: myrc-api
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: "prod,oauth2"
      
      # OAuth2 Google Configuration
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID: "${GOOGLE_CLIENT_ID}"
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET: "${GOOGLE_CLIENT_SECRET}"
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_SCOPE: "openid,profile,email"
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_REDIRECT_URI: "http://localhost:8080/login/oauth2/code/google"
      
      # Provider configuration
      SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_GOOGLE_AUTHORIZATION_URI: "https://accounts.google.com/o/oauth2/v2/auth"
      SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_GOOGLE_TOKEN_URI: "https://www.googleapis.com/oauth2/v4/token"
      SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_GOOGLE_USER_INFO_URI: "https://www.googleapis.com/oauth2/v3/userinfo"
      
      # Database
      SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres:5432/myrc"
      SPRING_DATASOURCE_USERNAME: "myrc"
      SPRING_DATASOURCE_PASSWORD: "${DB_PASSWORD}"
      
    depends_on:
      - postgres
    networks:
      - myrc-network

  postgres:
    image: postgres:16-alpine
    container_name: myrc-postgres
    environment:
      POSTGRES_DB: myrc
      POSTGRES_USER: myrc
      POSTGRES_PASSWORD: "${DB_PASSWORD}"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - myrc-network

  frontend:
    image: myrc-frontend:latest
    container_name: myrc-web
    ports:
      - "4200:80"
    depends_on:
      - api
    networks:
      - myrc-network

volumes:
  postgres_data:

networks:
  myrc-network:
    driver: bridge
```

#### 3. Environment File

```bash
# .env
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret
DB_PASSWORD=your-secure-password
```

### GitHub OAuth2 Docker Setup

#### 1. Register Application in GitHub

```
1. Go to GitHub Settings → Developer settings → OAuth Apps
2. Create new OAuth App
3. Application name: myRC
4. Homepage URL: http://localhost:4200
5. Authorization callback URL:
   - http://localhost:8080/login/oauth2/code/github
6. Copy Client ID and Client Secret
```

#### 2. Docker Compose Configuration

```yaml
version: '3.9'

services:
  api:
    image: myrc-api:latest
    container_name: myrc-api
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: "prod,oauth2"
      
      # OAuth2 GitHub Configuration
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_CLIENT_ID: "${GITHUB_CLIENT_ID}"
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_CLIENT_SECRET: "${GITHUB_CLIENT_SECRET}"
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_SCOPE: "user:email"
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_REDIRECT_URI: "http://localhost:8080/login/oauth2/code/github"
      
      # Database
      SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres:5432/myrc"
      SPRING_DATASOURCE_USERNAME: "myrc"
      SPRING_DATASOURCE_PASSWORD: "${DB_PASSWORD}"
      
    depends_on:
      - postgres
    networks:
      - myrc-network

  # ... rest of services
```

### Azure AD OAuth2 Docker Setup

#### 1. Register Application in Azure

```
1. Azure Portal → Azure Active Directory
2. App registrations → New registration
3. Name: myRC
4. Supported account types: Multitenant
5. Redirect URI: http://localhost:8080/login/oauth2/code/azure
6. Certificates & secrets → New client secret
```

#### 2. Docker Compose Configuration

```yaml
services:
  api:
    image: myrc-api:latest
    environment:
      SPRING_PROFILES_ACTIVE: "prod,oauth2"
      
      # OAuth2 Azure Configuration
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_AZURE_CLIENT_ID: "${AZURE_CLIENT_ID}"
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_AZURE_CLIENT_SECRET: "${AZURE_CLIENT_SECRET}"
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_AZURE_SCOPE: "https://graph.microsoft.com/.default"
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_AZURE_REDIRECT_URI: "http://localhost:8080/login/oauth2/code/azure"
      
      # Provider configuration
      SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_AZURE_AUTHORIZATION_URI: "https://login.microsoftonline.com/${AZURE_TENANT_ID}/oauth2/v2.0/authorize"
      SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_AZURE_TOKEN_URI: "https://login.microsoftonline.com/${AZURE_TENANT_ID}/oauth2/v2.0/token"
      SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_AZURE_USER_INFO_URI: "https://graph.microsoft.com/oidc/userinfo"
      SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_AZURE_JWK_SET_URI: "https://login.microsoftonline.com/common/discovery/v2.0/keys"
      
      # Database
      SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres:5432/myrc"
      SPRING_DATASOURCE_USERNAME: "myrc"
      SPRING_DATASOURCE_PASSWORD: "${DB_PASSWORD}"
```

### Multiple OAuth2 Providers

Configure multiple providers simultaneously:

```yaml
services:
  api:
    environment:
      SPRING_PROFILES_ACTIVE: "prod,oauth2"
      
      # Google
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID: "${GOOGLE_CLIENT_ID}"
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET: "${GOOGLE_CLIENT_SECRET}"
      
      # GitHub
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_CLIENT_ID: "${GITHUB_CLIENT_ID}"
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_CLIENT_SECRET: "${GITHUB_CLIENT_SECRET}"
      
      # Azure AD
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_AZURE_CLIENT_ID: "${AZURE_CLIENT_ID}"
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_AZURE_CLIENT_SECRET: "${AZURE_CLIENT_SECRET}"
```

---

## Development Setup

### All-in-One Development Docker Compose

Complete setup with LDAP, OAuth2, and all services:

```yaml
version: '3.9'

services:
  # OpenLDAP for development
  ldap:
    image: osixia/openldap:latest
    container_name: myrc-ldap-dev
    ports:
      - "389:389"
      - "636:636"
    environment:
      LDAP_ORGANISATION: "myRC Dev"
      LDAP_DOMAIN: "myrc.local"
      LDAP_BASE_DN: "dc=myrc,dc=local"
      LDAP_ADMIN_PASSWORD: "admin-password"
      LDAP_READONLY_USER: "true"
      LDAP_READONLY_USER_USERNAME: "readonly"
      LDAP_READONLY_USER_PASSWORD: "readonly-password"
    volumes:
      - ldap_data:/var/lib/ldap
      - ldap_config:/etc/ldap/slapd.d
    networks:
      - myrc-network

  # LDAP Admin UI
  phpldapadmin:
    image: osixia/phpldapadmin:latest
    container_name: myrc-ldap-admin
    ports:
      - "6443:443"
    environment:
      PHPLDAPADMIN_LDAP_HOSTS: "ldap"
      PHPLDAPADMIN_LDAP_CLIENT_ADD: "true"
    depends_on:
      - ldap
    networks:
      - myrc-network

  # PostgreSQL
  postgres:
    image: postgres:16-alpine
    container_name: myrc-postgres-dev
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: myrc
      POSTGRES_USER: myrc
      POSTGRES_PASSWORD: devpassword
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - myrc-network

  # Backend API with LDAP support
  api:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: myrc-api-dev
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: "dev,ldap,oauth2"
      
      # LDAP
      SPRING_LDAP_URLS: "ldap://ldap:389"
      SPRING_LDAP_BASE: "dc=myrc,dc=local"
      SPRING_LDAP_USERNAME: "cn=admin,dc=myrc,dc=local"
      SPRING_LDAP_PASSWORD: "admin-password"
      SPRING_LDAP_USER_SEARCH_FILTER: "(uid={0})"
      SPRING_LDAP_USER_SEARCH_BASE: "ou=users"
      SPRING_LDAP_GROUP_SEARCH_FILTER: "(member={0})"
      SPRING_LDAP_GROUP_SEARCH_BASE: "ou=groups"
      
      # OAuth2 (local development - use dummy values)
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID: "dev-google-client-id"
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET: "dev-google-secret"
      
      # Database
      SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres:5432/myrc"
      SPRING_DATASOURCE_USERNAME: "myrc"
      SPRING_DATASOURCE_PASSWORD: "devpassword"
      SPRING_JPA_HIBERNATE_DDL_AUTO: "create-drop"
      
    depends_on:
      - ldap
      - postgres
    networks:
      - myrc-network

  # Frontend with API proxy
  web:
    build:
      context: ./frontend
      dockerfile: Dockerfile.dev
    container_name: myrc-web-dev
    ports:
      - "4200:4200"
    environment:
      API_URL: "http://api:8080"
    depends_on:
      - api
    networks:
      - myrc-network

volumes:
  ldap_data:
  ldap_config:
  postgres_data:

networks:
  myrc-network:
    driver: bridge
```

Start development environment:

```bash
# Create .env if using OAuth2
cat > .env << EOF
GOOGLE_CLIENT_ID=dev-client-id
GOOGLE_CLIENT_SECRET=dev-secret
GITHUB_CLIENT_ID=dev-client-id
GITHUB_CLIENT_SECRET=dev-secret
EOF

# Start all services
docker-compose -f docker-compose.dev-ldap-oauth.yml up -d

# View logs
docker-compose -f docker-compose.dev-ldap-oauth.yml logs -f api

# Stop services
docker-compose -f docker-compose.dev-ldap-oauth.yml down
```

---

## Testing and Verification

### Test LDAP Connection

```bash
# From container
docker exec myrc-api-dev bash

# Inside container, test LDAP connection
ldapsearch -x -H ldap://ldap:389 \
  -D "cn=admin,dc=myrc,dc=local" \
  -w "admin-password" \
  -b "dc=myrc,dc=local" \
  "(uid=testuser)"

# Should return user details
```

### Test LDAP Authentication

```bash
# Using curl to test authentication endpoint
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "testpassword123"
  }'

# Expected response:
# {
#   "token": "eyJhbGc...",
#   "expiresIn": 3600,
#   "user": {
#     "username": "testuser",
#     "email": "testuser@myrc.local",
#     "groups": ["users"]
#   }
# }
```

### Test OAuth2 Flow

```bash
# 1. Open browser
open http://localhost:4200

# 2. Click "Login with Google" (or other provider)

# 3. Redirect to provider login

# 4. Authorize application

# 5. Redirect back to app with token

# 6. Should be logged in
```

### View Docker Container Logs

```bash
# API logs
docker logs -f myrc-api-dev

# LDAP logs
docker logs -f myrc-ldap-dev

# PostgreSQL logs
docker logs -f myrc-postgres-dev

# All logs
docker-compose logs -f
```

---

## Troubleshooting

### LDAP Connection Failed

```bash
# Check LDAP service is running
docker ps | grep ldap

# Test connection
docker exec myrc-ldap ldapwhoami -x -D "cn=admin,dc=myrc,dc=local" -w "admin-password"

# Check LDAP logs
docker logs myrc-ldap

# Verify environment variables
docker exec myrc-api env | grep LDAP
```

### LDAP Authentication Not Working

**Problem**: Users cannot login via LDAP

**Solution**:
1. Verify LDAP users exist:
   ```bash
   docker exec myrc-ldap ldapsearch -x -D "cn=admin,dc=myrc,dc=local" \
     -w "admin-password" -b "ou=users,dc=myrc,dc=local" "(uid=testuser)"
   ```

2. Check user search filter:
   ```bash
   # Wrong: SPRING_LDAP_USER_SEARCH_FILTER: "(uid={0})" for Active Directory
   # Right: SPRING_LDAP_USER_SEARCH_FILTER: "(sAMAccountName={0})" for Active Directory
   ```

3. Verify base DN:
   ```bash
   docker exec myrc-ldap ldapsearch -x -s base -b "dc=myrc,dc=local"
   ```

### OAuth2 Redirect URI Mismatch

**Problem**: OAuth2 provider returns "redirect_uri_mismatch" error

**Solution**:
1. Verify redirect URI in application
2. Match exactly in OAuth2 provider settings
3. Include protocol (http/https) and port

**Common URIs**:
- Development: `http://localhost:8080/login/oauth2/code/google`
- Production: `https://app.example.com/login/oauth2/code/google`

### Database Connection Issues

```bash
# Test PostgreSQL connection from API
docker exec myrc-api bash -c \
  "psql -h postgres -U myrc -d myrc -c 'SELECT version();'"

# Check PostgreSQL is running
docker ps | grep postgres

# View PostgreSQL logs
docker logs myrc-postgres-dev
```

### Port Conflicts

```bash
# Check what's using ports
lsof -i :8080    # API
lsof -i :4200    # Frontend
lsof -i :5432    # PostgreSQL
lsof -i :389     # LDAP

# Kill process using port (if needed)
kill -9 <PID>

# Or use different ports in docker-compose.yml
```

---

## Production Deployment

### Secrets Management

For production, use environment variables or Docker secrets:

```bash
# Create Docker secrets
echo "prod-ldap-password" | docker secret create ldap_password -
echo "prod-db-password" | docker secret create db_password -
echo "prod-oauth-secret" | docker secret create oauth_secret -

# Reference in docker-compose.yml
version: '3.9'

services:
  api:
    environment:
      SPRING_LDAP_PASSWORD_FILE: /run/secrets/ldap_password
      SPRING_DATASOURCE_PASSWORD_FILE: /run/secrets/db_password
    secrets:
      - ldap_password
      - db_password
      - oauth_secret

secrets:
  ldap_password:
    external: true
  db_password:
    external: true
  oauth_secret:
    external: true
```

### Health Checks

```yaml
services:
  api:
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

### Resource Limits

```yaml
services:
  api:
    mem_limit: 1g
    cpus: '1.0'
    
  postgres:
    mem_limit: 512m
    cpus: '0.5'
    
  ldap:
    mem_limit: 512m
    cpus: '0.5'
```

---

## Summary

This guide provides comprehensive Docker support for:

✅ LDAP/Active Directory integration  
✅ OAuth2 with multiple providers  
✅ Development environment setup  
✅ Production deployment guidance  
✅ Testing and troubleshooting  
✅ Secrets management  

Start with the development setup and gradually move to production configurations as needed.
