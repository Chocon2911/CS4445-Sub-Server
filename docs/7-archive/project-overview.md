# Project Overview - Fake Packet Server

## What Was Built

This project is a **Load Testing Server** - a Spring Boot application that simulates heavy processing to test computer performance.

## Files Created

### 📁 Core Application Files

```
src/main/java/com/CS445/CS4445_Sub_Server/
│
├── 📄 Cs4445SubServerApplication.java
│   └── Main entry point - starts the server
│
├── 📁 config/
│   └── 📄 SecurityConfig.java
│       └── Disables security so API is publicly accessible
│
├── 📁 controller/
│   └── 📄 FakePacketController.java
│       └── REST API endpoint - receives HTTP requests
│       └── Endpoints:
│           • POST /api/v1/fakePacket  (main endpoint)
│           • GET  /api/v1/health      (health check)
│
├── 📁 dto/ (Data Transfer Objects)
│   ├── 📄 FakePacketRequest.java
│   │   └── Defines what data you send to the server
│   │   └── Fields: packetId, cpuIntensity, ramIntensity, processingTimeMs, payload
│   │
│   └── 📄 FakePacketResponse.java
│       └── Defines what data the server sends back
│       └── Fields: packetId, status, processingTimeMs, cpuCycles, memoryUsedBytes, result, timestamp
│
├── 📁 entity/
│   └── 📄 PacketLog.java
│       └── Database table structure
│       └── Saves processing history to PostgreSQL
│
├── 📁 repository/
│   └── 📄 PacketLogRepository.java
│       └── Database operations (save, find, query)
│
└── 📁 service/
    └── 📄 FakePacketService.java
        └── THE MAIN WORKER - contains all the load algorithms
        └── Functions:
            • performCpuIntensiveWork()  - CPU load algorithms
            • performRamIntensiveWork()  - RAM allocation algorithms
            • performDatabaseOperations() - Database I/O
```

### 📁 Configuration Files

```
src/main/resources/
└── 📄 application.properties
    └── Server settings (port, database connection, logging)
```

### 📁 Documentation & Scripts

```
Project Root/
├── 📄 README.md                  - Technical documentation
├── 📄 CLAUDE.md                  - AI assistant guide
├── 📄 test-api.sh               - Linux/Mac test script
├── 📄 test-api.bat              - Windows test script
├── 📄 compose.yaml              - Docker PostgreSQL configuration
├── 📄 pom.xml                   - Maven dependencies & build config
│
└── 📁 docs/
    ├── 📄 summary_v1.md         - Complete beginner's guide
    ├── 📄 quick-start-guide.md  - 5-minute quick start
    └── 📄 project-overview.md   - This file!
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         YOUR REQUEST                             │
│  POST http://localhost:8080/api/v1/fakePacket                   │
│  Body: {"packetId": "test", "cpuIntensity": 7, ...}             │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                  SPRING BOOT APPLICATION                         │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐    │
│  │ Layer 1: CONTROLLER (FakePacketController.java)        │    │
│  │ • Receives HTTP request                                │    │
│  │ • Validates input                                      │    │
│  │ • Calls service layer                                  │    │
│  │ • Returns HTTP response                                │    │
│  └──────────────────────┬─────────────────────────────────┘    │
│                         │                                       │
│                         ▼                                       │
│  ┌────────────────────────────────────────────────────────┐    │
│  │ Layer 2: SERVICE (FakePacketService.java)              │    │
│  │                                                         │    │
│  │  ┌──────────────────────────────────────────┐         │    │
│  │  │ CPU Intensive Work                       │         │    │
│  │  │ • Prime number calculations              │ ◄───────┼──── CPU: 🔥🔥🔥
│  │  │ • SHA-256 hashing                        │         │
│  │  │ • Complex math (sqrt, log, cos)          │         │
│  │  │ • String manipulation                    │         │
│  │  └──────────────────────────────────────────┘         │
│  │                                                         │    │
│  │  ┌──────────────────────────────────────────┐         │    │
│  │  │ RAM Intensive Work                       │         │    │
│  │  │ • Large ArrayLists (millions of items)   │ ◄───────┼──── RAM: 📊📊📊
│  │  │ • Large HashMaps (nested structures)     │         │
│  │  │ • Byte arrays (10KB each x 1000s)        │         │
│  │  │ • Complex objects                        │         │
│  │  └──────────────────────────────────────────┘         │
│  │                                                         │    │
│  │  ┌──────────────────────────────────────────┐         │    │
│  │  │ Database Operations                      │         │    │
│  │  │ • Save packet log                        │ ◄───────┼──── I/O: 💾
│  │  │ • Query previous logs                    │         │
│  │  │ • Calculate statistics                   │         │
│  │  └───────────────┬──────────────────────────┘         │
│  └──────────────────┼─────────────────────────────────────┘    │
│                     │                                           │
└─────────────────────┼───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│              POSTGRESQL DATABASE (Docker Container)             │
│                                                                  │
│  Table: packet_logs                                             │
│  ┌──────────────────────────────────────────────────────┐      │
│  │ id | packetId | cpuIntensity | ramIntensity | ...    │      │
│  │ 1  | test-1   | 5            | 5            | ...    │      │
│  │ 2  | test-2   | 8            | 8            | ...    │      │
│  │ 3  | test-3   | 10           | 10           | ...    │      │
│  └──────────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow

### Request → Response Journey

```
1️⃣ You send request
   ↓
2️⃣ Controller receives it (FakePacketController.processFakePacket)
   ↓
3️⃣ Service processes it (FakePacketService.processFakePacket)
   ↓
4️⃣ CPU Work starts
   • Finding primes: 1, 2, 3, 5, 7, 11, 13... (thousands of them)
   • Hashing: SHA256("uuid-1"), SHA256("uuid-2")... (thousands of times)
   • Math: sqrt(1) * log(2) / cos(0.1)... (hundreds of thousands of operations)
   ↓
5️⃣ RAM Work starts
   • Creating list: ["uuid-1", "uuid-2", ... "uuid-1000000"]
   • Creating map: {entry-1: {nested data}, entry-2: {nested data}, ...}
   • Creating arrays: [random bytes], [random bytes], ... (1000+ times)
   ↓
6️⃣ Database Work
   • Save to database: INSERT INTO packet_logs VALUES (...)
   • Query database: SELECT * FROM packet_logs WHERE packetId = ...
   • Calculate stats: SUM, COUNT, etc.
   ↓
7️⃣ Build response with metrics
   ↓
8️⃣ Controller sends response back to you
   ↓
9️⃣ You receive: {"status": "SUCCESS", "processingTimeMs": 3500, ...}
```

## Technology Stack

| Technology | Purpose | Why Used |
|------------|---------|----------|
| **Java 25** | Programming Language | Modern, powerful, cross-platform |
| **Spring Boot 3.5.8** | Web Framework | Easy to build REST APIs |
| **Maven** | Build Tool | Manages dependencies, builds project |
| **PostgreSQL** | Database | Stores packet processing logs |
| **Docker** | Container Platform | Runs PostgreSQL easily |
| **Lombok** | Code Generator | Reduces boilerplate code (@Data, @Builder) |
| **Spring Data JPA** | Database Access | Easy database operations |
| **Spring Security** | Security Framework | (Disabled for this project) |
| **HikariCP** | Connection Pool | Efficient database connections |

## Key Algorithms Explained

### 🔥 CPU Intensive Algorithms

**1. Prime Number Calculation**
```
For each number from 2 to (intensity * 100000):
  - Check if it's prime
  - If yes, add to list
  - This requires many division operations
```

**2. Cryptographic Hashing (SHA-256)**
```
For i from 1 to (intensity * 1000):
  - Generate random UUID
  - Calculate SHA-256 hash
  - Hash computation is CPU-intensive
```

**3. Mathematical Operations**
```
For i from 1 to (intensity * 50000):
  - Calculate: sqrt(i) * log(i+1) / cos(i*0.1)
  - Trigonometry and logarithms are slow operations
```

### 📊 RAM Intensive Algorithms

**1. Large ArrayList**
```
Create list with (intensity * 100000) items
Each item = UUID string (~36 bytes)
Total memory ≈ intensity * 100000 * 36 bytes
```

**2. Large HashMap with Nested Data**
```
Create map with (intensity * 10000) entries
Each entry contains:
  - 3 UUID strings
  - Nested map structure
Total memory ≈ intensity * 10000 * 200 bytes
```

**3. Byte Arrays**
```
Create (intensity * 100) byte arrays
Each array = 10,000 bytes (10 KB)
Total memory = intensity * 100 * 10KB
```

## Performance Expectations

| Intensity | Expected CPU Time | Expected Memory | Best Use Case |
|-----------|------------------|-----------------|---------------|
| 1 | ~0.5 seconds | ~10 MB | Testing setup |
| 2 | ~1 second | ~20 MB | Light testing |
| 3 | ~1.5 seconds | ~30 MB | Learning |
| 5 | ~3 seconds | ~50 MB | Moderate load |
| 7 | ~5 seconds | ~70 MB | Heavy load |
| 10 | ~10+ seconds | ~100+ MB | Stress testing |

**Note**: Actual times depend on your computer's CPU speed, available RAM, and current load.

## API Endpoint Details

### POST /api/v1/fakePacket

**Purpose**: Process a fake packet with configurable load

**Request Example**:
```json
{
  "packetId": "unique-id-123",      // Required: Your unique identifier
  "cpuIntensity": 5,                 // Optional: 1-10 (default: 5)
  "ramIntensity": 5,                 // Optional: 1-10 (default: 5)
  "processingTimeMs": 2000,          // Optional: milliseconds (default: 1000)
  "payload": "any text you want"     // Optional: stored in database
}
```

**Response Example**:
```json
{
  "packetId": "unique-id-123",
  "status": "SUCCESS",
  "processingTimeMs": 2345,          // How long it took
  "cpuCycles": 123456,               // CPU operations performed
  "memoryUsedBytes": 52428800,       // RAM used (~50 MB)
  "result": "Packet processed. Total cycles for this packet ID: 123456, Logs count: 1",
  "timestamp": "2025-12-13T10:30:45.123"
}
```

### GET /api/v1/health

**Purpose**: Check if server is running

**Response**: `"Server is running"`

## Database Schema

### Table: packet_logs

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-incrementing ID |
| packet_id | VARCHAR | Your packet identifier |
| cpu_intensity | INTEGER | CPU intensity used (1-10) |
| ram_intensity | INTEGER | RAM intensity used (1-10) |
| processing_time_ms | BIGINT | How long it took |
| cpu_cycles | BIGINT | CPU operations performed |
| memory_used_bytes | BIGINT | Memory allocated |
| payload | VARCHAR(5000) | Your custom data |
| result | VARCHAR(5000) | Processing result |
| timestamp | TIMESTAMP | When it was processed |

## For Developers

### Adding New Load Algorithms

Edit `FakePacketService.java`:

```java
// Add to performCpuIntensiveWork()
private long performCpuIntensiveWork(int intensity) {
    // Add your CPU-intensive algorithm here
    // Example: More complex calculations, encoding, compression, etc.
}

// Add to performRamIntensiveWork()
private long performRamIntensiveWork(int intensity) {
    // Add your RAM-intensive algorithm here
    // Example: Larger data structures, caching, etc.
}
```

### Customizing Response

Edit `FakePacketResponse.java` to add new fields:

```java
@Data
@Builder
public class FakePacketResponse {
    // Existing fields...
    private YourNewField newField; // Add your new field
}
```

## Summary

This project provides a **complete, working REST API** that:
- ✅ Accepts HTTP POST requests
- ✅ Performs configurable CPU-intensive work
- ✅ Allocates configurable amounts of RAM
- ✅ Saves all data to PostgreSQL database
- ✅ Returns detailed processing metrics
- ✅ Works on Windows, Mac, and Linux
- ✅ Includes comprehensive documentation
- ✅ Includes test scripts for easy testing

**Perfect for**: Load testing, performance testing, learning about servers, demonstrating system resource usage, educational projects.
