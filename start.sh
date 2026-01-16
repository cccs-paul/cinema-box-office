#!/bin/bash
# Start script for Cinema Box Office project
# Author: Box Office Team
# License: MIT

set -e

echo "========================================="
echo "Cinema Box Office - Start Script"
echo "========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Parse arguments
if [ "$1" = "dev" ]; then
    echo "Starting in development mode..."
    docker-compose -f docker-compose.dev.yml up -d
    echo ""
    echo -e "${GREEN}✓ Development environment started${NC}"
    echo ""
    echo "Services:"
    echo "  - Frontend (hot reload): http://localhost:4200"
    echo "  - API: http://localhost:8080/api"
    echo "  - API Health: http://localhost:8080/api/health"
    echo "  - Database: localhost:5432"
    echo ""
    echo "View logs with: docker-compose -f docker-compose.dev.yml logs -f"
elif [ "$1" = "prod" ]; then
    echo "Starting in production mode..."
    docker-compose up -d
    echo ""
    echo -e "${GREEN}✓ Production environment started${NC}"
    echo ""
    echo "Services:"
    echo "  - Frontend: http://localhost"
    echo "  - API: http://localhost:8080/api"
    echo "  - API Health: http://localhost:8080/api/health"
    echo ""
    echo "View logs with: docker-compose logs -f"
else
    echo "Usage: $0 [dev|prod]"
    echo ""
    echo "Examples:"
    echo "  $0 dev   - Start development environment with hot reload"
    echo "  $0 prod  - Start production environment"
    exit 1
fi
