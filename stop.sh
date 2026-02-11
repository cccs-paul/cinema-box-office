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
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Parse arguments
if [ "$1" = "dev" ]; then
    echo "Stopping development environment..."
    docker compose --project-name myrc -f docker-compose.dev.yml down
    echo -e "${GREEN}✓ Development environment stopped${NC}"
elif [ "$1" = "prod" ]; then
    echo "Stopping production environment..."
    docker compose --project-name myrc down
    echo -e "${GREEN}✓ Production environment stopped${NC}"
elif [ "$1" = "testldap" ]; then
    echo "Stopping Test LDAP environment..."
    docker compose --project-name myrc-testldap -f docker-compose.testldap.yml down
    echo -e "${GREEN}✓ Test LDAP environment stopped${NC}"
elif [ "$1" = "all" ]; then
    echo "Stopping ALL myRC environments..."
    echo ""
    echo -e "${YELLOW}Stopping development containers...${NC}"
    docker compose --project-name myrc -f docker-compose.dev.yml down 2>/dev/null || true
    echo -e "${YELLOW}Stopping production containers...${NC}"
    docker compose --project-name myrc down 2>/dev/null || true
    echo -e "${YELLOW}Stopping Test LDAP containers...${NC}"
    docker compose --project-name myrc-testldap -f docker-compose.testldap.yml down 2>/dev/null || true
    echo ""
    echo -e "${GREEN}✓ All environments stopped${NC}"
else
    echo "Usage: $0 [dev|prod|testldap|all]"
    echo ""
    echo "Examples:"
    echo "  $0 dev       - Stop development environment"
    echo "  $0 prod      - Stop production environment"
    echo "  $0 testldap  - Stop LDAP test environment"
    echo "  $0 all       - Stop ALL running environments"
    exit 1
fi
