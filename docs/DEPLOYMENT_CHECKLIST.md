# Kubernetes Deployment Checklist

Use this checklist to ensure proper deployment and operation of myRC on Kubernetes.

## Pre-Deployment Planning

### Infrastructure Assessment
- [ ] Kubernetes cluster version identified (v1.19+)
- [ ] Cluster capacity assessed (CPU, memory, storage)
- [ ] Number of nodes determined (minimum 3 for HA)
- [ ] Storage provisioner configured (local, NFS, cloud provider)
- [ ] Network policies supported and enabled
- [ ] Ingress controller installed (NGINX, HAProxy, etc.)
- [ ] Load balancer configured (if using cloud provider)
- [ ] DNS configured for your domain

### Access and Permissions
- [ ] kubectl installed and configured
- [ ] kubeconfig accessible and tested
- [ ] Cluster admin access available
- [ ] RBAC roles defined for operations team
- [ ] ServiceAccount permissions planned
- [ ] Secret management strategy decided

### Container Registry
- [ ] Docker registry access configured
- [ ] Private registry credentials created
- [ ] Docker images built and tested locally
- [ ] Images pushed to registry
- [ ] Image pull secrets created in cluster

### SSL/TLS Certificates
- [ ] SSL certificates obtained (Let's Encrypt or CA)
- [ ] Certificate files secured
- [ ] Certificate expiration dates tracked
- [ ] Certificate renewal process documented
- [ ] Self-signed certificates for testing (if needed)

### Domain and DNS
- [ ] Production domain name selected
- [ ] DNS records created/updated
- [ ] DNS propagation verified
- [ ] Test domain available (staging environment)

## Configuration Preparation

### ConfigMap Setup
- [ ] ConfigMap file reviewed and updated
- [ ] Spring profiles configured correctly
- [ ] CORS allowed origins set to production domain
- [ ] Database connection string verified
- [ ] LDAP/OAuth2 endpoints configured
- [ ] Logging levels appropriate for environment

### Secrets Configuration
- [ ] Secrets storage strategy chosen
- [ ] Database password generated (min 20 characters)
- [ ] OAuth2 client secret generated
- [ ] LDAP bind password obtained
- [ ] JWT signing key generated
- [ ] All secrets stored securely (not in git)
- [ ] Secret rotation schedule established

### Image Configuration
- [ ] Image references updated in manifests
- [ ] Container registry specified correctly
- [ ] Image tags using semantic versioning
- [ ] Image pull secrets configured
- [ ] Image scan results reviewed

## Infrastructure Deployment

### Namespace Setup
- [ ] Namespace manifest reviewed
- [ ] Namespace labels appropriate
- [ ] Network policies planned for namespace
- [ ] ResourceQuotas defined (optional)
- [ ] Namespace deployed: `kubectl apply -f k8s/namespace.yaml`

### Storage Setup
- [ ] StorageClass reviewed
- [ ] PersistentVolumeClaim sizes appropriate
- [ ] Storage backup strategy verified
- [ ] Persistent volumes deployed

### Database Deployment
- [ ] PostgreSQL manifest reviewed
- [ ] Database credentials set in secrets
- [ ] Database name and user configured
- [ ] PVC size appropriate (10Gi default)
- [ ] Database deployed: `./k8s-deploy.sh install`
- [ ] Database health verified: `./k8s-health.sh`
- [ ] Database tested: `./k8s-deploy.sh exec postgres psql -U myrc`

### Network Configuration
- [ ] Network policies reviewed (if using)
- [ ] Service discovery tested
- [ ] Pod-to-pod communication verified
- [ ] Pod-to-external communication verified

## Application Deployment

### Backend API Deployment
- [ ] Backend manifest reviewed
- [ ] Container image specified and available
- [ ] Environment variables correct
- [ ] Resource requests/limits appropriate
- [ ] Health check endpoints configured
- [ ] Backend deployed: `./k8s-deploy.sh install`
- [ ] Backend pods running: `kubectl get pods -n myrc`
- [ ] Backend logs checked: `./k8s-deploy.sh logs api`
- [ ] Backend health verified: `curl http://localhost:8080/api/health` (port-forwarded)
- [ ] API connectivity to database verified

### Frontend Deployment
- [ ] Frontend manifest reviewed
- [ ] Container image specified and available
- [ ] NGINX configuration correct
- [ ] API proxy configuration verified
- [ ] Resource requests/limits appropriate
- [ ] Health check configured
- [ ] Frontend deployed
- [ ] Frontend pods running
- [ ] Frontend logs checked
- [ ] Frontend loaded in browser (port-forwarded)
- [ ] Frontend API connectivity verified

### Scaling Configuration
- [ ] Horizontal Pod Autoscaler configured
- [ ] Min/max replica counts appropriate
- [ ] CPU/memory thresholds reasonable
- [ ] HPA status verified: `kubectl get hpa -n myrc`

## Ingress and External Access

### Ingress Setup
- [ ] Ingress manifest reviewed
- [ ] Hostname updated to production domain
- [ ] TLS certificate secret created
- [ ] Path routing verified
- [ ] Ingress deployed: `kubectl apply -f k8s/ingress.yaml`
- [ ] Ingress status checked: `kubectl get ingress -n myrc`
- [ ] Ingress IP/hostname obtained
- [ ] DNS updated with Ingress IP/hostname

### TLS/SSL Configuration
- [ ] SSL certificate deployed
- [ ] Certificate renewal automated (cert-manager)
- [ ] TLS version set to 1.2 minimum
- [ ] HSTS header configured
- [ ] SSL labs test performed (if public)

### External Access Testing
- [ ] Domain resolves to Ingress IP
- [ ] HTTP redirects to HTTPS
- [ ] HTTPS connection successful
- [ ] Certificate displays correctly in browser
- [ ] Application loads in browser
- [ ] API responses received through frontend

## Authentication and Authorization

### RBAC Configuration
- [ ] ServiceAccount created
- [ ] Role with minimal permissions defined
- [ ] RoleBinding created
- [ ] RBAC policies tested
- [ ] Audit logging configured

### LDAP Integration (if using)
- [ ] LDAP server connectivity verified
- [ ] Bind credentials tested
- [ ] User search filter correct
- [ ] Group mapping configured
- [ ] LDAP integration tested with sample user
- [ ] See [LDAP.md](../docs/LDAP.md) for detailed steps

### OAuth2 Integration (if using)
- [ ] OAuth2 provider selected
- [ ] Client credentials obtained
- [ ] Redirect URIs configured
- [ ] Token endpoint verified
- [ ] User info endpoint verified
- [ ] OAuth2 login tested end-to-end
- [ ] See [OAUTH2.md](../docs/OAUTH2.md) for detailed steps

## Monitoring and Logging

### Metrics Collection
- [ ] Metrics server installed (for HPA)
- [ ] Prometheus installed (optional but recommended)
- [ ] Application metrics exposed
- [ ] ServiceMonitor configured (if using Prometheus)
- [ ] Metrics accessible in monitoring dashboard

### Logging Setup
- [ ] Logging aggregation configured
- [ ] Application logs captured
- [ ] Audit logs enabled
- [ ] Log retention policy set
- [ ] Log rotation configured
- [ ] ELK stack or Loki deployed (optional)

### Alerting Configuration
- [ ] Alert rules defined
- [ ] High error rate alert configured
- [ ] Pod not ready alert configured
- [ ] Certificate expiration alert configured
- [ ] Notification channels configured (email, Slack, etc.)
- [ ] Alert testing performed

### Health Checks
- [ ] Liveness probes configured
- [ ] Readiness probes configured
- [ ] Startup probes configured (if using Java 17+)
- [ ] Health check endpoints respond correctly
- [ ] Health check frequency appropriate

## Backup and Disaster Recovery

### Backup Strategy
- [ ] Backup schedule defined
- [ ] Database backup automated (daily minimum)
- [ ] Backup retention policy set (30 days minimum)
- [ ] Backup storage location secured
- [ ] Backup encryption enabled
- [ ] Backup script tested: `./k8s-deploy.sh backup`

### Disaster Recovery Plan
- [ ] Backup restoration procedure documented
- [ ] Restoration tested and verified
- [ ] RTO (Recovery Time Objective) defined
- [ ] RPO (Recovery Point Objective) defined
- [ ] Failover procedures documented
- [ ] Team trained on disaster recovery process

### High Availability
- [ ] Multiple replicas configured (minimum 2)
- [ ] Pod anti-affinity rules configured
- [ ] Node failure resilience tested
- [ ] Database replication configured (optional)
- [ ] Backup systems in place

## Security Validation

### Container Security
- [ ] Container images scanned for vulnerabilities
- [ ] Base images updated to latest patch
- [ ] Unnecessary packages removed from images
- [ ] Non-root user used in containers
- [ ] Read-only root filesystem tested (where possible)
- [ ] Security context applied

### Network Security
- [ ] Network policies restrict traffic
- [ ] Egress rules limit outbound connections
- [ ] Ingress only from expected sources
- [ ] Internal service communication uses ClusterIP
- [ ] Service mesh considered for advanced security

### Data Security
- [ ] Database passwords meet complexity requirements
- [ ] Secrets encrypted at rest
- [ ] Secrets encrypted in transit (TLS)
- [ ] Data encryption at rest configured (if available)
- [ ] Backup encryption verified
- [ ] No sensitive data in logs

### Access Security
- [ ] RBAC roles use least privilege
- [ ] ServiceAccount tokens properly scoped
- [ ] Audit logging enabled
- [ ] User access regularly reviewed
- [ ] Service accounts periodically rotated
- [ ] API authentication required

## Performance Optimization

### Resource Allocation
- [ ] CPU requests/limits appropriate
- [ ] Memory requests/limits appropriate
- [ ] Pod eviction policies configured
- [ ] QoS classes understood and applied
- [ ] Cluster autoscaling configured (if using cloud provider)

### Database Optimization
- [ ] Connection pooling configured (HikariCP)
- [ ] Pool size appropriate for load
- [ ] Query performance reviewed
- [ ] Indexes created on frequently queried columns
- [ ] Database statistics updated

### Caching
- [ ] Application caching configured
- [ ] Redis cache configured (optional)
- [ ] Cache invalidation strategy defined
- [ ] Cache hit rates monitored

### Load Testing
- [ ] Load test scenario defined
- [ ] Load test tools installed (ab, wrk, JMeter)
- [ ] Load test performed against staging
- [ ] Results analyzed and bottlenecks identified
- [ ] Scaling behavior verified

## Production Readiness

### Documentation
- [ ] Deployment procedure documented
- [ ] Operational runbooks created
- [ ] Troubleshooting guide available
- [ ] Architecture documentation updated
- [ ] Configuration options documented

### Team Preparation
- [ ] Operations team trained
- [ ] Escalation procedures defined
- [ ] On-call rotation established
- [ ] Incident response plan documented
- [ ] Communication channels established

### Testing Completion
- [ ] Functional testing completed
- [ ] Security testing completed
- [ ] Load testing completed
- [ ] Failover testing completed
- [ ] Backup restoration testing completed

### Final Verification
- [ ] All health checks passing
- [ ] All tests passing
- [ ] Security scan passed
- [ ] Performance acceptable
- [ ] No errors in logs
- [ ] DNS resolution working
- [ ] HTTPS connectivity confirmed
- [ ] API responding correctly
- [ ] Frontend rendering correctly
- [ ] Database operations successful

## Production Deployment Day

### Pre-Deployment
- [ ] Backup taken
- [ ] Team ready (developers, ops, on-call)
- [ ] Runbooks reviewed
- [ ] Rollback plan understood
- [ ] Communication channels open
- [ ] Deployment window confirmed

### Deployment Steps
1. [ ] Stop existing deployment (if applicable): `./k8s-deploy.sh uninstall`
2. [ ] Deploy new version: `./k8s-deploy.sh install`
3. [ ] Monitor health: `./k8s-health.sh --watch`
4. [ ] Verify functionality
5. [ ] Monitor logs for errors
6. [ ] Test critical workflows
7. [ ] Confirm database consistency

### Post-Deployment
- [ ] All pods running and healthy
- [ ] No errors in logs
- [ ] API responding correctly
- [ ] Frontend accessible and functional
- [ ] Database queries responding
- [ ] Monitoring dashboards operational
- [ ] Alerts not firing
- [ ] Performance metrics acceptable

### Communication
- [ ] Notify stakeholders of successful deployment
- [ ] Update status page
- [ ] Document deployment details
- [ ] Schedule retrospective if applicable

## Ongoing Operations

### Daily Monitoring
- [ ] Health check performed: `./k8s-health.sh`
- [ ] Pod status checked
- [ ] No error alerts
- [ ] Performance metrics reviewed
- [ ] Error logs reviewed

### Weekly Maintenance
- [ ] Backup tested
- [ ] Certificate expiration dates checked
- [ ] Security patches reviewed
- [ ] Dependency updates reviewed
- [ ] Unused resources cleaned up

### Monthly Review
- [ ] Resource utilization analyzed
- [ ] Scaling policies reviewed
- [ ] Cost optimization reviewed (if using cloud)
- [ ] Security audit performed
- [ ] Incident review performed (if applicable)

### Quarterly Tasks
- [ ] Disaster recovery drill performed
- [ ] Security assessment performed
- [ ] Load testing performed
- [ ] Documentation updated
- [ ] Team training updated

## Notes and Additional Information

```
Deployment Date:    _________________
Production Version: _________________
Deployed By:        _________________
Reviewed By:        _________________
Post-Deployment Checklist Completed: [ ] Yes [ ] No
Any Issues Encountered:
_________________________________________________________________
_________________________________________________________________

Additional Notes:
_________________________________________________________________
_________________________________________________________________
```

---

**Document Version:** 1.0  
**Last Updated:** January 2025  
**Next Review Date:** _________________
