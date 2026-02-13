# Kubernetes Deployment Guide

## Overview

This guide covers deploying myRC to Kubernetes clusters. The application is containerized and ready for production deployment.

> **Recommended:** For new deployments, use the **Helm chart** instead of raw manifests. See [docs/HELM.md](HELM.md) and [helm/myrc/README.md](../helm/myrc/README.md) for Helm-based deployment with composable values files for every scenario (dev, test, prod, LDAP, OAuth2, etc.).

## Prerequisites

- Kubernetes cluster (v1.19+)
- kubectl configured to access your cluster
- Docker images built and pushed to a registry:
  - `myrc-api:latest`
  - `myrc-web:latest`
- (Optional) NGINX Ingress Controller for external access
- (Optional) cert-manager for TLS certificates

## Quick Start

### 1. Create the Namespace and Deploy

```bash
# Apply all manifests
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/backend.yaml
kubectl apply -f k8s/frontend.yaml
kubectl apply -f k8s/ingress.yaml

# Or use Kustomize for a single command
kubectl apply -k k8s/
```

### 2. Verify Deployment

```bash
# Check namespace
kubectl get namespace myrc

# Check deployments
kubectl get deployments -n myrc

# Check pods
kubectl get pods -n myrc

# Check services
kubectl get svc -n myrc

# Check ingress
kubectl get ingress -n myrc
```

### 3. Access the Application

```bash
# Port-forward to access locally
kubectl port-forward -n myrc svc/frontend 8000:80

# Then open http://localhost:8000 in your browser

# Or port-forward the API
kubectl port-forward -n myrc svc/api 8080:8080
```

## Deployment Architecture

### Components

1. **PostgreSQL Database**
   - Single-instance deployment
   - Persistent volume for data storage
   - Environment: Kubernetes

2. **Backend API**
   - Multi-replica deployment (default: 2)
   - Horizontal Pod Autoscaler (2-10 replicas)
   - Health checks enabled
   - Resource limits configured

3. **Frontend**
   - Multi-replica deployment (default: 2)
   - Horizontal Pod Autoscaler (2-5 replicas)
   - Nginx-based static hosting
   - Health checks enabled

4. **Ingress Controller**
   - NGINX Ingress for external access
   - TLS termination with cert-manager
   - Path-based routing

## Configuration Management

### ConfigMap

The `configmap.yaml` contains application-level configuration:
- Database settings
- Logging levels
- API endpoints
- CORS settings

### Secrets

The `secrets.yaml` contains sensitive data:
- Database credentials
- LDAP credentials (if enabled)
- OAuth2 secrets
- JWT secrets

**⚠️ Important:** Replace the base64-encoded values with your actual secrets:

```bash
# Encode a secret value
echo -n "your-value" | base64

# Decode a secret value
echo "base64-encoded-value" | base64 -d
```

## Scaling

### Horizontal Pod Autoscaling

Both API and Frontend deployments include HPA configurations:

```bash
# View HPA status
kubectl get hpa -n myrc

# View detailed HPA status
kubectl describe hpa api-hpa -n myrc

# Manually scale (overrides HPA)
kubectl scale deployment api --replicas=5 -n myrc
```

### Vertical Pod Autoscaling (Optional)

To enable VPA, install the VPA controller first, then uncomment the VPA configurations in the manifests.

## Resource Management

### CPU and Memory Requests/Limits

- **Frontend**: 100m CPU / 128Mi Memory (request), 500m CPU / 256Mi Memory (limit)
- **API**: 250m CPU / 512Mi Memory (request), 1000m CPU / 1Gi Memory (limit)
- **Database**: 100m CPU / 256Mi Memory (request), 500m CPU / 512Mi Memory (limit)

Adjust these based on your workload:

```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

## Troubleshooting

### Check Pod Status

```bash
# Get pod status
kubectl get pods -n myrc

# Get detailed pod information
kubectl describe pod <pod-name> -n myrc

# View pod logs
kubectl logs <pod-name> -n myrc

# Stream logs
kubectl logs -f <pod-name> -n myrc

# View logs from a specific container
kubectl logs <pod-name> -c <container-name> -n myrc
```

### Common Issues

**Pods not starting:**
```bash
# Check events
kubectl describe pod <pod-name> -n myrc

# Check node status
kubectl get nodes

# Check node resources
kubectl top nodes
```

**Database connectivity issues:**
```bash
# Test database connection from a pod
kubectl exec -it <backend-pod> -n myrc -- \
  psql -h postgres -U myrc -d myrc -c "SELECT 1;"
```

**API health check failures:**
```bash
# Check API logs
kubectl logs -f deployment/api -n myrc

# Test API endpoint
kubectl exec -it <backend-pod> -n myrc -- \
  curl http://localhost:8080/api/health
```

## Maintenance

### Backup Database

```bash
# Backup PostgreSQL data
kubectl exec -it deployment/postgres -n myrc -- \
  pg_dump -U myrc myrc > backup.sql
```

### Restore Database

```bash
# Restore PostgreSQL data
kubectl exec -it deployment/postgres -n myrc -- \
  psql -U myrc myrc < backup.sql
```

### Update Deployment

```bash
# Update image
kubectl set image deployment/api \
  api=myrc-api:v1.1 \
  -n myrc

# Check rollout status
kubectl rollout status deployment/api -n myrc

# Rollback if needed
kubectl rollout undo deployment/api -n myrc
```

## Monitoring and Logging

### Prometheus Metrics (Optional)

Add service monitor annotations to enable Prometheus scraping:

```yaml
annotations:
  prometheus.io/scrape: "true"
  prometheus.io/port: "8080"
  prometheus.io/path: "/actuator/prometheus"
```

### Distributed Tracing (Optional)

Enable Jaeger tracing by adding environment variables:

```yaml
env:
- name: OTEL_EXPORTER_OTLP_ENDPOINT
  value: "http://jaeger-collector:4317"
```

## Next Steps

1. [Helm Deployment (recommended)](HELM.md)
2. [LDAP Integration](LDAP.md)
3. [OAuth2 Integration](OAUTH2.md)
4. [Security Best Practices](SECURITY.md)
5. [Production Deployment](PRODUCTION.md)
