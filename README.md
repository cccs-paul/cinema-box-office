# myRC - My Responsibility Centre

## Overview

myRC is a comprehensive fiscal year and responsibility centre management system built with a **Java 25 Spring Boot REST API backend** and an **Angular frontend**. The application enables organizations to track funding items, budgets, and allocations across multiple responsibility centres and fiscal years.

### Key Features
- Funding, spending, and procurement item management
- Four funding source types: Business Plan, On-Ramp, Approved Deficit, and Pressure
- Multi-currency support with exchange rate tracking
- File attachments for invoices, events, and quotes
- JSON export/import with base64-encoded file attachments
- Bilingual support (English/French) with fully translated status labels
- Dark/light theme support
- Role-based access control (OWNER, READ_WRITE, READ_ONLY)
- Fuzzy search and multi-criteria filtering

## Quick Start

For comprehensive instructions on running the application in development, production, Docker, Kubernetes, and VSCode, see **[RUNNING_THE_APPLICATION.md](RUNNING_THE_APPLICATION.md)**.

## Project Structure

```
myrc/
├── backend/                          # Java Spring Boot REST API
│   ├── src/
│   │   ├── main/java/com/myrc/  # Application source code
│   │   └── test/java/com/myrc/  # Unit and integration tests
│   ├── pom.xml                        # Maven configuration
│   ├── Dockerfile                     # Backend container image
│   └── .dockerignore                  # Docker build exclusions
├── frontend/                          # Angular Single Page Application
│   ├── src/
│   │   ├── app/                       # Angular application components
│   │   └── assets/                    # Static assets
│   ├── package.json                   # NPM dependencies
│   ├── angular.json                   # Angular CLI configuration
│   ├── Dockerfile                     # Frontend container image
│   ├── nginx.conf                     # Nginx web server configuration
│   └── .dockerignore                  # Docker build exclusions
├── docker-compose.yml                 # Multi-container orchestration
├── helm/myrc/                         # Helm chart for all deployment scenarios
└── pom.xml                            # Root Maven POM

```

## Prerequisites

- **Java 25 JDK** - For backend compilation
- **Node.js 20+** - For frontend build
- **Docker & Docker Compose** - For containerized deployment
- **Maven 3.8+** - For Java dependency management
- **npm 10+** - For Node.js package management

## Backend (Java 25 Spring Boot)

### Directory Structure
- `backend/src/main/java/com/myrc/` - Application source code
- `backend/src/main/resources/` - Configuration files
- `backend/src/test/java/com/myrc/` - Unit and integration tests

### Key Technologies
- **Framework**: Spring Boot 3.3.0
- **Database**: PostgreSQL (production), H2 (testing)
- **Testing**: JUnit 5, Mockito, REST Assured
- **Build Tool**: Maven

### API Endpoints

#### Health Check
```bash
GET /api/health
```
Response:
```json
{
  "status": "UP",
  "message": "myRC API is running"
}
```

#### Export/Import (JSON with base64-encoded file attachments)
```bash
GET  /api/responsibility-centres/{rcId}/fiscal-years/{fyId}/export
POST /api/responsibility-centres/{rcId}/fiscal-years/{fyId}/import
```

### Building Backend

**Local Build:**
```bash
cd backend
mvn clean package
```

**Run Locally:**
```bash
mvn spring-boot:run
```

**Run Tests:**
```bash
mvn test
```

### Docker Build

**Build Backend Image:**
```bash
docker build -f backend/Dockerfile -t myrc-api .
```

**Run Backend Container:**
```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/myrc \
  -e SPRING_DATASOURCE_USERNAME=myrc \
  -e SPRING_DATASOURCE_PASSWORD=myrc_password \
  myrc-api
```

## Frontend (Angular 17)

### Directory Structure
- `frontend/src/app/` - Angular components and services
- `frontend/src/assets/` - Static assets
- `frontend/src/environments/` - Environment configurations

### Key Technologies
- **Framework**: Angular 17
- **Language**: TypeScript 5.2
- **Build Tool**: Angular CLI
- **Web Server**: Nginx

### Building Frontend

**Install Dependencies:**
```bash
cd frontend
npm ci
```

**Development Build:**
```bash
npm start
# Application available at http://localhost:4200
```

**Production Build:**
```bash
npm run build
# Output: dist/myrc/
```

**Run Tests:**
```bash
npm test
```

### Docker Build

**Build Frontend Image:**
```bash
docker build -f frontend/Dockerfile -t myrc-web .
```

**Run Frontend Container:**
```bash
docker run -p 80:80 myrc-web
```

## Helm Deployment (Recommended)

The Helm chart supports all deployment scenarios with composable values files:

```bash
# Build images first
./build.sh

# Development (single replicas, debug logging)
helm install myrc-dev ./helm/myrc -f helm/myrc/values-dev.yaml

# Production (HA, TLS, autoscaling)
helm install myrc ./helm/myrc -f helm/myrc/values-prod.yaml

# Test LDAP (bundled OpenLDAP with Futurama users)
helm install myrc-testldap ./helm/myrc -f helm/myrc/values-testldap.yaml

# LDAP + App Accounts (existing corporate LDAP)
helm install myrc ./helm/myrc -f helm/myrc/values-ldap-app.yaml \
  --set auth.ldap.url=ldap://ldap.corp.com:389 \
  --set auth.ldap.managerPassword=<PASSWORD>

# OAuth2
helm install myrc ./helm/myrc -f helm/myrc/values-oauth2.yaml \
  --set auth.oauth2.providers.google.clientId=<ID> \
  --set auth.oauth2.providers.google.clientSecret=<SECRET>
```

See [docs/HELM.md](docs/HELM.md) and [helm/myrc/README.md](helm/myrc/README.md) for full documentation.

## Docker Compose Setup

### Start All Services

```bash
docker-compose up -d
```

This will start:
- **PostgreSQL Database** - Port 5432
- **Spring Boot API** - Port 8080 (`http://localhost:8080/api`)
- **Angular Frontend** - Port 80 (`http://localhost`)

### Stop Services

```bash
docker-compose down
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f api
docker-compose logs -f web
docker-compose logs -f postgres
```

### Database Access

**Connect to PostgreSQL:**
```bash
docker exec -it myrc-db psql -U myrc -d myrc
```

**Default Credentials:**
- Username: `myrc`
- Password: `myrc_password`
- Database: `myrc`

## Development Workflow

### Backend Development

1. Make changes to Java code in `backend/src/main/java/`
2. Add/update unit tests in `backend/src/test/java/`
3. Run `mvn clean test` to verify
4. Run `mvn spring-boot:run` to test locally
5. Commit changes

### Frontend Development

1. Make changes to Angular components in `frontend/src/app/`
2. Add/update unit tests alongside components
3. Run `npm start` for development server with hot reload
4. Run `npm test` to verify
5. Commit changes

### Integration Testing

```bash
# Terminal 1: Start backend
cd backend && mvn spring-boot:run

# Terminal 2: Start frontend dev server
cd frontend && npm start

# Terminal 3: Run e2e tests
cd frontend && npm run e2e
```

## Testing

### Backend Tests

```bash
# Unit and integration tests
mvn test

# Specific test class
mvn test -Dtest=HealthControllerTest

# With coverage report
mvn clean test jacoco:report
```

### Frontend Tests

```bash
# Unit tests (watch mode)
npm test

# Single run
npm test -- --watch=false

# With coverage
npm test -- --code-coverage
```

## Production Deployment

### Docker Compose Production

```bash
# Build images
docker-compose build --no-cache

# Start services
docker-compose up -d

# Verify services are healthy
docker-compose ps

# Check logs for issues
docker-compose logs -f
```

### Environment Variables

Backend environment variables (in `docker-compose.yml`):
- `SPRING_DATASOURCE_URL` - Database connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `SPRING_JPA_HIBERNATE_DDL_AUTO` - JPA DDL auto strategy
- `SPRING_PROFILES_ACTIVE` - Active Spring profile

## API Documentation

### REST API

The API follows RESTful conventions and returns JSON responses.

#### Base URL
```
http://localhost:8080/api
```

#### Response Format
```json
{
  "status": "string",
  "message": "string",
  "data": {}
}
```

#### Error Format
```json
{
  "status": "ERROR",
  "message": "Error description",
  "errors": []
}
```

## Security Considerations

### Frontend
- Content Security Policy headers configured in Nginx
- X-Frame-Options to prevent clickjacking
- X-Content-Type-Options to prevent MIME sniffing
- XSS protection headers enabled

### Backend
- CORS configuration for cross-origin requests
- SQL injection prevention via JPA
- CSRF tokens for state-changing operations
- Password encryption for sensitive data

### Database
- Non-root user for PostgreSQL access
- Strong password requirements
- Network isolation via Docker network

## Troubleshooting

### Backend Won't Start
```bash
# Check if port 8080 is available
lsof -i :8080

# View application logs
docker logs myrc-api

# Verify database connectivity
docker logs myrc-db
```

### Frontend Won't Load
```bash
# Check if port 80 is available
lsof -i :80

# Verify Nginx is running
docker logs myrc-web

# Check API connectivity
curl http://localhost:8080/api/health
```

### Database Connection Issues
```bash
# Test database connectivity
docker exec myrc-db pg_isready -U myrc

# Check PostgreSQL logs
docker logs myrc-db
```

## CI/CD Integration

### GitHub Actions (Example)

Create `.github/workflows/ci.yml`:

```yaml
name: CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_PASSWORD: test
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '25'
      - run: mvn clean test
      - uses: actions/setup-node@v3
        with:
          node-version: '20'
      - run: cd frontend && npm ci && npm test
```

## License

MIT License - See LICENSE file for details

## Contributing

1. Create a feature branch
2. Make your changes
3. Write tests
4. Submit a pull request

## Support

For issues and questions, please open an issue in the repository.

---

**Last Updated**: February 13, 2026
**Version**: 1.0.0
