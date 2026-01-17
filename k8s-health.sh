#!/bin/bash
#
# Kubernetes Health Check Script
# Monitors deployment health and provides diagnostics
#
# Usage: ./k8s-health.sh [--watch] [--interval 5]
#

set -e

NAMESPACE="cinema-box-office"
WATCH=false
INTERVAL=5

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --watch)
            WATCH=true
            shift
            ;;
        --interval)
            INTERVAL=$2
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Helper functions
check_pod_health() {
    local pod=$1
    local namespace=$2
    
    local status=$(kubectl get pod "$pod" -n "$namespace" -o jsonpath='{.status.phase}')
    local ready=$(kubectl get pod "$pod" -n "$namespace" -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}')
    
    if [ "$status" = "Running" ] && [ "$ready" = "True" ]; then
        echo -e "${GREEN}✓${NC}"
    elif [ "$status" = "Pending" ]; then
        echo -e "${YELLOW}⏳${NC}"
    else
        echo -e "${RED}✗${NC}"
    fi
}

check_deployment_health() {
    local deployment=$1
    local namespace=$2
    
    local replicas=$(kubectl get deployment "$deployment" -n "$namespace" -o jsonpath='{.status.replicas}')
    local ready=$(kubectl get deployment "$deployment" -n "$namespace" -o jsonpath='{.status.readyReplicas}')
    
    if [ "$replicas" = "$ready" ] && [ "$replicas" -gt 0 ]; then
        echo -e "${GREEN}✓${NC} ($ready/$replicas)"
    else
        echo -e "${RED}✗${NC} ($ready/$replicas)"
    fi
}

check_service_health() {
    local service=$1
    local namespace=$2
    
    local endpoints=$(kubectl get endpoints "$service" -n "$namespace" -o jsonpath='{.subsets[0].addresses[*].targetRef.name}' 2>/dev/null || echo "")
    
    if [ -z "$endpoints" ]; then
        echo -e "${RED}✗${NC} (no endpoints)"
    else
        echo -e "${GREEN}✓${NC}"
    fi
}

check_ingress_health() {
    local ingress=$1
    local namespace=$2
    
    local ip=$(kubectl get ingress "$ingress" -n "$namespace" -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")
    local hostname=$(kubectl get ingress "$ingress" -n "$namespace" -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "")
    
    if [ -z "$ip" ] && [ -z "$hostname" ]; then
        echo -e "${YELLOW}⏳${NC} (pending assignment)"
    else
        echo -e "${GREEN}✓${NC}"
    fi
}

get_pod_events() {
    local pod=$1
    local namespace=$2
    
    kubectl get events -n "$namespace" --field-selector involvedObject.name="$pod" --sort-by='.lastTimestamp' | tail -1
}

perform_health_check() {
    clear
    echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}Cinema Box Office - Kubernetes Health Check${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
    echo ""
    
    # Check namespace
    echo -e "${BLUE}Namespace:${NC} $NAMESPACE"
    if kubectl get namespace "$NAMESPACE" &> /dev/null; then
        echo -e "  Status: ${GREEN}✓ Exists${NC}"
    else
        echo -e "  Status: ${RED}✗ Not found${NC}"
        return 1
    fi
    echo ""
    
    # Check deployments
    echo -e "${BLUE}Deployments:${NC}"
    echo -n "  API:      "
    check_deployment_health "api" "$NAMESPACE"
    echo -n "  Frontend: "
    check_deployment_health "web" "$NAMESPACE"
    echo ""
    
    # Check services
    echo -e "${BLUE}Services:${NC}"
    echo -n "  API:      "
    check_service_health "api" "$NAMESPACE"
    echo -n "  Frontend: "
    check_service_health "web" "$NAMESPACE"
    echo -n "  Database: "
    check_service_health "postgres" "$NAMESPACE"
    echo ""
    
    # Check ingress
    echo -e "${BLUE}Ingress:${NC}"
    echo -n "  cinema-box-office: "
    check_ingress_health "cinema-box-office-ingress" "$NAMESPACE"
    echo ""
    
    # Check pods
    echo -e "${BLUE}Pods:${NC}"
    local pods=$(kubectl get pods -n "$NAMESPACE" -o jsonpath='{.items[*].metadata.name}')
    for pod in $pods; do
        local component=$(kubectl get pod "$pod" -n "$NAMESPACE" -o jsonpath='{.metadata.labels.component}')
        echo -n "  $component ($pod): "
        check_pod_health "$pod" "$NAMESPACE"
        
        # Show events if pod is not running
        local status=$(kubectl get pod "$pod" -n "$NAMESPACE" -o jsonpath='{.status.phase}')
        if [ "$status" != "Running" ]; then
            local event=$(get_pod_events "$pod" "$NAMESPACE")
            if [ -n "$event" ]; then
                echo "    Last event: $event"
            fi
        fi
    done
    echo ""
    
    # Check resource usage
    echo -e "${BLUE}Resource Usage:${NC}"
    if kubectl top nodes &> /dev/null; then
        echo "  Nodes:"
        kubectl top nodes | tail -n +2 | while read -r line; do
            echo "    $line"
        done
        echo ""
        
        echo "  Pods:"
        if kubectl top pod -n "$NAMESPACE" &> /dev/null; then
            kubectl top pod -n "$NAMESPACE" | tail -n +2 | while read -r line; do
                echo "    $line"
            done
        else
            echo "    (metrics not available)"
        fi
    else
        echo "  (metrics server not available)"
    fi
    echo ""
    
    # Check persistent volumes
    echo -e "${BLUE}Persistent Volumes:${NC}"
    local pvcs=$(kubectl get pvc -n "$NAMESPACE" -o jsonpath='{.items[*].metadata.name}' 2>/dev/null || echo "")
    if [ -z "$pvcs" ]; then
        echo "  (none)"
    else
        for pvc in $pvcs; do
            local status=$(kubectl get pvc "$pvc" -n "$NAMESPACE" -o jsonpath='{.status.phase}')
            echo "  $pvc: $status"
        done
    fi
    echo ""
    
    # Check configuration
    echo -e "${BLUE}Configuration:${NC}"
    echo "  ConfigMaps:"
    kubectl get configmap -n "$NAMESPACE" -o name 2>/dev/null | sed 's/^/    /'
    echo "  Secrets:"
    kubectl get secret -n "$NAMESPACE" -o name 2>/dev/null | grep -v default | sed 's/^/    /'
    echo ""
    
    # Summary
    echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
    local failed_pods=$(kubectl get pods -n "$NAMESPACE" --field-selector status.phase!=Running 2>/dev/null | wc -l)
    if [ "$failed_pods" -eq 0 ]; then
        echo -e "${GREEN}✓ All systems healthy${NC}"
    else
        echo -e "${YELLOW}⚠ Some issues detected${NC}"
    fi
    echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
    echo ""
}

# Main loop
if [ "$WATCH" = true ]; then
    while true; do
        perform_health_check
        echo "Refreshing in $INTERVAL seconds... (Press Ctrl+C to stop)"
        sleep "$INTERVAL"
    done
else
    perform_health_check
fi
