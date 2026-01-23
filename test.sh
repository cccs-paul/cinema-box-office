#!/bin/bash
# Test script for myRC project
# Author: myRC Team
# License: MIT

set -e

echo "========================================="
echo "myRC - Test Script"
echo "========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test backend
echo "========================================="
echo "Testing Backend..."
echo "========================================="
cd backend
mvn clean test
BACKEND_TEST_RESULT=$?
cd ..

if [ $BACKEND_TEST_RESULT -eq 0 ]; then
    echo -e "${GREEN}✓ Backend tests passed${NC}"
else
    echo -e "${RED}✗ Backend tests failed${NC}"
    exit 1
fi
echo ""

# Test frontend
echo "========================================="
echo "Testing Frontend..."
echo "========================================="
cd frontend
npm install --quiet
npm test -- --watch=false --code-coverage
FRONTEND_TEST_RESULT=$?
cd ..

if [ $FRONTEND_TEST_RESULT -eq 0 ]; then
    echo -e "${GREEN}✓ Frontend tests passed${NC}"
else
    echo -e "${RED}✗ Frontend tests failed${NC}"
    exit 1
fi
echo ""

echo "========================================="
echo -e "${GREEN}All tests passed!${NC}"
echo "========================================="
