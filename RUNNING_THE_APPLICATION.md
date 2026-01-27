# Running the myRC Application

**myRC - My Responsibility Centre**  
A comprehensive funding and resource management system for tracking fiscal year budgets, funding items, and organizational responsibility centres.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Running in Development Mode](#running-in-development-mode)
   - [VSCode Development](#vscode-development)
   - [Docker Development](#docker-development)
3. [Running in Production Mode](#running-in-production-mode)
   - [Docker Production](#docker-production)
4. [Running in Kubernetes](#running-in-kubernetes)
5. [Testing](#testing)
6. [Accessing the Application](#accessing-the-application)
7. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

- **Java 25** or higher (for backend development)
- **Maven 3.9+** (for backend builds)
- **Node.js 20+** and **npm** (for frontend development)
- **Docker** and **Docker Compose** (for containerized deployments)
- **kubectl** (for Kubernetes deployments)
- **Git** (for source control)

### Optional Tools

- **VS Code** (recommended IDE)
- **pgAdmin** (database management)
- **IntelliJ IDEA** (alternative Java IDE)

### Verify Installation

```bash
java --version        # Should show Java 25+
mvn --version         # Should show Maven 3.9+
node --version        # Should show Node 20+
npm --version         # Should show npm 10+
docker --version      # Should show Docker 20+
docker compose version # Should show Docker Compose v2+
kubectl version       # For Kubernetes deployments
```

---

## Running in Development Mode

Development mode provides hot-reloading for both frontend and backend, making it ideal for active development.

### VSCode Development

#### Backend (Spring Boot)

1. **Open the project in VS Code**
   ```bash
   cd /path/to/cinema-box-office
   code .
   ```

2. **Install Java Extension Pack** (if not already installed)
   - Extension ID: `vscjava.vscode-java-pack`

3. **Configure the backend**
   - Navigate to `backend/src/main/resources/application.yml`
   - Ensure database connection points to `localhost:5432`

4. **Start PostgreSQL** (required for backend)
   ```bash
   docker run -d --name myrc-postgres \
     -e POSTGRES_DB=myrc \
     -e POSTGRES_USER=myrc \
     -e POSTGRES_PASSWORD=myrc_password \
     -p 5432:5432 \
     postgres:16-alpine
   ```

5. **Run the backend**
   - Open `backend/src/main/java/com/boxoffice/Application.java`
   - Click "Run" or "Debug" above the `main` method
   - Or use terminal:
     ```bash
     cd backend
     mvn spring-boot:run
     ```

6. **Verify backend is running**
   - Open http://localhost:8080/api/health
   - Should return: `{"status":"UP","message":"myRC API is running"}`

#### Frontend (Angular)

1. **Install dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Start the development server**
   ```bash
   npm start
   # Or explicitly:
   ng serve --host 0.0.0.0 --port 4200
   ```

3. **Verify frontend is running**
   - Open http://localhost:4200
   - Should see the myRC login page

4. **Enable hot-reload**
   - Angular CLI automatically watches for file changes
   - Changes to TypeScript, HTML, or SCSS files trigger automatic recompilation

#### Full VSCode Debug Configuration

Create `.vscode/launch.json` for integrated debugging:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug Backend",
      "request": "launch",
      "mainClass": "com.boxoffice.Application",
      "projectName": "backend",
      "cwd": "${workspaceFolder}/backend",
      "env": {
        "SPRING_PROFILES_ACTIVE": "dev"
      }
    },
    {
      "type": "node",
      "name": "Debug Frontend",
      "request": "launch",
      "cwd": "${workspaceFolder}/frontend",
      "runtimeExecutable": "npm",
      "runtimeArgs": ["start"],
      "port": 9229
    }
  ],
  "compounds": [
    {
      "name": "Full Stack Debug",
      "configurations": ["Debug Backend", "Debug Frontend"]
    }
  ]
}
```

### Docker Development

Docker development mode uses hot-reload volumes for both frontend and backend.

#### Quick Start

```bash
# Build and start all services in development mode
./build-and-start.sh dev
```

#### Manual Docker Development

1. **Start development containers**
   ```bash
   docker compose --project-name myrc -f docker-compose.dev.yml up -d
   ```

2. **View logs**
   ```bash
   docker compose -f docker-compose.dev.yml logs -f
   
   # View specific service logs
   docker compose -f docker-compose.dev.yml logs -f web
   docker compose -f docker-compose.dev.yml logs -f api
   ```

3. **Check container status**
   ```bash
   docker compose -f docker-compose.dev.yml ps
   ```

4. **Stop development containers**
   ```bash
   docker compose -f docker-compose.dev.yml down
   ```

#### Development Services

The `docker-compose.dev.yml` starts:

- **Frontend** (Angular dev server): http://localhost:4200
- **Backend** (Spring Boot): http://localhost:8080
- **PostgreSQL**: localhost:5432
- **pgAdmin**: http://localhost:5050

#### Hot Reload with Docker

- Frontend: Uses Angular's built-in dev server with volume mounts
- Backend: Uses Spring Boot DevTools (if enabled)
- Changes to source files are reflected immediately without rebuilding containers

---

## Running in Production Mode

Production mode uses optimized builds with nginx for frontend and compiled JARs for backend.

### Docker Production

#### Automated Build and Start

The recommended way to run in production:

```bash
# Build and start in production mode
./build-and-start.sh prod
```

This script:
1. Builds the backend with Maven
2. Builds the frontend with Angular production configuration
3. Removes old Docker images
4. Builds fresh Docker images with production optimizations
5. Starts all services with `docker-compose.yml`

#### Manual Production Build

1. **Build the backend**
   ```bash
   cd backend
   mvn clean package -DskipTests
   cd ..
   ```

2. **Build the frontend**
   ```bash
   cd frontend
   npm install
   npm run build
   cd ..
   ```

3. **Build Docker images**
   ```bash
   docker compose build --no-cache
   ```

4. **Start production containers**
   ```bash
   docker compose --project-name myrc up -d
   ```

5. **Verify deployment**
   ```bash
   docker compose ps
   
   # Check health
   curl http://localhost:8080/api/health
   curl http://localhost/
   ```

#### Production Services

The `docker-compose.yml` starts:

- **Frontend** (nginx): http://localhost
- **Backend** (Java): http://localhost:8080
- **PostgreSQL**: localhost:5432
- **pgAdmin**: http://localhost:5050

#### Production Optimizations

- Frontend served by nginx with gzip compression
- Angular built with `--configuration production`
  - Ahead-of-Time (AOT) compilation
  - Tree-shaking and minification
  - Source maps disabled
- Backend JAR includes all dependencies
- Health checks enabled for all services
- Automatic restart policies

---

## Running in Kubernetes

Deploy myRC to a Kubernetes cluster for high availability and scalability.

### Prerequisites

- Kubernetes cluster (local or cloud)
- `kubectl` configured and connected to your cluster
- Kubernetes resources: 4 CPU, 8GB RAM minimum

### Quick Deployment

```bash
# Deploy all resources
./k8s-deploy.sh

# Check deployment status
./k8s-health.sh
```

### Manual Kubernetes Deployment

1. **Create namespace and secrets**
   ```bash
   kubectl apply -f k8s/namespace.yaml
   kubectl apply -f k8s/secrets.yaml
   ```

2. **Deploy database**
   ```bash
   kubectl apply -f k8s/postgres.yaml
   
   # Wait for database to be ready
   kubectl wait --for=condition=ready pod -l app=postgres -n myrc --timeout=300s
   ```

3. **Deploy backend**
   ```bash
   kubectl apply -f k8s/backend.yaml
   
   # Wait for backend to be ready
   kubectl wait --for=condition=ready pod -l app=myrc-api -n myrc --timeout=300s
   ```

4. **Deploy frontend**
   ```bash
   kubectl apply -f k8s/frontend.yaml
   
   # Wait for frontend to be ready
   kubectl wait --for=condition=ready pod -l app=myrc-web -n myrc --timeout=300s
   ```

5. **Configure ingress** (optional)
   ```bash
   kubectl apply -f k8s/ingress.yaml
   ```

### Verify Kubernetes Deployment

```bash
# Check all resources
kubectl get all -n myrc

# Check pods
kubectl get pods -n myrc

# Check services
kubectl get svc -n myrc

# View logs
kubectl logs -n myrc -l app=myrc-api --tail=50
kubectl logs -n myrc -l app=myrc-web --tail=50

# Check pod health
kubectl describe pod -n myrc -l app=myrc-api
```

### Accessing in Kubernetes

#### Port Forwarding (Development/Testing)

```bash
# Forward frontend
kubectl port-forward -n myrc svc/myrc-web 8080:80

# Forward backend
kubectl port-forward -n myrc svc/myrc-api 8081:8080

# Forward database
kubectl port-forward -n myrc svc/postgres 5432:5432
```

Then access:
- Frontend: http://localhost:8080
- Backend: http://localhost:8081
- Database: localhost:5432

#### Production Ingress

Configure your ingress controller to route traffic:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: myrc-ingress
  namespace: myrc
spec:
  rules:
  - host: myrc.example.com
    http:
      paths:
      - path: /api
        pathType: Prefix
        backend:
          service:
            name: myrc-api
            port:
              number: 8080
      - path: /
        pathType: Prefix
        backend:
          service:
            name: myrc-web
            port:
              number: 80
```

### Scaling in Kubernetes

```bash
# Scale backend replicas
kubectl scale deployment myrc-api -n myrc --replicas=3

# Scale frontend replicas
kubectl scale deployment myrc-web -n myrc --replicas=2

# Auto-scale based on CPU
kubectl autoscale deployment myrc-api -n myrc --cpu-percent=70 --min=2 --max=10
```

### Updating Kubernetes Deployment

```bash
# Update images
kubectl set image deployment/myrc-api -n myrc myrc-api=myrc-api:v2.0.0
kubectl set image deployment/myrc-web -n myrc myrc-web=myrc-web:v2.0.0

# Rolling restart
kubectl rollout restart deployment/myrc-api -n myrc
kubectl rollout restart deployment/myrc-web -n myrc

# Check rollout status
kubectl rollout status deployment/myrc-api -n myrc
```

---

## Testing

### Backend Tests

```bash
cd backend

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=FundingItemServiceTest

# Run with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Frontend Tests

```bash
cd frontend

# Run unit tests
npm test

# Run tests with coverage
npm run test:coverage

# Run end-to-end tests
npm run e2e

# View coverage report
open coverage/index.html
```

### Integration Tests

```bash
# Start test environment
docker compose -f docker-compose.dev.yml up -d

# Run integration tests
cd backend
mvn verify -Pintegration-tests

# Cleanup
docker compose -f docker-compose.dev.yml down
```

---

## Accessing the Application

### Default Credentials

**Application Login:**
- Username: `admin`
- Password: `Admin@123`

**pgAdmin (Database Console):**
- URL: http://localhost:5050
- Email: `admin@example.com`
- Password: `admin_password`

**PostgreSQL Direct Connection:**
- Host: `localhost`
- Port: `5432`
- Database: `myrc`
- Username: `myrc`
- Password: `myrc_password`

### Access Points by Environment

#### Development Mode (VSCode)
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api
- API Health: http://localhost:8080/api/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- Database: localhost:5432

#### Development Mode (Docker)
- Frontend: http://localhost:4200 (with hot reload)
- Backend API: http://localhost:8080/api
- pgAdmin: http://localhost:5050
- Database: localhost:5432

#### Production Mode (Docker)
- Frontend: http://localhost
- Backend API: http://localhost:8080/api
- pgAdmin: http://localhost:5050
- Database: localhost:5432

#### Kubernetes
- Frontend: http://<ingress-host>
- Backend API: http://<ingress-host>/api
- Or use port-forwarding (see Kubernetes section)

---

## Troubleshooting

### Common Issues

#### Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080
# Or on Linux
sudo netstat -tulpn | grep 8080

# Kill the process
kill -9 <PID>
```

#### Database Connection Failed

```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check database logs
docker logs myrc-db

# Verify connection
docker exec -it myrc-db psql -U myrc -d myrc -c "SELECT 1;"
```

#### Frontend Build Errors

```bash
# Clear npm cache and reinstall
cd frontend
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

#### Docker Build Fails

```bash
# Clear Docker cache
docker system prune -a --volumes

# Rebuild without cache
docker compose build --no-cache

# Check disk space
df -h
docker system df
```

#### Container Won't Start

```bash
# Check logs for specific container
docker logs myrc-api
docker logs myrc-web

# Check container status
docker inspect myrc-api

# Enter container for debugging
docker exec -it myrc-api sh
```

#### Angular Hot Reload Not Working

```bash
# Increase file watch limit (Linux)
echo fs.inotify.max_user_watches=524288 | sudo tee -a /etc/sysctl.conf
sudo sysctl -p

# Restart Angular dev server
npm start
```

### Health Checks

```bash
# Backend health
curl http://localhost:8080/api/health

# Frontend health (production)
curl http://localhost/

# Database health
docker exec myrc-db pg_isready -U myrc

# All services (Docker)
docker compose ps
```

### Logs

```bash
# View all logs
docker compose logs -f

# View specific service
docker compose logs -f api
docker compose logs -f web

# Last 100 lines
docker compose logs --tail=100 api

# Follow with timestamps
docker compose logs -f -t api
```

### Reset Everything

```bash
# Stop and remove all containers, volumes, and images
./stop.sh
docker compose down -v
docker system prune -a --volumes

# Rebuild from scratch
./build-and-start.sh dev
```

---

## Performance Tips

### Development
- Use `npm start` instead of `ng serve` for better defaults
- Enable Angular production mode even in dev: `ng serve --configuration development`
- Use Chrome DevTools for performance profiling

### Production
- Enable HTTPS/TLS for production deployments
- Use CDN for static assets
- Configure nginx caching headers
- Enable database connection pooling
- Monitor with tools like Prometheus + Grafana

### Kubernetes
- Use horizontal pod autoscaling (HPA)
- Configure resource requests and limits
- Use persistent volumes for database
- Enable cluster autoscaling for cloud deployments

---

## Additional Resources

- [Architecture Documentation](docs/ARCHITECTURE.md)
- [API Documentation](docs/API.md)
- [Deployment Checklist](docs/DEPLOYMENT_CHECKLIST.md)
- [Security Guide](docs/SECURITY.md)
- [Kubernetes Guide](docs/KUBERNETES.md)
- [Testing Guide](docs/TESTING.md)
- [Troubleshooting Guide](docs/TROUBLESHOOTING.md)

---

## Quick Reference Commands

```bash
# Development
./build-and-start.sh dev          # Start dev environment
docker compose -f docker-compose.dev.yml logs -f   # View dev logs
./stop.sh dev                     # Stop dev environment

# Production
./build-and-start.sh prod         # Build and start production
docker compose ps                 # Check status
docker compose logs -f            # View logs
./stop.sh prod                    # Stop production

# Kubernetes
./k8s-deploy.sh                   # Deploy to Kubernetes
./k8s-health.sh                   # Check health
kubectl get pods -n myrc          # View pods
kubectl logs -n myrc -l app=myrc-api   # View logs

# Testing
mvn test                          # Backend tests
npm test                          # Frontend tests
./test.sh                         # Run all tests

# Database
docker exec -it myrc-db psql -U myrc -d myrc   # Connect to DB
```

---

**Last Updated:** January 24, 2026  
**Version:** 2.0.0  
**License:** MIT
