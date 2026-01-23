#!/bin/bash
# Stop script for myRC project
# Author: myRC Team
# License: MIT

set -e

echo "========================================="
echo "myRC - Stop Script"
echo "========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# Parse arguments
if [ "$1" = "dev" ]; then
    echo "Stopping development environment..."
    docker compose --project-name myrc -f docker-compose.dev.yml down
elif [ "$1" = "prod" ]; then
    echo "Stopping production environment..."
    docker compose --project-name myrc down
else
    echo "Usage: $0 [dev|prod]"
    exit 1
fi

echo -e "${GREEN}✓ Environment stopped${NC}"
