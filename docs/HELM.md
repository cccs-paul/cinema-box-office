# Helm Deployment Guide

## Overview

myRC provides a comprehensive Helm chart for deploying to Kubernetes. The chart supports multiple deployment scenarios through composable values files, covering everything from local development to hardened production deployments with LDAP or OAuth2 integration.

## Prerequisites

- **Kubernetes** 1.25+
- **Helm** 3.10+
- **Container images** built and available in a registry:
  - `myrc-api:latest` (Spring Boot backend)
  - `myrc-web:latest` (Angular/Nginx frontend)
- *(Optional)* NGINX Ingress Controller
- *(Optional)* cert-manager for TLS certificate automation

### Building Images

```bash
# Build both images from the project root
./build.sh

# Or build individually
cd backend && mvn clean package -DskipTests && cd ..
docker build -f backend/Dockerfile -t myrc-api:latest .
docker build -f frontend/Dockerfile -t myrc-web:latest .
```

## Chart Structure

```
helm/myrc/
├── Chart.yaml                 # Chart metadata
├── values.yaml                # Default values (all parameters)
├── values-dev.yaml            # Development environment
├── values-test.yaml           # Test / QA environment
├── values-prod.yaml           # Production (hardened, HA)
├── values-app-accounts.yaml   # App accounts only authentication
├── values-ldap.yaml           # LDAP only (existing LDAP server)
├── values-ldap-app.yaml       # LDAP + app accounts
├── values-testldap.yaml       # Bundled test LDAP (Futurama users)
├── values-oauth2.yaml         # OAuth2 / OIDC authentication
├── values-minikube.yaml       # Minikube / local Kubernetes
├── templates/
│   ├── _helpers.tpl           # Template helper functions
│   ├── namespace.yaml         # Namespace
│   ├── configmap.yaml         # Application configuration
│   ├── secrets.yaml           # Secrets (DB, LDAP, OAuth2)
│   ├── backend.yaml           # Backend Deployment + Service
│   ├── frontend.yaml          # Frontend Deployment + Service
│   ├── postgresql.yaml        # PostgreSQL StatefulSet (optional)
│   ├── ingress.yaml           # Ingress (optional)
│   ├── hpa.yaml               # HorizontalPodAutoscalers
│   ├── pdb.yaml               # PodDisruptionBudgets
│   ├── rbac.yaml              # ServiceAccount, Role, RoleBinding
│   ├── testldap.yaml          # Test LDAP Deployment (optional)
│   └── NOTES.txt              # Post-install notes
└── README.md                  # Chart documentation
```

## Deployment Scenarios

### Scenario 1: App Accounts Only (Simplest)

No external identity provider. Users register and log in with local database accounts.

```bash
helm install myrc ./helm/myrc -f helm/myrc/values-app-accounts.yaml
```

### Scenario 2: Development

Single replicas, debug logging, no TLS. Ideal for local Kubernetes or CI.

```bash
helm install myrc-dev ./helm/myrc -f helm/myrc/values-dev.yaml
```

### Scenario 3: Test / QA

2 replicas per component, autoscaling, TLS, INFO-level logging.

```bash
helm install myrc-test ./helm/myrc -f helm/myrc/values-test.yaml
```

### Scenario 4: Production

High-availability deployment with strict security, pod anti-affinity, and production-grade resource limits.

```bash
helm install myrc ./helm/myrc -f helm/myrc/values-prod.yaml \
  --set postgresql.auth.password=<STRONG_PASSWORD> \
  --set ingress.host=myrc.yourdomain.com
```

### Scenario 5: LDAP Only (Existing LDAP / Active Directory)

Connects to an existing corporate LDAP/AD. App accounts are disabled.

```bash
helm install myrc ./helm/myrc -f helm/myrc/values-ldap.yaml \
  --set auth.ldap.url=ldap://ldap.corp.com:389 \
  --set auth.ldap.baseDn=dc=corp,dc=com \
  --set auth.ldap.managerDn="cn=svc-myrc,ou=service-accounts,dc=corp,dc=com" \
  --set auth.ldap.managerPassword=<LDAP_PASSWORD>
```

**Customizing group-to-role mappings:**

Edit the values file or pass them on the command line:

```bash
--set 'auth.ldap.groupMappings[0].groupDn=cn=myrc-admins,ou=groups,dc=corp,dc=com' \
--set 'auth.ldap.groupMappings[0].role=ADMIN' \
--set 'auth.ldap.groupMappings[0].isAdmin=true' \
--set 'auth.ldap.groupMappings[1].groupDn=cn=myrc-users,ou=groups,dc=corp,dc=com' \
--set 'auth.ldap.groupMappings[1].role=USER' \
--set 'auth.ldap.groupMappings[1].isAdmin=false'
```

### Scenario 6: LDAP + App Accounts

Both LDAP and local accounts enabled. Useful during LDAP migration or for service accounts that don't exist in LDAP.

```bash
helm install myrc ./helm/myrc -f helm/myrc/values-ldap-app.yaml \
  --set auth.ldap.url=ldap://ldap.corp.com:389 \
  --set auth.ldap.baseDn=dc=corp,dc=com \
  --set auth.ldap.managerDn="cn=svc-myrc,ou=service-accounts,dc=corp,dc=com" \
  --set auth.ldap.managerPassword=<LDAP_PASSWORD>
```

### Scenario 7: Test LDAP (Bundled OpenLDAP)

Deploys a bundled OpenLDAP server pre-populated with Futurama / Planet Express test users. No external LDAP required.

```bash
helm install myrc-testldap ./helm/myrc -f helm/myrc/values-testldap.yaml
```

**Test user credentials** (password = username):

| Username    | LDAP Group    | App Role |
|-------------|---------------|----------|
| `professor` | admin_staff   | ADMIN    |
| `hermes`    | admin_staff   | ADMIN    |
| `fry`       | ship_crew     | USER     |
| `leela`     | ship_crew     | USER     |
| `bender`    | ship_crew     | USER     |

### Scenario 8: OAuth2 / OIDC

External OAuth2 providers (Google, GitHub, Azure AD).

```bash
helm install myrc ./helm/myrc -f helm/myrc/values-oauth2.yaml \
  --set auth.oauth2.providers.google.clientId=<CLIENT_ID> \
  --set auth.oauth2.providers.google.clientSecret=<CLIENT_SECRET>
```

### Scenario 9: Minikube

Minimal resources, NodePort services, no ingress. Use `minikube service` to access.

```bash
helm install myrc-local ./helm/myrc -f helm/myrc/values-minikube.yaml
```

After deploy:
```bash
minikube service myrc-local-frontend -n myrc
```

## Combining Scenarios

Values files can be stacked. Later files override earlier values:

```bash
# Production + LDAP + app accounts
helm install myrc ./helm/myrc \
  -f helm/myrc/values-prod.yaml \
  -f helm/myrc/values-ldap-app.yaml \
  --set auth.ldap.url=ldap://ldap.corp.com:389 \
  --set auth.ldap.managerPassword=<PASSWORD>

# Test environment with bundled test LDAP
helm install myrc-test ./helm/myrc \
  -f helm/myrc/values-test.yaml \
  -f helm/myrc/values-testldap.yaml

# Minikube with test LDAP
helm install myrc-local ./helm/myrc \
  -f helm/myrc/values-minikube.yaml \
  -f helm/myrc/values-testldap.yaml
```

## Using an External Database

To connect to an existing PostgreSQL instance instead of the bundled StatefulSet:

```bash
helm install myrc ./helm/myrc \
  --set postgresql.enabled=false \
  --set externalDatabase.host=db.corp.com \
  --set externalDatabase.port=5432 \
  --set externalDatabase.database=myrc \
  --set externalDatabase.username=myrc_user \
  --set externalDatabase.password=<DB_PASSWORD>
```

Or reference an existing Kubernetes Secret:

```bash
helm install myrc ./helm/myrc \
  --set postgresql.enabled=false \
  --set externalDatabase.host=db.corp.com \
  --set externalDatabase.existingSecret=my-db-secret \
  --set externalDatabase.existingSecretPasswordKey=password
```

## Upgrading

```bash
# Upgrade with new values
helm upgrade myrc ./helm/myrc -f helm/myrc/values-prod.yaml

# Upgrade and override specific values
helm upgrade myrc ./helm/myrc \
  -f helm/myrc/values-prod.yaml \
  --set backend.image.tag=2.0.0 \
  --set frontend.image.tag=2.0.0
```

## Uninstalling

```bash
helm uninstall myrc

# To also remove persistent data (PVCs):
kubectl delete pvc -l app.kubernetes.io/instance=myrc -n myrc
```

## Troubleshooting

### Check Pod Status

```bash
kubectl get pods -n myrc
kubectl describe pod <pod-name> -n myrc
kubectl logs <pod-name> -n myrc
```

### Check ConfigMap and Secrets

```bash
kubectl get configmap -n myrc
kubectl describe configmap myrc-config -n myrc

kubectl get secrets -n myrc
```

### Common Issues

| Issue | Cause | Fix |
|-------|-------|-----|
| Backend `CrashLoopBackOff` | DB not ready | Wait for StatefulSet, check `kubectl logs` |
| LDAP connection refused | Wrong URL or LDAP down | Verify `auth.ldap.url`, check LDAP service |
| Ingress 502 | Backend not ready | Check readiness probes, backend logs |
| PVC pending | No StorageClass | Set `postgresql.storage.storageClassName` |
| ImagePullBackOff | Image not in registry | Push images, set `imagePullSecrets` |

### Dry-Run & Debug

```bash
# Render templates without deploying
helm template myrc ./helm/myrc -f helm/myrc/values-dev.yaml

# Dry-run install
helm install myrc ./helm/myrc --dry-run -f helm/myrc/values-dev.yaml

# Lint the chart
helm lint ./helm/myrc
```
