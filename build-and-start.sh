#!/bin/bash

################################################################################
# myRC - Build and Start Script
# Copyright (c) 2026 myRC Team
# Licensed under MIT License
#
# Description: Builds code locally and starts containers with docker-compose
# Usage: ./build-and-start.sh [dev|prod]
# Default: dev (development environment)
################################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ENVIRONMENT="${1:-dev}"
PROJECT_NAME="myrc"

# Validate environment parameter
if [ "$ENVIRONMENT" != "dev" ] && [ "$ENVIRONMENT" != "prod" ]; then
    echo -e "${RED}✗ Invalid environment: $ENVIRONMENT${NC}"
    echo "Usage: $0 [dev|prod]"
    exit 1
fi

echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║    myRC - Build and Start Script                  ║${NC}"
echo -e "${BLUE}║    Environment: ${ENVIRONMENT}                                              ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Step 0: Cleanup docker environment
docker system prune -a --volumes -f

# Step 1: Build Backend
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}[1/5] Building Backend with Maven...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

if [ ! -d "backend" ]; then
    echo -e "${RED}✗ backend directory not found${NC}"
    exit 1
fi

cd backend
echo -e "${GREEN}Running: mvn clean package -DskipTests${NC}"
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Backend build successful${NC}"
else
    echo -e "${RED}✗ Backend build failed${NC}"
    exit 1
fi
cd ..
echo ""

# Step 2: Build Frontend
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}[2/5] Building Frontend with npm...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

if [ ! -d "frontend" ]; then
    echo -e "${RED}✗ frontend directory not found${NC}"
    exit 1
fi

cd frontend
echo -e "${GREEN}Running: npm install${NC}"
npm install
echo -e "${GREEN}Running: npm run build${NC}"
npm run build
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Frontend build successful${NC}"
else
    echo -e "${RED}✗ Frontend build failed${NC}"
    exit 1
fi
cd ..
echo ""

# Step 3: Stop Existing Services
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}[3/5] Stopping Existing Services...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

if [ "$ENVIRONMENT" = "dev" ]; then
    COMPOSE_FILE="docker-compose.dev.yml"
else
    COMPOSE_FILE="docker-compose.yml"
fi

if [ ! -f "$COMPOSE_FILE" ]; then
    echo -e "${RED}✗ Docker compose file not found: $COMPOSE_FILE${NC}"
    exit 1
fi

# First, try to stop using docker compose for both possible configurations
echo -e "${GREEN}Stopping containers via docker compose...${NC}"

# Stop regular containers
docker compose down 2>/dev/null || true

# Stop dev containers  
docker compose --project-name "$PROJECT_NAME" -f "docker-compose.dev.yml" down 2>/dev/null || true

# Force remove all possible containers by name
ALL_CONTAINER_NAMES="myrc-db myrc-api myrc-pgadmin myrc-web myrc-db-dev myrc-api-dev myrc-pgadmin-dev myrc-web-dev"

echo -e "${GREEN}Ensuring no conflicting containers exist...${NC}"
for container in $ALL_CONTAINER_NAMES; do
    if docker ps -a --format '{{.Names}}' | grep -q "^${container}$"; then
        echo -e "${YELLOW}  Removing container: $container${NC}"
        docker rm -f "$container" 2>/dev/null || true
    fi
done

echo -e "${GREEN}✓ Environment cleaned${NC}"
echo ""

# Step 4: Remove and Rebuild Docker Images
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}[4/5] Removing Existing Images and Rebuilding...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Remove existing images
if [ "$ENVIRONMENT" = "dev" ]; then
    IMAGE_NAMES="myrc-api myrc-web"
else
    IMAGE_NAMES="myrc-api myrc-web"
fi

echo -e "${GREEN}Removing existing Docker images...${NC}"
for image in $IMAGE_NAMES; do
    if docker images -q "$image" 2>/dev/null | grep -q .; then
        echo -e "${YELLOW}  Removing image: $image${NC}"
        docker rmi -f "$image" 2>/dev/null || true
    fi
done

# Rebuild images
echo -e "${GREEN}Building fresh Docker images...${NC}"
docker compose -f "$COMPOSE_FILE" build --no-cache

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Images rebuilt successfully${NC}"
else
    echo -e "${RED}✗ Failed to rebuild images${NC}"
    exit 1
fi
echo ""

# Step 5: Start Services
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}[5/5] Starting Services...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

echo -e "${GREEN}Starting all services...${NC}"
docker compose --project-name "$PROJECT_NAME" -f "$COMPOSE_FILE" up -d

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Services started successfully${NC}"
else
    echo -e "${RED}✗ Failed to start services${NC}"
    exit 1
fi
echo ""

# Wait for services to be ready
echo -e "${YELLOW}Waiting for services to be ready...${NC}"
sleep 10

# Display container status
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Container Status:${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
docker compose -f "$COMPOSE_FILE" ps
echo ""

# Health checks
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Service Health Checks:${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Check API health
echo -n "API Health: "
API_RESPONSE=$(curl -s http://localhost:8080/api/health 2>&1 | grep -o '"status":"UP"' || true)
if [ -n "$API_RESPONSE" ]; then
    echo -e "${GREEN}✓ UP${NC}"
else
    echo -e "${YELLOW}⏳ Starting (may take a moment)${NC}"
fi

# Check Frontend
echo -n "Frontend: "
FRONTEND_RESPONSE=$(curl -s http://localhost:4200 2>&1 | grep -c "app-root" || true)
if [ "$FRONTEND_RESPONSE" -gt 0 ]; then
    echo -e "${GREEN}✓ Running${NC}"
else
    echo -e "${YELLOW}⏳ Starting (may take a moment)${NC}"
fi

# Check Database
echo -n "Database: "
DB_STATUS=$(docker compose -f "$COMPOSE_FILE" ps postgres 2>&1 | grep -c "healthy" || true)
if [ "$DB_STATUS" -gt 0 ]; then
    echo -e "${GREEN}✓ UP${NC}"
else
    echo -e "${YELLOW}⏳ Starting (may take a moment)${NC}"
fi

echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}✓ Build and Start Complete!${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}Access Points:${NC}"
if [ "$ENVIRONMENT" = "dev" ]; then
    echo -e "  Frontend (hot reload): ${BLUE}http://localhost:4200${NC}"
else
    echo -e "  Frontend: ${BLUE}http://localhost${NC}"
fi
echo -e "  API:       ${BLUE}http://localhost:8080/api${NC}"
echo -e "  pgAdmin:   ${BLUE}http://localhost:5050${NC} (if enabled)"
echo ""
echo -e "${YELLOW}Default Credentials:${NC}"
echo -e "  Username: ${BLUE}admin${NC}"
echo -e "  Password: ${BLUE}Admin@123${NC}"
echo ""
echo -e "${YELLOW}Database Console Credentials:${NC}"
echo -e "  Host:     ${BLUE}localhost:5432${NC}"
echo -e "  Database: ${BLUE}myrc${NC}"
echo -e "  User:     ${BLUE}myrc${NC}"
echo -e "  Password: ${BLUE}myrc_password${NC}"
echo ""
echo -e "${YELLOW}pgAdmin Credentials:${NC}"
echo -e "  URL:      ${BLUE}http://localhost:5050${NC}"
echo -e "  Email:    ${BLUE}admin@example.com${NC}"
echo -e "  Password: ${BLUE}admin_password${NC}"
echo ""
echo -e "${YELLOW}Useful Commands:${NC}"
echo -e "  View logs:     ${BLUE}docker compose -f $COMPOSE_FILE logs -f${NC}"
echo -e "  Stop services: ${BLUE}./stop.sh $ENVIRONMENT${NC}"
echo -e "  Restart:       ${BLUE}./start.sh $ENVIRONMENT${NC}"
echo ""
