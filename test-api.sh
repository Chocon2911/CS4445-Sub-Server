#!/bin/bash

# Color codes for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080/api/v1"

echo -e "${BLUE}================================${NC}"
echo -e "${BLUE}Fake Packet API Testing Script${NC}"
echo -e "${BLUE}================================${NC}\n"

# Check if server is running
echo -e "${YELLOW}Checking server health...${NC}"
curl -s $BASE_URL/health
echo -e "\n"

# Test 1: Low Load
echo -e "${GREEN}Test 1: Low Load (CPU: 2, RAM: 2, Time: 500ms)${NC}"
curl -X POST $BASE_URL/fakePacket \
  -H "Content-Type: application/json" \
  -d '{
    "packetId": "test-low-001",
    "cpuIntensity": 2,
    "ramIntensity": 2,
    "processingTimeMs": 500,
    "payload": "low load test"
  }' | jq '.'
echo -e "\n"

# Test 2: Medium Load
echo -e "${GREEN}Test 2: Medium Load (CPU: 5, RAM: 5, Time: 2000ms)${NC}"
curl -X POST $BASE_URL/fakePacket \
  -H "Content-Type: application/json" \
  -d '{
    "packetId": "test-medium-001",
    "cpuIntensity": 5,
    "ramIntensity": 5,
    "processingTimeMs": 2000,
    "payload": "medium load test"
  }' | jq '.'
echo -e "\n"

# Test 3: High Load
echo -e "${GREEN}Test 3: High Load (CPU: 10, RAM: 10, Time: 5000ms)${NC}"
curl -X POST $BASE_URL/fakePacket \
  -H "Content-Type: application/json" \
  -d '{
    "packetId": "test-high-001",
    "cpuIntensity": 10,
    "ramIntensity": 10,
    "processingTimeMs": 5000,
    "payload": "high load test"
  }' | jq '.'
echo -e "\n"

# Test 4: Stress Test - Multiple Concurrent Requests
echo -e "${RED}Test 4: Stress Test - 5 Concurrent Requests${NC}"
echo -e "${YELLOW}This will generate significant CPU and RAM load...${NC}\n"

for i in {1..5}; do
  (
    curl -X POST $BASE_URL/fakePacket \
      -H "Content-Type: application/json" \
      -d "{
        \"packetId\": \"stress-test-$i\",
        \"cpuIntensity\": 8,
        \"ramIntensity\": 8,
        \"processingTimeMs\": 3000,
        \"payload\": \"stress test $i\"
      }" | jq ". + {testId: \"stress-test-$i\"}"
  ) &
done

wait
echo -e "\n${GREEN}All tests completed!${NC}"
