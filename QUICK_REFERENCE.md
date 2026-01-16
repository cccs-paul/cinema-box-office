# Quick Reference Guide

**Cinema Box Office Project** - Quick Command Reference

## 🚀 Quick Start

### Production Deployment
```bash
./start.sh prod
# Frontend: http://localhost
# API: http://localhost:8080/api
```

### Development Setup
```bash
./start.sh dev
# Frontend (hot reload): http://localhost:4200
# API: http://localhost:8080/api
```

### Stop Services
```bash
./stop.sh prod   # Stop production
./stop.sh dev    # Stop development
```

## 🔨 Building & Testing

### Build All
```bash
./build.sh
```

### Run All Tests
```bash
./test.sh
```

### Backend Only
```bash
cd backend
mvn clean package        # Build
mvn spring-boot:run      # Run locally
mvn test                 # Test
mvn test -Dtest=HealthControllerTest  # Specific test
```

### Frontend Only
```bash
cd frontend
npm install              # Install dependencies
npm start               # Dev server (hot reload)
npm run build           # Production build
npm test                # Run tests
npm run build:prod      # Production build
```

## 🐳 Docker Commands

### Build Images
```bash
docker-compose build                    # Build all
docker build -f backend/Dockerfile -t cinema-box-office-api .
docker build -f frontend/Dockerfile -t cinema-box-office-web .
```

### Run Services
```bash
docker-compose up -d                    # Start all (production)
docker-compose -f docker-compose.dev.yml up -d  # Start all (development)
docker-compose down                     # Stop all (prod)
docker-compose -f docker-compose.dev.yml down   # Stop all (dev)
```

### View Logs
```bash
docker-compose logs -f                  # All services
docker-compose logs -f api              # Backend only
docker-compose logs -f web              # Frontend only
docker-compose logs -f postgres         # Database only
```

### Access Database
```bash
docker exec -it cinema-box-office-db psql -U boxoffice -d boxoffice
```

## 📝 Project Files

### Key Directories
- **backend/** - Java Spring Boot API
- **frontend/** - Angular Single Page Application
- **docker-compose.yml** - Production orchestration
- **docker-compose.dev.yml** - Development orchestration

### Important Files
- **README.md** - Full documentation
- **DEVELOPMENT.md** - Development guide
- **DOCKER.md** - Docker documentation
- **PROJECT_STATUS.md** - Status report
- **INITIALIZATION.md** - Initialization summary

### Shell Scripts
- **build.sh** - Build backend & frontend
- **test.sh** - Run all tests
- **start.sh** - Start services (prod/dev)
- **stop.sh** - Stop services (prod/dev)

## 🌐 API Endpoints

### Health Check
```
GET /api/health
```

Response:
```json
{
  "status": "UP",
  "message": "Box Office API is running"
}
```

## 🔧 Configuration

### Backend Profiles
- **production** - PostgreSQL with logging disabled
- **development** - PostgreSQL with debug logging
- **test** - H2 in-memory database

### Environment Variables
- `SPRING_DATASOURCE_URL` - Database connection
- `SPRING_DATASOURCE_USERNAME` - DB user
- `SPRING_DATASOURCE_PASSWORD` - DB password
- `SPRING_JPA_HIBERNATE_DDL_AUTO` - JPA DDL strategy
- `SPRING_PROFILES_ACTIVE` - Active profile

## 📊 Database

### Connection Details
- **Host**: localhost (dev) or postgres (Docker)
- **Port**: 5432
- **Database**: boxoffice
- **Username**: boxoffice
- **Password**: boxoffice_password

### Access Database CLI
```bash
# From host (if PostgreSQL installed)
psql -h localhost -U boxoffice -d boxoffice

# From Docker container
docker exec -it cinema-box-office-db psql -U boxoffice -d boxoffice
```

## 🔍 Common Tasks

### Update Dependencies
```bash
# Backend
cd backend && mvn clean install

# Frontend
cd frontend && npm update
```

### Clean Build
```bash
./build.sh              # Clean build all

# Or individual
cd backend && mvn clean
cd frontend && rm -rf dist node_modules
```

### Reset Docker Environment
```bash
docker-compose down -v              # Remove volumes
docker-compose build --no-cache     # Rebuild without cache
docker-compose up -d                # Start fresh
```

### View Application Logs (Local)
```bash
# Backend
tail -f /tmp/boxoffice*.log

# Frontend (dev server)
# Check terminal where npm start is running
```

## 🛡️ Security

### Default Ports
- Frontend: 80 (prod) / 4200 (dev)
- Backend API: 8080
- Database: 5432 (internal)

### Security Headers (Frontend)
- X-Frame-Options
- X-Content-Type-Options
- X-XSS-Protection
- Referrer-Policy

### Best Practices
- Use strong database password
- Don't commit secrets to git
- Use .env files for local development
- Enable HTTPS in production
- Use Spring Security for API

## 📱 IDE Setup

### VS Code
- Open workspace: `cinema-box-office.code-workspace`
- Recommended extensions listed in workspace file
- Debug configurations available

### IntelliJ IDEA
1. Open project
2. Import `pom.xml` as Maven project
3. Auto-detect backend and frontend

## 🚨 Troubleshooting

### Port Already in Use
```bash
lsof -i :8080   # Check port 8080
lsof -i :4200   # Check port 4200
kill -9 <PID>   # Kill process
```

### Database Connection Error
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check logs
docker logs cinema-box-office-db

# Restart database
docker-compose restart postgres
```

### Build Failures
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Clear npm cache
npm cache clean --force

# Rebuild
./build.sh
```

### Docker Issues
```bash
# Remove all containers and volumes
docker-compose down -v

# Rebuild without cache
docker-compose build --no-cache

# Start fresh
docker-compose up -d
```

## 📞 Getting Help

- **README.md** - Comprehensive documentation
- **DEVELOPMENT.md** - Development setup
- **DOCKER.md** - Docker configuration
- **Code Comments** - Inline documentation
- **JavaDoc** - Backend API documentation

## 🎯 Development Workflow

1. Create feature branch
   ```bash
   git checkout -b feature/my-feature
   ```

2. Make changes and commit
   ```bash
   git add .
   git commit -m "feat: add new feature"
   ```

3. Run tests locally
   ```bash
   ./test.sh
   ```

4. Push to remote
   ```bash
   git push origin feature/my-feature
   ```

5. Create pull request and get review

---

**Last Updated**: January 16, 2026  
**Version**: 1.0.0  
**Status**: Production Ready
