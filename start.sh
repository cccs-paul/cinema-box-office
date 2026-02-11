#!/bin/bash
# Start script for myRC project
# Author: myRC Team
# License: MIT

set -e

echo "========================================="
echo "myRC - Start Script"
echo "========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Parse arguments
if [ "$1" = "dev" ]; then
    echo "Starting in development mode..."
    docker compose --project-name myrc -f docker-compose.dev.yml up -d
    echo ""
    echo -e "${GREEN}✓ Development environment started${NC}"
    echo ""
    echo "Services:"
    echo "  - Frontend (hot reload): http://localhost:4200"
    echo "  - API: http://localhost:8080/api"
    echo "  - API Health: http://localhost:8080/api/actuator/health"
    echo "  - Database: localhost:5432"
    echo ""
    echo "Database Console Credentials:"
    echo "  - Host:     localhost:5432"
    echo "  - Database: myrc"
    echo "  - User:     myrc"
    echo "  - Password: myrc_password"
    echo ""
    echo "pgAdmin Credentials:"
    echo "  - URL:      http://localhost:5050"
    echo "  - Email:    admin@example.com"
    echo "  - Password: admin_password"
    echo ""
    echo "View logs with: docker compose -f docker-compose.dev.yml logs -f"
elif [ "$1" = "prod" ]; then
    echo "Starting in production mode..."
    docker compose --project-name myrc up -d
    echo ""
    echo -e "${GREEN}✓ Production environment started${NC}"
    echo ""
    echo "Services:"
    echo "  - Frontend: http://localhost"
    echo "  - API: http://localhost:8080/api"
    echo "  - API Health: http://localhost:8080/api/actuator/health"
    echo ""
    echo "Database Console Credentials:"
    echo "  - Host:     localhost:5432"
    echo "  - Database: myrc"
    echo "  - User:     myrc"
    echo "  - Password: myrc_password"
    echo ""
    echo "pgAdmin Credentials:"
    echo "  - URL:      http://localhost:5050"
    echo "  - Email:    admin@example.com"
    echo "  - Password: admin_password"
    echo ""
    echo "View logs with: docker compose logs -f"
elif [ "$1" = "testldap" ]; then
    echo "Starting in Test LDAP mode (Planet Express)..."
    docker compose --project-name myrc-testldap -f docker-compose.testldap.yml up -d
    echo ""
    echo -e "${GREEN}✓ Test LDAP environment started${NC}"
    echo ""
    echo -e "${YELLOW}Services:${NC}"
    echo "  - Frontend (hot reload): http://localhost:4200"
    echo "  - API:                   http://localhost:8080/api"
    echo "  - API Health:            http://localhost:8080/api/actuator/health"
    echo "  - OpenLDAP:              ldap://localhost:10389"
    echo "  - Database:              localhost:5432"
    echo "  - pgAdmin:               http://localhost:5050"
    echo ""
    echo -e "${CYAN}LDAP Test Users (Planet Express / Futurama):${NC}"
    echo "  Admin Users (admin_staff group):"
    echo "    professor / professor  (Hubert J. Farnsworth)"
    echo "    hermes / hermes        (Hermes Conrad)"
    echo "  Regular Users (ship_crew group):"
    echo "    fry / fry              (Philip J. Fry)"
    echo "    leela / leela          (Turanga Leela)"
    echo "    bender / bender        (Bender B. Rodriguez)"
    echo "  Other Users (no group):"
    echo "    zoidberg / zoidberg    (John A. Zoidberg)"
    echo "    amy / amy              (Amy Wong)"
    echo ""
    echo "View logs with: docker compose --project-name myrc-testldap -f docker-compose.testldap.yml logs -f"
else
    echo "Usage: $0 [dev|prod|testldap]"
    echo ""
    echo "Examples:"
    echo "  $0 dev       - Start development environment with hot reload"
    echo "  $0 prod      - Start production environment"
    echo "  $0 testldap  - Start LDAP test environment (Planet Express users)"
    exit 1
fi
