#!/bin/bash

# Health check script
# Usage: ./health-check.sh [host] [port]

HOST=${1:-localhost}
PORT=${2:-8080}

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

check_endpoint() {
    local endpoint=$1
    local url="http://$HOST:$PORT$endpoint"

    if curl -f -s "$url" > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} $endpoint"
        return 0
    else
        echo -e "${RED}✗${NC} $endpoint"
        return 1
    fi
}

check_json_endpoint() {
    local endpoint=$1
    local url="http://$HOST:$PORT$endpoint"

    response=$(curl -f -s "$url")
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓${NC} $endpoint"
        echo "$response" | jq '.' 2>/dev/null || echo "$response"
        return 0
    else
        echo -e "${RED}✗${NC} $endpoint"
        return 1
    fi
}

echo "Running health checks on $HOST:$PORT"
echo "========================================"

# Basic health check
echo -e "\n${YELLOW}1. Application Health${NC}"
check_json_endpoint "/actuator/health"

# Server status
echo -e "\n${YELLOW}2. Server Status${NC}"
check_json_endpoint "/api/v1/server/status"

# Metrics endpoint
echo -e "\n${YELLOW}3. Metrics Endpoint${NC}"
check_endpoint "/actuator/prometheus"

# Info endpoint
echo -e "\n${YELLOW}4. Application Info${NC}"
check_json_endpoint "/actuator/info"

echo -e "\n========================================"
echo "Health check completed"
