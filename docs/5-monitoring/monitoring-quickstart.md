# Monitoring Quick Start (5 Minutes)

## What You'll Get

Real-time graphs showing:
- 📊 CPU usage
- 💾 Memory usage
- 🌐 HTTP requests per second
- ⏱️ Request processing time
- 🔗 Database connections
- ✅ Application health status

## Setup in 3 Steps

### Step 1: Start All Services

```bash
# Start monitoring stack (Prometheus, Grafana, PostgreSQL)
docker compose up -d

# Start your application
./mvnw spring-boot:run
```

### Step 2: Open Grafana

1. Open browser: http://localhost:3000
2. Login:
   - Username: `admin`
   - Password: `admin`
3. Dashboard "CS4445 Sub Server Dashboard" loads automatically

### Step 3: Generate Some Load

```bash
# Send a test request
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"test","cpuIntensity":7,"ramIntensity":7,"processingTimeMs":3000}'
```

**Watch the Grafana dashboard** - you'll see:
- CPU spike up
- Memory increase
- Request rate increase
- Request duration ~3 seconds

## Access URLs

| Service | URL | Login |
|---------|-----|-------|
| **Grafana Dashboard** | http://localhost:3000 | admin/admin |
| **Prometheus UI** | http://localhost:9090 | None |
| **Raw Metrics** | http://localhost:8080/actuator/prometheus | None |
| **Health Check** | http://localhost:8080/actuator/health | None |

## What the Dashboard Shows

```
┌─────────────────────────────────────────────────────┐
│         CS4445 Sub Server Dashboard                 │
├─────────────────────┬───────────────────────────────┤
│  CPU Usage %        │  JVM Memory Usage (bytes)     │
│  Real-time graph    │  Heap vs Non-heap memory      │
├─────────────────────┼───────────────────────────────┤
│  HTTP Requests/sec  │  Request Duration (p95)       │
│  Traffic rate       │  Performance metrics          │
├─────────────────────┼───────────────────────────────┤
│  Active DB Conn     │  Application Status           │
│  Gauge (0-10)       │  UP/DOWN indicator            │
└─────────────────────┴───────────────────────────────┘
```

## Test Scenarios

### Scenario 1: Light Load
```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"light","cpuIntensity":3,"ramIntensity":3,"processingTimeMs":1000}'
```
**Expected**: Small increase in CPU and memory

### Scenario 2: Heavy Load
```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"heavy","cpuIntensity":10,"ramIntensity":10,"processingTimeMs":5000}'
```
**Expected**: CPU spikes to 80-100%, large memory increase

### Scenario 3: Stress Test (10 concurrent requests)
```bash
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d "{\"packetId\":\"stress-$i\",\"cpuIntensity\":8,\"ramIntensity\":8,\"processingTimeMs\":3000}" &
done
wait
```
**Expected**: Request rate spikes, CPU maxes out, memory increases significantly

## Using Prometheus Directly

1. Open http://localhost:9090
2. Click "Graph" tab
3. Try these queries:

**CPU Usage:**
```
system_cpu_usage * 100
```

**Memory Used (MB):**
```
jvm_memory_used_bytes / 1024 / 1024
```

**Requests per second:**
```
rate(http_server_requests_seconds_count[1m])
```

4. Click "Execute" to see the graph

## Stop Monitoring

```bash
# Stop all services
docker compose down

# Stop and remove all data (fresh start next time)
docker compose down -v
```

## Troubleshooting

**Dashboard shows "No Data"?**
1. Check app is running: `curl http://localhost:8080/actuator/health`
2. Check Prometheus targets: http://localhost:9090/targets (should show "UP")
3. Wait 15-30 seconds for first data collection

**Can't access Grafana?**
- Make sure port 3000 isn't already in use
- Check Docker: `docker ps` (should see cs4445-grafana)

**Prometheus can't reach app?**
- On Linux, edit `prometheus.yml`: change `host.docker.internal:8080` to `172.17.0.1:8080`
- Restart: `docker compose restart prometheus`

## What's Next?

- 📖 Read full guide: [docs/monitoring-guide.md](monitoring-guide.md)
- 🎨 Customize dashboards in Grafana
- 📊 Create custom metrics
- 🔔 Set up alerts for critical metrics

## Visual Flow

```
Send Request → Spring Boot App → Generates Metrics
                      ↓
              Prometheus Scrapes Metrics (every 10s)
                      ↓
              Stores in Time-Series Database
                      ↓
              Grafana Queries Prometheus
                      ↓
              Displays Beautiful Graphs
                      ↓
              You See Real-Time Performance! 🎉
```

## Key Files Created

```
prometheus.yml                          # Prometheus config
compose.yaml                            # Docker services
grafana/provisioning/
  ├── datasources/prometheus.yml        # Auto-configure Prometheus
  └── dashboards/
      ├── dashboard.yml                 # Dashboard provider
      └── cs4445-dashboard.json         # Pre-built dashboard
```

Happy monitoring! 📊
