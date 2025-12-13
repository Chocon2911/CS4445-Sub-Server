#!/bin/bash

# Color codes for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080/api/v1"

echo -e "${BLUE}====================================${NC}"
echo -e "${BLUE}Server Control API Test${NC}"
echo -e "${BLUE}====================================${NC}\n"

echo -e "${GREEN}1. Checking initial status...${NC}"
curl -s $BASE_URL/server/status | jq '.'
echo -e "\n"

echo -e "${YELLOW}2. Sending packet while server is OPEN (should succeed)...${NC}"
curl -s -X POST $BASE_URL/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"before-close","cpuIntensity":3,"ramIntensity":3,"processingTimeMs":500}' | jq '.'
echo -e "\n"

echo -e "${RED}3. CLOSING the server...${NC}"
curl -s -X POST "$BASE_URL/server/close?reason=Testing" | jq '.'
echo -e "\n"

echo -e "${GREEN}4. Checking status (should be CLOSED)...${NC}"
curl -s $BASE_URL/server/status | jq '.'
echo -e "\n"

echo -e "${YELLOW}5. Trying to send packet while CLOSED (should be rejected)...${NC}"
curl -s -X POST $BASE_URL/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"during-close","cpuIntensity":3,"ramIntensity":3,"processingTimeMs":500}' | jq '.'
echo -e "\n"

echo -e "${GREEN}6. OPENING the server...${NC}"
curl -s -X POST "$BASE_URL/server/open?reason=Test+complete" | jq '.'
echo -e "\n"

echo -e "${GREEN}7. Checking status (should be OPEN)...${NC}"
curl -s $BASE_URL/server/status | jq '.'
echo -e "\n"

echo -e "${YELLOW}8. Sending packet after OPEN (should succeed)...${NC}"
curl -s -X POST $BASE_URL/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"after-open","cpuIntensity":3,"ramIntensity":3,"processingTimeMs":500}' | jq '.'
echo -e "\n"

echo -e "${BLUE}====================================${NC}"
echo -e "${GREEN}Test complete!${NC}"
echo -e "${BLUE}====================================${NC}"
