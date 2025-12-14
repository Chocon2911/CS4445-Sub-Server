# Quick Start Guide (5 Minutes)

## Super Quick Setup

### 1. Install Requirements (One-time)
```bash
# Install Java 25 (if not installed)
# Windows/Mac: Download from https://www.oracle.com/java/technologies/downloads/
# Linux/Mac (using SDKMAN):
curl -s "https://get.sdkman.io" | bash
sdk install java 25-open

# Install Docker Desktop
# Download from: https://www.docker.com/products/docker-desktop/
```

### 2. Start Server (Every time)
```bash
# Open terminal in project folder
./mvnw spring-boot:run

# Wait for: "Started Cs4445SubServerApplication"
```

### 3. Test It (In a new terminal)
```bash
# Simple test
curl http://localhost:8080/api/v1/health

# Real test - watch Task Manager/Activity Monitor!
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"quick-test","cpuIntensity":7,"ramIntensity":7,"processingTimeMs":3000}'
```

## Visual Flow

```
┌─────────────┐
│   You       │
│ (Your PC)   │
└──────┬──────┘
       │
       │ 1. Send Request (via curl/Postman)
       │    {"cpuIntensity": 10, "ramIntensity": 10}
       │
       ▼
┌─────────────────────────────────────────┐
│   Server (http://localhost:8080)        │
│                                          │
│  ┌────────────────────────────────┐    │
│  │  FakePacketController          │    │
│  │  Receives your request         │    │
│  └────────────┬───────────────────┘    │
│               │                         │
│               ▼                         │
│  ┌────────────────────────────────┐    │
│  │  FakePacketService             │    │
│  │  • CPU Work (math, hashing)    │◄───┼── CPU Usage ⬆️⬆️⬆️
│  │  • RAM Work (big arrays)       │◄───┼── RAM Usage ⬆️⬆️⬆️
│  │  • Database Work (save data)   │    │
│  └────────────┬───────────────────┘    │
│               │                         │
│               ▼                         │
│  ┌────────────────────────────────┐    │
│  │  PostgreSQL Database           │    │
│  │  (Runs in Docker)              │    │
│  └────────────┬───────────────────┘    │
└───────────────┼─────────────────────────┘
                │
                │ 2. Return Response
                │    {"status": "SUCCESS", "processingTimeMs": 3500}
                │
                ▼
       ┌─────────────┐
       │   You       │
       │ Get Results │
       └─────────────┘
```

## Load Intensity Guide

| Intensity | CPU Usage | RAM Usage | Best For |
|-----------|-----------|-----------|----------|
| 1-3 | Low (~20%) | ~10-30 MB | Learning, testing |
| 4-6 | Medium (~50%) | ~40-60 MB | Normal load testing |
| 7-9 | High (~80%) | ~70-90 MB | Stress testing |
| 10 | Max (~100%) | ~100+ MB | Extreme stress test |

## Example Requests

### Beginner (Safe)
```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"beginner","cpuIntensity":3,"ramIntensity":3,"processingTimeMs":1000}'
```

### Intermediate (Noticeable)
```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"intermediate","cpuIntensity":6,"ramIntensity":6,"processingTimeMs":2000}'
```

### Advanced (Heavy Load!)
```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"advanced","cpuIntensity":9,"ramIntensity":9,"processingTimeMs":5000}'
```

### Expert (Stress Test - 5 requests at once!)
```bash
# Mac/Linux/WSL
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d "{\"packetId\":\"expert-$i\",\"cpuIntensity\":8,\"ramIntensity\":8,\"processingTimeMs\":3000}" &
done
wait

# Windows (PowerShell)
1..5 | ForEach-Object { Start-Job -ScriptBlock { curl -X POST http://localhost:8080/api/v1/fakePacket -H "Content-Type: application/json" -d "{`"packetId`":`"expert-$_`",`"cpuIntensity`":8,`"ramIntensity`":8,`"processingTimeMs`":3000}" } } | Wait-Job | Receive-Job
```

## Monitoring Your Computer

**Windows:**
- Press `Ctrl + Shift + Esc` → Performance tab
- Watch CPU and Memory graphs spike!

**Mac:**
- Press `Cmd + Space` → type "Activity Monitor"
- Watch CPU and Memory usage increase!

**Linux:**
- Run `htop` in terminal
- See real-time CPU and memory usage!

## Stop Server

Press `Ctrl + C` in the terminal where server is running.

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Server won't start | Make sure Docker Desktop is running |
| Port 8080 in use | Stop other programs or change port in application.properties |
| curl not found | Install curl or use Postman |
| Java not found | Install Java 25 from Oracle website |

## What's Next?

📖 Read the full guide: `docs/summary_v1.md`
📚 Technical details: `README.md`

Have fun testing! 🚀
