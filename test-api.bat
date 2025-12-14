@echo off
REM Windows batch script for testing the Fake Packet API

set BASE_URL=http://localhost:8080/api/v1

echo ================================
echo Fake Packet API Testing Script
echo ================================
echo.

REM Check if server is running
echo Checking server health...
curl -s %BASE_URL%/health
echo.
echo.

REM Test 1: Low Load
echo Test 1: Low Load (CPU: 2, RAM: 2, Time: 500ms)
curl -X POST %BASE_URL%/fakePacket ^
  -H "Content-Type: application/json" ^
  -d "{\"packetId\":\"test-low-001\",\"cpuIntensity\":2,\"ramIntensity\":2,\"processingTimeMs\":500,\"payload\":\"low load test\"}"
echo.
echo.

REM Test 2: Medium Load
echo Test 2: Medium Load (CPU: 5, RAM: 5, Time: 2000ms)
curl -X POST %BASE_URL%/fakePacket ^
  -H "Content-Type: application/json" ^
  -d "{\"packetId\":\"test-medium-001\",\"cpuIntensity\":5,\"ramIntensity\":5,\"processingTimeMs\":2000,\"payload\":\"medium load test\"}"
echo.
echo.

REM Test 3: High Load
echo Test 3: High Load (CPU: 10, RAM: 10, Time: 5000ms)
curl -X POST %BASE_URL%/fakePacket ^
  -H "Content-Type: application/json" ^
  -d "{\"packetId\":\"test-high-001\",\"cpuIntensity\":10,\"ramIntensity\":10,\"processingTimeMs\":5000,\"payload\":\"high load test\"}"
echo.
echo.

echo All tests completed!
pause
