# Dockerfile Documentation

This document provides detailed information about the Docker configuration for Cinema Box Office.

## Backend Dockerfile

### Multi-Stage Build

The backend Dockerfile uses a two-stage build process:

**Stage 1: Builder**
- Base: `eclipse-temurin:25-jdk-noble` (Java 25 JDK)
- Runs Maven to compile and package the application
- Creates a JAR file in `target/`

**Stage 2: Runtime**
- Base: `eclipse-temurin:25-jre-noble` (Java 25 Runtime)
- Copies JAR from builder stage
- Creates non-root user (`boxoffice:1000`)
- Configures health check
- Exposes port 8080

### Security Features
- Non-root user for security
- Read-only file system where possible
- Health checks enabled
- Minimal attack surface with JRE-only runtime

### Build Command
```bash
docker build -f backend/Dockerfile -t cinema-box-office-api .
```

## Frontend Dockerfile

### Multi-Stage Build

The frontend Dockerfile uses a two-stage build process:

**Stage 1: Builder**
- Base: `node:20-alpine` (Lightweight Node.js 20)
- Installs dependencies
- Builds Angular application
- Creates optimized production build

**Stage 2: Runtime**
- Base: `nginx:1.27-alpine` (Lightweight Nginx)
- Copies built Angular app
- Copies Nginx configuration
- Creates non-root user
- Configures health check
- Exposes port 80

### Security Features
- Non-root user for security
- Alpine Linux for minimal image size
- Security headers configured in Nginx
- Health checks enabled

### Build Command
```bash
docker build -f frontend/Dockerfile -t cinema-box-office-web .
```

## Image Sizes

- Backend: ~400MB (Java runtime + Spring Boot app)
- Frontend: ~50MB (Nginx + Angular build)

## Health Checks

### Backend Health Check
```
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3
CMD java -jar app.jar --check-health || exit 1
```

### Frontend Health Check
```
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3
CMD wget --quiet --tries=1 --spider http://localhost:80/ || exit 1
```

## Environment Variables

See [README.md](./README.md) for comprehensive environment variable documentation.

---

**Last Updated**: January 16, 2026
