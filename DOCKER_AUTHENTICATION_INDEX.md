# Docker LDAP and OAuth2 Integration - Quick Navigation

## 📋 Start Here

**Choose your path:**

### 🚀 **I want to start developing with LDAP today**
→ Read [Quick Start Guide](DOCKER_AUTHENTICATION_SETUP.md#quick-start-guide)
→ Run: `docker-compose -f docker-compose.dev-full.yml up -d`
→ Full Guide: [docs/DOCKER_LDAP_OAUTH2.md](docs/DOCKER_LDAP_OAUTH2.md#part-1-ldap-integration-with-docker)

### 🔐 **I need to integrate with corporate LDAP**
→ Read [Corporate LDAP Setup](docs/DOCKER_LDAP_OAUTH2.md#12-corporate-ldapadaactive-directory-setup)
→ Guide: [docs/LDAP.md](docs/LDAP.md)
→ Security: [docs/SECURITY.md](docs/SECURITY.md)

### 🌐 **I want to set up OAuth2 authentication**
→ Read [OAuth2 Quick Start](DOCKER_AUTHENTICATION_SETUP.md#oauth2-development-environment)
→ Run: `docker-compose -f docker-compose.oauth2.yml up -d`
→ Guide: [docs/DOCKER_LDAP_OAUTH2.md](docs/DOCKER_LDAP_OAUTH2.md#part-2-oauth2-integration-with-docker)

### 🏭 **I need production deployment guidance**
→ Read: [docs/PRODUCTION.md](docs/PRODUCTION.md)
→ Follow: [docs/DEPLOYMENT_CHECKLIST.md](docs/DEPLOYMENT_CHECKLIST.md)
→ Security: [docs/SECURITY.md](docs/SECURITY.md)

### 🧪 **I want to test my setup**
→ Read: [docs/TESTING.md](docs/TESTING.md)
→ Troubleshoot: [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)

---

## 📚 Complete Documentation Index

### Core Configuration Files

| File | Purpose | Size | Status |
|------|---------|------|--------|
| [docker-compose.dev-full.yml](docker-compose.dev-full.yml) | Development stack with OpenLDAP | 5.0K | ✅ Ready |
| [docker-compose.oauth2.yml](docker-compose.oauth2.yml) | OAuth2 with Keycloak stack | 6.8K | ✅ Ready |
| [ldap-init.ldif](ldap-init.ldif) | LDAP users & groups initialization | 2.5K | ✅ Ready |

### Main Documentation

| File | Topics | Lines | Read Time |
|------|--------|-------|-----------|
| [**DOCKER_AUTHENTICATION_SETUP.md**](DOCKER_AUTHENTICATION_SETUP.md) | Complete implementation guide with all scenarios | 500+ | 15-20 min |
| [**DOCKER_LDAP_OAUTH2.md**](docs/DOCKER_LDAP_OAUTH2.md) | Deep dive on LDAP and OAuth2 integration | 500+ | 15-20 min |

### Reference Documentation

| File | Topics | Lines |
|------|--------|-------|
| [docs/LDAP.md](docs/LDAP.md) | LDAP configuration, filters, setup | 7.6K |
| [docs/OAUTH2.md](docs/OAUTH2.md) | OAuth2 providers (Google, GitHub, Azure, Keycloak) | 11K |
| [docs/SECURITY.md](docs/SECURITY.md) | Security best practices, LDAPS, secrets management | 9K |
| [docs/PRODUCTION.md](docs/PRODUCTION.md) | Production deployment, scaling, monitoring | 11K |
| [docs/TESTING.md](docs/TESTING.md) | Test procedures, validation, verification | 14K |
| [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) | Common issues, debugging, solutions | 19K |

### Architecture & Implementation

| File | Topics | Lines |
|------|--------|-------|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | System design, data flow, components | 17K |
| [docs/IMPLEMENTATION_GUIDE.md](docs/IMPLEMENTATION_GUIDE.md) | Step-by-step implementation | 13K |
| [docs/DEPLOYMENT_CHECKLIST.md](docs/DEPLOYMENT_CHECKLIST.md) | Pre-deployment verification checklist | 14K |
| [docs/KUBERNETES.md](docs/KUBERNETES.md) | K8s deployment with authentication | 5.9K |

---

## 🎯 Use Case Navigation

### Scenario 1: Local Development with LDAP

**Files to Use:**
- Configuration: [docker-compose.dev-full.yml](docker-compose.dev-full.yml)
- Users: [ldap-init.ldif](ldap-init.ldif) (4 pre-configured test users)
- Documentation: [docs/DOCKER_LDAP_OAUTH2.md - Section 1.1](docs/DOCKER_LDAP_OAUTH2.md#11-development-setup-with-openldap)

**Quick Start:**
```bash
docker-compose -f docker-compose.dev-full.yml up -d
# Users available: testuser, adminuser, movieuser, ticketuser
# LDAP Admin UI: https://localhost:6443
```

**Test Users:**
| Username | Password | Role |
|----------|----------|------|
| testuser | testpassword123 | Regular user |
| adminuser | adminpassword123 | Administrator |
| movieuser | moviepassword123 | Movie Manager |
| ticketuser | ticketpassword123 | Ticket Seller |

### Scenario 2: Corporate LDAP/Active Directory

**Files to Use:**
- Guide: [docs/DOCKER_LDAP_OAUTH2.md - Section 1.2](docs/DOCKER_LDAP_OAUTH2.md#12-corporate-ldapadaactive-directory-setup)
- Configuration: [docs/LDAP.md](docs/LDAP.md)
- Reference: [docs/DOCKER_AUTHENTICATION_SETUP.md - Corporate LDAP](DOCKER_AUTHENTICATION_SETUP.md#scenario-2-corporate-ldap-integration)

**Setup Steps:**
1. Configure SPRING_LDAP_URLS to your corporate LDAP
2. Set up service account credentials
3. Update search filters for your directory type
4. Enable LDAPS if required
5. Test with your corporate credentials

### Scenario 3: OAuth2 (Google, GitHub, Azure AD)

**Files to Use:**
- Configuration: [docker-compose.oauth2.yml](docker-compose.oauth2.yml)
- Guide: [docs/DOCKER_LDAP_OAUTH2.md - Part 2](docs/DOCKER_LDAP_OAUTH2.md#part-2-oauth2-integration-with-docker)
- Reference: [docs/OAUTH2.md](docs/OAUTH2.md)

**Quick Start:**
```bash
export GOOGLE_CLIENT_ID=your-id
export GOOGLE_CLIENT_SECRET=your-secret
docker-compose -f docker-compose.oauth2.yml up -d
```

**Provider-Specific Guides:**
- Google: [Section 2.1](docs/DOCKER_LDAP_OAUTH2.md#21-google-oauth2)
- GitHub: [Section 2.2](docs/DOCKER_LDAP_OAUTH2.md#22-github-oauth2)
- Azure AD: [Section 2.3](docs/DOCKER_LDAP_OAUTH2.md#23-azure-ad-microsoft-entra-id)
- Keycloak: [Section 2.4](docs/DOCKER_LDAP_OAUTH2.md#24-keycloak-self-hosted-oauth2)

### Scenario 4: Self-Hosted OAuth2 (Keycloak)

**Files to Use:**
- Configuration: [docker-compose.oauth2.yml](docker-compose.oauth2.yml) (Keycloak included)
- Guide: [docs/DOCKER_LDAP_OAUTH2.md - Section 2.4](docs/DOCKER_LDAP_OAUTH2.md#24-keycloak-self-hosted-oauth2)

**Quick Start:**
```bash
docker-compose -f docker-compose.oauth2.yml up -d
# Access at http://localhost:8180
# Login: admin / admin_password
```

### Scenario 5: Production Deployment

**Files to Use:**
1. Start: [docs/PRODUCTION.md](docs/PRODUCTION.md)
2. Checklist: [docs/DEPLOYMENT_CHECKLIST.md](docs/DEPLOYMENT_CHECKLIST.md)
3. Security: [docs/SECURITY.md](docs/SECURITY.md)
4. Architecture: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

**Pre-Deployment Steps:**
- [ ] Configure corporate LDAP or OAuth2 providers
- [ ] Enable LDAPS for secure LDAP connections
- [ ] Set up Docker secrets for credentials
- [ ] Enable TLS/HTTPS for all endpoints
- [ ] Configure health checks and monitoring
- [ ] Test authentication flows
- [ ] Document for operations team
- [ ] Plan failover/recovery procedures

---

## 🔧 Common Commands

### Start Development Environment (LDAP)

```bash
# Start all services with LDAP
docker-compose -f docker-compose.dev-full.yml up -d

# View service status
docker-compose -f docker-compose.dev-full.yml ps

# View logs
docker-compose -f docker-compose.dev-full.yml logs -f api

# Test LDAP user login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "testpassword123"}'

# Stop services
docker-compose -f docker-compose.dev-full.yml down
```

### Start OAuth2 Environment

```bash
# Start with environment variables
export GOOGLE_CLIENT_ID=your-id
export GOOGLE_CLIENT_SECRET=your-secret
docker-compose -f docker-compose.oauth2.yml up -d

# Access applications
# Frontend: http://localhost:4200
# Keycloak: http://localhost:8180 (admin/admin_password)

# Stop services
docker-compose -f docker-compose.oauth2.yml down
```

### Debug and Verify

```bash
# Check service health
docker-compose logs | grep healthcheck

# Test LDAP connectivity
docker exec cinema-ldap ldapsearch \
  -x -H ldap://localhost:389 \
  -b "dc=cinema,dc=local" \
  -D "cn=admin,dc=cinema,dc=local" \
  -w admin

# Test API health
curl http://localhost:8080/actuator/health

# View API logs for authentication
docker logs cinema-api | grep -i auth

# Connect to database
docker exec cinema-postgres psql -U cinema_user -d cinema_db -c "SELECT 1"
```

---

## 📞 Help and Support

### Common Questions

**Q: Where are the test user credentials?**
A: See [ldap-init.ldif](ldap-init.ldif) or [DOCKER_AUTHENTICATION_SETUP.md](DOCKER_AUTHENTICATION_SETUP.md#test-users) - 4 users ready to use

**Q: How do I connect to corporate LDAP?**
A: Follow [docs/DOCKER_LDAP_OAUTH2.md - Section 1.2](docs/DOCKER_LDAP_OAUTH2.md#12-corporate-ldapadaactive-directory-setup)

**Q: How do I set up Google OAuth2?**
A: See [docs/DOCKER_LDAP_OAUTH2.md - Section 2.1](docs/DOCKER_LDAP_OAUTH2.md#21-google-oauth2) or [docs/OAUTH2.md](docs/OAUTH2.md)

**Q: What do I do if authentication fails?**
A: Check [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) for debugging steps

**Q: How do I deploy to production?**
A: Follow [docs/PRODUCTION.md](docs/PRODUCTION.md) and [docs/DEPLOYMENT_CHECKLIST.md](docs/DEPLOYMENT_CHECKLIST.md)

### Troubleshooting

| Issue | Reference |
|-------|-----------|
| LDAP connection refused | [Troubleshooting](docs/TROUBLESHOOTING.md#ldap-connection-issues) |
| User not found | [Troubleshooting](docs/TROUBLESHOOTING.md#ldap-connection-issues) |
| OAuth2 redirect mismatch | [Troubleshooting](docs/TROUBLESHOOTING.md#oauth2-issues) |
| Frontend can't reach API | [Troubleshooting](docs/TROUBLESHOOTING.md#service-communication) |
| Database connection error | [Troubleshooting](docs/TROUBLESHOOTING.md#postgresql-issues) |

---

## 📊 What's Included

✅ **Docker Compose Files:**
- Development with OpenLDAP
- OAuth2 with Keycloak
- Basic development stack
- Production configuration

✅ **LDAP Configuration:**
- 4 pre-configured test users
- User groups and organizational units
- Automatic initialization on startup

✅ **Comprehensive Documentation:**
- 11 documentation files (~5,500 lines)
- Quick start guides
- Detailed configuration references
- Troubleshooting and debugging guides
- Security best practices
- Production deployment guidance

✅ **Testing & Verification:**
- Health check configurations
- Testing procedures
- Service verification commands
- Debugging utilities

---

## 🎓 Learning Path

### Beginner (15-30 minutes)
1. Read: [DOCKER_AUTHENTICATION_SETUP.md - Quick Start](DOCKER_AUTHENTICATION_SETUP.md#quick-start-guide)
2. Run: `docker-compose -f docker-compose.dev-full.yml up -d`
3. Test: Log in with testuser / testpassword123
4. Explore: phpLDAPadmin UI at https://localhost:6443

### Intermediate (1-2 hours)
1. Read: [docs/DOCKER_LDAP_OAUTH2.md](docs/DOCKER_LDAP_OAUTH2.md)
2. Try: Corporate LDAP configuration (Section 1.2)
3. Try: OAuth2 setup (Part 2)
4. Review: [docs/TESTING.md](docs/TESTING.md)

### Advanced (2-4 hours)
1. Read: [docs/PRODUCTION.md](docs/PRODUCTION.md)
2. Study: [docs/SECURITY.md](docs/SECURITY.md)
3. Plan: [docs/DEPLOYMENT_CHECKLIST.md](docs/DEPLOYMENT_CHECKLIST.md)
4. Understand: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

---

## 📝 Files Summary

| Category | File | Status |
|----------|------|--------|
| **Config** | docker-compose.dev-full.yml | ✅ Ready |
| **Config** | docker-compose.oauth2.yml | ✅ Ready |
| **Config** | ldap-init.ldif | ✅ Ready |
| **Guide** | DOCKER_AUTHENTICATION_SETUP.md | ✅ Ready |
| **Guide** | DOCKER_SETUP_COMPLETE.md | ✅ Ready |
| **Docs** | docs/DOCKER_LDAP_OAUTH2.md | ✅ Ready |
| **Docs** | docs/LDAP.md | ✅ Ready |
| **Docs** | docs/OAUTH2.md | ✅ Ready |
| **Docs** | docs/SECURITY.md | ✅ Ready |
| **Docs** | docs/PRODUCTION.md | ✅ Ready |
| **Docs** | docs/TESTING.md | ✅ Ready |
| **Docs** | docs/TROUBLESHOOTING.md | ✅ Ready |
| **Docs** | docs/ARCHITECTURE.md | ✅ Ready |
| **Docs** | docs/DEPLOYMENT_CHECKLIST.md | ✅ Ready |
| **Docs** | docs/IMPLEMENTATION_GUIDE.md | ✅ Ready |
| **Docs** | docs/KUBERNETES.md | ✅ Ready |

---

## ✨ Next Steps

1. **Choose your scenario** above
2. **Read the relevant guide**
3. **Run the Docker Compose file**
4. **Follow the quick start commands**
5. **Refer to documentation as needed**
6. **Test your setup** using provided commands
7. **Deploy to production** following checklists

---

**All documentation, configurations, and setup files are ready. Start with the Quick Start guide above matching your use case.**
