#!/bin/bash

# Deployment script for CS4445 Sub Server
# Usage: ./deploy.sh [environment]
# Example: ./deploy.sh staging

set -e  # Exit on error

# Configuration
ENVIRONMENT=${1:-staging}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Main deployment process
main() {
    log_info "Starting deployment to $ENVIRONMENT"

    cd "$PROJECT_ROOT"

    # Check if .env file exists
    if [ ! -f .env ]; then
        log_warning ".env file not found, copying from .env.example"
        cp .env.example .env
        log_warning "Please update .env with your configuration"
        exit 1
    fi

    # Load environment variables
    export $(grep -v '^#' .env | xargs)

    # Backup current deployment
    log_info "Creating backup of current deployment"
    docker compose -f docker-compose.prod.yml ps > backup_$(date +%Y%m%d_%H%M%S).txt || true

    # Pull latest images
    log_info "Pulling latest Docker images"
    docker compose -f docker-compose.prod.yml pull

    # Stop old containers
    log_info "Stopping old containers"
    docker compose -f docker-compose.prod.yml down --remove-orphans

    # Start new containers
    log_info "Starting new containers"
    docker compose -f docker-compose.prod.yml up -d

    # Wait for services to be healthy
    log_info "Waiting for services to be healthy..."
    sleep 10

    # Health check
    log_info "Running health check"
    MAX_ATTEMPTS=30
    ATTEMPT=0

    while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
        if curl -f http://localhost:${APP_PORT:-8080}/actuator/health > /dev/null 2>&1; then
            log_success "Health check passed!"
            break
        fi
        ATTEMPT=$((ATTEMPT + 1))
        log_info "Health check attempt $ATTEMPT/$MAX_ATTEMPTS"
        sleep 2
    done

    if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
        log_error "Health check failed after $MAX_ATTEMPTS attempts"
        log_error "Rolling back deployment"
        docker compose -f docker-compose.prod.yml logs app
        docker compose -f docker-compose.prod.yml down
        exit 1
    fi

    # Show status
    log_info "Deployment status:"
    docker compose -f docker-compose.prod.yml ps

    # Show logs
    log_info "Recent logs:"
    docker compose -f docker-compose.prod.yml logs --tail=50 app

    log_success "Deployment to $ENVIRONMENT completed successfully!"
    log_info "Application is running at http://localhost:${APP_PORT:-8080}"
    log_info "Grafana is running at http://localhost:${GRAFANA_PORT:-3000}"
    log_info "Prometheus is running at http://localhost:${PROMETHEUS_PORT:-9090}"
}

# Run main function
main "$@"
