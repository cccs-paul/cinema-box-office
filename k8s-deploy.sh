#!/bin/bash
#
# Kubernetes Deployment Helper Script
# Simplifies common Kubernetes operations for myRC
#
# Usage: ./k8s-deploy.sh [command] [options]
#

set -e

NAMESPACE="myrc"
KUSTOMIZE_DIR="k8s"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
log_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

log_success() {
    echo -e "${GREEN}✓${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

log_error() {
    echo -e "${RED}✗${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl not found. Please install kubectl first."
        exit 1
    fi
    
    if ! command -v kustomize &> /dev/null; then
        log_error "kustomize not found. Please install kustomize first."
        exit 1
    fi
    
    # Check cluster connection
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot connect to Kubernetes cluster. Please configure kubectl."
        exit 1
    fi
    
    log_success "All prerequisites met"
}

# Install command
install() {
    log_info "Installing myRC to Kubernetes cluster..."
    
    # Create namespace if it doesn't exist
    log_info "Creating namespace: $NAMESPACE"
    kubectl apply -f "$KUSTOMIZE_DIR/namespace.yaml"
    
    # Apply Kustomize configuration
    log_info "Applying Kustomize configuration..."
    kubectl apply -k "$KUSTOMIZE_DIR"
    
    log_success "Installation complete"
    
    # Show deployment status
    show_status
}

# Update command
update() {
    local component=$1
    local image=$2
    
    if [ -z "$component" ] || [ -z "$image" ]; then
        log_error "Usage: $0 update <component> <image>"
        log_info "Example: $0 update api myrc-api:v1.1"
        exit 1
    fi
    
    log_info "Updating $component to $image..."
    kubectl set image deployment/$component \
        $component=$image \
        -n $NAMESPACE
    
    log_info "Monitoring rollout..."
    kubectl rollout status deployment/$component -n $NAMESPACE
    
    log_success "$component updated successfully"
}

# Rollback command
rollback() {
    local component=$1
    
    if [ -z "$component" ]; then
        log_error "Usage: $0 rollback <component>"
        exit 1
    fi
    
    log_info "Rolling back $component to previous version..."
    kubectl rollout undo deployment/$component -n $NAMESPACE
    
    kubectl rollout status deployment/$component -n $NAMESPACE
    log_success "$component rolled back successfully"
}

# Show status command
show_status() {
    log_info "Deployment Status:"
    echo ""
    
    log_info "Pods:"
    kubectl get pods -n $NAMESPACE
    echo ""
    
    log_info "Services:"
    kubectl get svc -n $NAMESPACE
    echo ""
    
    log_info "Ingress:"
    kubectl get ingress -n $NAMESPACE
    echo ""
    
    log_info "Horizontal Pod Autoscaler:"
    kubectl get hpa -n $NAMESPACE
}

# Show logs command
show_logs() {
    local component=$1
    
    if [ -z "$component" ]; then
        log_error "Usage: $0 logs <component>"
        log_info "Available components: api, web, postgres"
        exit 1
    fi
    
    log_info "Showing logs for $component..."
    kubectl logs -f deployment/$component -n $NAMESPACE
}

# Port forward command
port_forward() {
    local component=$1
    local local_port=$2
    local remote_port=$3
    
    if [ -z "$component" ] || [ -z "$local_port" ] || [ -z "$remote_port" ]; then
        log_error "Usage: $0 port-forward <component> <local-port> <remote-port>"
        log_info "Example: $0 port-forward api 8080 8080"
        exit 1
    fi
    
    log_info "Setting up port forward: localhost:$local_port -> $component:$remote_port"
    kubectl port-forward -n $NAMESPACE "svc/$component" "$local_port:$remote_port"
}

# Scale deployment command
scale() {
    local component=$1
    local replicas=$2
    
    if [ -z "$component" ] || [ -z "$replicas" ]; then
        log_error "Usage: $0 scale <component> <replicas>"
        log_info "Example: $0 scale api 5"
        exit 1
    fi
    
    log_info "Scaling $component to $replicas replicas..."
    kubectl scale deployment/$component --replicas=$replicas -n $NAMESPACE
    
    kubectl rollout status deployment/$component -n $NAMESPACE
    log_success "$component scaled successfully"
}

# Execute command in pod
exec_pod() {
    local component=$1
    shift
    local cmd=$@
    
    if [ -z "$component" ]; then
        log_error "Usage: $0 exec <component> <command>"
        log_info "Example: $0 exec postgres psql -U myrc -d myrc"
        exit 1
    fi
    
    # Get first pod of component
    local pod=$(kubectl get pod -n $NAMESPACE -l "app=$component" -o jsonpath='{.items[0].metadata.name}')
    
    if [ -z "$pod" ]; then
        log_error "No pods found for component: $component"
        exit 1
    fi
    
    log_info "Executing command in pod: $pod"
    kubectl exec -it "$pod" -n $NAMESPACE -- $cmd
}

# Backup database
backup_database() {
    log_info "Creating database backup..."
    
    local backup_file="myrc-backup-$(date +%Y%m%d-%H%M%S).sql.gz"
    
    # Get postgres pod
    local pod=$(kubectl get pod -n $NAMESPACE -l "component=database" -o jsonpath='{.items[0].metadata.name}')
    
    if [ -z "$pod" ]; then
        log_error "Database pod not found"
        exit 1
    fi
    
    # Create backup
    kubectl exec -n $NAMESPACE "$pod" -- \
        pg_dump -U myrc myrc | gzip > "$backup_file"
    
    log_success "Backup created: $backup_file"
}

# Restore database
restore_database() {
    local backup_file=$1
    
    if [ -z "$backup_file" ] || [ ! -f "$backup_file" ]; then
        log_error "Usage: $0 restore <backup-file>"
        exit 1
    fi
    
    log_warning "This will overwrite the current database. Continue? (yes/no)"
    read -r confirm
    
    if [ "$confirm" != "yes" ]; then
        log_info "Restore cancelled"
        exit 0
    fi
    
    log_info "Restoring database from: $backup_file"
    
    # Get postgres pod
    local pod=$(kubectl get pod -n $NAMESPACE -l "component=database" -o jsonpath='{.items[0].metadata.name}')
    
    if [ -z "$pod" ]; then
        log_error "Database pod not found"
        exit 1
    fi
    
    # Restore backup
    zcat "$backup_file" | kubectl exec -i -n $NAMESPACE "$pod" -- \
        psql -U myrc myrc
    
    log_success "Database restored successfully"
}

# Uninstall command
uninstall() {
    log_warning "This will delete all resources in namespace: $NAMESPACE"
    log_warning "Continue? (yes/no)"
    read -r confirm
    
    if [ "$confirm" != "yes" ]; then
        log_info "Uninstall cancelled"
        exit 0
    fi
    
    log_info "Uninstalling myRC..."
    kubectl delete namespace "$NAMESPACE"
    
    log_success "Uninstall complete"
}

# Show help
show_help() {
    cat << EOF
myRC Kubernetes Deployment Helper

Usage: $0 [command] [options]

Commands:
  install                    Install application to Kubernetes
  status                     Show deployment status
  logs <component>           Show component logs (api, web, postgres)
  port-forward <component> <local-port> <remote-port>
                             Forward local port to component
  update <component> <image> Update component to new image
  rollback <component>       Rollback component to previous version
  scale <component> <replicas>
                             Scale component to number of replicas
  exec <component> <cmd>     Execute command in component pod
  backup                     Backup database
  restore <backup-file>      Restore database from backup
  uninstall                  Remove all resources
  help                       Show this help message

Examples:
  $0 install
  $0 status
  $0 logs api
  $0 port-forward api 8080 8080
  $0 update api myrc-api:v1.1
  $0 scale api 5
  $0 exec postgres psql -U myrc -d myrc
  $0 backup
  $0 restore myrc-backup-20260117-120000.sql.gz

EOF
}

# Main script
main() {
    local command=$1
    
    # Check prerequisites first
    check_prerequisites
    
    case "$command" in
        install)
            install
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs "$2"
            ;;
        port-forward)
            port_forward "$2" "$3" "$4"
            ;;
        update)
            update "$2" "$3"
            ;;
        rollback)
            rollback "$2"
            ;;
        scale)
            scale "$2" "$3"
            ;;
        exec)
            shift
            exec_pod "$@"
            ;;
        backup)
            backup_database
            ;;
        restore)
            restore_database "$2"
            ;;
        uninstall)
            uninstall
            ;;
        help|--help|-h|"")
            show_help
            ;;
        *)
            log_error "Unknown command: $command"
            show_help
            exit 1
            ;;
    esac
}

main "$@"
