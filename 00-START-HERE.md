# 🎬 Cinema Box Office - START HERE

**Welcome!** This is your complete, production-ready Box Office Management System.

## ✅ What You Have

A fully-initialized project with:
- ✅ Java 25 Spring Boot REST API backend
- ✅ Angular 17 single-page frontend application
- ✅ PostgreSQL database integration
- ✅ Docker containerization with Docker Compose
- ✅ Comprehensive testing infrastructure
- ✅ Complete documentation
- ✅ Automation scripts
- ✅ Production-ready security

## 🚀 Quick Start (5 Minutes)

### Start Everything with Docker

```bash
# Start production environment
./start.sh prod

# Or start development with hot reload
./start.sh dev
```

**That's it!** Now visit:
- Frontend: http://localhost (production) or http://localhost:4200 (development)
- API Health: http://localhost:8080/api/health

### Stop Services
```bash
./stop.sh prod    # or ./stop.sh dev
```

## 📚 Documentation

Start with these in order:

1. **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** ⭐ MOST USEFUL
   - Commands and tips
   - Common tasks
   - Troubleshooting

2. **[README.md](README.md)** - Full Documentation
   - Complete project overview
   - API documentation
   - Architecture details
   - Testing guide

3. **[DEVELOPMENT.md](DEVELOPMENT.md)** - For Developers
   - Local development setup
   - Debugging guide
   - Development workflow

4. **[DOCKER.md](DOCKER.md)** - Docker Details
   - Image configuration
   - Security features
   - Multi-stage builds

5. **[PROJECT_STATUS.md](PROJECT_STATUS.md)** - Technical Details
   - What was created
   - Technology stack
   - File statistics

6. **[INITIALIZATION.md](INITIALIZATION.md)** - Project Setup
   - How project was initialized
   - Next development phases

## 🎯 Common Tasks

### Build Everything
```bash
./build.sh
```

### Run All Tests
```bash
./test.sh
```

### Backend Only (Local)
```bash
cd backend
mvn spring-boot:run
# Backend runs on: http://localhost:8080/api
```

### Frontend Only (Local)
```bash
cd frontend
npm install
npm start
# Frontend runs on: http://localhost:4200
```

### View Database
```bash
# If PostgreSQL running via Docker:
docker exec -it cinema-box-office-db psql -U boxoffice -d boxoffice
```

## 🏗️ Project Structure

```
cinema-box-office/
├── backend/               # Java Spring Boot API
│   ├── src/main/         # Application code
│   ├── src/test/         # Tests
│   └── pom.xml           # Maven config
├── frontend/             # Angular SPA
│   ├── src/              # TypeScript/Angular
│   └── package.json      # NPM config
├── docker-compose.yml    # Production setup
├── docker-compose.dev.yml # Development setup
├── build.sh              # Build script
├── test.sh               # Test script
├── start.sh              # Start services
└── stop.sh               # Stop services
```

## 🔍 What's Running

### Services (Production)
- **Frontend (Port 80)**: Nginx serving Angular app
- **Backend API (Port 8080)**: Spring Boot REST API
- **Database (Port 5432)**: PostgreSQL

### Services (Development)
- **Frontend (Port 4200)**: Angular dev server with hot reload
- **Backend (Port 8080)**: Spring Boot with auto-reload
- **Database (Port 5432)**: PostgreSQL

## 🧪 Testing

### Quick Test
```bash
./test.sh
```

### Backend Tests Only
```bash
cd backend
mvn test
```

### Frontend Tests Only
```bash
cd frontend
npm test
```

## 🔐 Default Credentials

**Database**:
- Host: localhost (dev) / postgres (Docker)
- Port: 5432
- Database: boxoffice
- Username: boxoffice
- Password: boxoffice_password

## 🛠️ Next Steps

### For Development
1. Read [DEVELOPMENT.md](DEVELOPMENT.md)
2. Set up your IDE (VS Code or IntelliJ)
3. Make your first changes
4. Run tests

### For Operations
1. Read [DOCKER.md](DOCKER.md)
2. Review [docker-compose.yml](docker-compose.yml)
3. Deploy to your environment
4. Monitor with `docker-compose logs -f`

### For Architecture
1. Review [README.md](README.md) - Architecture section
2. Check [PROJECT_STATUS.md](PROJECT_STATUS.md) - Technology stack
3. Review source code in backend/ and frontend/

## 💡 Key Files to Know

| File | Purpose |
|------|---------|
| `backend/src/main/java/com/boxoffice/BoxOfficeApplication.java` | Backend entry point |
| `frontend/src/app/app.component.ts` | Frontend main component |
| `docker-compose.yml` | Production setup |
| `docker-compose.dev.yml` | Development setup |
| `backend/Dockerfile` | Backend containerization |
| `frontend/Dockerfile` | Frontend containerization |
| `.editorconfig` | Code formatting rules |
| `pom.xml` | Maven configuration |

## ✨ Features Included

### Backend
- ✅ Spring Boot 3.3.0
- ✅ REST API with health endpoint
- ✅ PostgreSQL integration
- ✅ JUnit 5 tests
- ✅ Non-blocking Docker build

### Frontend
- ✅ Angular 17 standalone components
- ✅ TypeScript 5.2
- ✅ API integration
- ✅ Responsive design
- ✅ Security headers via Nginx

### Infrastructure
- ✅ Multi-container Docker setup
- ✅ Production & development environments
- ✅ Health checks
- ✅ Network isolation
- ✅ Non-root users

## 🐛 Troubleshooting

### Port Already in Use
```bash
lsof -i :8080      # Check what's using port
kill -9 <PID>      # Kill the process
```

### Database Won't Connect
```bash
docker logs cinema-box-office-db    # Check database logs
docker-compose restart postgres     # Restart database
```

### Build Fails
```bash
./build.sh          # Start fresh build
# Or individually:
cd backend && mvn clean install
cd frontend && npm ci && npm run build
```

### Docker Issues
```bash
docker-compose down -v              # Remove everything
docker-compose build --no-cache     # Rebuild
docker-compose up -d                # Start fresh
```

## 📞 Need More Help?

1. **Quick commands?** → [QUICK_REFERENCE.md](QUICK_REFERENCE.md)
2. **Setup issues?** → [DEVELOPMENT.md](DEVELOPMENT.md)
3. **Docker questions?** → [DOCKER.md](DOCKER.md)
4. **Full details?** → [README.md](README.md)
5. **Code comments** → Check source files (well documented)

## 📋 Checklist for First Run

- [ ] You can run `./start.sh prod` or `./start.sh dev`
- [ ] Frontend loads at http://localhost (or :4200)
- [ ] Backend API responds to http://localhost:8080/api/health
- [ ] Database is accessible
- [ ] You've read [QUICK_REFERENCE.md](QUICK_REFERENCE.md)
- [ ] You understand the project structure
- [ ] Tests pass when you run `./test.sh`

## 🎓 Learning Paths

### For Backend Developers
1. Start `./start.sh dev`
2. Edit `backend/src/main/java/com/boxoffice/controller/`
3. Run `mvn test` in backend folder
4. Changes auto-reload

### For Frontend Developers
1. Start `./start.sh dev`
2. Edit `frontend/src/app/`
3. Changes auto-refresh at :4200
4. Run `npm test` to verify

### For DevOps/Operations
1. Study `docker-compose.yml`
2. Review Dockerfiles
3. Try `docker-compose logs -f`
4. Read [DOCKER.md](DOCKER.md)

## 🚢 Ready to Deploy?

1. Build production images: `./build.sh`
2. Review [docker-compose.yml](docker-compose.yml)
3. Update environment variables
4. Deploy to your server
5. Monitor with `docker-compose logs -f`

---

## 🎉 You're All Set!

Start coding! Questions? Check the docs above.

**Happy coding!** 🚀

---

**Project Version**: 1.0.0  
**Created**: January 16, 2026  
**Status**: Production Ready  
**License**: MIT
