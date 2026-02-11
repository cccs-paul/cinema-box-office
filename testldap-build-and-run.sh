#!/bin/bash

################################################################################
# myRC - Test LDAP Build and Run Script
# Copyright (c) 2026 myRC Team
# Licensed under MIT License
#
# Description: Builds code locally and starts containers configured for
#              LDAP-only authentication using the docker-test-openldap image.
#              Uses Futurama / Planet Express themed test users.
#
# Usage: ./testldap-build-and-run.sh
#
# Documentation: docs/TEST_LDAP_DEPLOYMENT.md
# LDAP Image:    https://github.com/rroemhild/docker-test-openldap
################################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="myrc-testldap"
COMPOSE_FILE="docker-compose.testldap.yml"

echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║    myRC - Test LDAP Build and Run Script                      ║${NC}"
echo -e "${BLUE}║    Authentication: LDAP only (Planet Express)                 ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Validate compose file exists
if [ ! -f "$COMPOSE_FILE" ]; then
    echo -e "${RED}✗ Docker compose file not found: $COMPOSE_FILE${NC}"
    echo -e "${RED}  Run this script from the project root directory.${NC}"
    exit 1
fi

# ============================================================================
# Step 0: Docker Cleanup
# ============================================================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}[0/5] Cleaning up Docker environment...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

docker system prune -a --volumes -f

echo -e "${GREEN}✓ Docker environment cleaned${NC}"
echo ""

# ============================================================================
# Step 1: Build Backend
# ============================================================================
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

# ============================================================================
# Step 2: Build Frontend
# ============================================================================
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

# ============================================================================
# Step 3: Stop Existing Services
# ============================================================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}[3/5] Stopping Existing Services...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Stop test LDAP containers
echo -e "${GREEN}Stopping test LDAP containers via docker compose...${NC}"
docker compose --project-name "$PROJECT_NAME" -f "$COMPOSE_FILE" down 2>/dev/null || true

# Also stop regular and dev containers to avoid port conflicts
docker compose down 2>/dev/null || true
docker compose --project-name "myrc" -f "docker-compose.dev.yml" down 2>/dev/null || true

# Force remove all possible containers by name (test LDAP + regular + dev)
ALL_CONTAINER_NAMES="myrc-testldap-openldap myrc-testldap-db myrc-testldap-api myrc-testldap-web myrc-testldap-pgadmin myrc-db myrc-api myrc-pgadmin myrc-web myrc-db-dev myrc-api-dev myrc-pgadmin-dev myrc-web-dev"

echo -e "${GREEN}Ensuring no conflicting containers exist...${NC}"
for container in $ALL_CONTAINER_NAMES; do
    if docker ps -a --format '{{.Names}}' | grep -q "^${container}$"; then
        echo -e "${YELLOW}  Removing container: $container${NC}"
        docker rm -f "$container" 2>/dev/null || true
    fi
done

echo -e "${GREEN}✓ Environment cleaned${NC}"
echo ""

# ============================================================================
# Step 4: Remove and Rebuild Docker Images
# ============================================================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}[4/5] Removing Existing Images and Rebuilding...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Remove existing application images
IMAGE_NAMES="myrc-testldap-api myrc-testldap-web myrc-api myrc-web"

echo -e "${GREEN}Removing existing Docker images...${NC}"
for image in $IMAGE_NAMES; do
    if docker images -q "$image" 2>/dev/null | grep -q .; then
        echo -e "${YELLOW}  Removing image: $image${NC}"
        docker rmi -f "$image" 2>/dev/null || true
    fi
done

# Rebuild images
echo -e "${GREEN}Building fresh Docker images...${NC}"
docker compose --project-name "$PROJECT_NAME" -f "$COMPOSE_FILE" build --no-cache

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Images rebuilt successfully${NC}"
else
    echo -e "${RED}✗ Failed to rebuild images${NC}"
    exit 1
fi
echo ""

# ============================================================================
# Step 5: Start Services
# ============================================================================
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
sleep 15

# ============================================================================
# Container Status
# ============================================================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Container Status:${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
docker compose --project-name "$PROJECT_NAME" -f "$COMPOSE_FILE" ps
echo ""

# ============================================================================
# Health Checks
# ============================================================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Service Health Checks:${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Check OpenLDAP
echo -n "OpenLDAP: "
LDAP_RESPONSE=$(docker exec myrc-testldap-openldap ldapsearch -H ldap://localhost:10389 -x -b "dc=planetexpress,dc=com" -D "cn=admin,dc=planetexpress,dc=com" -w GoodNewsEveryone "(objectClass=organization)" dn 2>/dev/null | grep -c "dn:" || true)
if [ "$LDAP_RESPONSE" -gt 0 ]; then
    echo -e "${GREEN}✓ UP (dc=planetexpress,dc=com)${NC}"
else
    echo -e "${YELLOW}⏳ Starting (may take a moment)${NC}"
fi

# Check API health
echo -n "API Health: "
API_RESPONSE=$(curl -s http://localhost:8080/api/actuator/health/liveness 2>&1 | grep -o '"status":"UP"' || true)
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
DB_STATUS=$(docker compose --project-name "$PROJECT_NAME" -f "$COMPOSE_FILE" ps postgres 2>&1 | grep -c "healthy" || true)
if [ "$DB_STATUS" -gt 0 ]; then
    echo -e "${GREEN}✓ UP${NC}"
else
    echo -e "${YELLOW}⏳ Starting (may take a moment)${NC}"
fi

# Check Login Methods endpoint
echo -n "Login Methods: "
LOGIN_METHODS=$(curl -s http://localhost:8080/api/auth/login-methods 2>&1 || true)
LDAP_ENABLED=$(echo "$LOGIN_METHODS" | grep -o '"ldapEnabled":true' || true)
APP_DISABLED=$(echo "$LOGIN_METHODS" | grep -o '"appAccount":{"enabled":false' || true)
if [ -n "$LDAP_ENABLED" ]; then
    echo -e "${GREEN}✓ LDAP enabled${NC}"
else
    echo -e "${YELLOW}⏳ Waiting for API (may take a moment)${NC}"
fi

echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}✓ Test LDAP Build and Start Complete!${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}Access Points:${NC}"
echo -e "  Frontend: ${BLUE}http://localhost:4200${NC}"
echo -e "  API:      ${BLUE}http://localhost:8080/api${NC}"
echo -e "  pgAdmin:  ${BLUE}http://localhost:5050${NC}"
echo -e "  OpenLDAP: ${BLUE}ldap://localhost:10389${NC}"
echo ""
echo -e "${CYAN}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║  LDAP Test Users (Planet Express / Futurama)                  ║${NC}"
echo -e "${CYAN}╠════════════════════════════════════════════════════════════════╣${NC}"
echo -e "${CYAN}║                                                              ║${NC}"
echo -e "${CYAN}║  ${YELLOW}ADMIN Users${CYAN} (admin_staff group):                            ║${NC}"
echo -e "${CYAN}║    professor / professor  ${GREEN}(Hubert J. Farnsworth - Owner)${CYAN}    ║${NC}"
echo -e "${CYAN}║    hermes / hermes        ${GREEN}(Hermes Conrad - Bureaucrat)${CYAN}      ║${NC}"
echo -e "${CYAN}║                                                              ║${NC}"
echo -e "${CYAN}║  ${YELLOW}Regular Users${CYAN} (ship_crew group):                            ║${NC}"
echo -e "${CYAN}║    fry / fry              ${GREEN}(Philip J. Fry - Delivery boy)${CYAN}    ║${NC}"
echo -e "${CYAN}║    leela / leela          ${GREEN}(Turanga Leela - Captain)${CYAN}         ║${NC}"
echo -e "${CYAN}║    bender / bender        ${GREEN}(Bender B. Rodriguez - Robot)${CYAN}     ║${NC}"
echo -e "${CYAN}║                                                              ║${NC}"
echo -e "${CYAN}║  ${YELLOW}Other Users${CYAN} (no group):                                     ║${NC}"
echo -e "${CYAN}║    zoidberg / zoidberg    ${GREEN}(John A. Zoidberg - Doctor)${CYAN}       ║${NC}"
echo -e "${CYAN}║    amy / amy              ${GREEN}(Amy Wong - Intern)${CYAN}               ║${NC}"
echo -e "${CYAN}║                                                              ║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}LDAP Configuration:${NC}"
echo -e "  Base DN:    ${BLUE}dc=planetexpress,dc=com${NC}"
echo -e "  Admin DN:   ${BLUE}cn=admin,dc=planetexpress,dc=com${NC}"
echo -e "  Admin Pass: ${BLUE}GoodNewsEveryone${NC}"
echo -e "  Users OU:   ${BLUE}ou=people,dc=planetexpress,dc=com${NC}"
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
echo -e "  View logs:        ${BLUE}docker compose --project-name $PROJECT_NAME -f $COMPOSE_FILE logs -f${NC}"
echo -e "  View API logs:    ${BLUE}docker compose --project-name $PROJECT_NAME -f $COMPOSE_FILE logs -f api${NC}"
echo -e "  View LDAP logs:   ${BLUE}docker compose --project-name $PROJECT_NAME -f $COMPOSE_FILE logs -f openldap${NC}"
echo -e "  Stop services:    ${BLUE}./stop.sh testldap${NC}"
echo -e "  List LDAP users:  ${BLUE}ldapsearch -H ldap://localhost:10389 -x -b 'ou=people,dc=planetexpress,dc=com' -D 'cn=admin,dc=planetexpress,dc=com' -w GoodNewsEveryone '(objectClass=inetOrgPerson)' uid cn mail${NC}"
echo ""
echo -e "${YELLOW}Documentation:${NC}"
echo -e "  See ${BLUE}docs/TEST_LDAP_DEPLOYMENT.md${NC} for full details"
echo ""
