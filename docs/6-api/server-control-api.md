# Server Control API Documentation

## Overview

The server now includes control APIs that allow you to "open" and "close" the server without actually shutting it down. When the server is "closed", it will reject all fakePacket requests but remain running.

**Important**: This does NOT shut down the actual application or your computer - it's like putting a "Closed" sign on a shop while the shop itself is still there.

## New API Endpoints

### 1. Close the Server

**Endpoint**: `POST /api/v1/server/close`

**What it does**: Puts the server in "CLOSED" state. All fakePacket requests will be rejected.

**Request**:
```bash
# Basic close
curl -X POST http://localhost:8080/api/v1/server/close

# Close with reason
curl -X POST "http://localhost:8080/api/v1/server/close?reason=Maintenance"
```

**Response**:
```json
{
  "open": false,
  "status": "CLOSED",
  "lastStateChange": "2025-12-13T10:30:45.123",
  "reason": "Maintenance"
}
```

### 2. Open the Server

**Endpoint**: `POST /api/v1/server/open`

**What it does**: Puts the server back in "OPEN" state. It will accept fakePacket requests again.

**Request**:
```bash
# Basic open
curl -X POST http://localhost:8080/api/v1/server/open

# Open with reason
curl -X POST "http://localhost:8080/api/v1/server/open?reason=Maintenance+complete"
```

**Response**:
```json
{
  "open": true,
  "status": "OPEN",
  "lastStateChange": "2025-12-13T10:35:20.456",
  "reason": "Maintenance complete"
}
```

### 3. Check Server Status

**Endpoint**: `GET /api/v1/server/status`

**What it does**: Returns the current state of the server (OPEN or CLOSED)

**Request**:
```bash
curl http://localhost:8080/api/v1/server/status
```

**Response**:
```json
{
  "open": true,
  "status": "OPEN",
  "lastStateChange": "2025-12-13T10:35:20.456",
  "reason": "Maintenance complete"
}
```

## Behavior When Server is Closed

When the server is in CLOSED state:

### fakePacket Requests are REJECTED

**Request**:
```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"test","cpuIntensity":5,"ramIntensity":5}'
```

**Response** (HTTP 503 Service Unavailable):
```json
{
  "packetId": "test",
  "status": "REJECTED",
  "processingTimeMs": null,
  "cpuCycles": null,
  "memoryUsedBytes": null,
  "result": "Server is currently closed. Please open the server first using /api/v1/server/open",
  "timestamp": null
}
```

### Other Endpoints Still Work

- `/api/v1/health` - Still works ✅
- `/api/v1/server/status` - Still works ✅
- `/api/v1/server/open` - Still works ✅
- `/api/v1/server/close` - Still works ✅

## Use Cases

### 1. Simulating Downtime

Test how your application handles server downtime:

```bash
# Close the server
curl -X POST http://localhost:8080/api/v1/server/close?reason=Simulating+downtime

# Try to send packets (will be rejected)
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"test","cpuIntensity":5,"ramIntensity":5}'

# Open the server again
curl -X POST http://localhost:8080/api/v1/server/open?reason=Downtime+ended
```

### 2. Maintenance Mode

Put the server in maintenance mode:

```bash
# Enter maintenance mode
curl -X POST http://localhost:8080/api/v1/server/close?reason=Under+maintenance

# Check status
curl http://localhost:8080/api/v1/server/status

# Exit maintenance mode
curl -X POST http://localhost:8080/api/v1/server/open?reason=Maintenance+complete
```

### 3. Testing Client Error Handling

Test how client applications handle rejected requests:

```bash
# Close server
curl -X POST http://localhost:8080/api/v1/server/close

# Your client app tries to send requests
# It should receive 503 Service Unavailable and handle it gracefully
```

### 4. Controlled Load Testing

Control when the server accepts load:

```bash
# Close server first
curl -X POST http://localhost:8080/api/v1/server/close

# Prepare your load testing tools
# ...

# Open server and start load test at precise time
curl -X POST http://localhost:8080/api/v1/server/open
# Start load test immediately
```

## Complete Example Workflow

```bash
# 1. Check initial status
curl http://localhost:8080/api/v1/server/status
# Response: {"open": true, "status": "OPEN", ...}

# 2. Send a packet (should work)
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"before-close","cpuIntensity":5,"ramIntensity":5}'
# Response: {"status": "SUCCESS", ...}

# 3. Close the server
curl -X POST http://localhost:8080/api/v1/server/close?reason=Testing
# Response: {"open": false, "status": "CLOSED", ...}

# 4. Try to send a packet (will be rejected)
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"during-close","cpuIntensity":5,"ramIntensity":5}'
# Response: {"status": "REJECTED", "result": "Server is currently closed...", ...}

# 5. Check status
curl http://localhost:8080/api/v1/server/status
# Response: {"open": false, "status": "CLOSED", "reason": "Testing", ...}

# 6. Open the server
curl -X POST http://localhost:8080/api/v1/server/open?reason=Test+complete
# Response: {"open": true, "status": "OPEN", ...}

# 7. Send a packet again (should work now)
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"after-open","cpuIntensity":5,"ramIntensity":5}'
# Response: {"status": "SUCCESS", ...}
```

## Using Postman

### Close Server
1. Create new request: `POST http://localhost:8080/api/v1/server/close`
2. (Optional) Add query parameter: `reason` = `Your reason here`
3. Click Send

### Open Server
1. Create new request: `POST http://localhost:8080/api/v1/server/open`
2. (Optional) Add query parameter: `reason` = `Your reason here`
3. Click Send

### Check Status
1. Create new request: `GET http://localhost:8080/api/v1/server/status`
2. Click Send

## Windows Batch Script for Testing

Save as `test-server-control.bat`:

```batch
@echo off
echo ====================================
echo Server Control API Test
echo ====================================
echo.

echo 1. Checking initial status...
curl http://localhost:8080/api/v1/server/status
echo.
echo.

echo 2. Closing the server...
curl -X POST http://localhost:8080/api/v1/server/close?reason=Testing
echo.
echo.

echo 3. Trying to send packet (should be rejected)...
curl -X POST http://localhost:8080/api/v1/fakePacket -H "Content-Type: application/json" -d "{\"packetId\":\"test\",\"cpuIntensity\":5,\"ramIntensity\":5}"
echo.
echo.

echo 4. Opening the server...
curl -X POST http://localhost:8080/api/v1/server/open?reason=Test+complete
echo.
echo.

echo 5. Trying to send packet (should work now)...
curl -X POST http://localhost:8080/api/v1/fakePacket -H "Content-Type: application/json" -d "{\"packetId\":\"test\",\"cpuIntensity\":5,\"ramIntensity\":5}"
echo.
echo.

echo Test complete!
pause
```

## Linux/Mac Shell Script for Testing

Save as `test-server-control.sh`:

```bash
#!/bin/bash

echo "===================================="
echo "Server Control API Test"
echo "===================================="
echo

echo "1. Checking initial status..."
curl http://localhost:8080/api/v1/server/status
echo
echo

echo "2. Closing the server..."
curl -X POST "http://localhost:8080/api/v1/server/close?reason=Testing"
echo
echo

echo "3. Trying to send packet (should be rejected)..."
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"test","cpuIntensity":5,"ramIntensity":5}'
echo
echo

echo "4. Opening the server..."
curl -X POST "http://localhost:8080/api/v1/server/open?reason=Test+complete"
echo
echo

echo "5. Trying to send packet (should work now)..."
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"test","cpuIntensity":5,"ramIntensity":5}'
echo
echo

echo "Test complete!"
```

Make it executable:
```bash
chmod +x test-server-control.sh
```

## Response Status Codes

| Endpoint | Status when OPEN | Status when CLOSED |
|----------|------------------|-------------------|
| POST /api/v1/fakePacket | 200 OK | 503 Service Unavailable |
| GET /api/v1/health | 200 OK | 200 OK |
| POST /api/v1/server/open | 200 OK | 200 OK |
| POST /api/v1/server/close | 200 OK | 200 OK |
| GET /api/v1/server/status | 200 OK | 200 OK |

## Important Notes

1. **Not a Real Shutdown**: This does NOT actually shut down the server or your computer. The JVM and Spring Boot application continue running.

2. **Thread-Safe**: The state management uses `AtomicBoolean` so it's safe to call from multiple threads.

3. **Initial State**: The server starts in OPEN state by default.

4. **Logs**: All state changes are logged, so you can see them in the server console.

5. **Health Check**: The `/api/v1/health` endpoint always works, even when server is closed.

6. **Persistence**: The state is NOT persisted. If you restart the server, it will be OPEN again.

## Troubleshooting

**Q: I closed the server but it's still responding to fakePacket requests**
- A: Make sure you're using POST method, not GET
- Check the server logs to confirm the close request was received

**Q: How do I restart the server to OPEN state?**
- A: Call the `/api/v1/server/open` endpoint, or restart the application

**Q: Can I see the state in the logs?**
- A: Yes! Check your server console for log messages like "Server CLOSED at..." or "Server OPENED at..."

## Summary

You now have three new endpoints:
- ✅ `POST /api/v1/server/close` - Close the server (reject fakePacket requests)
- ✅ `POST /api/v1/server/open` - Open the server (accept fakePacket requests)
- ✅ `GET /api/v1/server/status` - Check if server is open or closed

Perfect for:
- Simulating downtime
- Maintenance mode
- Testing error handling
- Controlled load testing
- Educational demonstrations
