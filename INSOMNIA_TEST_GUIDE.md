# Insomnia API Test Guide for CS4445 Subscription Server

## Quick Start

**Base URL:** `http://localhost:8080`

**Important:** This application does NOT use authentication by default. You can send requests directly without Basic Auth credentials.

## Prerequisites

1. Ensure Docker services are running:
   ```bash
   docker compose up -d
   ```

2. Verify the application is running:
   ```bash
   docker compose ps
   ```
   All services should show "healthy" or "running" status.

---

## API Endpoints Reference

### 1. Health Check

**Method:** `GET`
**Endpoint:** `/api/v1/health`

**Expected Response (200 OK):**
```
Server is running
```

**Test in Insomnia:**
- Create a new GET request
- URL: `http://localhost:8080/api/v1/health`
- Click Send

---

### 2. Get Server Status

**Method:** `GET`
**Endpoint:** `/api/v1/server/status`

**Expected Response (200 OK):**
```json
{
  "open": true,
  "status": "OPEN",
  "reason": "Server started",
  "lastStateChange": "2025-12-20T01:22:53.465"
}
```

**Test in Insomnia:**
- Create a new GET request
- URL: `http://localhost:8080/api/v1/server/status`
- Click Send

---

### 3. Process Fake Packet (Normal Request)

**Method:** `POST`
**Endpoint:** `/api/v1/fakePacket`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "packetId": "test-packet-001",
  "cpuIntensity": 5,
  "ramIntensity": 5,
  "processingTimeMs": 1000,
  "payload": "test data"
}
```

**Expected Response (200 OK):**
```json
{
  "packetId": "test-packet-001",
  "status": "SUCCESS",
  "processingTimeMs": 1500,
  "cpuCycles": 150000,
  "memoryUsedBytes": 52428800,
  "result": "Packet processed. Total cycles for this packet ID: 150000, Logs count: 1",
  "timestamp": "2025-12-20T01:23:00.000"
}
```

**Test in Insomnia:**
1. Create a new POST request
2. URL: `http://localhost:8080/api/v1/fakePacket`
3. Set Body type to "JSON"
4. Paste the request body above
5. Click Send

**Parameters Explained:**
- `packetId`: Unique identifier for the packet (string)
- `cpuIntensity`: CPU load level 1-10 (higher = more CPU work)
- `ramIntensity`: RAM load level 1-10 (higher = more memory allocation)
- `processingTimeMs`: Minimum processing time in milliseconds
- `payload`: Optional data payload (string)

---

### 4. Process Packet (Minimal Request)

**Request Body:**
```json
{
  "packetId": "minimal-001"
}
```

**Expected Behavior:**
- Uses default values: cpuIntensity=5, ramIntensity=5, processingTimeMs=1000
- Should return SUCCESS status

---

### 5. Process Packet (High Intensity)

**Request Body:**
```json
{
  "packetId": "high-load-001",
  "cpuIntensity": 10,
  "ramIntensity": 10,
  "processingTimeMs": 2000
}
```

**Expected Behavior:**
- Maximum CPU and RAM intensive operations
- Longer processing time (>2 seconds)
- Higher cpuCycles and memoryUsedBytes values

---

### 6. Process Packet (Low Intensity)

**Request Body:**
```json
{
  "packetId": "low-load-001",
  "cpuIntensity": 1,
  "ramIntensity": 1,
  "processingTimeMs": 100
}
```

**Expected Behavior:**
- Minimal CPU and RAM operations
- Faster processing time (~100ms)
- Lower resource usage values

---

### 7. Close Server

**Method:** `POST`
**Endpoint:** `/api/v1/server/close?reason=Maintenance`

**Expected Response (200 OK):**
```json
{
  "open": false,
  "status": "CLOSED",
  "reason": "Maintenance",
  "lastStateChange": "2025-12-20T01:25:00.000"
}
```

**Test in Insomnia:**
- Create a new POST request
- URL: `http://localhost:8080/api/v1/server/close`
- Add Query Parameter: `reason` = `Maintenance`
- Click Send

---

### 8. Process Packet When Server Closed

**Method:** `POST`
**Endpoint:** `/api/v1/fakePacket`

**Request Body:** (same as test #3)

**Expected Response (503 Service Unavailable):**
```json
{
  "packetId": "test-packet-001",
  "status": "REJECTED",
  "result": "Server is currently closed. Please open the server first using /api/v1/server/open",
  "processingTimeMs": null,
  "cpuCycles": null,
  "memoryUsedBytes": null,
  "timestamp": null
}
```

---

### 9. Open Server

**Method:** `POST`
**Endpoint:** `/api/v1/server/open?reason=Maintenance complete`

**Expected Response (200 OK):**
```json
{
  "open": true,
  "status": "OPEN",
  "reason": "Maintenance complete",
  "lastStateChange": "2025-12-20T01:26:00.000"
}
```

**Test in Insomnia:**
- Create a new POST request
- URL: `http://localhost:8080/api/v1/server/open`
- Add Query Parameter: `reason` = `Maintenance complete`
- Click Send

---

## Edge Case Tests

### 10. Null Packet ID

**Request Body:**
```json
{
  "packetId": null,
  "cpuIntensity": 5,
  "ramIntensity": 5
}
```

**Expected Behavior:**
- Should auto-generate UUID-based packet ID (format: `packet-<uuid>`)
- Returns SUCCESS status

---

### 11. Empty Packet ID

**Request Body:**
```json
{
  "packetId": "",
  "cpuIntensity": 5,
  "ramIntensity": 5
}
```

**Expected Behavior:**
- Should auto-generate UUID-based packet ID
- Returns SUCCESS status

---

### 12. Extreme CPU Intensity

**Request Body:**
```json
{
  "packetId": "extreme-cpu",
  "cpuIntensity": 999999,
  "ramIntensity": 5,
  "processingTimeMs": 100
}
```

**Expected Behavior:**
- Should clamp cpuIntensity to maximum value (10)
- Processing time will be longer due to high CPU work

---

### 13. Negative Values

**Request Body:**
```json
{
  "packetId": "negative-test",
  "cpuIntensity": -10,
  "ramIntensity": -5,
  "processingTimeMs": 1000
}
```

**Expected Behavior:**
- Should clamp values to minimum (1)
- Returns SUCCESS status

---

### 14. Zero Values

**Request Body:**
```json
{
  "packetId": "zero-test",
  "cpuIntensity": 0,
  "ramIntensity": 0,
  "processingTimeMs": 0
}
```

**Expected Behavior:**
- Should clamp intensities to minimum (1)
- Processing might complete very quickly

---

### 15. Special Characters in Packet ID

**Request Body:**
```json
{
  "packetId": "test-!@#$%^&*()_+-=[]{}|;:,.<>?",
  "cpuIntensity": 3,
  "ramIntensity": 3
}
```

**Expected Behavior:**
- Should accept special characters
- Returns SUCCESS status

---

### 16. Unicode Characters in Packet ID

**Request Body:**
```json
{
  "packetId": "测试-テスト-테스트-🚀🔥💻",
  "cpuIntensity": 3,
  "ramIntensity": 3
}
```

**Expected Behavior:**
- Should accept Unicode characters and emojis
- Returns SUCCESS status

---

### 17. Very Long Packet ID

**Request Body:**
```json
{
  "packetId": "very-long-packet-id-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "cpuIntensity": 1,
  "ramIntensity": 1,
  "processingTimeMs": 100
}
```

**Expected Behavior:**
- Should accept long packet IDs
- May be truncated by database if exceeds column length

---

### 18. Large Payload

**Request Body:**
```json
{
  "packetId": "large-payload",
  "cpuIntensity": 1,
  "ramIntensity": 1,
  "processingTimeMs": 100,
  "payload": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA..."
}
```

**Note:** Create a payload with 100,000+ characters by repeating "A"

**Expected Behavior:**
- Should handle large payloads
- Processing time may increase slightly

---

### 19. Empty Request Body

**Request Body:**
```json
{}
```

**Expected Behavior:**
- Should use all default values
- Auto-generates packet ID
- Returns SUCCESS status

---

### 20. Request with Extra Unknown Fields

**Request Body:**
```json
{
  "packetId": "test-extra-fields",
  "cpuIntensity": 5,
  "ramIntensity": 5,
  "unknownField1": "value1",
  "unknownField2": 12345,
  "extraData": {
    "nested": "object"
  }
}
```

**Expected Behavior:**
- Should ignore unknown fields
- Returns SUCCESS status with known fields processed

---

### 21. Malformed JSON

**Request Body:**
```
{"packetId": "test", "cpuIntensity":
```

**Expected Response (400 Bad Request):**
Error message about JSON parsing failure

---

### 22. Wrong Data Types

**Request Body:**
```json
{
  "packetId": "test-wrong-types",
  "cpuIntensity": "not-a-number",
  "ramIntensity": "also-not-a-number"
}
```

**Expected Response (400 Bad Request):**
Error message about type mismatch

---

### 23. Missing Content-Type Header

**Test in Insomnia:**
- Create POST request to `/api/v1/fakePacket`
- Add JSON body
- Remove the `Content-Type: application/json` header
- Click Send

**Expected Response (415 Unsupported Media Type):**
Error about unsupported media type

---

### 24. GET Request to POST Endpoint

**Method:** `GET`
**Endpoint:** `/api/v1/fakePacket`

**Expected Response (405 Method Not Allowed):**
Error message about method not allowed

---

### 25. Non-existent Endpoint

**Method:** `GET`
**Endpoint:** `/api/v1/nonexistent`

**Expected Response (404 Not Found):**
Error message about endpoint not found

---

## Test Scenarios

### Scenario 1: Normal Operation Flow

**Purpose:** Verify basic packet processing works correctly

**Steps:**
1. **Check Server Status**
   - GET `/api/v1/server/status`
   - Verify: `"open": true`

2. **Process First Packet**
   - POST `/api/v1/fakePacket` with packetId "scenario1-packet"
   - Verify: `"status": "SUCCESS"`
   - Note the `cpuCycles` and `memoryUsedBytes` values

3. **Process Same Packet Again**
   - POST `/api/v1/fakePacket` with same packetId
   - Verify: `"status": "SUCCESS"`
   - Verify: Logs count increments in the result message

4. **Check Database** (optional)
   ```bash
   docker exec -it cs4445-postgres psql -U myuser -d mydatabase -c "SELECT * FROM packet_logs WHERE packet_id = 'scenario1-packet';"
   ```
   - Should see 2 entries

---

### Scenario 2: Server Lifecycle Management

**Purpose:** Verify server can be opened and closed, affecting packet processing

**Steps:**
1. **Get Initial Status**
   - GET `/api/v1/server/status`
   - Verify: `"open": true`

2. **Process Packet (Should Succeed)**
   - POST `/api/v1/fakePacket` with packetId "lifecycle-test-1"
   - Verify: `"status": "SUCCESS"`

3. **Close Server**
   - POST `/api/v1/server/close?reason=Testing`
   - Verify: `"open": false`, `"reason": "Testing"`

4. **Try Processing Packet (Should Fail)**
   - POST `/api/v1/fakePacket` with packetId "lifecycle-test-2"
   - Verify: HTTP 503, `"status": "REJECTED"`

5. **Check Status**
   - GET `/api/v1/server/status`
   - Verify: `"open": false`

6. **Reopen Server**
   - POST `/api/v1/server/open?reason=Testing complete`
   - Verify: `"open": true`

7. **Process Packet Again (Should Succeed)**
   - POST `/api/v1/fakePacket` with packetId "lifecycle-test-3"
   - Verify: `"status": "SUCCESS"`

---

### Scenario 3: Intensity Levels Comparison

**Purpose:** Verify different intensity levels affect processing time and resource usage

**Steps:**
1. **Low Intensity**
   - POST `/api/v1/fakePacket`
   - Body: `{"packetId": "intensity-low", "cpuIntensity": 1, "ramIntensity": 1, "processingTimeMs": 100}`
   - Note: `processingTimeMs`, `cpuCycles`, `memoryUsedBytes`

2. **Medium Intensity**
   - POST `/api/v1/fakePacket`
   - Body: `{"packetId": "intensity-med", "cpuIntensity": 5, "ramIntensity": 5, "processingTimeMs": 100}`
   - Note values

3. **High Intensity**
   - POST `/api/v1/fakePacket`
   - Body: `{"packetId": "intensity-high", "cpuIntensity": 10, "ramIntensity": 10, "processingTimeMs": 100}`
   - Note values

4. **Compare Results**
   - High intensity should have: longer processing time, more CPU cycles, more memory used
   - Medium should be between low and high
   - All should respect minimum processingTimeMs

---

### Scenario 4: Edge Cases and Error Handling

**Purpose:** Verify the API handles edge cases gracefully

**Steps:**
1. **Null Packet ID** (Test #10)
2. **Empty Packet ID** (Test #11)
3. **Extreme Values** (Tests #12, #13)
4. **Special Characters** (Tests #15, #16)
5. **Malformed JSON** (Test #21)
6. **Wrong Data Types** (Test #22)
7. **Wrong HTTP Method** (Test #24)

**Verification:**
- Tests 1-5 should return SUCCESS (200)
- Tests 6-7 should return appropriate error codes (400, 405)

---

### Scenario 5: Concurrent Requests

**Purpose:** Verify the system can handle multiple simultaneous requests

**Steps:**
1. In Insomnia, create 5-10 identical POST requests to `/api/v1/fakePacket`
2. Body: `{"packetId": "concurrent-<number>", "cpuIntensity": 8, "ramIntensity": 8, "processingTimeMs": 2000}`
3. Use different packetId for each (concurrent-1, concurrent-2, etc.)
4. Send all requests quickly one after another (or use Insomnia's Runner feature)

**Verification:**
- All requests should return SUCCESS
- Check database to ensure all packets were logged
- Processing times may vary based on system load

---

### Scenario 6: Same Packet ID Multiple Times

**Purpose:** Verify the system correctly aggregates statistics for the same packet ID

**Steps:**
1. **First Request**
   - POST `/api/v1/fakePacket`
   - Body: `{"packetId": "repeat-test", "cpuIntensity": 5, "ramIntensity": 5}`
   - Note the result message showing "Logs count: 1"

2. **Second Request**
   - Same body as above
   - Verify result shows "Logs count: 2"

3. **Third Request**
   - Same body
   - Verify result shows "Logs count: 3"

4. **Verify Aggregation**
   - The `totalCycles` value should be cumulative across all 3 requests

---

## Database Verification

After running tests, you can verify the data was stored correctly:

```bash
# Connect to PostgreSQL
docker exec -it cs4445-postgres psql -U myuser -d mydatabase

# View recent packet logs
SELECT packet_id, cpu_intensity, ram_intensity, cpu_cycles, memory_used_bytes, timestamp
FROM packet_logs
ORDER BY timestamp DESC
LIMIT 20;

# Count logs by packet ID
SELECT packet_id, COUNT(*) as log_count
FROM packet_logs
GROUP BY packet_id
ORDER BY log_count DESC;

# View logs for specific packet
SELECT * FROM packet_logs WHERE packet_id = 'your-packet-id';

# Exit psql
\q
```

---

## Monitoring

### Prometheus Metrics

**URL:** http://localhost:9090

**Useful Queries:**
- `up` - Check if services are up
- `jvm_memory_used_bytes` - JVM memory usage
- `http_server_requests_seconds_count` - Request count
- `http_server_requests_seconds_sum` - Total request duration

### Grafana Dashboards

**URL:** http://localhost:3000
**Credentials:** admin / admin

Create dashboards to visualize:
- Request throughput
- Processing times
- CPU/Memory usage
- Error rates

---

## Troubleshooting

### Issue: Connection Refused

**Symptoms:** Cannot connect to `http://localhost:8080`

**Solutions:**
1. Check if Docker containers are running:
   ```bash
   docker compose ps
   ```
2. Check application logs:
   ```bash
   docker compose logs app
   ```
3. Restart services:
   ```bash
   docker compose restart
   ```

---

### Issue: Database Connection Error

**Symptoms:** 500 errors mentioning database

**Solutions:**
1. Check PostgreSQL health:
   ```bash
   docker compose ps postgres
   ```
2. Check PostgreSQL logs:
   ```bash
   docker compose logs postgres
   ```
3. Restart PostgreSQL:
   ```bash
   docker compose restart postgres
   ```

---

### Issue: Slow Response Times

**Symptoms:** Requests take much longer than expected

**Possible Causes:**
- High CPU/RAM intensity settings (expected behavior)
- Multiple concurrent requests
- System resource constraints

**Solutions:**
- Use lower intensity values for testing
- Monitor system resources (CPU, RAM)
- Check Docker container resource limits

---

### Issue: Data Not Persisting

**Symptoms:** Packet logs disappear after container restart

**Check:**
- Docker volumes are correctly configured in `compose.yaml`
- Volume `postgres-data` exists: `docker volume ls`

---

## Tips for Testing in Insomnia

1. **Create a Workspace:** Organize all CS4445 tests in one workspace

2. **Use Environment Variables:**
   - Create an environment with `base_url = http://localhost:8080`
   - Use `{{ base_url }}/api/v1/health` in requests

3. **Organize Requests in Folders:**
   - Health & Status
   - Packet Processing
   - Server Management
   - Edge Cases
   - Error Scenarios

4. **Use Request Chaining:**
   - Extract response values using Insomnia's templating
   - Example: Use timestamp from one request in the next

5. **Create Test Suites:**
   - Use Insomnia's Test Runner
   - Run all scenarios automatically

6. **Export/Import:**
   - Export your Insomnia collection for sharing
   - Keep a backup of your test collection

---

## Quick Reference: HTTP Status Codes

| Code | Meaning | When You'll See It |
|------|---------|-------------------|
| 200 | OK | Successful packet processing, server state changes |
| 400 | Bad Request | Malformed JSON, wrong data types |
| 404 | Not Found | Non-existent endpoint |
| 405 | Method Not Allowed | Using GET on POST endpoint or vice versa |
| 415 | Unsupported Media Type | Missing Content-Type header |
| 503 | Service Unavailable | Server is closed, packet rejected |

---

## Sample Insomnia Collection Structure

```
CS4445 Subscription Server
├── 📁 Health & Status
│   ├── GET Health Check
│   └── GET Server Status
├── 📁 Server Management
│   ├── POST Open Server
│   └── POST Close Server
├── 📁 Packet Processing - Normal
│   ├── POST Normal Packet
│   ├── POST Minimal Packet
│   ├── POST High Intensity
│   └── POST Low Intensity
├── 📁 Packet Processing - Edge Cases
│   ├── POST Null Packet ID
│   ├── POST Empty Packet ID
│   ├── POST Extreme CPU
│   ├── POST Negative Values
│   ├── POST Special Characters
│   ├── POST Unicode Characters
│   ├── POST Large Payload
│   └── POST Empty Body
└── 📁 Error Scenarios
    ├── POST Malformed JSON (400)
    ├── POST Wrong Data Types (400)
    ├── GET Wrong Method (405)
    ├── POST Missing Content-Type (415)
    └── GET Non-existent Endpoint (404)
```

---

## Next Steps

1. **Start Testing:** Begin with the Normal Operation Flow (Scenario 1)
2. **Explore Edge Cases:** Try all the edge case tests
3. **Monitor:** Open Prometheus/Grafana while testing
4. **Database:** Check PostgreSQL after each scenario
5. **Document:** Note any unexpected behaviors or bugs

Happy Testing! 🚀
