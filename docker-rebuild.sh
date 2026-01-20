#!/bin/bash
#
# Docker Image Rebuild Script
# Cinema Box Office - Rebuild all Docker images
#
# Author: Box Office Team
# License: MIT
# Date: January 17, 2026
#
# Description:
#   Rebuilds all Docker images for the Cinema Box Office project.
#   This script rebuilds both the backend API and frontend web images
#   with the latest code changes.
#
# Usage:
#   ./docker-rebuild.sh              # Rebuild all images for development
#   ./docker-rebuild.sh prod         # Rebuild all images for production
#   ./docker-rebuild.sh --no-cache   # Rebuild with no cache
#   ./docker-rebuild.sh --help       # Show this help message
#

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to display help
show_help() {
    echo ""
    echo "Docker Image Rebuild Script"
    echo "=============================="
    echo ""
    echo "Usage: ./docker-rebuild.sh [OPTIONS] [ENVIRONMENT]"
    echo ""
    echo "Options:"
    echo "  --no-cache       Build images without using cache"
    echo "  --help           Show this help message"
    echo ""
    echo "Environment:"
    echo "  dev              Build development images (default)"
    echo "  prod             Build production images"
    echo ""
    echo "Examples:"
    echo "  ./docker-rebuild.sh              # Rebuild dev images"
    echo "  ./docker-rebuild.sh prod         # Rebuild production images"
    echo "  ./docker-rebuild.sh --no-cache   # Rebuild dev images without cache"
    echo ""
}

# Configuration
ENVIRONMENT="${1:-dev}"
NO_CACHE=""
DOCKER_COMPOSE_FILE="docker-compose.dev.yml"

# Parse command line arguments
for arg in "$@"; do
    case $arg in
        --no-cache)
            NO_CACHE="--no-cache"
            shift
            ;;
        --help)
            show_help
            exit 0
            ;;
        prod|production)
            ENVIRONMENT="prod"
            DOCKER_COMPOSE_FILE="docker-compose.yml"
            shift
            ;;
        dev|development)
            ENVIRONMENT="dev"
            DOCKER_COMPOSE_FILE="docker-compose.dev.yml"
            shift
            ;;
    esac
done

echo ""
echo -e "${BLUE}========================================="
echo "Cinema Box Office - Docker Rebuild"
echo "==========================================${NC}"
echo ""

# Verify Docker is available
echo "Checking prerequisites..."
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker is not installed${NC}"
    exit 1
fi

if ! command -v docker &> /dev/null || ! docker compose version &> /dev/null; then
    echo -e "${RED}Error: Docker Compose is not available${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker is installed${NC}"
echo -e "${GREEN}✓ Docker Compose is available${NC}"
echo ""

# Determine compose file
if [ ! -f "$DOCKER_COMPOSE_FILE" ]; then
    echo -e "${RED}Error: File not found: $DOCKER_COMPOSE_FILE${NC}"
    exit 1
fi

echo -e "${BLUE}Environment:${NC} $ENVIRONMENT"
echo -e "${BLUE}Compose File:${NC} $DOCKER_COMPOSE_FILE"
if [ -n "$NO_CACHE" ]; then
    echo -e "${BLUE}Build Option:${NC} No Cache"
fi
echo ""

# Build images
echo -e "${YELLOW}Building Docker images...${NC}"
echo ""

if [ -n "$NO_CACHE" ]; then
    docker compose -f "$DOCKER_COMPOSE_FILE" build $NO_CACHE
else
    docker compose -f "$DOCKER_COMPOSE_FILE" build
fi

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✓ Docker images built successfully${NC}"
    echo ""
    
    # Display built images
    echo -e "${BLUE}Built Images:${NC}"
    docker images | grep cinema-box-office | awk '{print "  " $1 ":" $2 " (" $7 " " $8 " " $9 ")"}'
    echo ""
    
    # Show usage instructions
    echo -e "${BLUE}Next Steps:${NC}"
    if [ "$ENVIRONMENT" = "dev" ]; then
        echo "  1. Start services: docker compose -f docker-compose.dev.yml up"
        echo "  2. Frontend:       http://localhost:4200"
        echo "  3. Backend API:    http://localhost:8080/api"
        echo "  4. API Swagger:    http://localhost:8080/api/swagger-ui.html"
        echo "  5. pgAdmin:        http://localhost:5050"
    else
        echo "  1. Start services: docker compose up"
        echo "  2. Services will be available on configured ports"
    fi
    echo ""
    
    exit 0
else
    echo ""
    echo -e "${RED}✗ Docker build failed${NC}"
    exit 1
fi
