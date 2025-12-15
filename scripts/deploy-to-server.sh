#!/bin/bash

# Multi-Server Deployment Script
# Deploys the application to a single server

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
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
IMAGE_TAG=$4
ENVIRONMENT=$5
SSH_PORT=$6

# Validate arguments
if [ -z "$SERVER_IP" ] || [ -z "$DEPLOY_USER" ] || [ -z "$DEPLOY_PATH" ] || [ -z "$IMAGE_TAG" ]; then
    print_error "Usage: $0 <server_ip> <deploy_user> <deploy_path> <image_tag> [environment] [ssh_port]"
    print_info "Example: $0 192.168.1.100 deployer /app/cs4445-sub-server main staging 22"
    print_info "Example (CKey): $0 n1.ckey.vn root /app/cs4445-sub-server main staging 3494"
    exit 1
fi

ENVIRONMENT=${ENVIRONMENT:-production}
SSH_PORT=${SSH_PORT:-22}

# Detect if this is a CKey.com server and set appropriate compose file
if [[ "$SERVER_IP" == *"ckey.vn"* ]]; then
    COMPOSE_FILE="docker-compose.ckey.yml"
    print_info "Detected CKey.com server - using host networking"
else
    COMPOSE_FILE="docker-compose.prod.yml"
fi

print_info "Starting deployment to $SERVER_IP"
print_info "User: $DEPLOY_USER"
print_info "Path: $DEPLOY_PATH"
print_info "Image: $IMAGE_TAG"
print_info "Environment: $ENVIRONMENT"
print_info "SSH Port: $SSH_PORT"
print_info "Compose File: $COMPOSE_FILE"

# SSH configuration
SSH_KEY="${HOME}/.ssh/deploy_key"
SSH_OPTS="-p $SSH_PORT -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=ERROR"

if [ -f "$SSH_KEY" ]; then
    SSH_OPTS="$SSH_OPTS -i $SSH_KEY"
fi

# Function to run SSH commands
run_ssh() {
    ssh $SSH_OPTS "${DEPLOY_USER}@${SERVER_IP}" "$@"
}

# Step 1: Check server connectivity
print_info "Step 1/7: Testing connection to $SERVER_IP..."
if run_ssh "echo 'Connection successful'" &> /dev/null; then
    print_success "Connected to $SERVER_IP"
else
    print_error "Cannot connect to $SERVER_IP"
    exit 1
fi

# Step 2: Backup current deployment
print_info "Step 2/7: Creating backup..."
run_ssh "cd $DEPLOY_PATH && \
    if [ -f $COMPOSE_FILE ]; then \
        docker compose -f $COMPOSE_FILE ps > deployment-backup-\$(date +%Y%m%d-%H%M%S).txt; \
        echo 'Backup created'; \
    else \
        echo 'No existing deployment to backup'; \
    fi"
print_success "Backup completed"

# Step 3: Pull latest code and image
print_info "Step 3/7: Pulling latest code and Docker image..."

# Extract just the tag from the full image reference (e.g., "main" from "ghcr.io/repo:main")
# If IMAGE_TAG contains full image path, extract the tag; otherwise use as-is
TAG_ONLY=$(echo "$IMAGE_TAG" | grep -oP '(?<=:)[^:]+$' || echo "$IMAGE_TAG")

run_ssh "cd $DEPLOY_PATH && \
    git fetch origin && \
    git checkout main && \
    git pull origin main && \
    echo 'IMAGE_TAG=$TAG_ONLY' > .env.deploy && \
    docker compose -f $COMPOSE_FILE pull app"
print_success "Latest code and image pulled"

# Step 4: Stop old containers (graceful)
print_info "Step 4/7: Stopping old containers..."
run_ssh "cd $DEPLOY_PATH && \
    docker compose -f $COMPOSE_FILE stop app || true"
print_success "Old containers stopped"

# Step 5: Start new containers
print_info "Step 5/7: Starting new containers..."
run_ssh "cd $DEPLOY_PATH && \
    export IMAGE_TAG=$TAG_ONLY && \
    docker compose -f $COMPOSE_FILE up -d app && \
    docker compose -f $COMPOSE_FILE ps"
print_success "New containers started"

# Step 6: Wait for application to be ready
print_info "Step 6/7: Waiting for application to start..."
MAX_WAIT=60
WAITED=0
while [ $WAITED -lt $MAX_WAIT ]; do
    if run_ssh "docker logs \$(docker ps -q -f name=cs4445-app) 2>&1 | grep -q 'Started Cs4445SubServerApplication'"; then
        print_success "Application started successfully"
        break
    fi

    if [ $WAITED -eq $MAX_WAIT ]; then
        print_error "Application did not start within ${MAX_WAIT}s"
        print_info "Container logs:"
        run_ssh "docker logs \$(docker ps -q -f name=cs4445-app) --tail 50"
        exit 1
    fi

    echo -n "."
    sleep 2
    WAITED=$((WAITED + 2))
done
echo ""

# Step 7: Health check
print_info "Step 7/7: Running health check..."
sleep 5  # Give it a few more seconds

HEALTH_CHECK_RETRIES=5
HEALTH_CHECK_INTERVAL=3

for i in $(seq 1 $HEALTH_CHECK_RETRIES); do
    if curl -f -s --max-time 10 "http://${SERVER_IP}:8080/actuator/health" > /dev/null 2>&1; then
        print_success "Health check passed"
        break
    else
        if [ $i -eq $HEALTH_CHECK_RETRIES ]; then
            print_error "Health check failed after $HEALTH_CHECK_RETRIES attempts"
            print_info "Checking container status..."
            run_ssh "docker ps -a | grep cs4445"
            print_info "Recent logs:"
            run_ssh "docker logs \$(docker ps -q -f name=cs4445-app) --tail 30"
            exit 1
        else
            print_warning "Health check attempt $i/$HEALTH_CHECK_RETRIES failed, retrying..."
            sleep $HEALTH_CHECK_INTERVAL
        fi
    fi
done

# Clean up old images
print_info "Cleaning up old Docker images..."
run_ssh "docker image prune -f" || print_warning "Could not prune old images"

# Deployment summary
print_success "========================================="
print_success "Deployment to $SERVER_IP completed!"
print_success "========================================="
print_info "Server: $SERVER_IP"
print_info "Image: $IMAGE_TAG"
print_info "Environment: $ENVIRONMENT"
print_info "Health endpoint: http://${SERVER_IP}:8080/actuator/health"
print_info "API endpoint: http://${SERVER_IP}:8080/api/v1/fakePacket"
print_success "========================================="

exit 0
