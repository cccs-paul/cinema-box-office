# Kubernetes Deployment Files

This directory contains all Kubernetes manifests needed to deploy myRC on Kubernetes clusters.

## File Structure

```
k8s/
├── namespace.yaml              # Kubernetes namespace definition
├── configmap.yaml              # Application configuration
├── secrets.yaml                # Sensitive configuration (template)
├── postgres.yaml               # PostgreSQL database deployment
├── backend.yaml                # Backend API deployment
├── frontend.yaml               # Frontend web application deployment
├── ingress.yaml                # Ingress controller configuration
├── kustomization.yaml          # Kustomize configuration for unified deployment
└── README.md                   # This file
```

## Quick Start

### Prerequisites

- Kubernetes cluster v1.19 or higher
- `kubectl` configured to connect to your cluster
- `kustomize` installed
- Docker images pushed to accessible registry

### Installation

```bash
# Install to default cinema-box-office namespace
./k8s-deploy.sh install

# Verify deployment
./k8s-deploy.sh status

# Watch health status
./k8s-health.sh --watch --interval 10
```

### Uninstallation

```bash
./k8s-deploy.sh uninstall
```

## File Descriptions

### namespace.yaml

Creates the `cinema-box-office` namespace with appropriate labels for organization and tracking.

```bash
kubectl apply -f k8s/namespace.yaml
```

### configmap.yaml

Contains application configuration for:
- Spring profiles (dev, prod, oauth2, ldap)
- Logging levels
- CORS configuration
- Database connection details
- Authentication provider settings

Update before deployment:

```yaml
data:
  SPRING_PROFILES_ACTIVE: "prod,oauth2"
  CORS_ALLOWED_ORIGINS: "https://your-domain.com"
  SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres:5432/boxoffice"
```

### secrets.yaml (TEMPLATE)

Contains sensitive data that must be updated before deployment:

```bash
# Create secrets from template
kubectl create secret generic cinema-box-office-secrets \
  --from-literal=db-password="your-secure-password" \
  --from-literal=db-user="boxoffice" \
  --from-literal=oauth2-client-id="your-client-id" \
  --from-literal=oauth2-client-secret="your-client-secret" \
  --from-literal=ldap-password="your-ldap-password" \
  -n cinema-box-office
```

**Important**: Never commit actual secrets to version control. Always use:
- Kubernetes Secrets
- External secret management (Vault, AWS Secrets Manager, etc.)
- CI/CD secret management

### postgres.yaml

PostgreSQL database deployment with:
- 1 replica (increase for HA)
- PersistentVolumeClaim for data persistence (10Gi)
- Liveness and readiness probes
- Resource limits and requests
- Database initialization

Includes:
- `postgres` Deployment
- `postgres` Service (ClusterIP)
- `postgres-data` PersistentVolumeClaim
- Health checks configured

### backend.yaml

Backend API deployment with:
- 2 replicas (configurable)
- Spring Boot with Java 25
- Horizontal Pod Autoscaler (2-10 replicas)
- Resource limits: 250m CPU request / 1000m limit, 512Mi memory request / 1Gi limit
- Pod anti-affinity for distribution
- Health checks on `/api/health`
- Environment variables from ConfigMap and Secrets

Includes:
- `api` Deployment
- `api` Service (ClusterIP)
- `api` Horizontal Pod Autoscaler
- Health check configuration

### frontend.yaml

Frontend web application deployment with:
- 2 replicas (configurable)
- NGINX serving Angular app
- Horizontal Pod Autoscaler (2-5 replicas)
- Resource limits: 100m CPU request / 500m limit, 128Mi memory request / 256Mi limit
- Pod anti-affinity for distribution
- Health checks on `/`
- Reverse proxy configuration to backend

Includes:
- `web` Deployment
- `web` Service (ClusterIP)
- `web` Horizontal Pod Autoscaler
- Health check configuration

### ingress.yaml

NGINX Ingress configuration with:
- TLS/SSL support (requires certificate)
- Path-based routing
- RBAC configuration
- ServiceAccount with minimal permissions
- Hostname configuration

Update before deployment:

```yaml
hosts:
  - host: cinema-box-office.example.com
    paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: web
            port:
              number: 80
```

### kustomization.yaml

Kustomize configuration for deploying all manifests in order:

```bash
# Deploy all resources
kubectl apply -k k8s/

# Dry run to verify
kubectl apply -k k8s/ --dry-run=client

# Preview rendered manifests
kustomize build k8s/
```

## Deployment Guide

### 1. Prepare Configuration

```bash
# Edit configmap.yaml with your settings
vim k8s/configmap.yaml

# Edit secrets.yaml with credentials
vim k8s/secrets.yaml
```

### 2. Update Image References

Edit backend.yaml and frontend.yaml to use your Docker registry:

```yaml
# Update image references
image: your-registry.com/cinema-box-office-api:v1.0
image: your-registry.com/cinema-box-office-frontend:v1.0
```

### 3. Create Secrets

```bash
# Create namespace first
kubectl apply -f k8s/namespace.yaml

# Create secrets
kubectl create secret generic cinema-box-office-secrets \
  --from-literal=db-password="$(openssl rand -base64 32)" \
  --from-literal=oauth2-client-secret="$(openssl rand -base64 32)" \
  -n cinema-box-office
```

### 4. Deploy Application

```bash
# Deploy all resources
./k8s-deploy.sh install

# Or use kubectl directly
kubectl apply -k k8s/
```

### 5. Verify Deployment

```bash
# Check pod status
./k8s-deploy.sh status

# Monitor health
./k8s-health.sh --watch

# View logs
./k8s-deploy.sh logs api
./k8s-deploy.sh logs web
./k8s-deploy.sh logs postgres
```

### 6. Access Application

```bash
# Get Ingress IP/hostname
kubectl get ingress cinema-box-office-ingress -n cinema-box-office

# Port forward for local testing
./k8s-deploy.sh port-forward web 4200 80
./k8s-deploy.sh port-forward api 8080 8080

# Access application
open http://localhost:4200
```

## Operational Tasks

### View Deployment Status

```bash
./k8s-deploy.sh status
```

### View Logs

```bash
./k8s-deploy.sh logs api
./k8s-deploy.sh logs web
./k8s-deploy.sh logs postgres
```

### Port Forwarding

```bash
# Forward frontend
./k8s-deploy.sh port-forward web 4200 80

# Forward API
./k8s-deploy.sh port-forward api 8080 8080

# Forward database
./k8s-deploy.sh port-forward postgres 5432 5432
```

### Scale Deployments

```bash
# Scale API to 5 replicas
./k8s-deploy.sh scale api 5

# Scale frontend to 3 replicas
./k8s-deploy.sh scale web 3
```

### Update Images

```bash
# Update API to new version
./k8s-deploy.sh update api cinema-box-office-api:v1.1

# Update frontend to new version
./k8s-deploy.sh update web cinema-box-office-frontend:v1.1
```

### Rollback Deployment

```bash
# Rollback API to previous version
./k8s-deploy.sh rollback api

# Rollback frontend to previous version
./k8s-deploy.sh rollback web
```

### Database Backup/Restore

```bash
# Create backup
./k8s-deploy.sh backup

# Restore from backup
./k8s-deploy.sh restore boxoffice-backup-20260117-120000.sql.gz
```

### Execute Commands in Pods

```bash
# Execute in database pod
./k8s-deploy.sh exec postgres psql -U boxoffice -d boxoffice

# Execute in API pod
./k8s-deploy.sh exec api sh

# Execute in frontend pod
./k8s-deploy.sh exec web sh
```

## Health Monitoring

Use the health check script to monitor cluster health:

```bash
# Single check
./k8s-health.sh

# Continuous monitoring
./k8s-health.sh --watch --interval 5
```

The health check script verifies:
- Pod status and readiness
- Deployment replicas
- Service endpoints
- Ingress configuration
- Resource usage
- Persistent volumes
- Configuration integrity

## Resource Allocation

Default resource requests and limits:

| Component | Request CPU | Request Mem | Limit CPU | Limit Mem |
|-----------|------------|------------|-----------|-----------|
| API       | 250m       | 512Mi      | 1000m     | 1Gi       |
| Frontend  | 100m       | 128Mi      | 500m      | 256Mi     |
| Database  | 250m       | 256Mi      | 500m      | 512Mi     |

Adjust based on your load and cluster capacity.

## Horizontal Pod Autoscaling

### Backend API

- Min replicas: 2
- Max replicas: 10
- Target CPU utilization: 70%
- Target memory utilization: 80%

### Frontend

- Min replicas: 2
- Max replicas: 5
- Target CPU utilization: 70%
- Target memory utilization: 80%

### View HPA Status

```bash
kubectl get hpa -n cinema-box-office
kubectl describe hpa api -n cinema-box-office
```

## Persistence

### Database Persistence

- PersistentVolumeClaim: `postgres-data` (10Gi)
- Mount path: `/var/lib/postgresql/data`
- StorageClass: (uses default, update if needed)

### Other Persistent Volumes

For production deployments, consider adding:
- Backup storage PVC
- Log storage PVC
- Cache storage PVC (Redis)

## Networking

### Services

- `postgres` - ClusterIP (internal only)
- `api` - ClusterIP
- `web` - ClusterIP

### Ingress

- NGINX Ingress Controller
- Path-based routing to services
- TLS/SSL support
- Hostname: cinema-box-office.example.com (update as needed)

### Network Policies

Restrict traffic between components:

```bash
# Deny all ingress by default
# Allow specific traffic

# Applied via network-policies.yaml (create if needed)
```

## Security Considerations

1. **Secrets Management**
   - Never commit secrets to version control
   - Use external secret management (Vault, AWS Secrets Manager)
   - Rotate secrets regularly

2. **RBAC**
   - Minimal permissions for ServiceAccounts
   - No cluster-admin role used

3. **Pod Security**
   - Run as non-root user (1001)
   - Read-only root filesystem where possible
   - Drop unnecessary Linux capabilities

4. **Image Security**
   - Use private Docker registry
   - Scan images for vulnerabilities
   - Sign images for verification

5. **Network Security**
   - Network policies restrict traffic
   - TLS/SSL for external communication
   - Internal communication over ClusterIP services

## Troubleshooting

### Pods not starting

```bash
# Check pod status
kubectl get pods -n cinema-box-office

# Check pod events
kubectl describe pod <pod-name> -n cinema-box-office

# View logs
kubectl logs <pod-name> -n cinema-box-office
```

### Persistent volume issues

```bash
# Check PVCs
kubectl get pvc -n cinema-box-office

# Describe PVC
kubectl describe pvc <pvc-name> -n cinema-box-office

# Check PVs
kubectl get pv
```

### Database connection issues

```bash
# Test database connection
./k8s-deploy.sh port-forward postgres 5432 5432
psql -h localhost -U boxoffice -d boxoffice

# Check database pod
./k8s-deploy.sh exec postgres psql -U boxoffice -d boxoffice
```

### Ingress not working

```bash
# Check ingress status
kubectl get ingress -n cinema-box-office

# Describe ingress
kubectl describe ingress cinema-box-office-ingress -n cinema-box-office

# Check NGINX controller logs
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx
```

## Documentation

See additional documentation:
- [KUBERNETES.md](../docs/KUBERNETES.md) - Deployment guide
- [PRODUCTION.md](../docs/PRODUCTION.md) - Production deployment
- [SECURITY.md](../docs/SECURITY.md) - Security best practices
- [LDAP.md](../docs/LDAP.md) - LDAP integration
- [OAUTH2.md](../docs/OAUTH2.md) - OAuth2 integration

## Support

For issues or questions:
- Check the logs: `./k8s-deploy.sh logs <component>`
- Use health check: `./k8s-health.sh`
- Review events: `kubectl get events -n cinema-box-office`
- Check documentation in docs/ directory
