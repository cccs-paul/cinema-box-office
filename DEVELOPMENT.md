# Development Setup Guide

## Quick Start

### Prerequisites
- Java 25 JDK
- Node.js 20+
- npm 10+
- Git

### Local Setup (Without Docker)

#### 1. Backend Setup

```bash
# Navigate to backend directory
cd backend

# Install dependencies (Maven will download automatically)
mvn clean install

# Run the application
mvn spring-boot:run

# API will be available at http://localhost:8080/api
```

#### 2. Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm start

# Application will be available at http://localhost:4200
```

#### 3. Database Setup (PostgreSQL)

```bash
# Install PostgreSQL locally or use Docker
docker run --name cinema-db \
  -e POSTGRES_DB=boxoffice \
  -e POSTGRES_USER=boxoffice \
  -e POSTGRES_PASSWORD=boxoffice_password \
  -p 5432:5432 \
  postgres:16-alpine

# Create schema (optional, Spring Boot will do this automatically)
```

### Docker Setup

#### Using Docker Compose (Recommended)

```bash
# Start all services
docker-compose up -d

# Access points:
# - Frontend: http://localhost
# - API: http://localhost:8080/api
# - API Health: http://localhost:8080/api/health
```

## Development Commands

### Backend

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run

# Test
mvn test

# Run specific test
mvn test -Dtest=HealthControllerTest

# Generate coverage report
mvn clean test jacoco:report
```

### Frontend

```bash
# Install dependencies
npm install

# Start dev server
npm start

# Build for production
npm run build

# Run tests
npm test

# Run tests once
npm test -- --watch=false

# Lint code
npm lint
```

## IDE Setup

### IntelliJ IDEA

1. Open project root directory
2. File → Open → Select `pom.xml`
3. Import as Maven project
4. IntelliJ will auto-detect backend and frontend

### Visual Studio Code

1. Install extensions:
   - Extension Pack for Java
   - Angular Language Service
   - Prettier
   - ESLint

2. Open workspace settings: `.vscode/settings.json`

3. Run configurations available in Run menu

## Debugging

### Backend Debugging

**IntelliJ IDEA:**
- Set breakpoints in Java code
- Run → Debug 'BoxOfficeApplication'

**VS Code:**
- Install "Debugger for Java"
- Set breakpoints
- Run → Start Debugging

### Frontend Debugging

**VS Code:**
- Open DevTools (F12)
- Source tab for JavaScript debugging
- Console for errors

**Chrome DevTools:**
- F12 in Chrome
- Sources tab for TypeScript debugging

## Common Issues

### Port Already in Use

```bash
# Find process using port
lsof -i :8080
lsof -i :4200

# Kill process
kill -9 <PID>
```

### Maven Dependency Issues

```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Reinstall
mvn clean install
```

### Node Modules Issues

```bash
# Clear npm cache
npm cache clean --force

# Reinstall
rm -rf node_modules package-lock.json
npm install
```

### Docker Issues

```bash
# Remove all containers
docker-compose down -v

# Rebuild images
docker-compose build --no-cache

# Restart
docker-compose up -d
```

## Performance Tips

1. **Backend**: Use Spring Boot DevTools for faster reload
2. **Frontend**: Use Angular CLI's watch mode during development
3. **Database**: Use H2 for local testing instead of PostgreSQL
4. **Build Cache**: Docker multi-stage builds cache dependencies

---

**Last Updated**: January 16, 2026
