# Test LDAP Deployment

> **myRC** — Test LDAP Authentication Deployment Guide
>
> Copyright © 2026 myRC Team — Licensed under MIT License
>
> Version 1.0.0 — 2026-02-01

## Overview

This document describes how to run myRC with **LDAP-only authentication** using a local
test LDAP server. The deployment uses the
[docker-test-openldap](https://github.com/rroemhild/docker-test-openldap) image, which
provides an OpenLDAP server pre-populated with
[Futurama](https://en.wikipedia.org/wiki/Futurama) / Planet Express themed test users.

The test LDAP deployment is useful for:

- Verifying the LDAP authentication flow end-to-end
- Testing group-to-role mapping
- Testing LDAP user attribute mapping
- Developing and debugging LDAP-related features
- Demonstrating the application's LDAP capabilities without an enterprise LDAP server

---

## Quick Start

From the project root directory:

```bash
./testldap-build-and-run.sh
```

This single command will:

1. Clean the Docker environment
2. Build the backend with Maven
3. Build the frontend with npm
4. Stop any existing containers (test LDAP, dev, and prod) to free ports
5. Rebuild Docker images from scratch
6. Start all services with LDAP-only authentication

---

## Architecture

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Frontend   │────▸│   Backend    │────▸│  PostgreSQL   │
│  (Angular)   │     │ (Spring Boot)│     │    (Data)     │
│  :4200       │     │  :8080       │     │  :5432        │
└──────────────┘     └──────┬───────┘     └──────────────┘
                            │
                            │ LDAP bind + search
                            ▼
                     ┌──────────────┐
                     │   OpenLDAP   │
                     │ (Test Server)│
                     │  :10389      │
                     └──────────────┘
```

### Services

| Service    | Container Name          | Port(s)           | Description                               |
|------------|-------------------------|--------------------|-------------------------------------------|
| Frontend   | `myrc-testldap-web`     | `4200`             | Angular dev server                         |
| Backend    | `myrc-testldap-api`     | `8080`             | Spring Boot API (LDAP-only auth)           |
| PostgreSQL | `myrc-testldap-db`      | `5432`             | Application database                       |
| OpenLDAP   | `myrc-testldap-openldap`| `10389` / `10636`  | Test LDAP server (plain / TLS)             |
| pgAdmin    | `myrc-testldap-pgadmin` | `5050`             | Database management UI                     |

---

## LDAP Server Details

The test LDAP server is provided by the
[rroemhild/docker-test-openldap](https://github.com/rroemhild/docker-test-openldap)
Docker image. It runs OpenLDAP pre-populated with the `planetexpress.com` domain from
the TV show Futurama.

### Connection Parameters

| Parameter          | Value                                          |
|--------------------|------------------------------------------------|
| **URL**            | `ldap://localhost:10389`                        |
| **Base DN**        | `dc=planetexpress,dc=com`                      |
| **Admin DN**       | `cn=admin,dc=planetexpress,dc=com`             |
| **Admin Password** | `GoodNewsEveryone`                              |
| **Users OU**       | `ou=people,dc=planetexpress,dc=com`            |
| **User Filter**    | `(uid={0})`                                     |
| **Group Filter**   | `(member={0})`                                  |

### LDAP Directory Structure

```
dc=planetexpress,dc=com
├── cn=admin                            (LDAP admin)
└── ou=people
    ├── cn=Hubert J. Farnsworth         (uid: professor)
    ├── cn=Philip J. Fry                (uid: fry)
    ├── cn=John A. Zoidberg             (uid: zoidberg)
    ├── cn=Hermes Conrad                (uid: hermes)
    ├── cn=Turanga Leela                (uid: leela)
    ├── cn=Bender Bending Rodriguez     (uid: bender)
    ├── cn=Amy Wong                     (uid: amy)
    ├── cn=admin_staff                  (group)
    └── cn=ship_crew                    (group)
```

---

## Test User Accounts

### Admin Users (admin_staff group → ADMIN role)

| Username    | Password    | Full Name               | Email                         | Job Title    |
|-------------|-------------|--------------------------|-------------------------------|--------------|
| `professor` | `professor` | Hubert J. Farnsworth     | professor@planetexpress.com   | Owner        |
| `hermes`    | `hermes`    | Hermes Conrad            | hermes@planetexpress.com      | Bureaucrat   |

### Regular Users (ship_crew group → USER role)

| Username | Password | Full Name                | Email                       | Job Title     |
|----------|----------|--------------------------|-----------------------------|---------------|
| `fry`    | `fry`    | Philip J. Fry            | fry@planetexpress.com       | Delivery boy  |
| `leela`  | `leela`  | Turanga Leela            | leela@planetexpress.com     | Captain       |
| `bender` | `bender` | Bender Bending Rodriguez | bender@planetexpress.com    | Ship's Robot  |

### Other Users (no group membership → defaults to USER role)

| Username   | Password   | Full Name         | Email                        | Job Title |
|------------|------------|--------------------|-----------------------------|-----------|
| `zoidberg` | `zoidberg` | John A. Zoidberg  | zoidberg@planetexpress.com  | Doctor    |
| `amy`      | `amy`      | Amy Wong          | amy@planetexpress.com       | Intern    |

> **Note:** In this test deployment, the username is also the password for every
> account.

---

## Group-to-Role Mappings

The application maps LDAP groups to application roles:

| LDAP Group                                               | Application Role | Admin? | Members                    |
|----------------------------------------------------------|------------------|--------|----------------------------|
| `cn=admin_staff,ou=people,dc=planetexpress,dc=com`       | `ADMIN`          | Yes    | professor, hermes          |
| `cn=ship_crew,ou=people,dc=planetexpress,dc=com`         | `USER`           | No     | fry, leela, bender         |

Users without any group membership (zoidberg, amy) are automatically assigned the
`USER` role.

---

## Attribute Mapping

The following LDAP attributes are mapped to myRC user fields:

| myRC Field    | LDAP Attribute | Example (fry)              |
|---------------|----------------|----------------------------|
| Username      | `uid`          | `fry`                      |
| Email         | `mail`         | `fry@planetexpress.com`    |
| Display Name  | `cn`           | `Philip J. Fry`            |
| Surname       | `sn`           | `Fry`                      |
| Given Name    | `givenName`    | `Philip`                   |
| Group Membership | `memberOf`  | (LDAP group DNs)           |

---

## Authentication Flow

1. User opens `http://localhost:4200` and sees the login page
2. Only the **LDAP** login tab is shown (app-account and OAuth2 are disabled)
3. User enters their LDAP uid and password (e.g. `fry` / `fry`)
4. The backend connects to the OpenLDAP server at `ldap://openldap:10389`
5. Backend performs an LDAP search for the user in `ou=people,dc=planetexpress,dc=com`
6. Backend binds as the found user with the provided password
7. Backend looks up group membership and maps it to application roles
8. If `allow-sign-up` is enabled (default), a local user record is created on first login
9. A session is created and the user is redirected to the dashboard

---

## Configuration Details

### Login Methods

In this deployment, **only LDAP authentication is enabled**:

| Login Method | Enabled |
|-------------|---------|
| App Account  | ❌ No   |
| LDAP         | ✅ Yes  |
| OAuth2       | ❌ No   |

This is configured via Spring Boot environment variables in
`docker-compose.testldap.yml`:

```yaml
APP_SECURITY_LOGIN_METHODS_APP_ACCOUNT_ENABLED: "false"
APP_SECURITY_LOGIN_METHODS_LDAP_ENABLED: "true"
APP_SECURITY_LOGIN_METHODS_OAUTH2_ENABLED: "false"
APP_SECURITY_LDAP_ENABLED: "true"
```

### Debug Logging

The test LDAP deployment enables debug-level logging for:

- `com.myrc` — Application code
- `org.springframework.security` — Spring Security
- `org.springframework.ldap` — Spring LDAP

This helps troubleshoot authentication issues. View logs with:

```bash
docker compose --project-name myrc-testldap -f docker-compose.testldap.yml logs -f api
```

---

## Differences from Standard Deployment

| Aspect               | `build-and-start.sh`         | `testldap-build-and-run.sh`      |
|-----------------------|------------------------------|----------------------------------|
| Authentication        | App Account (username/pass)  | LDAP only                        |
| Users                 | admin / Admin@123            | Futurama characters (see above)  |
| LDAP Server           | None                         | docker-test-openldap             |
| Docker Compose File   | `docker-compose.dev.yml`     | `docker-compose.testldap.yml`    |
| Project Name          | `myrc`                       | `myrc-testldap`                  |
| Container Prefix      | `myrc-`                      | `myrc-testldap-`                 |
| Frontend Port         | `4200`                       | `4200`                           |
| API Port              | `8080`                       | `8080`                           |
| Database Port         | `5432`                       | `5432`                           |
| Extra Ports           | —                            | `10389` (LDAP), `10636` (LDAPS)  |

> **Note:** Both scripts use the same application ports (`4200`, `8080`, `5432`), so
> only one deployment can run at a time.

---

## Useful Commands

### View Service Logs

```bash
# All services
docker compose --project-name myrc-testldap -f docker-compose.testldap.yml logs -f

# Backend API only
docker compose --project-name myrc-testldap -f docker-compose.testldap.yml logs -f api

# OpenLDAP server only
docker compose --project-name myrc-testldap -f docker-compose.testldap.yml logs -f openldap
```

### Stop Services

```bash
docker compose --project-name myrc-testldap -f docker-compose.testldap.yml down
```

### Stop and Remove Data Volumes

```bash
docker compose --project-name myrc-testldap -f docker-compose.testldap.yml down -v
```

### Query LDAP Server Directly

```bash
# List all users
ldapsearch -H ldap://localhost:10389 -x \
  -b "ou=people,dc=planetexpress,dc=com" \
  -D "cn=admin,dc=planetexpress,dc=com" \
  -w GoodNewsEveryone \
  "(objectClass=inetOrgPerson)" uid cn mail

# List all groups
ldapsearch -H ldap://localhost:10389 -x \
  -b "ou=people,dc=planetexpress,dc=com" \
  -D "cn=admin,dc=planetexpress,dc=com" \
  -w GoodNewsEveryone \
  "(objectClass=Group)" cn member

# Test user authentication (bind as fry)
ldapwhoami -H ldap://localhost:10389 -x \
  -D "cn=Philip J. Fry,ou=people,dc=planetexpress,dc=com" \
  -w fry
```

### Check Login Methods API

```bash
curl -s http://localhost:8080/api/auth/login-methods | python3 -m json.tool
```

Expected response:

```json
{
  "appAccount": {
    "enabled": false,
    "allowRegistration": false
  },
  "ldapEnabled": true,
  "oauth2Enabled": false
}
```

---

## Troubleshooting

### LDAP Connection Refused

If the API cannot connect to the LDAP server:

1. Verify the OpenLDAP container is running:
   ```bash
   docker ps | grep myrc-testldap-openldap
   ```
2. Check OpenLDAP logs:
   ```bash
   docker compose --project-name myrc-testldap -f docker-compose.testldap.yml logs openldap
   ```
3. Verify LDAP is reachable from the API container:
   ```bash
   docker exec myrc-testldap-api curl -v telnet://openldap:10389
   ```

### Login Fails with Valid Credentials

1. Check API security logs for detailed error messages:
   ```bash
   docker compose --project-name myrc-testldap -f docker-compose.testldap.yml logs -f api | grep -i "ldap\|auth\|security"
   ```
2. Verify the user exists in LDAP:
   ```bash
   ldapsearch -H ldap://localhost:10389 -x \
     -b "ou=people,dc=planetexpress,dc=com" \
     -D "cn=admin,dc=planetexpress,dc=com" \
     -w GoodNewsEveryone \
     "(uid=fry)"
   ```
3. Verify direct LDAP bind works:
   ```bash
   ldapwhoami -H ldap://localhost:10389 -x \
     -D "cn=Philip J. Fry,ou=people,dc=planetexpress,dc=com" \
     -w fry
   ```

### No Login Tab Shown on Frontend

If the login page shows "No login methods available":

1. Check the login-methods endpoint:
   ```bash
   curl -s http://localhost:8080/api/auth/login-methods
   ```
2. Verify the environment variables are correctly set:
   ```bash
   docker inspect myrc-testldap-api | grep -A 5 "LOGIN_METHODS\|LDAP_ENABLED"
   ```

### Port Conflicts

If ports `4200`, `8080`, `5432`, `10389`, or `10636` are already in use:

1. Stop the standard deployment first:
   ```bash
   docker compose --project-name myrc -f docker-compose.dev.yml down
   ```
2. Check for other processes using the ports:
   ```bash
   sudo lsof -i :4200 -i :8080 -i :5432 -i :10389
   ```

---

## Files

| File                              | Description                                  |
|-----------------------------------|----------------------------------------------|
| `testldap-build-and-run.sh`       | Build and deploy script for test LDAP setup   |
| `docker-compose.testldap.yml`     | Docker Compose with LDAP-only configuration   |
| `docs/TEST_LDAP_DEPLOYMENT.md`    | This documentation file                       |
| `backend/src/main/resources/ldap-config.yml` | Reference LDAP configuration template |

---

## Related Documentation

- [LDAP Configuration Reference](LDAP.md)
- [Docker LDAP & OAuth2 Setup](DOCKER_LDAP_OAUTH2.md)
- [Security Architecture](SECURITY.md)
- [User Guide](USER_GUIDE.md)
- [docker-test-openldap GitHub Repository](https://github.com/rroemhild/docker-test-openldap)
