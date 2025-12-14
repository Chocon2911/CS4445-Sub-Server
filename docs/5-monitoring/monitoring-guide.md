# Prometheus & Grafana Monitoring Guide

## Overview

This guide explains how to set up and use Prometheus and Grafana to monitor your CS4445 Sub Server application.

### What Is Monitoring?

**Monitoring** means watching your application to see:
- How much CPU and memory it's using
- How many requests it's receiving
- How long requests take to process
- If there are any errors
- Database connection status

### Tools We're Using

1. **Prometheus** - Collects metrics (measurements) from your application
2. **Grafana** - Displays metrics in beautiful graphs and dashboards
3. **Spring Boot Actuator** - Exposes metrics from your Spring Boot app

## Architecture

```
┌─────────────────┐
│  Your App       │
│  (Port 8080)    │
│  /actuator/     │
│  prometheus     │
└────────┬────────┘
         │
         │ Scrapes metrics every 10s
         │
         ▼
┌─────────────────┐
│  Prometheus     │
│  (Port 9090)    │
│  Stores metrics │
└────────┬────────┘
         │
         │ Queries metrics
         │
         ▼
┌─────────────────┐
│  Grafana        │
│  (Port 3000)    │
│  Displays       │
│  dashboards     │
└─────────────────┘
```

## Quick Start

### 1. Start All Services

```bash
# Start PostgreSQL, Prometheus, and Grafana
docker compose up -d

# Start your Spring Boot application
./mvnw spring-boot:run
```

### 2. Access the Services

| Service | URL | Credentials |
|---------|-----|-------------|
| Your Application | http://localhost:8080 | None |
| Prometheus | http://localhost:9090 | None |
| Grafana | http://localhost:3000 | admin / admin |

### 3. View Metrics

**Option 1: Grafana Dashboard (Recommended)**
1. Open http://localhost:3000
2. Login with `admin` / `admin`
3. Dashboard "CS4445 Sub Server Dashboard" will load automatically
4. See real-time graphs of CPU, memory, requests, etc.

**Option 2: Prometheus UI**
1. Open http://localhost:9090
2. Click "Graph"
3. Enter a query (examples below)
4. Click "Execute"

**Option 3: Raw Metrics**
1. Open http://localhost:8080/actuator/prometheus
2. See all raw metrics in Prometheus format

## Understanding the Grafana Dashboard

### Panel 1: CPU Usage
- **What it shows**: How much CPU your application is using (0-100%)
- **Why it's important**: High CPU usage means your app is working hard
- **Normal range**: 0-20% idle, 50-80% under load, >90% stress test

### Panel 2: JVM Memory Usage
- **What it shows**: How much RAM the Java application is using
- **Why it's important**: Shows memory allocation and garbage collection
- **Types**:
  - Heap: Main memory for your objects
  - Non-heap: JVM internal memory

### Panel 3: HTTP Requests Rate
- **What it shows**: How many HTTP requests per second
- **Why it's important**: Shows how busy your server is
- **During testing**: Will spike when you send fakePacket requests

### Panel 4: HTTP Request Duration (95th percentile)
- **What it shows**: How long requests take (95% of requests are faster than this)
- **Why it's important**: Shows performance - lower is better
- **Expected values**:
  - Low intensity (1-3): 0.5 - 2 seconds
  - Medium intensity (4-7): 2 - 5 seconds
  - High intensity (8-10): 5 - 15 seconds

### Panel 5: Active DB Connections
- **What it shows**: How many database connections are in use
- **Why it's important**: Shows database load
- **Normal range**: 1-5 connections (max configured: 10)

### Panel 6: Application Status
- **What it shows**: Is your app running? (UP/DOWN)
- **Why it's important**: Quick health check
- **States**:
  - GREEN (UP): Application is running
  - RED (DOWN): Application is not responding

## Common Prometheus Queries

You can use these in Prometheus UI or create custom Grafana panels.

### CPU Usage
```promql
# Current CPU usage as percentage
system_cpu_usage{application="CS4445-Sub-Server"} * 100

# Average CPU over last 5 minutes
avg_over_time(system_cpu_usage{application="CS4445-Sub-Server"}[5m]) * 100
```

### Memory Usage
```promql
# Total JVM memory used
sum(jvm_memory_used_bytes{application="CS4445-Sub-Server"})

# Heap memory used (in MB)
jvm_memory_used_bytes{application="CS4445-Sub-Server", area="heap"} / 1024 / 1024
```

### HTTP Requests
```promql
# Total requests
sum(http_server_requests_seconds_count{application="CS4445-Sub-Server"})

# Requests per second (last 1 minute)
rate(http_server_requests_seconds_count{application="CS4445-Sub-Server"}[1m])

# Requests by status code
sum by (status) (http_server_requests_seconds_count{application="CS4445-Sub-Server"})
```

### Request Duration
```promql
# Average request duration
rate(http_server_requests_seconds_sum{application="CS4445-Sub-Server"}[5m]) /
rate(http_server_requests_seconds_count{application="CS4445-Sub-Server"}[5m])

# 95th percentile latency
histogram_quantile(0.95,
  sum(rate(http_server_requests_seconds_bucket{application="CS4445-Sub-Server"}[5m])) by (le, uri)
)
```

### Database Connections
```promql
# Active connections
hikaricp_connections_active{application="CS4445-Sub-Server"}

# Idle connections
hikaricp_connections_idle{application="CS4445-Sub-Server"}

# Total connections
hikaricp_connections{application="CS4445-Sub-Server"}
```

### Application Health
```promql
# Is application up? (1 = yes, 0 = no)
up{job="spring-boot-app"}
```

## Testing the Monitoring

### Test 1: Send Low Load Request and Watch Metrics

```bash
# Send a low load request
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"monitor-test-1","cpuIntensity":3,"ramIntensity":3,"processingTimeMs":2000}'

# Watch Grafana dashboard:
# - CPU should increase slightly
# - Memory should increase slightly
# - Request rate should show 1 request
# - Request duration should be ~2 seconds
```

### Test 2: Send High Load Request and Watch Metrics

```bash
# Send a high load request
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"monitor-test-2","cpuIntensity":10,"ramIntensity":10,"processingTimeMs":5000}'

# Watch Grafana dashboard:
# - CPU should spike to 70-100%
# - Memory should increase significantly
# - Request duration should be 5+ seconds
```

### Test 3: Stress Test with Multiple Concurrent Requests

```bash
# Send 10 concurrent requests
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d "{\"packetId\":\"stress-$i\",\"cpuIntensity\":8,\"ramIntensity\":8,\"processingTimeMs\":3000}" &
done
wait

# Watch Grafana dashboard:
# - Request rate should spike to 10 req/s
# - CPU should max out
# - Memory should increase a lot
# - Active DB connections should increase
```

## Available Actuator Endpoints

Your application exposes these monitoring endpoints:

| Endpoint | Description | Example |
|----------|-------------|---------|
| /actuator/health | Health status | http://localhost:8080/actuator/health |
| /actuator/info | Application info | http://localhost:8080/actuator/info |
| /actuator/metrics | List all metrics | http://localhost:8080/actuator/metrics |
| /actuator/metrics/{name} | Specific metric | http://localhost:8080/actuator/metrics/jvm.memory.used |
| /actuator/prometheus | Prometheus format | http://localhost:8080/actuator/prometheus |

### Example: View Specific Metric

```bash
# Get JVM memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Get HTTP request count
curl http://localhost:8080/actuator/metrics/http.server.requests

# Get CPU usage
curl http://localhost:8080/actuator/metrics/system.cpu.usage
```

## Customizing the Dashboard

### Add a New Panel in Grafana

1. Open Grafana (http://localhost:3000)
2. Open "CS4445 Sub Server Dashboard"
3. Click "Add panel" (top right)
4. Click "Add an empty panel"
5. In the query editor, enter a Prometheus query
6. Customize visualization (graph type, colors, etc.)
7. Click "Apply"
8. Click "Save dashboard" (disk icon)

### Example: Add Panel for Error Rate

1. Add new panel
2. Query:
   ```promql
   rate(http_server_requests_seconds_count{application="CS4445-Sub-Server",status=~"5.."}[1m])
   ```
3. Title: "Error Rate (5xx responses)"
4. Unit: "reqps" (requests per second)
5. Apply and save

## Docker Commands

### Start Monitoring Stack
```bash
docker compose up -d
```

### Stop Monitoring Stack
```bash
docker compose down
```

### View Logs
```bash
# Prometheus logs
docker logs cs4445-prometheus

# Grafana logs
docker logs cs4445-grafana

# PostgreSQL logs
docker logs cs4445-postgres
```

### Restart a Service
```bash
# Restart Prometheus
docker compose restart prometheus

# Restart Grafana
docker compose restart grafana
```

### Remove All Data (Fresh Start)
```bash
# Stop and remove containers and volumes
docker compose down -v

# Start fresh
docker compose up -d
```

## Troubleshooting

### Problem 1: Grafana shows "No Data"

**Symptoms**: Grafana dashboard is empty or shows "No Data"

**Solutions**:
1. Check if Spring Boot app is running:
   ```bash
   curl http://localhost:8080/actuator/health
   ```
2. Check if Prometheus can reach your app:
   - Open http://localhost:9090/targets
   - Look for "spring-boot-app" target
   - Should show "UP" in green
3. If target is "DOWN":
   - Make sure app is running on port 8080
   - Check prometheus.yml configuration
   - On Linux, change target to `172.17.0.1:8080` instead of `host.docker.internal:8080`

### Problem 2: Prometheus shows "connection refused"

**Symptoms**: Prometheus targets page shows "connection refused"

**Solutions**:
1. Check app is running: `curl http://localhost:8080/actuator/prometheus`
2. On Linux, edit `prometheus.yml`:
   ```yaml
   - targets: ['172.17.0.1:8080']  # Instead of host.docker.internal:8080
   ```
3. Restart Prometheus: `docker compose restart prometheus`

### Problem 3: Can't login to Grafana

**Symptoms**: Login page doesn't accept credentials

**Solution**:
- Default credentials: `admin` / `admin`
- If changed: Reset by removing Grafana volume:
  ```bash
  docker compose down
  docker volume rm cs4445-sub-server_grafana-data
  docker compose up -d
  ```

### Problem 4: Metrics not updating

**Symptoms**: Dashboard shows old data

**Solutions**:
1. Check Prometheus is scraping:
   - Open http://localhost:9090/targets
   - Verify "Last Scrape" time is recent
2. Check Grafana refresh:
   - Top right corner, set refresh to "5s"
3. Reload Prometheus config:
   ```bash
   curl -X POST http://localhost:9090/-/reload
   ```

### Problem 5: Port already in use

**Symptoms**: Error starting services

**Solutions**:
```bash
# Check what's using the port
lsof -i :9090  # Prometheus
lsof -i :3000  # Grafana

# Kill the process or change port in compose.yaml
```

## Advanced: Adding Custom Metrics

You can add custom metrics to your application.

### 1. Create a Custom Metric

Add to your service class:

```java
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

@Service
public class FakePacketService {

    private final Counter packetsProcessed;

    public FakePacketService(MeterRegistry registry) {
        this.packetsProcessed = Counter.builder("packets.processed")
            .description("Total packets processed")
            .tag("type", "fake")
            .register(registry);
    }

    public void processFakePacket(...) {
        // Process packet

        // Increment counter
        packetsProcessed.increment();
    }
}
```

### 2. View in Prometheus

Query: `packets_processed_total`

### 3. Add to Grafana Dashboard

Create panel with query: `rate(packets_processed_total[1m])`

## Monitoring Best Practices

1. **Set up alerts**: Configure Grafana alerts for high CPU, low memory, errors
2. **Monitor trends**: Look at metrics over time, not just current values
3. **Baseline performance**: Know what's "normal" for your app
4. **Test under load**: Use monitoring during stress tests
5. **Review regularly**: Check dashboards daily during development

## Useful Resources

- **Prometheus Documentation**: https://prometheus.io/docs/
- **Grafana Documentation**: https://grafana.com/docs/
- **Spring Boot Actuator**: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
- **Micrometer**: https://micrometer.io/docs

## Summary

You now have:
- ✅ Prometheus collecting metrics every 10 seconds
- ✅ Grafana dashboard showing real-time performance
- ✅ Spring Boot Actuator exposing application metrics
- ✅ Docker Compose managing all services
- ✅ Pre-configured dashboard with key metrics

**Quick Access URLs:**
- Application: http://localhost:8080
- Grafana: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9090
- Metrics: http://localhost:8080/actuator/prometheus

Happy monitoring! 📊
