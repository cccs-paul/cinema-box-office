# myRC Helm Chart

Helm chart for deploying the **myRC** (Responsibility Centre Management) application on Kubernetes.

## Components

| Component | Description |
|-----------|-------------|
| **Backend API** | Spring Boot REST API (Java 25) |
| **Frontend** | Angular application served by Nginx |
| **PostgreSQL** | Database (bundled StatefulSet or external) |
| **Test LDAP** | Optional bundled OpenLDAP for dev/test |

## Prerequisites

- Kubernetes 1.25+
- Helm 3.10+
- Container images built and available:
  - `myrc-api:latest` — backend
  - `myrc-web:latest` — frontend

## Quick Start

```bash
# Build images first
./build.sh

# Install with defaults (app accounts, bundled PostgreSQL)
helm install myrc ./helm/myrc

# Install for development
helm install myrc-dev ./helm/myrc -f helm/myrc/values-dev.yaml

# Install with test LDAP (Futurama users)
helm install myrc-testldap ./helm/myrc -f helm/myrc/values-testldap.yaml
```

## Deployment Scenarios

### 1. App Accounts Only (Default)

Local database accounts. No external identity provider needed.

```bash
helm install myrc ./helm/myrc -f helm/myrc/values-app-accounts.yaml
```

### 2. Development

Single replicas, debug logging, no TLS.

```bash
helm install myrc-dev ./helm/myrc -f helm/myrc/values-dev.yaml
```

### 3. Test / QA

Multiple replicas, autoscaling, TLS, INFO logging.

```bash
helm install myrc-test ./helm/myrc -f helm/myrc/values-test.yaml
```

### 4. Production

HA deployment with strict TLS, pod anti-affinity, and production resource limits.

```bash
helm install myrc ./helm/myrc -f helm/myrc/values-prod.yaml \
  --set postgresql.auth.password=<STRONG_DB_PASSWORD> \
  --set ingress.host=myrc.yourdomain.com
```

### 5. LDAP Only (Existing LDAP Server)

Connects to your corporate LDAP/Active Directory. App accounts disabled.

```bash
helm install myrc ./helm/myrc -f helm/myrc/values-ldap.yaml \
  --set auth.ldap.url=ldap://ldap.corp.com:389 \
  --set auth.ldap.baseDn=dc=corp,dc=com \
  --set auth.ldap.managerDn=cn=svc-myrc,ou=service-accounts,dc=corp,dc=com \
  --set auth.ldap.managerPassword=<LDAP_PASSWORD>
```

### 6. LDAP + App Accounts

Both LDAP and local accounts are available. Useful during LDAP migration or for service accounts.

```bash
helm install myrc ./helm/myrc -f helm/myrc/values-ldap-app.yaml \
  --set auth.ldap.url=ldap://ldap.corp.com:389 \
  --set auth.ldap.baseDn=dc=corp,dc=com \
  --set auth.ldap.managerDn=cn=svc-myrc,ou=service-accounts,dc=corp,dc=com \
  --set auth.ldap.managerPassword=<LDAP_PASSWORD>
```

### 7. Test LDAP (Bundled OpenLDAP)

Deploys a bundled OpenLDAP with pre-populated Futurama / Planet Express test users. Ideal for development and integration testing.

```bash
helm install myrc-testldap ./helm/myrc -f helm/myrc/values-testldap.yaml
```

**Test users** (password = username):

| Username | Group | Role |
|----------|-------|------|
| professor | admin_staff | ADMIN |
| hermes | admin_staff | ADMIN |
| fry | ship_crew | USER |
| leela | ship_crew | USER |
| bender | ship_crew | USER |

### 8. OAuth2 / OIDC

External OAuth2 providers (Google, GitHub, Azure AD).

```bash
helm install myrc ./helm/myrc -f helm/myrc/values-oauth2.yaml \
  --set auth.oauth2.providers.google.clientId=<CLIENT_ID> \
  --set auth.oauth2.providers.google.clientSecret=<CLIENT_SECRET>
```

### 9. Minikube / Local Kubernetes

Minimal resources, NodePort services, no ingress controller required.

```bash
helm install myrc-local ./helm/myrc -f helm/myrc/values-minikube.yaml
```

## Combining Scenarios

Values files can be stacked. Later files override earlier ones:

```bash
# Production with LDAP + app accounts
helm install myrc ./helm/myrc \
  -f helm/myrc/values-prod.yaml \
  -f helm/myrc/values-ldap-app.yaml \
  --set auth.ldap.url=ldap://ldap.corp.com:389 \
  --set auth.ldap.managerPassword=<PASSWORD>

# Test environment with test LDAP
helm install myrc-test ./helm/myrc \
  -f helm/myrc/values-test.yaml \
  -f helm/myrc/values-testldap.yaml
```

## Using an External Database

To connect to an existing PostgreSQL instead of the bundled StatefulSet:

```bash
helm install myrc ./helm/myrc \
  --set postgresql.enabled=false \
  --set externalDatabase.host=db.corp.com \
  --set externalDatabase.port=5432 \
  --set externalDatabase.database=myrc \
  --set externalDatabase.username=myrc_user \
  --set externalDatabase.password=<DB_PASSWORD>
```

Or reference an existing Kubernetes secret:

```bash
helm install myrc ./helm/myrc \
  --set postgresql.enabled=false \
  --set externalDatabase.host=db.corp.com \
  --set externalDatabase.existingSecret=my-db-secret \
  --set externalDatabase.existingSecretPasswordKey=password
```

## Configuration Reference

### Global

| Parameter | Description | Default |
|-----------|-------------|---------|
| `global.namespace` | Kubernetes namespace | `myrc` |
| `imagePullSecrets` | Image pull secret names | `[]` |

### Backend

| Parameter | Description | Default |
|-----------|-------------|---------|
| `backend.replicaCount` | Replicas | `2` |
| `backend.image.repository` | Image | `myrc-api` |
| `backend.image.tag` | Tag | `latest` |
| `backend.springProfiles` | Spring profiles | `prod` |
| `backend.resources.requests.cpu` | CPU request | `250m` |
| `backend.resources.requests.memory` | Memory request | `512Mi` |
| `backend.autoscaling.enabled` | Enable HPA | `true` |
| `backend.podDisruptionBudget.enabled` | Enable PDB | `true` |

### Frontend

| Parameter | Description | Default |
|-----------|-------------|---------|
| `frontend.replicaCount` | Replicas | `2` |
| `frontend.image.repository` | Image | `myrc-web` |
| `frontend.image.tag` | Tag | `latest` |
| `frontend.resources.requests.cpu` | CPU request | `100m` |
| `frontend.resources.requests.memory` | Memory request | `128Mi` |

### PostgreSQL

| Parameter | Description | Default |
|-----------|-------------|---------|
| `postgresql.enabled` | Deploy bundled PostgreSQL | `true` |
| `postgresql.auth.database` | Database name | `myrc` |
| `postgresql.auth.username` | Username | `myrc` |
| `postgresql.auth.password` | Password | `myrc_password` |
| `postgresql.storage.size` | PVC size | `10Gi` |
| `postgresql.storage.storageClassName` | Storage class | `standard` |

### Ingress

| Parameter | Description | Default |
|-----------|-------------|---------|
| `ingress.enabled` | Enable ingress | `true` |
| `ingress.className` | Ingress class | `nginx` |
| `ingress.host` | Hostname | `myrc.example.com` |
| `ingress.tls.enabled` | Enable TLS | `true` |
| `ingress.tls.secretName` | TLS secret | `myrc-tls` |

### Authentication

| Parameter | Description | Default |
|-----------|-------------|---------|
| `auth.appAccount.enabled` | App accounts | `true` |
| `auth.appAccount.allowRegistration` | Self-registration | `true` |
| `auth.ldap.enabled` | LDAP auth | `false` |
| `auth.ldap.url` | LDAP URL | `ldap://ldap.example.com:389` |
| `auth.ldap.baseDn` | Base DN | `dc=example,dc=com` |
| `auth.ldap.managerDn` | Manager DN | `cn=admin,...` |
| `auth.ldap.managerPassword` | Manager password | `""` |
| `auth.ldap.groupMappings` | Group→role maps | `[]` |
| `auth.oauth2.enabled` | OAuth2 auth | `false` |
| `auth.oauth2.providers` | Provider configs | `{}` |
| `testLdap.enabled` | Bundled test LDAP | `false` |

### Flyway & JPA

| Parameter | Description | Default |
|-----------|-------------|---------|
| `flyway.enabled` | Enable Flyway | `true` |
| `jpa.ddlAuto` | DDL strategy | `validate` |

## Uninstalling

```bash
helm uninstall myrc
# Clean up PVCs (data will be lost):
kubectl delete pvc -l app.kubernetes.io/instance=myrc -n myrc
```

## Development

```bash
# Lint the chart
helm lint ./helm/myrc

# Dry-run render templates
helm template myrc ./helm/myrc -f helm/myrc/values-dev.yaml

# Diff before upgrade
helm diff upgrade myrc ./helm/myrc -f helm/myrc/values-prod.yaml
```
