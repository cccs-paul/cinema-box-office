# Kubernetes Troubleshooting Guide

## Quick Diagnostic Commands

```bash
# Get overall cluster health
./k8s-health.sh

# Get deployment status
./k8s-deploy.sh status

# View logs for all components
./k8s-deploy.sh logs api
./k8s-deploy.sh logs web
./k8s-deploy.sh logs postgres

# Describe cluster resources
kubectl describe nodes -n cinema-box-office
kubectl describe pods -n cinema-box-office
kubectl describe services -n cinema-box-office
kubectl describe ingress -n cinema-box-office
```

## Pods Not Starting

### Symptoms
- Pod status is `Pending`, `CrashLoopBackOff`, `ImagePullBackOff`, or `Error`
- Deployment shows 0 ready replicas
- Application not accessible

### Diagnosis

```bash
# Get pod status
kubectl get pods -n cinema-box-office

# Describe pod for detailed status
kubectl describe pod <pod-name> -n cinema-box-office

# Check pod events
kubectl get events -n cinema-box-office --sort-by='.lastTimestamp'

# View pod logs
kubectl logs <pod-name> -n cinema-box-office
kubectl logs <pod-name> -n cinema-box-office --previous  # For crashed pods
```

### Common Causes and Solutions

#### ImagePullBackOff
**Cause**: Docker image not found in registry or registry credentials invalid

```bash
# Check image reference in manifest
kubectl get deployment api -n cinema-box-office -o jsonpath='{.spec.template.spec.containers[0].image}'

# Solution: Update image reference
kubectl set image deployment/api api=your-registry.com/cinema-box-office-api:v1.0 -n cinema-box-office
```

#### CrashLoopBackOff
**Cause**: Application crashes due to configuration or runtime error

```bash
# View detailed logs
kubectl logs <pod-name> -n cinema-box-office -f

# Common causes:
# 1. Database connection failed
# 2. Configuration missing
# 3. Port already in use
# 4. Memory/resource issues

# Check logs for:
java.sql.SQLException
NullPointerException
OutOfMemoryError
```

#### Pending Pod
**Cause**: Insufficient resources or PVC not available

```bash
# Check events for details
kubectl describe pod <pod-name> -n cinema-box-office

# Check resource availability
kubectl top nodes
kubectl describe nodes

# Check PVC status
kubectl get pvc -n cinema-box-office
kubectl describe pvc <pvc-name> -n cinema-box-office

# Solutions:
# 1. Increase cluster resources
# 2. Free up existing resources
# 3. Adjust resource requests/limits
```

#### Init Container Failed
**Cause**: Initialization step failed

```bash
# View init container logs
kubectl logs <pod-name> -n cinema-box-office -c init

# Check init container status in describe
kubectl describe pod <pod-name> -n cinema-box-office
```

## Database Connection Issues

### Symptoms
- Backend logs show: `SQLException`, `Connection refused`
- Frontend shows: `API is not available`
- Health check endpoint fails

### Diagnosis

```bash
# Check database pod status
kubectl get pods -n cinema-box-office | grep postgres

# Check database logs
./k8s-deploy.sh logs postgres

# Test database connectivity from API pod
./k8s-deploy.sh exec api bash -c "nc -zv postgres 5432"

# Port forward database
./k8s-deploy.sh port-forward postgres 5432 5432

# Connect locally
psql -h localhost -U boxoffice -d boxoffice
```

### Common Causes and Solutions

#### Database Pod Not Running
```bash
# Check pod status
kubectl get pods -n cinema-box-office | grep postgres

# If pending, check PVC
kubectl get pvc -n cinema-box-office
kubectl describe pvc postgres-data -n cinema-box-office

# If node has no space
kubectl top nodes
df -h

# Solution: Delete and recreate PVC
kubectl delete pvc postgres-data -n cinema-box-office
kubectl apply -f k8s/postgres.yaml
```

#### Connection Refused
```bash
# Check if postgres service exists
kubectl get svc postgres -n cinema-box-office

# Test endpoint
./k8s-deploy.sh exec api bash -c "curl postgres:5432 || echo 'Expected: connection refused'"

# Check environment variables in API pod
./k8s-deploy.sh exec api env | grep DATABASE

# Verify connection string
# Should be: jdbc:postgresql://postgres:5432/boxoffice
```

#### Authentication Failed
```bash
# Check database credentials in secrets
kubectl get secret cinema-box-office-secrets -n cinema-box-office -o yaml

# Verify base64 encoding
echo "your-base64-password" | base64 -d

# Update secrets if incorrect
kubectl delete secret cinema-box-office-secrets -n cinema-box-office
kubectl create secret generic cinema-box-office-secrets \
  --from-literal=db-password="new-password" \
  -n cinema-box-office
```

#### Database Locked/Unresponsive
```bash
# Check database status
./k8s-deploy.sh exec postgres psql -U boxoffice -d boxoffice -c "SELECT version();"

# Check active connections
./k8s-deploy.sh exec postgres psql -U boxoffice -d boxoffice -c "SELECT * FROM pg_stat_activity;"

# Kill long-running queries
./k8s-deploy.sh exec postgres psql -U boxoffice -d boxoffice -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='boxoffice' AND pid<>pg_backend_pid();"

# Restart database pod
kubectl delete pod -l component=database -n cinema-box-office
```

## Persistent Volume Issues

### Symptoms
- Pod stuck in `Pending` state
- Database data lost after pod restart
- `PersistentVolumeClaim` stuck in `Pending` state

### Diagnosis

```bash
# Check PVC status
kubectl get pvc -n cinema-box-office

# Describe PVC
kubectl describe pvc postgres-data -n cinema-box-office

# Check PV status
kubectl get pv

# Check storage classes
kubectl get storageclass
```

### Common Causes and Solutions

#### PVC Not Provisioned
```bash
# Cause: No StorageClass available or provisioner not running
kubectl get storageclass

# Solution: Create storage class if missing
kubectl apply -f - <<EOF
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: standard
provisioner: kubernetes.io/host-path
EOF
```

#### PV Not Released
```bash
# Check PV status
kubectl get pv

# If stuck in `Released` state
kubectl patch pv <pv-name> -p '{"spec":{"claimRef": null}}'

# Delete and recreate
kubectl delete pvc postgres-data -n cinema-box-office
kubectl delete pv <pv-name>
kubectl apply -f k8s/postgres.yaml
```

#### Disk Space Issue
```bash
# Check node disk usage
df -h

# Clean up unused volumes
docker volume prune

# Delete old pods/deployments
kubectl delete pod <old-pod> -n cinema-box-office
```

## API Connectivity Issues

### Symptoms
- Frontend shows: `Failed to connect to API`
- API health check fails
- CORS errors in browser console

### Diagnosis

```bash
# Check API pod status
kubectl get pods -n cinema-box-office | grep api

# Check API logs
./k8s-deploy.sh logs api

# Port forward API
./k8s-deploy.sh port-forward api 8080 8080

# Test API endpoint
curl -v http://localhost:8080/api/health

# Check API service
kubectl get svc api -n cinema-box-office
kubectl describe svc api -n cinema-box-office
```

### Common Causes and Solutions

#### API Pod Not Running
```bash
# Check pod status
kubectl describe pod <api-pod> -n cinema-box-office

# Check logs
kubectl logs <api-pod> -n cinema-box-office

# Common issues:
# - Memory issue: `OutOfMemoryError`
# - Database not accessible: `SQLException`
# - Port already in use: `Port already in use`

# Solution: Increase resource limits or fix configuration
kubectl set resources deployment/api \
  --limits=cpu=1000m,memory=1Gi \
  --requests=cpu=250m,memory=512Mi \
  -n cinema-box-office
```

#### Service Endpoint Empty
```bash
# Check endpoints
kubectl get endpoints api -n cinema-box-office

# If empty, pods are not ready
# Check pod readiness probe
kubectl describe pod <api-pod> -n cinema-box-office | grep -A 5 "Readiness"

# Test readiness endpoint manually
./k8s-deploy.sh port-forward api 8080 8080
curl -v http://localhost:8080/api/health
```

#### Frontend Can't Reach API
```bash
# Check proxy configuration
./k8s-deploy.sh exec web cat /etc/nginx/conf.d/default.conf

# Port forward frontend
./k8s-deploy.sh port-forward web 4200 80

# Test API through proxy
curl -v http://localhost:4200/api/health

# Check browser console for errors
# CORS errors: Check CORS configuration
# 502 Bad Gateway: Check backend service
```

## Ingress and External Access Issues

### Symptoms
- Domain doesn't resolve
- Certificate warnings
- 502 Bad Gateway error
- Cannot access application externally

### Diagnosis

```bash
# Check Ingress status
kubectl get ingress -n cinema-box-office

# Get Ingress IP/hostname
kubectl get ingress cinema-box-office-ingress -n cinema-box-office -o jsonpath='{.status.loadBalancer.ingress[0]}'

# Check Ingress configuration
kubectl describe ingress cinema-box-office-ingress -n cinema-box-office

# Check NGINX controller logs
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx

# Test DNS resolution
nslookup cinema-box-office.example.com
```

### Common Causes and Solutions

#### Ingress Not Getting IP/Hostname
```bash
# Cause: NGINX Ingress controller not installed
# Solution: Install NGINX Ingress
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm install nginx ingress-nginx/ingress-nginx -n ingress-nginx --create-namespace
```

#### Certificate Issues
```bash
# Check certificate secret
kubectl get secret cinema-box-office-tls -n cinema-box-office

# Check certificate validity
echo | openssl s_client -servername cinema-box-office.example.com -connect cinema-box-office.example.com:443 2>/dev/null | openssl x509 -noout -dates

# Recreate certificate if expired
kubectl delete secret cinema-box-office-tls -n cinema-box-office
kubectl create secret tls cinema-box-office-tls \
  --cert=/path/to/cert.pem \
  --key=/path/to/key.pem \
  -n cinema-box-office
```

#### 502 Bad Gateway
```bash
# Check backend services are running
kubectl get svc -n cinema-box-office

# Check endpoints
kubectl get endpoints -n cinema-box-office

# Check backend pod logs
./k8s-deploy.sh logs api

# Test backend directly
./k8s-deploy.sh port-forward api 8080 8080
curl http://localhost:8080/api/health
```

## Performance Issues

### Symptoms
- High response times
- CPU/memory usage spiking
- Pods being evicted
- Timeouts

### Diagnosis

```bash
# Check resource usage
kubectl top pods -n cinema-box-office
kubectl top nodes

# Check HPA status
kubectl get hpa -n cinema-box-office

# Check metrics availability
kubectl get deployment metrics-server -n kube-system

# Check node conditions
kubectl describe nodes | grep -A 5 "Conditions:"
```

### Common Causes and Solutions

#### High CPU Usage
```bash
# Identify pod using high CPU
kubectl top pods -n cinema-box-office --sort-by=cpu

# Check pod logs for errors
kubectl logs <pod-name> -n cinema-box-office

# Profile Java application
./k8s-deploy.sh exec api jstack <pid> > stack.txt

# Solutions:
# 1. Increase CPU limits
# 2. Optimize code
# 3. Increase replicas via HPA
```

#### High Memory Usage
```bash
# Identify pod using high memory
kubectl top pods -n cinema-box-office --sort-by=memory

# Check for memory leaks
./k8s-deploy.sh exec api jmap -heap <pid>

# Solutions:
# 1. Increase memory limits
# 2. Fix memory leaks
# 3. Optimize queries
```

#### Pod Eviction
```bash
# Check eviction reason
kubectl describe pod <pod-name> -n cinema-box-office

# Check node pressure
kubectl describe nodes | grep -A 5 "Allocatable\|Allocated resources"

# Solutions:
# 1. Add more nodes to cluster
# 2. Reduce pod resource requests
# 3. Add disk space
```

## Authentication Issues

### Symptoms
- Login fails
- 401 Unauthorized errors
- LDAP connection errors
- OAuth2 redirect issues

### Diagnosis

```bash
# Check authentication logs
./k8s-deploy.sh logs api | grep -i "auth\|ldap\|oauth"

# Check LDAP configuration
kubectl get configmap cinema-box-office-config -n cinema-box-office -o yaml | grep LDAP

# Check OAuth2 configuration
kubectl get configmap cinema-box-office-config -n cinema-box-office -o yaml | grep OAUTH

# Test LDAP connectivity
./k8s-deploy.sh exec api bash -c "ldapsearch -H ldap://ldap-server:389 -D 'cn=admin,dc=example,dc=com' -w password"
```

### Common Causes and Solutions

#### LDAP Connection Failed
```bash
# Cause: LDAP server not accessible
# Check LDAP configuration
kubectl get configmap cinema-box-office-config -n cinema-box-office -o yaml

# Test connectivity
./k8s-deploy.sh exec api bash -c "nc -zv ldap-server 389"

# Check DNS resolution
./k8s-deploy.sh exec api bash -c "nslookup ldap-server"

# Solution: Update LDAP configuration with correct server and port
kubectl edit configmap cinema-box-office-config -n cinema-box-office
```

#### OAuth2 Invalid Client
```bash
# Cause: Client ID/Secret incorrect
# Check OAuth2 configuration
kubectl get secret cinema-box-office-secrets -n cinema-box-office -o yaml

# Decode base64 values
echo "base64-value" | base64 -d

# Update secrets with correct credentials
kubectl delete secret cinema-box-office-secrets -n cinema-box-office
kubectl create secret generic cinema-box-office-secrets \
  --from-literal=oauth2-client-id="correct-id" \
  --from-literal=oauth2-client-secret="correct-secret" \
  -n cinema-box-office
```

## Resource Quota Issues

### Symptoms
- Pods stuck in `Pending` state
- Quota exceeded errors
- Cannot create new resources

### Diagnosis

```bash
# Check resource quotas
kubectl get resourcequota -n cinema-box-office
kubectl describe resourcequota <quota-name> -n cinema-box-office

# Check current resource usage
kubectl describe resourcequota <quota-name> -n cinema-box-office

# Check pod requests
kubectl get pods -n cinema-box-office -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{.spec.containers[0].resources}{"\n"}{end}'
```

### Common Causes and Solutions

#### Quota Exceeded
```bash
# Solution 1: Increase quota
kubectl patch resourcequota <quota-name> -p '{"spec":{"hard":{"cpu":"50","memory":"100Gi"}}}' -n cinema-box-office

# Solution 2: Reduce pod resource requests
kubectl set resources deployment/api --requests=cpu=200m,memory=256Mi -n cinema-box-office

# Solution 3: Delete unused resources
kubectl delete pod <unused-pod> -n cinema-box-office
```

## Network Policy Issues

### Symptoms
- Pods can't communicate
- Connection timeouts
- DNS resolution fails

### Diagnosis

```bash
# Check network policies
kubectl get networkpolicy -n cinema-box-office

# Check connectivity between pods
kubectl exec -it <pod-name> -n cinema-box-office -- bash
apt-get update && apt-get install -y curl
curl http://api:8080/api/health

# Check DNS
nslookup postgres
```

### Common Causes and Solutions

#### Pods Can't Communicate
```bash
# Cause: Too restrictive network policy
# Check network policies
kubectl get networkpolicy -n cinema-box-office

# Temporarily disable network policies to test
kubectl delete networkpolicy -l app=cinema-box-office -n cinema-box-office

# If communication works, review and update policies
# to allow required traffic

# Example: Allow frontend to backend traffic
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-web-to-api
  namespace: cinema-box-office
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
EOF
```

## Backup and Restore Issues

### Symptoms
- Backup fails
- Restore operation hangs
- Data inconsistency after restore

### Diagnosis

```bash
# Check backup job logs
./k8s-deploy.sh exec postgres pg_dump --version

# Verify backup file
ls -lh boxoffice-backup-*.sql.gz

# Test backup file integrity
gunzip -t boxoffice-backup-*.sql.gz

# Check database consistency
./k8s-deploy.sh exec postgres psql -U boxoffice -d boxoffice -c "SELECT pg_database.datname, COUNT(*) FROM pg_database JOIN pg_class ON pg_database.oid = pg_class.relnamespace GROUP BY datname;"
```

### Common Causes and Solutions

#### Backup Takes Too Long
```bash
# Cause: Large database or slow storage
# Reduce backup size
./k8s-deploy.sh exec postgres psql -U boxoffice -d boxoffice -c "SELECT datname, pg_size_pretty(pg_database_size(datname)) FROM pg_database WHERE datname='boxoffice';"

# Solution: Increase pod resources
kubectl set resources pod <backup-pod> --limits=cpu=2000m,memory=2Gi -n cinema-box-office
```

#### Restore Fails
```bash
# Cause: Incompatible backup format or corrupted file
# Verify backup integrity
gunzip -t boxoffice-backup-*.sql.gz

# Check PostgreSQL version compatibility
./k8s-deploy.sh exec postgres psql -U boxoffice -d boxoffice -c "SELECT version();"

# Solution: Create new backup and try restore
./k8s-deploy.sh backup
# Test with small amount of data first
```

## Debugging with Shell Access

### Execute Commands in Pod

```bash
# Interactive shell
./k8s-deploy.sh exec api bash

# Run single command
./k8s-deploy.sh exec api ps aux

# Run Java diagnostics
./k8s-deploy.sh exec api jps -l        # List Java processes
./k8s-deploy.sh exec api jstat -gc <pid> 1000  # GC statistics
./k8s-deploy.sh exec api jmap -heap <pid>     # Memory map
```

### Common Debug Commands

```bash
# Check environment variables
./k8s-deploy.sh exec api env | grep SPRING

# Test network connectivity
./k8s-deploy.sh exec api bash -c "ping -c 3 postgres"
./k8s-deploy.sh exec api bash -c "curl postgres:5432"

# Check file permissions
./k8s-deploy.sh exec api ls -la /app/

# View application logs
./k8s-deploy.sh exec api tail -f logs/application.log
```

## Emergency Procedures

### Cluster Restart

```bash
# Gracefully drain a node
kubectl drain <node-name> --ignore-daemonsets

# Scale down deployments before restart
kubectl scale deployment --all --replicas=1 -n cinema-box-office

# Restart cluster

# Scale back up
kubectl scale deployment api --replicas=3 -n cinema-box-office
kubectl scale deployment web --replicas=2 -n cinema-box-office
```

### Rollback Failed Deployment

```bash
# View rollout history
kubectl rollout history deployment/api -n cinema-box-office

# Rollback to previous version
./k8s-deploy.sh rollback api

# Rollback to specific revision
kubectl rollout undo deployment/api --to-revision=2 -n cinema-box-office
```

### Force Delete Stuck Pod

```bash
# Graceful delete
kubectl delete pod <pod-name> -n cinema-box-office --grace-period=30

# Force delete (last resort)
kubectl delete pod <pod-name> -n cinema-box-office --grace-period=0 --force
```

## Support and Escalation

If issues persist after following this guide:

1. **Collect diagnostic information**
   ```bash
   ./k8s-health.sh > health-report.txt
   kubectl describe pod -n cinema-box-office > pod-description.txt
   kubectl logs -l component=api -n cinema-box-office > api-logs.txt
   ```

2. **Check logs and events**
   ```bash
   kubectl get events -n cinema-box-office --sort-by='.lastTimestamp'
   ```

3. **Contact support with**
   - Environment details (K8s version, node count, resources)
   - Error messages and logs
   - Steps to reproduce
   - Diagnostic files collected above

## Additional Resources

- [Kubernetes Troubleshooting](https://kubernetes.io/docs/tasks/debug-application-cluster/)
- [Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [NGINX Ingress Troubleshooting](https://kubernetes.github.io/ingress-nginx/troubleshooting/)
