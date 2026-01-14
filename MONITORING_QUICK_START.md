# Monitoring Quick Start Guide

## Overview

This guide helps you quickly set up and verify the monitoring system for CS4445 Subscription Server.

## Monitoring Architecture

```
┌─────────────────┐
│  Spring Boot    │
│  Application    │  Exposes metrics via /actuator/prometheus
│  (Port 8080)    │
└────────┬────────┘
         │
         │ Scrapes metrics every 15s
         ▼
┌─────────────────┐
│   Prometheus    │  Stores time-series data
│   (Port 9090)   │
└────────┬────────┘
         │
         │ Queries data
         ▼
┌─────────────────┐
│    Grafana      │  Visualizes metrics
│   (Port 3000)   │
└─────────────────┘
```

## Quick Start Steps

### Step 1: Start All Services

```bash
cd /path/to/CS4445-Sub-Server
docker compose up -d
```

**Verify all services are running:**

```bash
docker compose ps
```

Expected output:
```
NAME                   STATUS    PORTS
cs4445-app             healthy   0.0.0.0:8080->8080/tcp
cs4445-postgres        healthy   0.0.0.0:5432->5432/tcp
cs4445-prometheus      running   0.0.0.0:9090->9090/tcp
cs4445-grafana         running   0.0.0.0:3000->3000/tcp
```

---

### Step 2: Verify Metrics Endpoint

**Test the metrics endpoint:**

```bash
curl http://localhost:8080/actuator/prometheus | grep "app_"
```

**Expected output (sample):**

```
# HELP app_requests_total Total number of requests received
# TYPE app_requests_total counter
app_requests_total{application="CS4445-Sub-Server",environment="development",type="all",} 0.0

# HELP app_errors_total Total number of errors occurred
# TYPE app_errors_total counter
app_errors_total{application="CS4445-Sub-Server",environment="development",type="all",} 0.0

# HELP app_connections_total Total number of connections established
# TYPE app_connections_total counter
app_connections_total{application="CS4445-Sub-Server",environment="development",type="all",} 0.0

# HELP app_jobs_processed_total Total number of jobs processed successfully
# TYPE app_jobs_processed_total counter
app_jobs_processed_total{application="CS4445-Sub-Server",environment="development",type="all",} 0.0

# HELP app_connections_current Current number of active connections
# TYPE app_connections_current gauge
app_connections_current{application="CS4445-Sub-Server",environment="development",type="active",} 0.0

# HELP app_queue_length Current queue length
# TYPE app_queue_length gauge
app_queue_length{application="CS4445-Sub-Server",environment="development",type="pending",} 0.0
```

---

### Step 3: Verify Prometheus is Scraping

1. **Open Prometheus UI:** http://localhost:9090

2. **Check Targets:**
   - Go to **Status** → **Targets**
   - Look for `spring-boot` target
   - Status should be **UP**

3. **Test a Query:**
   - Go to **Graph** tab
   - Enter: `app_requests_total`
   - Click **Execute**
   - You should see the metric (value may be 0 initially)

**Alternative via curl:**

```bash
curl -s 'http://localhost:9090/api/v1/query?query=app_requests_total' | jq
```

---

### Step 4: Generate Test Traffic

**Generate some requests to populate metrics:**

```bash
# Send 50 test requests
for i in {1..50}; do
  curl -X POST http://localhost:8080/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d "{\"packetId\":\"test-$i\",\"cpuIntensity\":5,\"ramIntensity\":5,\"processingTimeMs\":1000}"
  echo "Sent request $i"
done
```

**Or use this faster parallel version:**

```bash
# Install GNU parallel if needed: apt-get install parallel
seq 1 50 | parallel -j 10 curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d "{\"packetId\":\"test-{}\",\"cpuIntensity\":5,\"ramIntensity\":5}"
```

---

### Step 5: Verify Metrics Are Updating

**Check via API endpoint:**

```bash
curl http://localhost:8080/api/v1/metrics/summary
```

**Expected output:**

```
Metrics Summary - Total Requests: 50, Total Errors: 0, Total Connections: 50, Jobs Processed: 50, Current Connections: 0, Queue Length: 0
```

**Check in Prometheus:**

1. Go to http://localhost:9090/graph
2. Run these queries:

```promql
# Total requests (should be 50)
app_requests_total

# Request rate per second
rate(app_requests_total[1m])

# Current queue length
app_queue_length

# Processing time p95
histogram_quantile(0.95, rate(app_processing_time_seconds_bucket[5m])) * 1000
```

---

### Step 6: Set Up Grafana

1. **Open Grafana:** http://localhost:3000

2. **Login:**
   - Username: `admin`
   - Password: `admin`
   - (You'll be prompted to change password - you can skip)

3. **Add Prometheus Data Source:**
   - Click **Configuration** (⚙️) → **Data Sources**
   - Click **Add data source**
   - Select **Prometheus**
   - Set URL: `http://prometheus:9090`
   - Click **Save & Test**
   - Should see: ✅ "Data source is working"

4. **Create Your First Dashboard:**
   - Click **+** → **Dashboard**
   - Click **Add new panel**
   - In Query, enter: `rate(app_requests_total[5m]) * 60`
   - Set legend: `Requests per minute`
   - Panel title: `Request Rate`
   - Click **Apply**
   - Click **💾 Save** (top right)
   - Give it a name: `CS4445 Monitoring`

---

## Verification Checklist

Use this checklist to verify everything is working:

- [ ] Docker Compose services are all running
- [ ] Application is accessible at http://localhost:8080
- [ ] Metrics endpoint returns data: http://localhost:8080/actuator/prometheus
- [ ] Prometheus UI is accessible: http://localhost:9090
- [ ] Prometheus shows app target as UP
- [ ] Grafana UI is accessible: http://localhost:3000
- [ ] Prometheus data source is connected in Grafana
- [ ] Test requests generate metric changes
- [ ] Metrics summary endpoint returns data: http://localhost:8080/api/v1/metrics/summary

---

## Testing All Metric Types

### Test Counter Metrics

```bash
# Generate requests
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d '{"packetId":"counter-test","cpuIntensity":3,"ramIntensity":3}'
done

# Check in Prometheus
# Query: app_requests_total
# Should see value increasing
```

### Test Gauge Metrics

```bash
# Send concurrent requests to test current connections
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d '{"packetId":"gauge-test","cpuIntensity":10,"ramIntensity":10,"processingTimeMs":5000}' &
done

# Quickly check current connections while requests are processing
curl http://localhost:8080/actuator/prometheus | grep "app_connections_current"

# Prometheus Query: app_connections_current
# Should see > 0 while requests are processing
```

### Test Histogram Metrics

```bash
# Generate requests with varying processing times
for intensity in 1 3 5 7 10; do
  for i in {1..20}; do
    curl -X POST http://localhost:8080/api/v1/fakePacket \
      -H "Content-Type: application/json" \
      -d "{\"packetId\":\"histogram-$intensity-$i\",\"cpuIntensity\":$intensity,\"ramIntensity\":$intensity}"
  done
done

# Prometheus Queries:
# P50: histogram_quantile(0.50, rate(app_processing_time_seconds_bucket[5m])) * 1000
# P95: histogram_quantile(0.95, rate(app_processing_time_seconds_bucket[5m])) * 1000
# P99: histogram_quantile(0.99, rate(app_processing_time_seconds_bucket[5m])) * 1000
```

### Test Error Metrics

```bash
# Close server to generate errors
curl -X POST "http://localhost:8080/api/v1/server/close?reason=Testing"

# Send requests (will be rejected)
for i in {1..20}; do
  curl -X POST http://localhost:8080/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d '{"packetId":"error-test","cpuIntensity":5,"ramIntensity":5}'
done

# Reopen server
curl -X POST "http://localhost:8080/api/v1/server/open?reason=Testing complete"

# Check error metrics
# Prometheus Query: app_errors_total
# Should see ~20 errors
# Query by type: sum by (type) (app_errors_total)
```

---

## Monitoring Schema Verification

### Verify All Counters

```bash
curl http://localhost:8080/actuator/prometheus | grep -E "(app_requests_total|app_errors_total|app_connections_total|app_jobs_processed_total)"
```

**Expected:**
- ✅ `app_requests_total` - Counter
- ✅ `app_errors_total` - Counter
- ✅ `app_connections_total` - Counter
- ✅ `app_jobs_processed_total` - Counter

### Verify All Gauges

```bash
curl http://localhost:8080/actuator/prometheus | grep -E "(process_cpu_usage|jvm_memory_used_bytes|app_connections_current|app_queue_length)"
```

**Expected:**
- ✅ `process_cpu_usage` - Gauge (CPU usage)
- ✅ `jvm_memory_used_bytes` - Gauge (RAM usage)
- ✅ `app_connections_current` - Gauge (Current connections)
- ✅ `app_queue_length` - Gauge (Queue length)

### Verify All Histograms

```bash
curl http://localhost:8080/actuator/prometheus | grep -E "(app_request_latency_seconds|app_response_size_bytes|app_processing_time_seconds)"
```

**Expected:**
- ✅ `app_request_latency_seconds_*` - Histogram (Request latency)
- ✅ `app_response_size_bytes_*` - Histogram (Response size)
- ✅ `app_processing_time_seconds_*` - Histogram (Processing time)

---

## Essential Prometheus Queries

### Counters

```promql
# Total requests
app_requests_total

# Request rate (req/s)
rate(app_requests_total[5m])

# Request rate (req/min)
rate(app_requests_total[5m]) * 60

# Total requests in last hour
increase(app_requests_total[1h])
```

### Gauges

```promql
# Current CPU usage (%)
process_cpu_usage * 100

# Current memory usage (%)
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# Current connections
app_connections_current

# Current queue length
app_queue_length
```

### Histograms

```promql
# Average request latency (ms)
(rate(app_request_latency_seconds_sum[5m]) / rate(app_request_latency_seconds_count[5m])) * 1000

# P95 request latency (ms)
histogram_quantile(0.95, rate(app_request_latency_seconds_bucket[5m])) * 1000

# Average processing time (ms)
(rate(app_processing_time_seconds_sum[5m]) / rate(app_processing_time_seconds_count[5m])) * 1000

# Average response size (bytes)
rate(app_response_size_bytes_sum[5m]) / rate(app_response_size_bytes_count[5m])
```

---

## Load Testing for Metrics

### Scenario 1: Sustained Load

```bash
# 500 requests at moderate intensity
for i in {1..500}; do
  curl -X POST http://localhost:8080/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d "{\"packetId\":\"load-$i\",\"cpuIntensity\":5,\"ramIntensity\":5}"
done
```

**Metrics to watch:**
- Request rate
- CPU usage
- Memory usage
- Queue length

### Scenario 2: Burst Load

```bash
# Send 100 concurrent requests
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d "{\"packetId\":\"burst-$i\",\"cpuIntensity\":8,\"ramIntensity\":8}" &
done
wait
```

**Metrics to watch:**
- Current connections spike
- Queue length spike
- Request latency increase

### Scenario 3: Variable Load

```bash
# Variable intensity
for intensity in 1 2 3 5 8 10 8 5 3 2 1; do
  for i in {1..50}; do
    curl -X POST http://localhost:8080/api/v1/fakePacket \
      -H "Content-Type: application/json" \
      -d "{\"packetId\":\"variable-$intensity-$i\",\"cpuIntensity\":$intensity,\"ramIntensity\":$intensity}"
  done
  echo "Completed intensity level: $intensity"
done
```

**Metrics to watch:**
- Processing time variation
- CPU/Memory usage correlation with intensity

---

## Troubleshooting

### Metrics not showing in Prometheus

**Problem:** Prometheus can't scrape metrics

**Solutions:**

1. Check if app is running:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. Check if metrics endpoint is accessible:
   ```bash
   curl http://localhost:8080/actuator/prometheus
   ```

3. Check Prometheus config:
   ```bash
   cat prometheus.yml
   ```

4. Check Prometheus logs:
   ```bash
   docker compose logs prometheus
   ```

5. Verify Prometheus can reach app:
   ```bash
   docker exec cs4445-prometheus wget -O- http://app:8080/actuator/prometheus
   ```

---

### Grafana can't connect to Prometheus

**Problem:** Data source test fails

**Solutions:**

1. Check Prometheus URL in Grafana:
   - Should be `http://prometheus:9090` (Docker network)
   - NOT `http://localhost:9090`

2. Check Prometheus is running:
   ```bash
   docker compose ps prometheus
   ```

3. Test from Grafana container:
   ```bash
   docker exec cs4445-grafana wget -O- http://prometheus:9090/api/v1/query?query=up
   ```

---

### No data in dashboards

**Problem:** Panels show "No Data"

**Solutions:**

1. Check time range (top right in Grafana)
   - Set to "Last 15 minutes" or "Last 1 hour"

2. Check if metrics exist in Prometheus:
   - Go to Prometheus UI: http://localhost:9090
   - Run the same query

3. Generate test data:
   ```bash
   # Send some requests
   for i in {1..10}; do
     curl -X POST http://localhost:8080/api/v1/fakePacket \
       -H "Content-Type: application/json" \
       -d '{"packetId":"test","cpuIntensity":5,"ramIntensity":5}'
   done
   ```

4. Check query syntax in panel edit mode

---

## Next Steps

Once you've verified everything is working:

1. ✅ Read [PROMETHEUS_METRICS_GUIDE.md](PROMETHEUS_METRICS_GUIDE.md) for detailed query documentation
2. ✅ Read [GRAFANA_DASHBOARD_GUIDE.md](GRAFANA_DASHBOARD_GUIDE.md) for dashboard setup
3. ✅ Use [INSOMNIA_TEST_GUIDE.md](INSOMNIA_TEST_GUIDE.md) for API testing
4. ✅ Use [SQL_QUERY_GUIDE.md](SQL_QUERY_GUIDE.md) for database verification
5. ✅ Set up alerts for critical metrics
6. ✅ Create custom dashboards for your needs
7. ✅ Document your monitoring strategy

---

## Quick Reference Commands

```bash
# Start all services
docker compose up -d

# Stop all services
docker compose down

# View logs
docker compose logs -f app
docker compose logs -f prometheus
docker compose logs -f grafana

# Restart services
docker compose restart app

# Check metrics endpoint
curl http://localhost:8080/actuator/prometheus | grep app_

# Get metrics summary
curl http://localhost:8080/api/v1/metrics/summary

# Test with a single request
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"test","cpuIntensity":5,"ramIntensity":5}'

# Access UIs
# Application: http://localhost:8080
# Prometheus: http://localhost:9090
# Grafana: http://localhost:3000
```

---

Happy Monitoring! 📊🚀
