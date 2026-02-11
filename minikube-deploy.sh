#!/bin/bash
# =========================================
# myRC - Minikube Test Deployment Script
# =========================================
# Builds the application, loads Docker images into minikube,
# and deploys the full stack using the K8s manifests with a
# minikube-specific kustomize overlay (k8s-minikube/).
#
# The overlay adapts the production K8s manifests for local minikube:
#   - Reduces replicas to 1 (single-node cluster)
#   - Disables TLS/SSL redirect on ingress
#   - Uses myrc.local hostname instead of myrc.example.com
#   - Relaxes PDB and HPA settings for local dev
#   - Adjusts CORS for local access
#
# Prerequisites:
#   - minikube installed and running (minikube start)
#   - kubectl installed
#   - Docker installed (for building images)
#   - Java 25 JDK, Maven, Node.js (for building the app)
#
# Usage:
#   ./minikube-deploy.sh              # Full build + deploy
#   ./minikube-deploy.sh --skip-build # Deploy only (uses existing build artifacts)
#   ./minikube-deploy.sh --teardown   # Remove everything from minikube
#   ./minikube-deploy.sh --status     # Show deployment status
#
# Author: myRC Team
# License: MIT

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

NAMESPACE="myrc"
BACKEND_IMAGE="myrc-api:latest"
FRONTEND_IMAGE="myrc-web:latest"
OVERLAY_DIR="$SCRIPT_DIR/k8s-minikube"

# ─── Helper Functions ───────────────────────────────────────────────

print_header() {
    echo ""
    echo -e "${BLUE}=========================================${NC}"
    echo -e "${BOLD}$1${NC}"
    echo -e "${BLUE}=========================================${NC}"
}

print_step() {
    echo -e "${CYAN}▸ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# ─── Teardown ───────────────────────────────────────────────────────

teardown() {
    print_header "Tearing Down myRC from Minikube"

    if kubectl get namespace "$NAMESPACE" &>/dev/null; then
        print_step "Deleting namespace '$NAMESPACE' and all resources..."
        kubectl delete namespace "$NAMESPACE" --timeout=120s 2>/dev/null || true
        print_success "Namespace '$NAMESPACE' deleted"
    else
        print_warning "Namespace '$NAMESPACE' does not exist"
    fi

    print_step "Removing images from minikube..."
    minikube image rm "$BACKEND_IMAGE" 2>/dev/null || true
    minikube image rm "$FRONTEND_IMAGE" 2>/dev/null || true
    print_success "Images removed"

    echo ""
    print_success "Teardown complete"
    exit 0
}

# ─── Status ─────────────────────────────────────────────────────────

status() {
    print_header "myRC Minikube Deployment Status"

    if ! kubectl get namespace "$NAMESPACE" &>/dev/null; then
        print_error "Namespace '$NAMESPACE' does not exist. Application is not deployed."
        exit 1
    fi

    echo ""
    echo -e "${BOLD}Pods:${NC}"
    kubectl get pods -n "$NAMESPACE" -o wide
    echo ""
    echo -e "${BOLD}Services:${NC}"
    kubectl get svc -n "$NAMESPACE"
    echo ""
    echo -e "${BOLD}StatefulSets:${NC}"
    kubectl get statefulset -n "$NAMESPACE"
    echo ""
    echo -e "${BOLD}Deployments:${NC}"
    kubectl get deployments -n "$NAMESPACE"
    echo ""
    echo -e "${BOLD}Ingress:${NC}"
    kubectl get ingress -n "$NAMESPACE" 2>/dev/null || echo "  No ingress resources found"
    echo ""
    echo -e "${BOLD}PVCs:${NC}"
    kubectl get pvc -n "$NAMESPACE"
    echo ""

    # Show access URL
    MINIKUBE_IP=$(minikube ip 2>/dev/null || echo "unknown")
    echo -e "${BOLD}Access:${NC}"
    echo "  Minikube IP: $MINIKUBE_IP"
    echo "  Add to /etc/hosts: $MINIKUBE_IP myrc.local"
    echo "  Frontend: http://myrc.local"
    echo "  API: http://myrc.local/api"
    echo ""
    echo -e "${BOLD}Port-forward (alternative):${NC}"
    echo "  kubectl port-forward -n $NAMESPACE svc/frontend 8000:80"
    echo "  kubectl port-forward -n $NAMESPACE svc/api 8080:8080"
    exit 0
}

# ─── Parse Arguments ────────────────────────────────────────────────

SKIP_BUILD=false

for arg in "$@"; do
    case "$arg" in
        --skip-build)
            SKIP_BUILD=true
            ;;
        --teardown)
            teardown
            ;;
        --status)
            status
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --skip-build   Skip application build, use existing artifacts"
            echo "  --teardown     Remove all myRC resources from minikube"
            echo "  --status       Show current deployment status"
            echo "  --help, -h     Show this help message"
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $arg${NC}"
            echo "Use --help to see available options"
            exit 1
            ;;
    esac
done

# ─── Prerequisites Check ───────────────────────────────────────────

print_header "myRC - Minikube Test Deployment"

print_step "Checking prerequisites..."

MISSING=()

if ! command -v minikube &>/dev/null; then
    MISSING+=("minikube")
fi

if ! command -v kubectl &>/dev/null; then
    MISSING+=("kubectl")
fi

if ! command -v docker &>/dev/null; then
    MISSING+=("docker")
fi

if [ "$SKIP_BUILD" = false ]; then
    if ! command -v java &>/dev/null; then
        MISSING+=("java (JDK 25)")
    fi
    if ! command -v mvn &>/dev/null; then
        MISSING+=("maven")
    fi
    if ! command -v node &>/dev/null; then
        MISSING+=("node")
    fi
fi

if [ ${#MISSING[@]} -gt 0 ]; then
    print_error "Missing required tools: ${MISSING[*]}"
    exit 1
fi

# Verify overlay directory exists
if [ ! -d "$OVERLAY_DIR" ] || [ ! -f "$OVERLAY_DIR/kustomization.yaml" ]; then
    print_error "Minikube kustomize overlay not found at k8s-minikube/"
    print_error "This directory should be present in the repository."
    exit 1
fi

# Check minikube is running
if ! minikube status --format='{{.Host}}' 2>/dev/null | grep -q "Running"; then
    print_error "Minikube is not running. Start it with: minikube start"
    echo ""
    echo "  Recommended minikube settings for this application:"
    echo "    minikube start --cpus=4 --memory=8192 --disk-size=20g"
    exit 1
fi

print_success "All prerequisites met"

# ─── Build Application ─────────────────────────────────────────────

if [ "$SKIP_BUILD" = false ]; then
    print_header "Building Application"

    # Backend
    print_step "Building backend (Maven)..."
    cd backend
    mvn clean package -DskipTests -q
    cd ..
    print_success "Backend JAR built"

    # Frontend
    print_step "Building frontend (Angular)..."
    cd frontend
    npm install --silent 2>/dev/null
    npm run build --silent 2>/dev/null
    cd ..
    print_success "Frontend built"
else
    # Verify artifacts exist
    print_header "Verifying Build Artifacts"

    if ! ls backend/target/*.jar &>/dev/null; then
        print_error "No backend JAR found in backend/target/. Run without --skip-build first."
        exit 1
    fi
    if [ ! -d "frontend/dist/myrc/browser" ]; then
        print_error "No frontend build found in frontend/dist/myrc/browser/. Run without --skip-build first."
        exit 1
    fi
    print_success "Build artifacts found"
fi

# ─── Build Docker Images Inside Minikube ────────────────────────────

print_header "Building Docker Images in Minikube"

print_step "Switching to minikube's Docker daemon..."
eval $(minikube docker-env)

print_step "Building backend image ($BACKEND_IMAGE)..."
docker build -t "$BACKEND_IMAGE" -f backend/Dockerfile .
print_success "Backend image built"

print_step "Building frontend image ($FRONTEND_IMAGE)..."
docker build -t "$FRONTEND_IMAGE" -f frontend/Dockerfile .
print_success "Frontend image built"

# Switch back to host Docker daemon
eval $(minikube docker-env --unset)

# ─── Enable Required Minikube Addons ────────────────────────────────

print_header "Configuring Minikube Addons"

print_step "Enabling ingress addon..."
minikube addons enable ingress 2>/dev/null || true
print_success "Ingress addon enabled"

print_step "Enabling storage-provisioner addon..."
minikube addons enable storage-provisioner 2>/dev/null || true
print_success "Storage provisioner enabled"

print_step "Enabling metrics-server addon (for HPA)..."
minikube addons enable metrics-server 2>/dev/null || true
print_success "Metrics server enabled"

# ─── Deploy to Minikube ─────────────────────────────────────────────

print_header "Deploying to Minikube"

print_step "Validating kustomize overlay..."
if ! kubectl kustomize "$OVERLAY_DIR" >/dev/null 2>&1; then
    print_error "Kustomize overlay validation failed:"
    kubectl kustomize "$OVERLAY_DIR"
    exit 1
fi
print_success "Kustomize overlay is valid"

print_step "Applying K8s manifests (kustomize overlay: k8s-minikube/)..."
kubectl apply -k "$OVERLAY_DIR"
print_success "Manifests applied"

# ─── Wait for Pods ──────────────────────────────────────────────────

print_header "Waiting for Pods to be Ready"

print_step "Waiting for PostgreSQL..."
kubectl rollout status statefulset/postgres -n "$NAMESPACE" --timeout=180s
print_success "PostgreSQL is ready"

print_step "Waiting for backend API (this may take a while for Flyway migrations)..."
kubectl rollout status deployment/api -n "$NAMESPACE" --timeout=300s
print_success "Backend API is ready"

print_step "Waiting for frontend..."
kubectl rollout status deployment/frontend -n "$NAMESPACE" --timeout=120s
print_success "Frontend is ready"

# ─── Wait for Ingress ──────────────────────────────────────────────

print_step "Waiting for ingress controller to assign address..."
INGRESS_READY=false
for i in $(seq 1 30); do
    INGRESS_ADDR=$(kubectl get ingress myrc-ingress -n "$NAMESPACE" \
        -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || true)
    if [ -n "$INGRESS_ADDR" ]; then
        INGRESS_READY=true
        break
    fi
    sleep 2
done

if [ "$INGRESS_READY" = true ]; then
    print_success "Ingress address assigned: $INGRESS_ADDR"
else
    print_warning "Ingress address not yet assigned (this is normal — use minikube tunnel or port-forward)"
fi

# ─── Show Results ───────────────────────────────────────────────────

MINIKUBE_IP=$(minikube ip)

print_header "Deployment Complete!"

echo ""
echo -e "${BOLD}Pod Status:${NC}"
kubectl get pods -n "$NAMESPACE"
echo ""

echo -e "${BOLD}Services:${NC}"
kubectl get svc -n "$NAMESPACE"
echo ""

echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BOLD}  Access Information${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "  ${BOLD}Minikube IP:${NC} $MINIKUBE_IP"
echo ""
echo -e "  ${BOLD}Option 1 — Via Ingress (recommended):${NC}"
echo "    Add to /etc/hosts:"
echo -e "      ${CYAN}echo \"$MINIKUBE_IP myrc.local\" | sudo tee -a /etc/hosts${NC}"
echo ""
echo "    Then open:"
echo -e "      Frontend:  ${CYAN}http://myrc.local${NC}"
echo -e "      API:       ${CYAN}http://myrc.local/api${NC}"
echo ""
echo -e "  ${BOLD}Option 2 — Via Port-Forward:${NC}"
echo -e "      ${CYAN}kubectl port-forward -n $NAMESPACE svc/frontend 8000:80 &${NC}"
echo -e "      ${CYAN}kubectl port-forward -n $NAMESPACE svc/api 8080:8080 &${NC}"
echo ""
echo "    Then open:"
echo -e "      Frontend:  ${CYAN}http://localhost:8000${NC}"
echo -e "      API:       ${CYAN}http://localhost:8080/api${NC}"
echo ""
echo -e "  ${BOLD}Option 3 — Via Minikube Tunnel:${NC}"
echo -e "      ${CYAN}minikube tunnel${NC}  (in a separate terminal, requires sudo)"
echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${BOLD}Useful Commands:${NC}"
echo "  Status:        ./minikube-deploy.sh --status"
echo "  Teardown:      ./minikube-deploy.sh --teardown"
echo "  Redeploy:      ./minikube-deploy.sh --skip-build"
echo "  Logs (API):    kubectl logs -n $NAMESPACE -l component=backend -f"
echo "  Logs (Web):    kubectl logs -n $NAMESPACE -l component=frontend -f"
echo "  Logs (DB):     kubectl logs -n $NAMESPACE -l component=database -f"
echo "  Shell (API):   kubectl exec -it -n $NAMESPACE deploy/api -- /bin/bash"
echo "  Shell (DB):    kubectl exec -it -n $NAMESPACE statefulset/postgres -- psql -U boxoffice"
echo ""
