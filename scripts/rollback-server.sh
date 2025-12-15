#!/bin/bash

# Multi-Server Rollback Script
# Rolls back the application to the previous version on a single server

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Parse arguments
SERVER_IP=$1
DEPLOY_USER=$2
DEPLOY_PATH=$3
ROLLBACK_TAG=$4  # Optional - if not provided, uses 'previous' tag
SSH_PORT=$5      # Optional - defaults to 22

if [ -z "$SERVER_IP" ] || [ -z "$DEPLOY_USER" ] || [ -z "$DEPLOY_PATH" ]; then
    print_error "Usage: $0 <server_ip> <deploy_user> <deploy_path> [rollback_tag] [ssh_port]"
    print_info "Example: $0 192.168.1.100 deployer /app/cs4445-sub-server v1.0.0 22"
    print_info "Example (CKey): $0 n1.ckey.vn root /app/cs4445-sub-server v1.0.0 3494"
    exit 1
fi

ROLLBACK_TAG=${ROLLBACK_TAG:-previous}
SSH_PORT=${SSH_PORT:-22}

print_warning "========================================="
print_warning "Starting ROLLBACK on $SERVER_IP"
print_warning "========================================="
print_info "Target version: $ROLLBACK_TAG"
print_info "SSH Port: $SSH_PORT"

# SSH configuration
SSH_KEY="${HOME}/.ssh/deploy_key"
SSH_OPTS="-p $SSH_PORT -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=ERROR"

if [ -f "$SSH_KEY" ]; then
    SSH_OPTS="$SSH_OPTS -i $SSH_KEY"
fi

run_ssh() {
    ssh $SSH_OPTS "${DEPLOY_USER}@${SERVER_IP}" "$@"
}

# Step 1: Check connection
print_info "Step 1/5: Testing connection..."
if run_ssh "echo 'Connected'" &> /dev/null; then
    print_success "Connected to $SERVER_IP"
else
    print_error "Cannot connect to $SERVER_IP"
    exit 1
fi

# Step 2: Find previous version
print_info "Step 2/5: Finding previous version..."
if [ "$ROLLBACK_TAG" == "previous" ]; then
    PREVIOUS_IMAGE=$(run_ssh "docker images --format '{{.Repository}}:{{.Tag}}' | grep cs4445-sub-server | grep -v latest | head -n 1")

    if [ -z "$PREVIOUS_IMAGE" ]; then
        print_error "No previous image found"
        print_info "Available images:"
        run_ssh "docker images | grep cs4445-sub-server"
        exit 1
    fi

    print_info "Found previous image: $PREVIOUS_IMAGE"
else
    PREVIOUS_IMAGE="ghcr.io/YOUR_USERNAME/cs4445-sub-server:$ROLLBACK_TAG"
    print_info "Using specified image: $PREVIOUS_IMAGE"
fi

# Step 3: Stop current deployment
print_info "Step 3/5: Stopping current deployment..."
run_ssh "cd $DEPLOY_PATH && \
    docker compose -f docker-compose.prod.yml down app || true"
print_success "Current deployment stopped"

# Step 4: Pull and start previous version
print_info "Step 4/5: Starting previous version..."
run_ssh "cd $DEPLOY_PATH && \
    docker pull $PREVIOUS_IMAGE || echo 'Image already exists locally' && \
    export IMAGE_TAG=$ROLLBACK_TAG && \
    docker compose -f docker-compose.prod.yml up -d app"
print_success "Previous version started"

# Step 5: Health check
print_info "Step 5/5: Verifying rollback..."
sleep 10

MAX_RETRIES=10
for i in $(seq 1 $MAX_RETRIES); do
    if curl -f -s --max-time 10 "http://${SERVER_IP}:8080/actuator/health" > /dev/null 2>&1; then
        print_success "Rollback successful - application is healthy"
        break
    else
        if [ $i -eq $MAX_RETRIES ]; then
            print_error "Rollback verification failed"
            print_info "Container status:"
            run_ssh "docker ps -a | grep cs4445"
            print_info "Logs:"
            run_ssh "docker logs \$(docker ps -q -f name=cs4445-app) --tail 50"
            exit 1
        else
            echo -n "."
            sleep 3
        fi
    fi
done
echo ""

print_success "========================================="
print_success "Rollback completed on $SERVER_IP"
print_success "========================================="
print_info "Rolled back to: $PREVIOUS_IMAGE"
print_success "========================================="

exit 0
