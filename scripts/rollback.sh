#!/bin/bash

# Rollback script for CS4445 Sub Server
# Usage: ./rollback.sh [image-tag]
# Example: ./rollback.sh v1.0.0

set -e

# Configuration
PREVIOUS_TAG=${1:-previous}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${YELLOW}[ROLLBACK]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

main() {
    cd "$PROJECT_ROOT"

    log_info "Starting rollback to version: $PREVIOUS_TAG"

    # Load environment
    export $(grep -v '^#' .env | xargs) || true

    # Update IMAGE_TAG in .env
    log_info "Updating image tag to $PREVIOUS_TAG"
    sed -i "s/IMAGE_TAG=.*/IMAGE_TAG=$PREVIOUS_TAG/" .env || \
    sed -i '' "s/IMAGE_TAG=.*/IMAGE_TAG=$PREVIOUS_TAG/" .env

    # Pull the previous image
    log_info "Pulling previous image"
    docker compose -f docker-compose.prod.yml pull app

    # Stop current containers
    log_info "Stopping current containers"
    docker compose -f docker-compose.prod.yml down

    # Start with previous image
    log_info "Starting services with previous image"
    docker compose -f docker-compose.prod.yml up -d

    # Wait and health check
    sleep 15

    log_info "Running health check"
    if curl -f http://localhost:${APP_PORT:-8080}/actuator/health > /dev/null 2>&1; then
        log_success "Rollback successful! Application is healthy."
    else
        log_error "Rollback failed - health check did not pass"
        docker compose -f docker-compose.prod.yml logs app
        exit 1
    fi

    docker compose -f docker-compose.prod.yml ps

    log_success "Rollback completed successfully!"
}

main "$@"
