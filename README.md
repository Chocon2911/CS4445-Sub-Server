# CS4445 Subscription Server - Fake Packet API

A Spring Boot application with a load-testing API endpoint that simulates CPU and RAM intensive processing.

## Prerequisites

- Java 25 or higher
- Maven (included via Maven Wrapper)
- Docker and Docker Compose (for PostgreSQL)

## Quick Start

### 1. Install Java 25
Download and install Java 25 from [Oracle](https://www.oracle.com/java/technologies/downloads/) or use [SDKMAN](https://sdkman.io/):

```bash
# Using SDKMAN (recommended)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 25-open
```

### 2. Start the Application

```bash
# The application will automatically start PostgreSQL via Docker Compose
./mvnw spring-boot:run
```

The server will start on `http://localhost:8080`

## API Documentation

### POST /api/v1/fakePacket

Process a fake packet with configurable CPU and RAM load.

**Request Body:**
```json
{
  "packetId": "packet-001",
  "cpuIntensity": 5,
  "ramIntensity": 5,
  "processingTimeMs": 1000,
  "payload": "test data"
}
```

**Parameters:**
- `packetId` (string, required): Unique identifier for the packet
- `cpuIntensity` (integer, 1-10): CPU load intensity (default: 5)
  - 1 = Low CPU usage
  - 5 = Moderate CPU usage
  - 10 = High CPU usage (heavy prime calculations, hashing, math operations)
- `ramIntensity` (integer, 1-10): RAM load intensity (default: 5)
  - 1 = ~10 MB
  - 5 = ~50 MB
  - 10 = ~100+ MB (large data structures, maps, arrays)
- `processingTimeMs` (integer): Minimum processing time in milliseconds (default: 1000)
- `payload` (string): Optional data payload

**Response:**
```json
{
  "packetId": "packet-001",
  "status": "SUCCESS",
  "processingTimeMs": 2500,
  "cpuCycles": 150000,
  "memoryUsedBytes": 52428800,
  "result": "Packet processed. Total cycles for this packet ID: 150000, Logs count: 1",
  "timestamp": "2025-12-13T10:30:45.123"
}
```

**Response Fields:**
- `packetId`: The packet identifier
- `status`: Processing status (SUCCESS/FAILED)
- `processingTimeMs`: Total processing time in milliseconds
- `cpuCycles`: Number of CPU operations performed
- `memoryUsedBytes`: Approximate memory allocated in bytes
- `result`: Processing result message
- `timestamp`: Processing completion time

### GET /api/v1/health

Health check endpoint.

**Response:**
```
Server is running
```

### Server Control APIs

The server includes control endpoints to simulate opening/closing without actually shutting down.

#### POST /api/v1/server/close

Close the server (reject fakePacket requests). The server continues running but won't process packets.

**Request:**
```bash
curl -X POST "http://localhost:8080/api/v1/server/close?reason=Maintenance"
```

**Response:**
```json
{
  "open": false,
  "status": "CLOSED",
  "lastStateChange": "2025-12-13T10:30:45.123",
  "reason": "Maintenance"
}
```

#### POST /api/v1/server/open

Open the server (accept fakePacket requests again).

**Request:**
```bash
curl -X POST "http://localhost:8080/api/v1/server/open?reason=Maintenance+complete"
```

**Response:**
```json
{
  "open": true,
  "status": "OPEN",
  "lastStateChange": "2025-12-13T10:35:20.456",
  "reason": "Maintenance complete"
}
```

#### GET /api/v1/server/status

Check if server is currently open or closed.

**Request:**
```bash
curl http://localhost:8080/api/v1/server/status
```

**Response:**
```json
{
  "open": true,
  "status": "OPEN",
  "lastStateChange": "2025-12-13T10:35:20.456",
  "reason": "Server started"
}
```

**Note**: When closed, fakePacket requests return HTTP 503 with status "REJECTED". See [docs/server-control-api.md](docs/server-control-api.md) for detailed documentation.

## Testing the API

### Example curl commands:

**Low Load Test:**
```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{
    "packetId": "test-001",
    "cpuIntensity": 2,
    "ramIntensity": 2,
    "processingTimeMs": 500,
    "payload": "low load test"
  }'
```

**Medium Load Test:**
```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{
    "packetId": "test-002",
    "cpuIntensity": 5,
    "ramIntensity": 5,
    "processingTimeMs": 2000,
    "payload": "medium load test"
  }'
```

**High Load Test:**
```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{
    "packetId": "test-003",
    "cpuIntensity": 10,
    "ramIntensity": 10,
    "processingTimeMs": 5000,
    "payload": "high load test"
  }'
```

**Stress Test (Multiple Concurrent Requests):**
```bash
# Run 10 concurrent requests
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d "{
      \"packetId\": \"stress-test-$i\",
      \"cpuIntensity\": 8,
      \"ramIntensity\": 8,
      \"processingTimeMs\": 3000,
      \"payload\": \"stress test $i\"
    }" &
done
wait
```

## Load Generation Algorithms

The server implements several resource-intensive operations:

### CPU-Intensive Operations:
1. **Prime Number Calculation**: Finds prime numbers up to a threshold
2. **Cryptographic Hashing**: Performs SHA-256 hashing on random UUIDs
3. **Mathematical Operations**: Complex calculations (sqrt, log, cos)
4. **String Manipulation**: UUID generation and string concatenation

### RAM-Intensive Operations:
1. **Large ArrayLists**: Creates lists with thousands of UUID strings
2. **Large HashMaps**: Nested map structures with complex objects
3. **Byte Arrays**: Allocates multiple large byte arrays
4. **Concurrent Data Structures**: ConcurrentHashMap with nested collections

### Database Operations:
1. **Write Operations**: Saves packet logs to PostgreSQL
2. **Read Operations**: Queries historical packet data
3. **Aggregations**: Calculates statistics from stored logs

## Monitoring Resource Usage

### On Linux/WSL:
```bash
# Monitor CPU and memory
htop

# Or use top
top

# Monitor specific Java process
ps aux | grep java
```

### On Windows:
- Open Task Manager (Ctrl + Shift + Esc)
- Go to Performance tab to see CPU and Memory usage
- Go to Details tab to see java.exe process

## Database

The application uses PostgreSQL running in Docker. Connection details:
- Host: localhost
- Port: 5432
- Database: mydatabase
- Username: myuser
- Password: secret

To access the database:
```bash
docker exec -it $(docker ps -q -f ancestor=postgres:latest) psql -U myuser -d mydatabase
```

View packet logs:
```sql
SELECT * FROM packet_logs ORDER BY timestamp DESC LIMIT 10;
```

## Development

### Build
```bash
./mvnw clean install
```

### Run Tests
```bash
./mvnw test
```

### Build Docker Image
```bash
./mvnw spring-boot:build-image
```

## Project Structure

```
src/
├── main/
│   ├── java/com/CS445/CS4445_Sub_Server/
│   │   ├── Cs4445SubServerApplication.java  # Main application
│   │   ├── config/
│   │   │   └── SecurityConfig.java          # Security configuration
│   │   ├── controller/
│   │   │   └── FakePacketController.java    # REST API endpoint
│   │   ├── dto/
│   │   │   ├── FakePacketRequest.java       # Request DTO
│   │   │   └── FakePacketResponse.java      # Response DTO
│   │   ├── entity/
│   │   │   └── PacketLog.java               # Database entity
│   │   ├── repository/
│   │   │   └── PacketLogRepository.java     # JPA repository
│   │   └── service/
│   │       └── FakePacketService.java       # Business logic
│   └── resources/
│       └── application.properties           # Configuration
└── test/
    └── java/com/CS445/CS4445_Sub_Server/
```

## Troubleshooting

### Port 8080 already in use
```bash
# Find and kill the process using port 8080
# On Linux/WSL:
lsof -ti:8080 | xargs kill -9

# On Windows:
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### PostgreSQL container not starting
```bash
# Check Docker status
docker ps -a

# Restart Docker Compose
docker compose down
docker compose up -d
```

### Out of Memory errors
Increase JVM heap size:
```bash
export MAVEN_OPTS="-Xmx2g"
./mvnw spring-boot:run
```

## License

Educational project for CS4445 course.
