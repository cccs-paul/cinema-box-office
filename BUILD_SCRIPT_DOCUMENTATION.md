# Build and Start Script Documentation

## Overview

The `build-and-start.sh` script provides automated orchestration for building Docker images and managing containers in the myRC application. It streamlines the development workflow by combining image building, container management, and health verification in a single command.

**Version:** 1.0  
**Last Updated:** January 17, 2025  
**Location:** `/home/paul/git/myrc/build-and-start.sh`

## Quick Start

```bash
# Make script executable (if not already)
chmod +x build-and-start.sh

# Run with default development environment
./build-and-start.sh

# Run with production environment
./build-and-start.sh prod

# Run development environment explicitly
./build-and-start.sh dev
```

## Features

### 1. Docker Image Building
- **Backend**: Builds Java Spring Boot API (OpenJDK 25, Maven)
- **Frontend**: Builds Angular application (Node.js, npm)
- Uses multi-stage builds for optimized image sizes
- Caches dependencies for faster subsequent builds

### 2. Container Management
- Stops existing running containers gracefully
- Starts containers using docker-compose
- Supports both development and production environments
- Preserves data volumes during restarts

### 3. Health Verification
- **API Health**: Checks `/api/health` endpoint on port 8080
- **Frontend**: Verifies Angular app is serving on port 4200
- **Database**: Confirms PostgreSQL health check passes
- Retry logic with exponential backoff
- Timeout protection

### 4. Visual Feedback
- Color-coded output (Red/Green/Yellow/Blue)
- Progress indicators (✓/✗)
- Divider lines for readability
- Status displays with ASCII formatting

## Usage

### Basic Execution

```bash
./build-and-start.sh [ENVIRONMENT]
```

**Parameters:**
- `ENVIRONMENT` (optional): `dev` (default) or `prod`

### Execution Flow

The script performs 4 main steps:

#### Step 1: Build Backend Docker Image
- Compiles Java source code with Maven
- Creates optimized runtime image
- Tags as `myrc-api:latest`

```
[1/4] Building Backend Docker Image...
```

#### Step 2: Build Frontend Docker Image
- Builds Angular application
- Optimizes assets and bundles
- Tags as `myrc-web:latest`

```
[2/4] Building Frontend Docker Image...
```

#### Step 3: Stop Running Containers
- Gracefully stops existing containers
- Preserves volumes and data
- Waits for clean shutdown

```
[3/4] Stopping Running Containers...
```

#### Step 4: Start Containers
- Launches containers via docker-compose
- Applies environment configuration
- Starts dependency services (PostgreSQL, pgAdmin)

```
[4/4] Starting Containers...
```

## Configuration

### Environment Files

The script uses environment-specific docker-compose files:

- **Development**: `docker-compose.dev.yml`
- **Production**: `docker-compose.yml` (via `prod` parameter)

### Build Context

Both backend and frontend images are built from the repository root:

```bash
# Backend: Finds backend/Dockerfile and backend/pom.xml
docker build -f backend/Dockerfile -t myrc-api:latest .

# Frontend: Finds frontend/Dockerfile and frontend/src/
docker build -f frontend/Dockerfile -t myrc-web:latest .
```

This ensures:
- Both images have access to root-level `pom.xml`
- Proper dependency resolution
- Correct relative path resolution in Dockerfiles

## Output Example

```
╔════════════════════════════════════════════════════════════════╗
║    myRC - Build and Start Script                  ║
║    Environment: dev                                              ║
╚════════════════════════════════════════════════════════════════╝

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[1/4] Building Backend Docker Image...
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Building backend Docker image from root context...
...build output...
✓ Backend Docker image built successfully

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[2/4] Building Frontend Docker Image...
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Building frontend Docker image from root context...
...build output...
✓ Frontend Docker image built successfully

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[3/4] Stopping Running Containers...
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Stopping containers...
✓ Containers stopped successfully

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[4/4] Starting Containers...
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
...container startup...
✓ Containers started successfully

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔍 Verifying Services...

✓ API is healthy
✓ Frontend is responding
✓ Database is healthy

✓ All services are ready!

╔════════════════════════════════════════════════════════════════╗
║              🎬 System Ready                                    ║
╚════════════════════════════════════════════════════════════════╝

📍 Access Points:
  • API:      http://localhost:8080
  • Frontend: http://localhost:4200
  • pgAdmin:  http://localhost:5050

🔑 Database Credentials:
  • Host: localhost:5432
  • User: myrc
  • DB:   myrc

📝 Useful Commands:
  • View logs:     docker-compose -f docker-compose.dev.yml logs -f
  • Stop services: docker-compose -f docker-compose.dev.yml stop
  • Run tests:     ./test.sh
  • View status:   docker-compose -f docker-compose.dev.yml ps
```

## Troubleshooting

### Docker Build Fails

**Problem**: `COPY failed: file not found in build context`

**Solution**: Ensure you're running the script from the repository root:
```bash
cd /home/paul/git/myrc
./build-and-start.sh
```

### Port Already in Use

**Problem**: `Error response from daemon: Ports are not available`

**Solution**: Stop existing containers:
```bash
docker-compose stop
# or
docker kill myrc-api-dev myrc-web-dev
```

### Health Checks Timeout

**Problem**: Services timeout during health verification

**Solution**: Wait for containers to be ready and check logs:
```bash
docker-compose logs -f api
docker-compose logs -f web
```

### Docker Daemon Not Running

**Problem**: `Cannot connect to Docker daemon`

**Solution**: Start Docker:
```bash
# Linux
sudo systemctl start docker

# Or use Docker Desktop GUI
```

## Advanced Usage

### Build Only (Skip Container Management)

To rebuild images without stopping/starting containers:
```bash
docker build -f backend/Dockerfile -t myrc-api:latest .
docker build -f frontend/Dockerfile -t myrc-web:latest .
```

### View Build Output

Redirect to file for later review:
```bash
./build-and-start.sh dev > build.log 2>&1
cat build.log
```

### Parallel Builds (Manual)

Build images in separate terminals:
```bash
# Terminal 1
docker build -f backend/Dockerfile -t myrc-api:latest .

# Terminal 2
docker build -f frontend/Dockerfile -t myrc-web:latest .
```

## Performance Notes

- **First Build**: 10-15 minutes (downloads dependencies)
- **Subsequent Builds**: 2-5 minutes (uses Docker layer cache)
- **Rebuild without code changes**: <30 seconds (pure cache)

## Related Scripts

- [start.sh](start.sh): Starts services without rebuilding
- [stop.sh](stop.sh): Stops all running services
- [test.sh](test.sh): Runs test suite

## Script Improvements

Future enhancements could include:

- [ ] Option to skip backend/frontend builds individually
- [ ] Custom tag support
- [ ] Registry push capability
- [ ] Multi-platform builds (arm64, amd64)
- [ ] Build statistics and timing
- [ ] Interactive environment selection

## Error Codes

| Code | Meaning |
|------|---------|
| 0 | Success |
| 1 | Build failure or missing Dockerfile |
| 130 | User interrupted (Ctrl+C) |
| 143 | Terminated by system signal |

## File Structure Reference

```
/home/paul/git/myrc/
├── build-and-start.sh          ← This script
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/
│       └── test/
├── frontend/
│   ├── Dockerfile
│   ├── package.json
│   └── src/
├── docker-compose.dev.yml
├── docker-compose.yml
└── pom.xml (root)
```

## License

Copyright (c) 2025 myRC Team  
Licensed under MIT License

## Support

For issues or questions:
1. Check [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)
2. Review [DEVELOPMENT.md](DEVELOPMENT.md)
3. Check script logs for detailed error messages
4. Verify Docker and docker-compose installation
