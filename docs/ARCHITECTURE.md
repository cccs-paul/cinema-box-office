# Architecture and Design Documentation

## Overview

Cinema Box Office is a cloud-native microservices application designed for scalability, security, and maintainability. This document provides a comprehensive overview of the application architecture.

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Internet / Users                         │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ↓
┌─────────────────────────────────────────────────────────────┐
│                   NGINX Ingress Controller                  │
│          (TLS Termination, Path-based Routing)              │
└──────┬─────────────────────────────────────────────────┬────┘
       │                                                 │
       ↓                                                 ↓
┌─────────────────────┐                     ┌──────────────────────┐
│  Frontend Service   │                     │  Backend API Service │
│  (NGINX)            │                     │  (Spring Boot)       │
│  - Angular App      │                     │  - REST Endpoints    │
│  - Static Files     │                     │  - Business Logic    │
│  - API Proxy        │◄────────────────────┤  - Authentication    │
│  (4200)             │                     │  (8080)              │
└─────────────────────┘                     └──────┬───────────────┘
                                                   │
                                                   ↓
                                    ┌──────────────────────────┐
                                    │  PostgreSQL Database     │
                                    │  - User Data             │
                                    │  - Application Data      │
                                    │  - Audit Logs            │
                                    │  (5432)                  │
                                    └──────────────────────────┘
```

## Component Architecture

### 1. Frontend Service

**Technology**: Angular 17 + NGINX

**Responsibilities**:
- Serve single-page application
- Static asset caching
- API proxy for local development
- SSL/TLS termination
- Request routing

**Deployment**:
- Kubernetes Deployment (2 replicas default)
- ClusterIP Service (internal)
- Horizontal Pod Autoscaler (2-5 replicas)

**Resource Allocation**:
- CPU Request: 100m
- CPU Limit: 500m
- Memory Request: 128Mi
- Memory Limit: 256Mi

### 2. Backend API Service

**Technology**: Java 25 + Spring Boot 3.4.1 + Spring Security

**Responsibilities**:
- REST API endpoints
- Business logic
- Database operations
- Authentication & Authorization
- Logging and monitoring

**Features**:
- Health check endpoints
- Metrics export (Actuator)
- Request logging
- Error handling
- CORS support

**Deployment**:
- Kubernetes Deployment (2 replicas default)
- ClusterIP Service (internal)
- Horizontal Pod Autoscaler (2-10 replicas)

**Resource Allocation**:
- CPU Request: 250m
- CPU Limit: 1000m
- Memory Request: 512Mi
- Memory Limit: 1Gi

### 3. Database Service

**Technology**: PostgreSQL 16

**Responsibilities**:
- Persistent data storage
- User authentication data
- Application data
- Audit logs

**Features**:
- Automatic backups
- Health checks
- Connection pooling (HikariCP)
- Replication support (optional)

**Deployment**:
- Kubernetes Deployment (1 pod for now)
- ClusterIP Service (internal)
- PersistentVolumeClaim (10Gi)

**Resource Allocation**:
- CPU Request: 250m
- CPU Limit: 500m
- Memory Request: 256Mi
- Memory Limit: 512Mi

## Data Flow

### User Login Flow

```
1. User enters credentials in Frontend
   └─→ POST /api/auth/login (via Ingress/Proxy)
       └─→ Backend API receives request
           └─→ AuthenticationController.login()
               ├─→ Validate input
               ├─→ Query database for user
               ├─→ Generate JWT token
               └─→ Return token + user info
       └─→ Frontend stores token in localStorage
       └─→ Frontend redirects to dashboard
```

### API Request with Authentication

```
1. Frontend makes authenticated request
   └─→ GET /api/data with Authorization header
       └─→ Ingress routes to Backend Service
           └─→ Backend receives request
               ├─→ Spring Security intercepts
               ├─→ Validates JWT token
               ├─→ Extracts user information
               ├─→ Checks authorization (RBAC)
               ├─→ Calls business logic
               ├─→ Queries database
               └─→ Returns response
       └─→ Ingress returns response to Frontend
       └─→ Frontend renders data
```

### LDAP Authentication Flow

```
1. User clicks "Login with LDAP"
   └─→ Frontend redirects to Backend login
       └─→ Backend requests LDAP credentials
           └─→ User enters LDAP username/password
           └─→ Backend connects to LDAP server
               ├─→ Binds with user credentials
               ├─→ Searches for user in directory
               ├─→ Retrieves user attributes
               ├─→ Checks group membership
               └─→ Returns authorization
       └─→ Backend generates JWT token
       └─→ User logged in
```

### OAuth2 Authentication Flow

```
1. User clicks "Login with Google/GitHub/etc"
   └─→ Frontend redirects to OAuth2 provider
       └─→ User logs in with provider
       └─→ Provider redirects to Backend callback
           └─→ Backend exchanges code for token
           └─→ Backend requests user info from provider
           └─→ Backend creates/updates local user
           └─→ Backend generates JWT token
           └─→ Redirects Frontend to dashboard
       └─→ Frontend stores token
       └─→ User logged in
```

## Deployment Architecture

### Development Environment (Docker Compose)

```
Host Machine
├─ Docker Engine
├─ Docker Compose
├─ Docker Network: cinema-box-office
├─ Containers
│  ├─ Frontend (ports 4200)
│  ├─ Backend API (ports 8080)
│  └─ PostgreSQL (ports 5432)
└─ Volumes
   └─ postgres-data
```

### Production Environment (Kubernetes)

```
Kubernetes Cluster
├─ cinema-box-office Namespace
├─ Ingress (TLS termination)
├─ Frontend Pod (2 replicas)
│  ├─ Labels: component=frontend
│  └─ Anti-affinity rules
├─ Backend API Pod (2 replicas)
│  ├─ Labels: component=backend
│  └─ Anti-affinity rules
├─ PostgreSQL Pod (1 replica)
│  ├─ Labels: component=database
│  └─ PersistentVolumeClaim
├─ Services
│  ├─ frontend-svc (ClusterIP)
│  ├─ backend-svc (ClusterIP)
│  └─ database-svc (ClusterIP)
├─ ConfigMaps
│  └─ cinema-box-office-config
├─ Secrets
│  └─ cinema-box-office-secrets
└─ Horizontal Pod Autoscalers
   ├─ frontend-hpa (2-5 replicas)
   └─ backend-hpa (2-10 replicas)
```

## Scaling Strategy

### Horizontal Scaling

**Frontend Scaling**:
- Min Replicas: 2
- Max Replicas: 5
- Metric: CPU 70%, Memory 80%
- Pod Anti-affinity: Preferred across nodes

**Backend Scaling**:
- Min Replicas: 2
- Max Replicas: 10
- Metric: CPU 70%, Memory 80%
- Pod Anti-affinity: Preferred across nodes

### Database Scaling

**Current**: Single PostgreSQL instance

**Future Enhancements**:
- Primary-replica replication
- Read-write splitting
- Connection pooling (PgBouncer)
- Caching layer (Redis)

### Load Balancing

**Kubernetes Service Load Balancing**:
- Round-robin traffic distribution
- Session affinity (optional)
- Service mesh integration (optional)

**Ingress Load Balancing**:
- NGINX Ingress Controller
- Multiple backends
- SSL/TLS termination
- Rate limiting (optional)

## Security Architecture

### Network Security

```
┌─────────────────────────────────────────┐
│  Ingress Controller (Public)             │
│  - TLS termination                       │
│  - CORS validation                       │
└────────────────────┬────────────────────┘
                     │ ClusterIP
     ┌───────────────┼───────────────┐
     │               │               │
     ↓               ↓               ↓
  Frontend        Backend         Database
  (Internal)      (Internal)      (Internal)
  
No direct external access to Backend or Database
All traffic through Ingress → Services
```

### Authentication & Authorization

**Architecture**:
1. **Authentication**: Verify user identity
   - Local database authentication
   - LDAP directory integration
   - OAuth2 provider integration

2. **Authorization**: Verify user permissions
   - Role-based access control (RBAC)
   - Spring Security @PreAuthorize
   - Resource-level permissions

3. **Token Management**:
   - JWT tokens with expiration
   - Token refresh mechanism
   - Token revocation on logout

### Data Security

**In Transit**:
- TLS 1.2+ for all external communication
- HTTPS for frontend
- Encrypted database connections

**At Rest**:
- Database encryption (optional)
- Backup encryption
- Secret encryption in Kubernetes

### Secret Management

**Kubernetes Secrets**:
- Database credentials
- OAuth2 client secrets
- API keys
- JWT signing keys

**Storage**:
- Encrypted at rest (etcd encryption)
- Accessible only to authorized pods
- Rotated regularly

## Monitoring and Observability

### Health Checks

**Liveness Probe**:
- Endpoint: `/actuator/health`
- Interval: 30 seconds
- Timeout: 10 seconds
- Failure threshold: 3

**Readiness Probe**:
- Endpoint: `/actuator/health/ready`
- Interval: 10 seconds
- Timeout: 5 seconds
- Failure threshold: 3

### Metrics Collection

**Exposed Metrics**:
- HTTP request metrics
- Database connection pool metrics
- JVM metrics (memory, GC, threads)
- Custom application metrics

**Metrics Endpoint**:
- `/actuator/prometheus`
- Prometheus format
- Scrape interval: 30 seconds

### Logging

**Log Levels**:
- DEBUG: Development environments
- INFO: Production environments
- WARN: Errors and warnings
- ERROR: Critical errors

**Aggregation**:
- Centralized logging (ELK stack, Loki, etc.)
- Log retention: 30 days minimum
- Audit log retention: 1 year

### Alerting

**Alert Conditions**:
- Pod not ready (>5 minutes)
- High error rate (>5% of requests)
- Database unavailable
- High resource usage (>90% CPU/Memory)
- Certificate expiring (<30 days)

## Disaster Recovery

### Backup Strategy

**Database Backups**:
- Frequency: Daily
- Retention: 30 days
- Location: Separate storage
- Encryption: Enabled
- Testing: Monthly restoration test

**Backup Types**:
- Full backups: Daily
- Incremental backups: Hourly (optional)

### Recovery Objectives

**RTO (Recovery Time Objective)**: 1 hour
**RPO (Recovery Point Objective)**: 1 hour

### Failover Mechanisms

**Pod Failure**:
- Automatic restart via Kubernetes
- Health checks ensure readiness
- No manual intervention required

**Node Failure**:
- Pod rescheduling to healthy nodes
- Pod anti-affinity prevents single points of failure

**Database Failure**:
- Restore from backup
- Verify data integrity
- Notify stakeholders

## Performance Considerations

### Frontend Performance

**Optimization**:
- Static asset caching
- GZIP compression
- Lazy loading
- Code splitting
- CDN integration (optional)

**Target Metrics**:
- First Contentful Paint: <2 seconds
- Time to Interactive: <5 seconds
- Lighthouse Score: >90

### Backend Performance

**Optimization**:
- Database query optimization
- Connection pooling (HikariCP)
- Caching strategies
- Async request processing
- Request deduplication

**Target Metrics**:
- API response time: <500ms (p95)
- Database query time: <100ms (p95)
- Throughput: >1000 requests/second

### Database Performance

**Optimization**:
- Index optimization
- Query plan analysis
- Connection pooling
- Slow query logging
- Statistics maintenance

**Target Metrics**:
- Query execution time: <100ms (p95)
- Connection pool utilization: 50-80%
- Cache hit ratio: >95% (if caching enabled)

## Extensibility

### Adding New Features

**Database Schema Changes**:
1. Create migration script
2. Test in development
3. Apply during maintenance window
4. Verify data integrity

**API Endpoints**:
1. Define endpoint contract
2. Implement controller method
3. Add business logic
4. Add unit tests
5. Deploy backend
6. Update frontend

**Frontend Components**:
1. Create Angular component
2. Add routing rules
3. Integrate with API
4. Add styling
5. Test in browser
6. Deploy frontend

### Integration Points

**Third-party Services**:
- LDAP/Active Directory
- OAuth2 providers
- External APIs
- Monitoring systems
- Logging systems
- Backup storage

## Future Enhancements

### Short-term (1-3 months)
- [ ] Add database replication
- [ ] Implement Redis caching
- [ ] Add API rate limiting
- [ ] Enhance error handling
- [ ] Add comprehensive audit logging

### Medium-term (3-6 months)
- [ ] Implement service mesh (Istio)
- [ ] Add multi-region support
- [ ] Implement database sharding
- [ ] Add real-time notifications
- [ ] Enhance security with OAuth2/OIDC

### Long-term (6-12 months)
- [ ] Migrate to serverless functions
- [ ] Implement event-driven architecture
- [ ] Add machine learning features
- [ ] Implement multi-tenant support
- [ ] Global content delivery

## Technology Stack Summary

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Frontend | Angular | 17 | UI Framework |
| Frontend Server | NGINX | 1.27 | Web Server |
| Backend | Spring Boot | 3.4.1 | Application Framework |
| Backend Language | Java | 25 | Programming Language |
| Database | PostgreSQL | 16 | Data Storage |
| Container | Docker | Latest | Containerization |
| Orchestration | Kubernetes | 1.19+ | Container Orchestration |
| Service Mesh | NGINX Ingress | Latest | Ingress Controller |
| Authentication | Spring Security | 6.x | Security Framework |
| Monitoring | Prometheus | Latest | Metrics Collection |
| Logging | ELK Stack | Latest | Log Aggregation |

## Compliance and Standards

### Security Standards
- OWASP Top 10 compliance
- NIST Cybersecurity Framework
- CIS Kubernetes Benchmarks
- PCI DSS (if handling payments)

### Code Quality
- SonarQube analysis
- Code coverage >80%
- Linting and formatting
- Dependency scanning

### Performance
- Lighthouse audits >90
- API response time <500ms
- Database performance optimization
- Load testing >1000 req/s

## Documentation Map

- [KUBERNETES.md](KUBERNETES.md) - Kubernetes deployment guide
- [PRODUCTION.md](PRODUCTION.md) - Production deployment procedures
- [LDAP.md](LDAP.md) - LDAP integration
- [OAUTH2.md](OAUTH2.md) - OAuth2 integration
- [SECURITY.md](SECURITY.md) - Security best practices
- [TESTING.md](TESTING.md) - Testing procedures
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Troubleshooting guide
- [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md) - Deployment verification

## Support and Contact

For architecture questions:
- Review relevant documentation
- Check code comments
- Review design decisions in ADRs
- Contact development team

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | Jan 2025 | Engineering Team | Initial architecture documentation |
