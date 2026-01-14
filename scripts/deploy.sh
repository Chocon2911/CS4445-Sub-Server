#!/bin/bash

# Deploy script for CS4445 Sub-Server
# Usage: ./deploy.sh [server_number|all]

set -e

# Server configurations
SERVER_1="130.94.65.44"
SERVER_2="38.54.56.98"
SERVER_3="149.104.78.74"
SSH_USER="root"

# Docker config
IMAGE_NAME="cs4445-sub-server"
IMAGE_TAG="${IMAGE_TAG:-latest}"
CONTAINER_NAME="sub-server"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Build Docker image
build_image() {
    log_info "Building Docker image..."
    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
    docker save ${IMAGE_NAME}:${IMAGE_TAG} | gzip > /tmp/image.tar.gz
    log_info "Image built and saved to /tmp/image.tar.gz"
}

# Deploy to a single server
deploy_to_server() {
    local server_ip=$1
    local port=$2

    log_info "Deploying to ${server_ip}..."

    # Copy image to server
    scp /tmp/image.tar.gz ${SSH_USER}@${server_ip}:/tmp/

    # Load and run container
    ssh ${SSH_USER}@${server_ip} << EOF
        cd /tmp
        gunzip -c image.tar.gz | docker load
        docker stop ${CONTAINER_NAME} || true
        docker rm ${CONTAINER_NAME} || true
        docker run -d \
            --name ${CONTAINER_NAME} \
            --restart unless-stopped \
            -p ${port}:8080 \
            ${IMAGE_NAME}:${IMAGE_TAG}
        rm -f /tmp/image.tar.gz
        docker image prune -f
EOF

    log_info "Deployed to ${server_ip}:${port}"
}

# Main
main() {
    local target="${1:-all}"

    # Build first
    build_image

    case $target in
        1)
            deploy_to_server ${SERVER_1} 8081
            ;;
        2)
            deploy_to_server ${SERVER_2} 8082
            ;;
        3)
            deploy_to_server ${SERVER_3} 8083
            ;;
        all)
            deploy_to_server ${SERVER_1} 8081
            deploy_to_server ${SERVER_2} 8082
            deploy_to_server ${SERVER_3} 8083
            ;;
        *)
            log_error "Invalid target: ${target}"
            echo "Usage: $0 [1|2|3|all]"
            exit 1
            ;;
    esac

    # Cleanup
    rm -f /tmp/image.tar.gz
    log_info "Deployment completed!"
}

main "$@"
