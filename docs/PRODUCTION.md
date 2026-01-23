# Production Deployment Guide

## Overview

This guide covers best practices for deploying myRC to production Kubernetes clusters with security, reliability, and scalability considerations.

## Pre-Deployment Checklist

- [ ] Docker images built and tested locally
- [ ] Images pushed to private Docker registry
- [ ] Database backups configured
- [ ] SSL/TLS certificates obtained (Let's Encrypt or corporate CA)
- [ ] DNS records created pointing to Ingress
- [ ] Monitoring and logging infrastructure ready
- [ ] Backup and disaster recovery plan documented
- [ ] Security audit completed
- [ ] Load testing performed
- [ ] Incident response procedures defined

## Infrastructure Requirements

### Kubernetes Cluster

- **Version**: v1.19 or higher
- **Nodes**: Minimum 3 nodes for HA
- **Node Type**: At least 2 CPU, 4GB RAM per node
- **Storage**: PersistentVolume provisioner available

### Recommended Add-ons

```bash
# NGINX Ingress Controller
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm install nginx ingress-nginx/ingress-nginx -n ingress-nginx --create-namespace

# cert-manager for TLS
helm repo add jetstack https://charts.jetstack.io
helm install cert-manager jetstack/cert-manager -n cert-manager --create-namespace

# Prometheus for monitoring
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack -n monitoring --create-namespace

# ELK Stack or similar for logging
```

## Production Configuration

### 1. Update Secrets with Real Values

```bash
# Generate secure passwords
openssl rand -base64 32

# Create production secrets
kubectl create secret generic myrc-prod-secrets \
  --from-literal=db-password="$(openssl rand -base64 32)" \
  --from-literal=ldap-password="your-ldap-password" \
  --from-literal=oauth2-secret="$(openssl rand -base64 32)" \
  -n myrc
```

### 2. Update ConfigMap with Production Values

Edit `k8s/configmap.yaml`:

```yaml
data:
  SPRING_PROFILES_ACTIVE: "prod,oauth2"
  SPRING_JPA_HIBERNATE_DDL_AUTO: "validate"  # Prevent auto-creation
  LOGGING_LEVEL_ROOT: "WARN"
  CORS_ALLOWED_ORIGINS: "https://myrc.example.com"
```

### 3. Database Configuration

For production, use a managed database service (RDS, Cloud SQL, etc.):

```bash
# Skip the postgres deployment in production
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/backend.yaml
kubectl apply -f k8s/frontend.yaml
kubectl apply -f k8s/ingress.yaml

# Update backend.yaml to use external database
# Replace: jdbc:postgresql://postgres:5432/myrc
# With:    jdbc:postgresql://prod-db.rds.amazonaws.com:5432/myrc
```

### 4. SSL/TLS Configuration

#### Using cert-manager with Let's Encrypt

```yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: admin@example.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
```

#### Using Corporate CA

```bash
# Create certificate secret
kubectl create secret tls myrc-tls \
  --cert=/path/to/cert.pem \
  --key=/path/to/key.pem \
  -n myrc
```

### 5. Network Policies

Restrict traffic between components:

```yaml
---
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-backend-to-db
  namespace: myrc
spec:
  podSelector:
    matchLabels:
      component: database
  ingress:
  - from:
    - podSelector:
        matchLabels:
          component: backend
    ports:
    - protocol: TCP
      port: 5432

---
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-frontend-to-backend
  namespace: myrc
spec:
  podSelector:
    matchLabels:
      component: backend
  ingress:
  - from:
    - podSelector:
        matchLabels:
          component: frontend
    ports:
    - protocol: TCP
      port: 8080
```

## Deployment Steps

### 1. Create Namespace and Secrets

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/configmap.yaml
```

### 2. Deploy Database

```bash
# Skip for managed database services
kubectl apply -f k8s/postgres.yaml
kubectl wait --for=condition=ready pod -l component=database -n myrc --timeout=300s
```

### 3. Deploy Backend

```bash
kubectl apply -f k8s/backend.yaml
kubectl wait --for=condition=ready pod -l component=backend -n myrc --timeout=300s
```

### 4. Deploy Frontend

```bash
kubectl apply -f k8s/frontend.yaml
kubectl wait --for=condition=ready pod -l component=frontend -n myrc --timeout=300s
```

### 5. Configure Ingress

```bash
# Update ingress.yaml with your domain
# Replace myrc.example.com with your domain

kubectl apply -f k8s/ingress.yaml

# Verify ingress
kubectl get ingress -n myrc
```

### 6. Verify Deployment

```bash
# Check all pods running
kubectl get pods -n myrc

# Check services
kubectl get svc -n myrc

# Check ingress
kubectl get ingress -n myrc

# Get ingress IP/hostname
kubectl get ingress myrc-ingress -n myrc -o jsonpath='{.status.loadBalancer.ingress[0]}'
```

## Monitoring and Alerting

### Prometheus Configuration

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: myrc-api
  namespace: myrc
spec:
  selector:
    matchLabels:
      app: myrc
      component: backend
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
```

### Alert Rules

```yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: myrc-alerts
  namespace: myrc
spec:
  groups:
  - name: myrc
    rules:
    - alert: HighErrorRate
      expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
      for: 5m
      labels:
        severity: critical
      annotations:
        summary: "High error rate detected"
        description: "{{ $value }}% of requests returning 5xx errors"
    
    - alert: PodNotReady
      expr: min_over_time(kube_pod_status_ready{namespace="myrc"}[5m]) == 0
      for: 5m
      labels:
        severity: warning
      annotations:
        summary: "Pod not ready: {{ $labels.pod }}"
```

## Logging and Tracing

### ELK Stack Integration

```bash
# Deploy ELK Stack
helm install elasticsearch elastic/elasticsearch -n logging --create-namespace
helm install kibana elastic/kibana -n logging
helm install filebeat elastic/filebeat -n logging
```

### Application Logging

```yaml
# Update logging configuration
LOGGING_LEVEL_ROOT: "INFO"
LOGGING_LEVEL_COM_BOXOFFICE: "DEBUG"
LOGGING_PATTERN_CONSOLE: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

## Backup and Recovery

### Database Backups

```bash
# Automated backup with CronJob
apiVersion: batch/v1
kind: CronJob
metadata:
  name: postgres-backup
  namespace: myrc
spec:
  schedule: "0 2 * * *"  # Daily at 2 AM
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: postgres:16-alpine
            command:
            - /bin/bash
            - -c
            - |
              pg_dump -h postgres -U myrc myrc | \
                gzip > /backup/myrc-$(date +%Y%m%d-%H%M%S).sql.gz
            volumeMounts:
            - name: backup-storage
              mountPath: /backup
          volumes:
          - name: backup-storage
            persistentVolumeClaim:
              claimName: backup-pvc
          restartPolicy: OnFailure
```

### Recovery Procedure

```bash
# Restore from backup
kubectl exec -it deployment/postgres -n myrc -- bash
zcat /backup/myrc-20260117-020000.sql.gz | \
  psql -h postgres -U myrc myrc
```

## Performance Tuning

### Database Connection Pooling

```yaml
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE: "20"
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE: "5"
```

### Caching Configuration

```yaml
SPRING_CACHE_TYPE: "redis"
SPRING_REDIS_HOST: "redis.example.com"
SPRING_REDIS_PORT: "6379"
```

### Request Optimization

```yaml
SPRING_MVC_ASYNC_REQUEST_TIMEOUT: "30000"
SPRING_MVC_SERVLET_LOAD_ON_STARTUP: "0"
```

## Security Hardening

### Pod Security Policy

```yaml
apiVersion: policy/v1beta1
kind: PodSecurityPolicy
metadata:
  name: myrc-psp
spec:
  privileged: false
  allowPrivilegeEscalation: false
  requiredDropCapabilities:
    - ALL
  volumes:
    - 'configMap'
    - 'emptyDir'
    - 'projected'
    - 'secret'
    - 'downwardAPI'
    - 'persistentVolumeClaim'
  runAsUser:
    rule: 'MustRunAsNonRoot'
  fsGroup:
    rule: 'RunAsAny'
  readOnlyRootFilesystem: false
```

### RBAC Configuration

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: myrc
  namespace: myrc
rules:
- apiGroups: [""]
  resources: ["secrets", "configmaps"]
  verbs: ["get"]
```

## Scaling Configuration

### Horizontal Pod Autoscaling

```yaml
# Already configured in backend.yaml and frontend.yaml
# Adjust minReplicas and maxReplicas based on load
minReplicas: 3        # Increase for high availability
maxReplicas: 20       # Adjust based on cluster capacity
```

### Load Testing

```bash
# Using Apache Bench
ab -n 10000 -c 100 https://myrc.example.com/

# Using wrk
wrk -t12 -c400 -d30s https://myrc.example.com/
```

## Maintenance and Updates

### Rolling Updates

```bash
# Automatic rolling updates with deployment strategy
kubectl set image deployment/api \
  api=myrc-api:v1.1 \
  -n myrc

# Monitor rollout
kubectl rollout status deployment/api -n myrc
```

### Database Migrations

```bash
# Use migration tools (Liquibase, Flyway)
# Set SPRING_JPA_HIBERNATE_DDL_AUTO: "validate" in production
```

## Disaster Recovery

### Backup Strategy

- Daily database backups (automated CronJob)
- Weekly full cluster snapshots
- Off-site backup storage (S3, GCS, etc.)
- Tested recovery procedures

### Recovery Plan

1. Assess damage and scope
2. Restore database from latest backup
3. Redeploy application from container registry
4. Verify data integrity
5. Notify stakeholders
6. Conduct post-incident review

## Support and Documentation

See the following guides:
- [KUBERNETES.md](KUBERNETES.md) - Basic Kubernetes deployment
- [LDAP.md](LDAP.md) - LDAP integration
- [OAUTH2.md](OAUTH2.md) - OAuth2 integration
- [SECURITY.md](SECURITY.md) - Security best practices
