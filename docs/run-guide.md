# Complete Run Guide - CS4445 Sub Server

This guide walks you through running the entire CS4445 Sub Server application from start to finish, including all monitoring tools.

## Table of Contents

1. [Prerequisites Check](#prerequisites-check)
2. [First Time Setup](#first-time-setup)
3. [Starting the Application](#starting-the-application)
4. [Testing the Application](#testing-the-application)
5. [Monitoring with Grafana](#monitoring-with-grafana)
6. [Server Control](#server-control)
7. [Stopping the Application](#stopping-the-application)
8. [Common Workflows](#common-workflows)
9. [Troubleshooting](#troubleshooting)

## Prerequisites Check

Before you start, make sure you have everything installed:

### 1. Check Java

```bash
java -version
```

**Expected output**: `java version "25"` or higher

**If not installed**:
- **Windows/Mac**: Download from https://www.oracle.com/java/technologies/downloads/
- **Linux/Mac (SDKMAN)**:
  ```bash
  curl -s "https://get.sdkman.io" | bash
  sdk install java 25-open
  ```

### 2. Check Docker

```bash
docker --version
docker compose version
```

**Expected output**: Version numbers for both

**If not installed**:
- Download Docker Desktop from https://www.docker.com/products/docker-desktop/
- Install and start Docker Desktop
- Make sure the Docker icon is running in your system tray

### 3. Check curl (for testing)

```bash
curl --version
```

**Expected output**: Version number

**If not installed**:
- **Windows**: Should be pre-installed on Windows 10+
- **Mac**: Pre-installed
- **Linux**: `sudo apt install curl`

### 4. Verify Project Files

Make sure you're in the project directory:

```bash
# Windows
cd "C:\Users\Admin\OneDrive - Hanoi University of Science and Technology\New folder\year 4-1\CS4445\TeamProject\CS4445-Sub-Server"

# Mac/Linux/WSL
cd "/mnt/c/Users/Admin/OneDrive - Hanoi University of Science and Technology/New folder/year 4-1/CS4445/TeamProject/CS4445-Sub-Server"

# Check files exist
ls compose.yaml
ls prometheus.yml
ls pom.xml
```

## First Time Setup

### Step 1: Download Dependencies (One-time)

The first time you run the application, Maven will download all dependencies. This may take 5-10 minutes.

```bash
./mvnw clean install -DskipTests
```

**What this does**:
- Downloads all Java libraries
- Compiles the code
- Packages the application
- Skips tests for faster first build

**Expected output**: `BUILD SUCCESS`

### Step 2: Verify Docker Compose Configuration

Check that Docker Compose can read the configuration:

```bash
docker compose config
```

**Expected output**: YAML configuration without errors

## Starting the Application

### Option 1: Full Stack (Application + Monitoring)

This starts everything: PostgreSQL, Prometheus, Grafana, and your application.

#### Step 1: Start Docker Services

```bash
docker compose up -d
```

**What this starts**:
- PostgreSQL database (port 5432)
- Prometheus monitoring (port 9090)
- Grafana dashboards (port 3000)

**Expected output**:
```
[+] Running 4/4
 ✔ Network cs4445-sub-server_monitoring    Created
 ✔ Container cs4445-postgres               Started
 ✔ Container cs4445-prometheus             Started
 ✔ Container cs4445-grafana                Started
```

**Verify containers are running**:
```bash
docker ps
```

You should see 3 containers: `cs4445-postgres`, `cs4445-prometheus`, `cs4445-grafana`

#### Step 2: Start Spring Boot Application

Open a **new terminal window** (keep the first one open) and run:

```bash
./mvnw spring-boot:run
```

**Expected output** (last few lines):
```
Started Cs4445SubServerApplication in X.XXX seconds
```

**The application is now running!** 🎉

### Option 2: Quick Test (Application Only)

If you just want to test the API without monitoring:

```bash
# Start only PostgreSQL
docker compose up -d postgres

# Start application
./mvnw spring-boot:run
```

## Testing the Application

### Quick Health Check

In a **new terminal window**, test if the server is responding:

```bash
curl http://localhost:8080/api/v1/health
```

**Expected output**: `Server is running`

### Check Server Status

```bash
curl http://localhost:8080/api/v1/server/status
```

**Expected output**:
```json
{
  "open": true,
  "status": "OPEN",
  "lastStateChange": "2025-12-13T...",
  "reason": "Server started"
}
```

### Send a Test Packet (Light Load)

```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{
    "packetId": "test-1",
    "cpuIntensity": 3,
    "ramIntensity": 3,
    "processingTimeMs": 1000,
    "payload": "First test"
  }'
```

**Expected output**:
```json
{
  "packetId": "test-1",
  "status": "SUCCESS",
  "processingTimeMs": 1234,
  "cpuCycles": 45678,
  "memoryUsedBytes": 12345678,
  "result": "Packet processed. Total cycles for this packet ID: ...",
  "timestamp": "2025-12-13T..."
}
```

**If you see this response, everything is working!** ✅

### Run Automated Tests

Use the provided test scripts:

**Windows**:
```bash
test-api.bat
```

**Mac/Linux/WSL**:
```bash
./test-api.sh
```

This will run 4 tests:
1. ✅ Health check
2. ✅ Low load packet
3. ✅ Medium load packet
4. ✅ High load packet
5. ✅ Stress test (5 concurrent requests)

### Test Server Control

**Windows**:
```bash
test-server-control.bat
```

**Mac/Linux/WSL**:
```bash
./test-server-control.sh
```

This will:
1. ✅ Check initial status (OPEN)
2. ✅ Send packet (should succeed)
3. ✅ Close server
4. ✅ Try to send packet (should be rejected)
5. ✅ Open server
6. ✅ Send packet (should succeed again)

## Monitoring with Grafana

### Access Grafana Dashboard

1. **Open browser**: http://localhost:3000
2. **Login**:
   - Username: `admin`
   - Password: `admin`
3. **Change password** (optional): Click "Skip" to keep using admin/admin
4. **Dashboard loads automatically**: "CS4445 Sub Server Dashboard"

### Understanding the Dashboard

The dashboard shows 6 panels:

#### Panel 1: CPU Usage
- **Location**: Top left
- **Shows**: CPU percentage (0-100%)
- **What to watch**: Spikes when processing packets
- **Normal**: 0-20% idle, 50-100% under load

#### Panel 2: JVM Memory Usage
- **Location**: Top right
- **Shows**: Memory in bytes
- **What to watch**: Increases with ramIntensity
- **Types**: Heap (blue) and Non-heap (red)

#### Panel 3: HTTP Requests Rate
- **Location**: Middle left
- **Shows**: Requests per second
- **What to watch**: Spikes when sending requests
- **Normal**: 0 when idle, 1-10 under load

#### Panel 4: Request Duration (p95)
- **Location**: Middle right
- **Shows**: 95th percentile response time
- **What to watch**: How long requests take
- **Normal**: 0.5-15 seconds depending on intensity

#### Panel 5: Active DB Connections
- **Location**: Bottom left
- **Shows**: Number of active database connections
- **What to watch**: Should be 1-5 normally
- **Max**: 10 (configured limit)

#### Panel 6: Application Status
- **Location**: Bottom right
- **Shows**: UP (green) or DOWN (red)
- **What to watch**: Should always be green when app is running

### Send Requests and Watch Metrics

Keep Grafana open in one window and send requests in another:

**Light load**:
```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"monitor-light","cpuIntensity":3,"ramIntensity":3,"processingTimeMs":2000}'
```

**Watch Grafana**: Small increase in CPU and memory

**Heavy load**:
```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"monitor-heavy","cpuIntensity":10,"ramIntensity":10,"processingTimeMs":5000}'
```

**Watch Grafana**: CPU spikes to 80-100%, large memory increase!

**Stress test**:
```bash
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d "{\"packetId\":\"stress-$i\",\"cpuIntensity\":8,\"ramIntensity\":8,\"processingTimeMs\":3000}" &
done
wait
```

**Watch Grafana**: All metrics spike dramatically!

### Access Prometheus Directly

1. **Open browser**: http://localhost:9090
2. **Click**: "Graph" tab
3. **Enter query**: `system_cpu_usage * 100`
4. **Click**: "Execute"
5. **See**: CPU usage graph

**Other queries to try**:
- Memory: `jvm_memory_used_bytes`
- Requests: `rate(http_server_requests_seconds_count[1m])`
- DB Connections: `hikaricp_connections_active`

### View Raw Metrics

```bash
curl http://localhost:8080/actuator/prometheus
```

This shows all metrics in Prometheus format.

## Server Control

### Close the Server

```bash
curl -X POST http://localhost:8080/api/v1/server/close?reason=Testing
```

**Expected output**:
```json
{
  "open": false,
  "status": "CLOSED",
  "lastStateChange": "2025-12-13T...",
  "reason": "Testing"
}
```

### Try Sending Packet (Will Be Rejected)

```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"should-fail","cpuIntensity":5,"ramIntensity":5}'
```

**Expected output** (HTTP 503):
```json
{
  "packetId": "should-fail",
  "status": "REJECTED",
  "result": "Server is currently closed. Please open the server first using /api/v1/server/open",
  ...
}
```

### Open the Server Again

```bash
curl -X POST http://localhost:8080/api/v1/server/open?reason=Testing+complete
```

**Expected output**:
```json
{
  "open": true,
  "status": "OPEN",
  "lastStateChange": "2025-12-13T...",
  "reason": "Testing complete"
}
```

### Check Status Anytime

```bash
curl http://localhost:8080/api/v1/server/status
```

## Stopping the Application

### Proper Shutdown Sequence

#### Step 1: Stop Spring Boot Application

In the terminal where the application is running:
- Press `Ctrl + C`
- Wait for graceful shutdown (5-10 seconds)

**Expected output**:
```
Stopping application...
Application stopped
```

#### Step 2: Stop Docker Services

```bash
docker compose down
```

**Expected output**:
```
[+] Running 4/4
 ✔ Container cs4445-grafana     Removed
 ✔ Container cs4445-prometheus  Removed
 ✔ Container cs4445-postgres    Removed
 ✔ Network cs4445-sub-server_monitoring  Removed
```

### Quick Stop (Emergency)

If something is stuck:

```bash
# Force stop Docker containers
docker compose down --remove-orphans

# Kill Java process (if needed)
# Windows
taskkill /F /IM java.exe

# Mac/Linux
pkill -9 java
```

### Clean Stop (Remove All Data)

If you want a completely fresh start:

```bash
# Stop and remove all data
docker compose down -v

# This removes:
# - All containers
# - All networks
# - All volumes (database data, metrics, etc.)
```

**Warning**: This deletes all packet logs and monitoring history!

## Common Workflows

### Workflow 1: Daily Development

```bash
# 1. Start Docker services
docker compose up -d

# 2. Start application
./mvnw spring-boot:run

# 3. Do your work/testing

# 4. Stop application (Ctrl+C)

# 5. Stop Docker (optional - can leave running)
docker compose down
```

### Workflow 2: Quick Test

```bash
# 1. Start only database
docker compose up -d postgres

# 2. Start app
./mvnw spring-boot:run

# 3. Run test script
./test-api.sh

# 4. Stop
# Ctrl+C to stop app
docker compose down
```

### Workflow 3: Load Testing with Monitoring

```bash
# 1. Start everything
docker compose up -d
./mvnw spring-boot:run

# 2. Open Grafana
# Browser: http://localhost:3000

# 3. Run stress test
for i in {1..20}; do
  curl -X POST http://localhost:8080/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d "{\"packetId\":\"load-$i\",\"cpuIntensity\":9,\"ramIntensity\":9,\"processingTimeMs\":4000}" &
done
wait

# 4. Watch Grafana metrics spike!

# 5. Stop
# Ctrl+C to stop app
docker compose down
```

### Workflow 4: Debugging Performance

```bash
# 1. Start with monitoring
docker compose up -d
./mvnw spring-boot:run

# 2. Open monitoring tools
# Grafana: http://localhost:3000
# Prometheus: http://localhost:9090

# 3. Send problematic request
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"debug","cpuIntensity":10,"ramIntensity":10,"processingTimeMs":10000}'

# 4. Analyze metrics:
# - Check CPU usage pattern
# - Check memory allocation
# - Check request duration
# - Check database connections

# 5. Check application logs
# Look in the terminal where app is running
```

### Workflow 5: Demo Presentation

```bash
# Preparation (before demo)
docker compose up -d
./mvnw spring-boot:run

# During demo:

# 1. Show health check
curl http://localhost:8080/api/v1/health

# 2. Show Grafana dashboard
# Open: http://localhost:3000

# 3. Demo light load
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"demo-1","cpuIntensity":3,"ramIntensity":3,"processingTimeMs":1000}'

# 4. Demo heavy load
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"demo-2","cpuIntensity":10,"ramIntensity":10,"processingTimeMs":5000}'

# 5. Show server control
curl -X POST http://localhost:8080/api/v1/server/close
curl -X POST http://localhost:8080/api/v1/server/open

# 6. Demo stress test
./test-api.sh
```

## Troubleshooting

### Issue 1: "Port already in use"

**Symptoms**:
```
Error starting userland proxy: listen tcp 0.0.0.0:8080: bind: address already in use
```

**Solutions**:

**Find what's using the port**:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Mac/Linux
lsof -ti:8080
kill -9 <PID>
```

**Or change the port**:
Edit `src/main/resources/application.properties`:
```properties
server.port=8081
```

Then use `http://localhost:8081` instead.

### Issue 2: "JAVA_HOME not defined"

**Symptoms**:
```
The JAVA_HOME environment variable is not defined correctly
```

**Solutions**:

**Check Java installation**:
```bash
java -version
which java  # Mac/Linux
where java  # Windows
```

**Set JAVA_HOME**:
```bash
# Mac/Linux (add to ~/.bashrc or ~/.zshrc)
export JAVA_HOME=$(/usr/libexec/java_home -v 25)

# Windows (System Environment Variables)
JAVA_HOME=C:\Program Files\Java\jdk-25
```

### Issue 3: "Docker daemon not running"

**Symptoms**:
```
Cannot connect to the Docker daemon
```

**Solutions**:
1. Start Docker Desktop
2. Wait for it to fully start (whale icon stops animating)
3. Try again: `docker ps`

### Issue 4: "Connection refused" to database

**Symptoms**:
```
org.postgresql.util.PSQLException: Connection to localhost:5432 refused
```

**Solutions**:

**Check PostgreSQL is running**:
```bash
docker ps | grep postgres
```

**If not running, start it**:
```bash
docker compose up -d postgres
```

**Wait 10-20 seconds** for PostgreSQL to be ready, then start the app.

### Issue 5: Grafana shows "No Data"

**Symptoms**: Grafana dashboard is empty

**Solutions**:

**1. Check application is running**:
```bash
curl http://localhost:8080/actuator/health
```

**2. Check Prometheus targets**:
- Open: http://localhost:9090/targets
- Look for "spring-boot-app"
- Should show "UP" in green

**3. If target is DOWN**:
```bash
# On Linux, edit prometheus.yml
# Change: host.docker.internal:8080
# To: 172.17.0.1:8080

# Then restart Prometheus
docker compose restart prometheus
```

**4. Wait 15-30 seconds** for first data collection

### Issue 6: Application won't start

**Symptoms**: Application crashes on startup

**Solutions**:

**Check logs for errors**:
Look in the terminal output for error messages

**Common issues**:
1. Database not ready → Wait and retry
2. Port in use → Change port or kill process
3. Missing dependencies → Run `./mvnw clean install`

**Try fresh build**:
```bash
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

### Issue 7: Slow performance

**Symptoms**: Requests take very long

**Possible causes**:
1. High intensity values (cpuIntensity/ramIntensity = 10)
2. Long processingTimeMs
3. Computer is already busy

**Solutions**:
- Use lower intensity values (3-5)
- Reduce processingTimeMs
- Close other applications
- Check Grafana to see what's slow (CPU, memory, or database)

### Issue 8: Can't access Grafana/Prometheus

**Symptoms**: Browser can't connect

**Solutions**:

**Check containers are running**:
```bash
docker ps
```

**Check logs**:
```bash
docker logs cs4445-grafana
docker logs cs4445-prometheus
```

**Restart containers**:
```bash
docker compose restart grafana
docker compose restart prometheus
```

**Full reset**:
```bash
docker compose down -v
docker compose up -d
```

## Quick Reference

### Ports

| Service | Port | URL |
|---------|------|-----|
| Spring Boot App | 8080 | http://localhost:8080 |
| PostgreSQL | 5432 | localhost:5432 |
| Prometheus | 9090 | http://localhost:9090 |
| Grafana | 3000 | http://localhost:3000 |

### Important Endpoints

| Endpoint | Purpose |
|----------|---------|
| /api/v1/health | Health check |
| /api/v1/fakePacket | Process packet (POST) |
| /api/v1/server/status | Server status (GET) |
| /api/v1/server/open | Open server (POST) |
| /api/v1/server/close | Close server (POST) |
| /actuator/health | Detailed health |
| /actuator/prometheus | Metrics |

### Test Scripts

| Script | Purpose |
|--------|---------|
| test-api.sh / .bat | Test fakePacket API |
| test-server-control.sh / .bat | Test server control |

### Common Commands

```bash
# Start all Docker services
docker compose up -d

# Start application
./mvnw spring-boot:run

# Stop application
Ctrl + C

# Stop Docker services
docker compose down

# View logs
docker logs <container-name>

# Fresh start (removes all data)
docker compose down -v
docker compose up -d

# Build project
./mvnw clean install

# Run tests
./mvnw test
```

## Next Steps

Now that you know how to run everything:

1. 📖 **Read API docs**: [README.md](../README.md) for detailed API documentation
2. 📊 **Learn monitoring**: [monitoring-guide.md](monitoring-guide.md) for Prometheus/Grafana details
3. 🎮 **Test server control**: [server-control-api.md](server-control-api.md) for open/close features
4. 👶 **Beginner guide**: [summary_v1.md](summary_v1.md) for non-technical explanation
5. ⚡ **Quick starts**:
   - [quick-start-guide.md](quick-start-guide.md) for general usage
   - [monitoring-quickstart.md](monitoring-quickstart.md) for monitoring setup

Happy testing! 🚀
