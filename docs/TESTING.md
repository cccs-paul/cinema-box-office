# Kubernetes Testing Guide

This guide provides comprehensive testing procedures for validating Cinema Box Office deployments on Kubernetes.

## Test Environment Setup

### Prerequisites

```bash
# Install testing tools
kubectl version --client
helm version
kustomize version

# Install load testing tools
curl https://raw.githubusercontent.com/ahmetb/kubectx/master/kubectx | bash
apt-get install -y apache2-utils wrk
```

### Create Test Namespace

```bash
# Create isolated test namespace
kubectl create namespace cinema-box-office-test

# Configure kubectl to use test namespace
kubectl config set-context --current --namespace=cinema-box-office-test

# Update k8s/ manifests namespace before applying
sed -i 's/namespace: cinema-box-office/namespace: cinema-box-office-test/g' k8s/*.yaml
```

## Unit Test Validation

### Backend API Tests

```bash
# Run backend unit tests in container
./k8s-deploy.sh exec api mvn test

# Specific test class
./k8s-deploy.sh exec api mvn test -Dtest=AuthenticationControllerTest

# Test coverage
./k8s-deploy.sh exec api mvn test jacoco:report
./k8s-deploy.sh port-forward api 8080 8080
open http://localhost:8080/target/site/jacoco/index.html
```

### Frontend Tests

```bash
# Run Angular tests in container
./k8s-deploy.sh exec web npm test

# Run e2e tests
./k8s-deploy.sh exec web npm run e2e

# Run linting
./k8s-deploy.sh exec web npm run lint
```

## Integration Tests

### Database Connectivity

```bash
# Test database connection from API pod
./k8s-deploy.sh exec api bash -c "psql -h postgres -U boxoffice -d boxoffice -c 'SELECT version();'"

# Check database is initialized
./k8s-deploy.sh exec postgres psql -U boxoffice -d boxoffice -c "\dt"

# Verify schemas and tables
./k8s-deploy.sh exec postgres psql -U boxoffice -d boxoffice -c "SELECT * FROM information_schema.tables;"
```

### API Health Check

```bash
# Port forward API
./k8s-deploy.sh port-forward api 8080 8080

# Test health endpoint
curl -v http://localhost:8080/api/health
curl -v http://localhost:8080/actuator/metrics

# Verify API response
curl -s http://localhost:8080/api/health | jq .
```

### Frontend Health Check

```bash
# Port forward frontend
./k8s-deploy.sh port-forward web 4200 80

# Test frontend access
curl -I http://localhost:4200/

# Test API proxy through frontend
curl -v http://localhost:4200/api/health
```

### Cross-Pod Communication

```bash
# Test API to database communication
./k8s-deploy.sh exec api bash -c "curl -s http://postgres:5432" || echo "Database endpoint not accessible via HTTP (expected)"

# Test web to API communication
./k8s-deploy.sh exec web bash -c "curl -s http://api:8080/api/health"
```

## Authentication Testing

### LDAP Authentication

```bash
# Port forward API
./k8s-deploy.sh port-forward api 8080 8080

# Test LDAP login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "testpassword"
  }'

# Expected response: JWT token
# {
#   "token": "eyJhbGc...",
#   "expiresIn": 3600,
#   "user": {...}
# }
```

### OAuth2 Authentication

```bash
# Get authorization code
OAUTH_URL="https://oauth-provider.com/oauth/authorize?client_id=YOUR_CLIENT_ID&redirect_uri=http://localhost:4200/auth/callback&response_type=code&scope=openid%20profile%20email"

echo "Visit: $OAUTH_URL"

# Exchange code for token (in your frontend application)
# POST /oauth/token with code, client_id, client_secret
```

### JWT Token Validation

```bash
# Decode JWT token (without verifying signature)
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

echo $TOKEN | cut -d. -f2 | base64 -d | jq .

# Verify token signature
curl -X POST http://localhost:8080/api/auth/verify \
  -H "Authorization: Bearer $TOKEN"
```

## Load Testing

### API Load Test

```bash
# Using Apache Bench
ab -n 10000 -c 100 http://localhost:8080/api/health

# Using wrk (more realistic)
wrk -t12 -c400 -d30s http://localhost:8080/api/health

# Using wrk with custom script
cat > load-test.lua << 'EOF'
request = function()
    return wrk.format(nil, "/api/health")
end

response = function(status, headers, body)
    if status ~= 200 then
        print("Status: " .. status)
    end
end
EOF

wrk -t12 -c400 -d30s -s load-test.lua http://localhost:8080
```

### Frontend Load Test

```bash
# Test frontend static files
ab -n 1000 -c 100 http://localhost:4200/

# Test with keep-alive
ab -k -n 1000 -c 100 http://localhost:4200/

# Test with concurrent connections
wrk -t8 -c200 -d10s http://localhost:4200/
```

### Database Load Test

```bash
# Create test data
./k8s-deploy.sh exec postgres bash -c '
psql -U boxoffice -d boxoffice << SQL
CREATE TABLE test_load (
    id SERIAL PRIMARY KEY,
    data TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

INSERT INTO test_load (data) SELECT md5(random()::text) FROM generate_series(1, 100000);
SQL
'

# Run query performance test
./k8s-deploy.sh exec postgres bash -c '
psql -U boxoffice -d boxoffice << SQL
EXPLAIN ANALYZE SELECT COUNT(*) FROM test_load;
EXPLAIN ANALYZE SELECT * FROM test_load WHERE id > 50000 LIMIT 100;
SQL
'
```

### Sustained Load Test

```bash
# Run load test for 1 hour
wrk -t12 -c400 -d3600s http://localhost:8080/api/health

# Monitor resource usage during test
watch -n 1 "kubectl top pods -n cinema-box-office"
```

## Failover and Recovery Testing

### Pod Failure

```bash
# Get a pod name
POD=$(kubectl get pods -n cinema-box-office -l component=api -o jsonpath='{.items[0].metadata.name}')

# Delete pod and observe recovery
kubectl delete pod $POD -n cinema-box-office

# Monitor pod recovery
kubectl get pods -n cinema-box-office -w

# Verify API still responsive
curl http://localhost:8080/api/health
```

### Node Failure Simulation

```bash
# Cordon a node (prevent new pods)
kubectl cordon <node-name>

# Observe pod eviction and rescheduling
kubectl get pods -n cinema-box-office -w

# Uncordon node
kubectl uncordon <node-name>
```

### Database Failure

```bash
# Backup current database
./k8s-deploy.sh backup

# Delete database pod
kubectl delete pod -l component=database -n cinema-box-office

# Observe recovery
kubectl get pods -n cinema-box-office -w

# Verify data persistence
./k8s-deploy.sh exec postgres psql -U boxoffice -d boxoffice -c "SELECT COUNT(*) FROM information_schema.tables;"
```

### Rolling Update

```bash
# Update API image
./k8s-deploy.sh update api cinema-box-office-api:v1.1

# Observe rolling update
kubectl get deployment/api -n cinema-box-office -w

# Monitor during update
./k8s-health.sh --watch

# Verify no downtime
for i in {1..60}; do curl -s http://localhost:8080/api/health && echo " - Request $i OK" || echo " - Request $i FAILED"; sleep 1; done
```

## Configuration Testing

### ConfigMap Changes

```bash
# Edit ConfigMap
kubectl edit configmap cinema-box-office-config -n cinema-box-office

# Changes take effect on pod restart
kubectl rollout restart deployment/api -n cinema-box-office

# Verify changes
./k8s-deploy.sh logs api | grep "Configuration"
```

### Secret Rotation

```bash
# Create new secret
kubectl create secret generic cinema-box-office-secrets-new \
  --from-literal=db-password="new-password" \
  -n cinema-box-office

# Update deployment to use new secret
kubectl set env deployment/api DB_PASSWORD="new-value" -n cinema-box-office

# Verify connection still works
./k8s-deploy.sh logs api | grep -i "error\|warning"
```

## Security Testing

### RBAC Testing

```bash
# Test ServiceAccount permissions
kubectl auth can-i get secrets --as=system:serviceaccount:cinema-box-office:cinema-box-office -n cinema-box-office

# Test resource access
kubectl auth can-i get pods --as=system:serviceaccount:cinema-box-office:cinema-box-office -n cinema-box-office

# Verify cannot escalate privileges
kubectl auth can-i create clusterrolebinding --as=system:serviceaccount:cinema-box-office:cinema-box-office
```

### Network Policy Testing

```bash
# Test pod-to-pod communication with netcat
kubectl run -it --rm debug --image=ubuntu --restart=Never -n cinema-box-office -- bash
apt-get update && apt-get install -y curl netcat-openbsd

# Test connectivity to API
curl http://api:8080/api/health

# Test connectivity to database
nc -zv postgres 5432
```

### SSL/TLS Testing

```bash
# Test certificate validity
echo | openssl s_client -servername cinema-box-office.example.com -connect cinema-box-office.example.com:443 2>/dev/null | openssl x509 -noout -dates

# Test SSL/TLS version
echo | openssl s_client -tls1_2 -connect cinema-box-office.example.com:443 2>/dev/null | grep "Protocol"

# Test certificate chain
openssl s_client -connect cinema-box-office.example.com:443 -showcerts
```

## Performance Testing

### CPU and Memory Usage

```bash
# Monitor resource usage during load test
kubectl top pods -n cinema-box-office --sort-by=memory
kubectl top nodes

# Extended monitoring
watch -n 5 "kubectl top pods -n cinema-box-office && echo '---' && kubectl top nodes"
```

### Horizontal Pod Autoscaler

```bash
# Generate load to trigger autoscaling
wrk -t12 -c400 -d300s http://localhost:8080/api/health &

# Monitor HPA status
watch -n 5 "kubectl get hpa -n cinema-box-office"

# Monitor pod count change
watch -n 5 "kubectl get pods -n cinema-box-office | grep api | wc -l"

# Verify scale-down after load stops
sleep 300
watch -n 5 "kubectl get hpa -n cinema-box-office"
```

### Database Performance

```bash
# Test query performance
./k8s-deploy.sh exec postgres bash -c '
psql -U boxoffice -d boxoffice << SQL
EXPLAIN ANALYZE SELECT * FROM your_table LIMIT 100;
SQL
'

# Monitor slow queries
kubectl logs deployment/postgres -n cinema-box-office | grep "duration:"
```

## Persistence Testing

### Volume Failure

```bash
# Create test file
./k8s-deploy.sh exec postgres bash -c "echo 'test-data' > /var/lib/postgresql/test-file.txt"

# Delete pod
kubectl delete pod -l component=database -n cinema-box-office

# Verify file persists
./k8s-deploy.sh exec postgres bash -c "cat /var/lib/postgresql/test-file.txt"
```

### Backup and Restore

```bash
# Create backup
./k8s-deploy.sh backup

# Insert test data
./k8s-deploy.sh exec postgres bash -c '
psql -U boxoffice -d boxoffice << SQL
INSERT INTO test_table (name) VALUES ('test-entry');
SQL
'

# Restore from backup (will lose test data)
./k8s-deploy.sh restore boxoffice-backup-*.sql.gz

# Verify test data is gone
./k8s-deploy.sh exec postgres bash -c "psql -U boxoffice -d boxoffice -c \"SELECT * FROM test_table WHERE name = 'test-entry';\""
```

## User Acceptance Testing

### Frontend Functionality

```bash
# Port forward frontend
./k8s-deploy.sh port-forward web 4200 80

# Manual browser testing
open http://localhost:4200

# Test scenarios:
# 1. Load home page
# 2. Login with credentials
# 3. Navigate to each page
# 4. Submit forms
# 5. Verify error handling
# 6. Check mobile responsiveness
```

### API Functionality

```bash
# Test all endpoints
curl -X GET http://localhost:8080/api/health
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{...}'
# ... test all API endpoints

# Verify error responses
curl -X GET http://localhost:8080/api/nonexistent
```

## Test Report Template

```
# Kubernetes Deployment Test Report

## Test Date: _______________
## Environment: _______________
## Tester: _______________

### Test Summary
- Total Tests: ___
- Passed: ___
- Failed: ___
- Blocked: ___
- Pass Rate: ___%

### Test Details

#### Unit Tests
- Backend API: [ ] PASS [ ] FAIL
  Notes: ______________________
- Frontend: [ ] PASS [ ] FAIL
  Notes: ______________________

#### Integration Tests
- Database Connectivity: [ ] PASS [ ] FAIL
  Response Time: ____ ms
- API Health Check: [ ] PASS [ ] FAIL
  Response Time: ____ ms
- Frontend Accessibility: [ ] PASS [ ] FAIL
  Load Time: ____ ms

#### Load Testing
- API Load (10K requests): [ ] PASS [ ] FAIL
  Avg Response Time: ____ ms
  Error Rate: ___%
- Frontend Load (1K requests): [ ] PASS [ ] FAIL
  Avg Response Time: ____ ms
  Error Rate: ___%

#### Failover Testing
- Pod Failure Recovery: [ ] PASS [ ] FAIL
  Recovery Time: ____ sec
- Database Failure Recovery: [ ] PASS [ ] FAIL
  Data Loss: [ ] Yes [ ] No
- Rolling Update: [ ] PASS [ ] FAIL
  Downtime: ____ sec

#### Security Testing
- RBAC: [ ] PASS [ ] FAIL
- SSL/TLS: [ ] PASS [ ] FAIL
- Authentication: [ ] PASS [ ] FAIL

### Issues Found
1. _________________________________
2. _________________________________
3. _________________________________

### Recommendations
1. _________________________________
2. _________________________________

### Approval
Approved by: _________________ Date: _________
```

## Continuous Testing

### GitLab CI/CD Pipeline

```yaml
# .gitlab-ci.yml
test-kubernetes:
  image: ubuntu:24.04
  before_script:
    - apt-get update && apt-get install -y kubectl kustomize
  script:
    - kubectl apply -k k8s/ --dry-run=client
    - kustomize build k8s/
  only:
    - merge_requests
```

### Automated Health Monitoring

```bash
# Continuous health check script
while true; do
    ./k8s-health.sh
    if [ $? -ne 0 ]; then
        # Send alert
        echo "Health check failed!" | mail -s "K8s Health Alert" ops@example.com
    fi
    sleep 300  # Check every 5 minutes
done
```

## Reference

- [Kubernetes Testing Best Practices](https://kubernetes.io/docs/tasks/run-application/run-replicated-stateful-application/)
- [Load Testing Tools](https://en.wikipedia.org/wiki/Web_server_benchmarking)
- [Security Testing](https://kubernetes.io/docs/concepts/security/)
