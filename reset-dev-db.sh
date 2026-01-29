#!/bin/bash
# reset-dev-db.sh - Reset development database to start fresh
# This script stops all dev containers, removes the database volume, and restarts everything

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=========================================="
echo "  myRC Development Database Reset Script"
echo "=========================================="
echo ""

# Check if docker compose is available
if command -v docker &> /dev/null; then
    COMPOSE_CMD="docker compose"
else
    echo "Error: Docker is not installed or not in PATH"
    exit 1
fi

echo "⚠️  WARNING: This will DELETE all data in the development database!"
echo ""
read -p "Are you sure you want to continue? (y/N): " confirm
if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
    echo "Aborted."
    exit 0
fi

echo ""
echo "🛑 Stopping all dev containers..."
$COMPOSE_CMD -f docker-compose.dev.yml down --remove-orphans 2>/dev/null || true

# Also stop any manually started containers
docker stop myrc-api-dev myrc-web-dev myrc-db-dev myrc-pgadmin-dev 2>/dev/null || true
docker rm myrc-api-dev myrc-web-dev myrc-db-dev myrc-pgadmin-dev 2>/dev/null || true

echo ""
echo "🗑️  Removing database volume..."
# Try multiple possible volume names (depends on directory name when compose was first run)
docker volume rm postgres_data_dev 2>/dev/null || true
docker volume rm cinema-box-office_postgres_data_dev 2>/dev/null || true
docker volume rm myrc_postgres_data_dev 2>/dev/null || true

# List and remove any volumes that match the pattern
for vol in $(docker volume ls -q | grep -E "(postgres_data_dev|myrc.*postgres)" 2>/dev/null); do
    echo "   Removing volume: $vol"
    docker volume rm "$vol" 2>/dev/null || true
done

echo ""
echo "🧹 Cleaning up any orphan networks..."
docker network rm cinema-box-office_myrc-network-dev 2>/dev/null || true
docker network rm myrc_myrc-network-dev 2>/dev/null || true

echo ""
echo "✅ Database reset complete!"
echo ""
echo "To start fresh, run:"
echo "  ./build-and-start.sh dev"
echo ""
echo "Or manually with:"
echo "  docker compose -f docker-compose.dev.yml up -d"
echo ""
