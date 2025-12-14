@echo off
REM Windows batch script for testing Server Control API

set BASE_URL=http://localhost:8080/api/v1

echo ====================================
echo Server Control API Test
echo ====================================
echo.

echo 1. Checking initial status...
curl -s %BASE_URL%/server/status
echo.
echo.

echo 2. Sending packet while server is OPEN (should succeed)...
curl -s -X POST %BASE_URL%/fakePacket ^
  -H "Content-Type: application/json" ^
  -d "{\"packetId\":\"before-close\",\"cpuIntensity\":3,\"ramIntensity\":3,\"processingTimeMs\":500}"
echo.
echo.

echo 3. CLOSING the server...
curl -s -X POST "%BASE_URL%/server/close?reason=Testing"
echo.
echo.

echo 4. Checking status (should be CLOSED)...
curl -s %BASE_URL%/server/status
echo.
echo.

echo 5. Trying to send packet while CLOSED (should be rejected)...
curl -s -X POST %BASE_URL%/fakePacket ^
  -H "Content-Type: application/json" ^
  -d "{\"packetId\":\"during-close\",\"cpuIntensity\":3,\"ramIntensity\":3,\"processingTimeMs\":500}"
echo.
echo.

echo 6. OPENING the server...
curl -s -X POST "%BASE_URL%/server/open?reason=Test+complete"
echo.
echo.

echo 7. Checking status (should be OPEN)...
curl -s %BASE_URL%/server/status
echo.
echo.

echo 8. Sending packet after OPEN (should succeed)...
curl -s -X POST %BASE_URL%/fakePacket ^
  -H "Content-Type: application/json" ^
  -d "{\"packetId\":\"after-open\",\"cpuIntensity\":3,\"ramIntensity\":3,\"processingTimeMs\":500}"
echo.
echo.

echo ====================================
echo Test complete!
echo ====================================
pause
