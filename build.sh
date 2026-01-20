#!/bin/bash
# Build script for Cinema Box Office project
# Author: Box Office Team
# License: MIT

set -e

echo "========================================="
echo "Cinema Box Office - Build Script"
echo "========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
echo "Checking prerequisites..."

if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java 21 JDK is not installed${NC}"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed${NC}"
    exit 1
fi

if ! command -v node &> /dev/null; then
    echo -e "${RED}Error: Node.js is not installed${NC}"
    exit 1
fi

echo -e "${GREEN}✓ All prerequisites met${NC}"
echo ""

# Build backend
echo "========================================="
echo "Building Backend..."
echo "========================================="
cd backend
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Backend build successful${NC}"
else
    echo -e "${RED}✗ Backend build failed${NC}"
    exit 1
fi
cd ..
echo ""

# Build frontend
echo "========================================="
echo "Building Frontend..."
echo "========================================="
cd frontend
npm install --legacy-peer-deps
npm run build
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Frontend build successful${NC}"
else
    echo -e "${RED}✗ Frontend build failed${NC}"
    exit 1
fi
cd ..
echo ""

# Build Docker images
echo "========================================="
echo "Building Docker Images..."
echo "========================================="
if command -v docker &> /dev/null; then
    docker compose build
    echo -e "${GREEN}✓ Docker images built successfully${NC}"
else
    echo -e "${YELLOW}! Docker not found, skipping image build${NC}"
fi
echo ""

echo "========================================="
echo -e "${GREEN}Build completed successfully!${NC}"
echo "========================================="
echo ""
echo "Next steps:"
echo "  1. Run tests: ./test.sh"
echo "  2. Start with Docker: docker-compose up"
echo "  3. Or start locally:"
echo "     - Backend: cd backend && mvn spring-boot:run"
echo "     - Frontend: cd frontend && npm start"
