# CS4445 Monitoring Schema Documentation

## Schema Overview

This document defines the complete monitoring schema for the CS4445 Subscription Server according to the project requirements.

---

## Metric Categories

### 1. COUNTER Metrics (Cumulative, Always Increasing)

| Metric Name | Description | Prometheus Query | Unit |
|-------------|-------------|------------------|------|
| `app_requests_total` | Tổng số request (Total requests) | `app_requests_total` | count |
| `app_errors_total` | Tổng số error (Total errors) | `app_errors_total` | count |
| `app_connections_total` | Tổng số connection (Total connections) | `app_connections_total` | count |
| `app_jobs_processed_total` | Tổng số job đã xử lý (Total processed jobs) | `app_jobs_processed_total` | count |

**Example Queries:**

```promql
# Request rate per second
rate(app_requests_total[5m])

# Total errors in last hour
increase(app_errors_total[1h])

# Success rate percentage
((app_requests_total - app_errors_total) / app_requests_total) * 100
```

---

### 2. GAUGE Metrics (Current State, Can Go Up/Down)

| Metric Name | Description | Prometheus Query | Unit |
|-------------|-------------|------------------|------|
| `process_cpu_usage` | CPU usage | `process_cpu_usage * 100` | percent |
| `jvm_memory_used_bytes` | RAM usage | `jvm_memory_used_bytes{area="heap"}` | bytes |
| `app_connections_current` | Số connection hiện tại (Current connections) | `app_connections_current` | count |
| `app_queue_length` | Queue length | `app_queue_length` | count |

**Example Queries:**

```promql
# Current CPU usage (%)
process_cpu_usage * 100

# Current memory usage (MB)
jvm_memory_used_bytes{area="heap"} / 1024 / 1024

# Memory usage percentage
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# Average connections over 5 minutes
avg_over_time(app_connections_current[5m])
```

---

### 3. HISTOGRAM Metrics (Distribution of Values)

| Metric Name | Description | Prometheus Query (P95) | Unit |
|-------------|-------------|-------------------------|------|
| `app_request_latency_seconds` | Request latency | `histogram_quantile(0.95, rate(app_request_latency_seconds_bucket[5m])) * 1000` | milliseconds |
| `app_response_size_bytes` | Response size | `histogram_quantile(0.95, rate(app_response_size_bytes_bucket[5m]))` | bytes |
| `app_processing_time_seconds` | Processing time | `histogram_quantile(0.95, rate(app_processing_time_seconds_bucket[5m])) * 1000` | milliseconds |

**Example Queries:**

```promql
# Average request latency
(rate(app_request_latency_seconds_sum[5m]) / rate(app_request_latency_seconds_count[5m])) * 1000

# P50, P95, P99 latency
histogram_quantile(0.50, rate(app_request_latency_seconds_bucket[5m])) * 1000
histogram_quantile(0.95, rate(app_request_latency_seconds_bucket[5m])) * 1000
histogram_quantile(0.99, rate(app_request_latency_seconds_bucket[5m])) * 1000

# Average response size (KB)
(rate(app_response_size_bytes_sum[5m]) / rate(app_response_size_bytes_count[5m])) / 1024
```

---

## Complete Metric Mapping

### Counters

#### 1. Tổng số request (Total Requests)

**Metric:** `app_requests_total`

**Type:** Counter

**Description:** Tracks every incoming request to the API

**Tags:**
- `type="all"` - All requests
- `application="CS4445-Sub-Server"`
- `environment="development"`

**Implementation:**
```java
// In FakePacketController.java
metricsService.incrementTotalRequests();
```

**Prometheus Queries:**
```promql
# Total requests
app_requests_total

# Request rate (requests/second)
rate(app_requests_total[5m])

# Requests per minute
rate(app_requests_total[5m]) * 60

# Total requests today
increase(app_requests_total[24h])
```

---

#### 2. Tổng số error (Total Errors)

**Metric:** `app_errors_total`

**Type:** Counter

**Description:** Tracks all errors (server closed, exceptions, etc.)

**Tags:**
- `type="all"` - All errors
- `type="server_closed"` - Server closed errors
- `type="exception"` - Exception errors

**Implementation:**
```java
// In FakePacketController.java
metricsService.incrementTotalErrors("server_closed");
metricsService.incrementTotalErrors("exception");
```

**Prometheus Queries:**
```promql
# Total errors
app_errors_total

# Error rate
rate(app_errors_total[5m])

# Errors by type
sum by (type) (app_errors_total)

# Error percentage
(app_errors_total / app_requests_total) * 100
```

---

#### 3. Tổng số connection (Total Connections)

**Metric:** `app_connections_total`

**Type:** Counter

**Description:** Cumulative count of all connections made

**Implementation:**
```java
// In FakePacketController.java
metricsService.incrementTotalConnections();
```

**Prometheus Queries:**
```promql
# Total connections since startup
app_connections_total

# Connection rate
rate(app_connections_total[5m])

# Connections per minute
rate(app_connections_total[1m]) * 60
```

---

#### 4. Tổng số job đã xử lý (Total Jobs Processed)

**Metric:** `app_jobs_processed_total`

**Type:** Counter

**Description:** Successfully processed jobs

**Implementation:**
```java
// In FakePacketController.java
metricsService.incrementTotalJobsProcessed();
```

**Prometheus Queries:**
```promql
# Total jobs processed
app_jobs_processed_total

# Job processing rate
rate(app_jobs_processed_total[5m])

# Success rate
(app_jobs_processed_total / app_requests_total) * 100
```

---

### Gauges

#### 5. CPU usage

**Metric:** `process_cpu_usage`

**Type:** Gauge (Auto-collected by Micrometer)

**Description:** Current CPU usage of the application process

**Value Range:** 0.0 to 1.0 (0% to 100%)

**Prometheus Queries:**
```promql
# Current CPU usage (%)
process_cpu_usage * 100

# Average CPU over 5 minutes
avg_over_time(process_cpu_usage[5m]) * 100

# Max CPU in last hour
max_over_time(process_cpu_usage[1h]) * 100

# System CPU
system_cpu_usage * 100
```

---

#### 6. RAM usage

**Metric:** `jvm_memory_used_bytes`

**Type:** Gauge (Auto-collected by Micrometer)

**Description:** Current memory usage of the JVM

**Tags:**
- `area="heap"` - Heap memory
- `area="nonheap"` - Non-heap memory

**Prometheus Queries:**
```promql
# Heap memory used (bytes)
jvm_memory_used_bytes{area="heap"}

# Heap memory used (MB)
jvm_memory_used_bytes{area="heap"} / 1024 / 1024

# Heap memory used (GB)
jvm_memory_used_bytes{area="heap"} / 1024 / 1024 / 1024

# Memory usage percentage
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# Total memory (heap + non-heap)
sum(jvm_memory_used_bytes)
```

---

#### 7. Số connection hiện tại (Current Connections)

**Metric:** `app_connections_current`

**Type:** Gauge

**Description:** Number of currently active connections

**Implementation:**
```java
// In FakePacketController.java
metricsService.incrementCurrentConnections();  // On request start
metricsService.decrementCurrentConnections();  // On request end
```

**Prometheus Queries:**
```promql
# Current connections
app_connections_current

# Average concurrent connections
avg_over_time(app_connections_current[5m])

# Max concurrent connections
max_over_time(app_connections_current[1h])

# Connection utilization (if max is 100)
(app_connections_current / 100) * 100
```

---

#### 8. Queue length

**Metric:** `app_queue_length`

**Type:** Gauge

**Description:** Current number of jobs waiting in queue

**Implementation:**
```java
// In FakePacketController.java
metricsService.incrementQueueLength();  // When job enters queue
metricsService.decrementQueueLength();  // When job starts processing
```

**Prometheus Queries:**
```promql
# Current queue length
app_queue_length

# Average queue length
avg_over_time(app_queue_length[5m])

# Max queue length
max_over_time(app_queue_length[1h])

# Alert when queue > 20
app_queue_length > 20
```

---

### Histograms

#### 9. Request latency

**Metric:** `app_request_latency_seconds`

**Type:** Histogram/Timer

**Description:** Total request latency from start to finish

**Implementation:**
```java
// In FakePacketController.java
Timer.Sample sample = metricsService.startRequestLatencyTimer();
// ... process request ...
metricsService.stopRequestLatencyTimer(sample);
```

**Prometheus Queries:**
```promql
# Average latency (ms)
(rate(app_request_latency_seconds_sum[5m]) / rate(app_request_latency_seconds_count[5m])) * 1000

# Median (P50)
histogram_quantile(0.50, rate(app_request_latency_seconds_bucket[5m])) * 1000

# P95 latency
histogram_quantile(0.95, rate(app_request_latency_seconds_bucket[5m])) * 1000

# P99 latency
histogram_quantile(0.99, rate(app_request_latency_seconds_bucket[5m])) * 1000

# Max latency
max_over_time(app_request_latency_seconds_max[5m]) * 1000
```

---

#### 10. Response size

**Metric:** `app_response_size_bytes`

**Type:** Histogram/Distribution Summary

**Description:** Size of HTTP responses in bytes

**Implementation:**
```java
// In FakePacketController.java
String responseJson = objectMapper.writeValueAsString(response);
long responseSize = responseJson.getBytes().length;
metricsService.recordResponseSize(responseSize);
```

**Prometheus Queries:**
```promql
# Average response size (bytes)
rate(app_response_size_bytes_sum[5m]) / rate(app_response_size_bytes_count[5m])

# Average response size (KB)
(rate(app_response_size_bytes_sum[5m]) / rate(app_response_size_bytes_count[5m])) / 1024

# P95 response size
histogram_quantile(0.95, rate(app_response_size_bytes_bucket[5m]))

# Total bandwidth (bytes/sec)
rate(app_response_size_bytes_sum[5m])

# Total bandwidth (MB/sec)
rate(app_response_size_bytes_sum[5m]) / 1024 / 1024
```

---

#### 11. Processing time

**Metric:** `app_processing_time_seconds`

**Type:** Histogram/Timer

**Description:** Time spent actually processing the job (excludes waiting time)

**Implementation:**
```java
// In FakePacketController.java
metricsService.recordProcessingTime(response.getProcessingTimeMs(), "fake_packet");
```

**Prometheus Queries:**
```promql
# Average processing time (ms)
(rate(app_processing_time_seconds_sum[5m]) / rate(app_processing_time_seconds_count[5m])) * 1000

# P50 processing time
histogram_quantile(0.50, rate(app_processing_time_seconds_bucket[5m])) * 1000

# P95 processing time
histogram_quantile(0.95, rate(app_processing_time_seconds_bucket[5m])) * 1000

# P99 processing time
histogram_quantile(0.99, rate(app_processing_time_seconds_bucket[5m])) * 1000

# Processing time by job type
sum by (type) (rate(app_processing_time_seconds_sum[5m])) / sum by (type) (rate(app_processing_time_seconds_count[5m]))
```

---

## Data Flow

### Request Lifecycle and Metrics Collection

```
1. Request arrives
   ├─> INCREMENT: app_requests_total
   ├─> INCREMENT: app_connections_total
   ├─> INCREMENT: app_connections_current
   └─> START: request_latency timer

2. Add to queue
   └─> INCREMENT: app_queue_length

3. Start processing
   └─> DECREMENT: app_queue_length

4. Process job
   └─> RECORD: processing_time

5. Generate response
   └─> RECORD: response_size

6. Request completes
   ├─> STOP: request_latency timer
   ├─> DECREMENT: app_connections_current
   └─> IF success: INCREMENT app_jobs_processed_total
       IF error: INCREMENT app_errors_total
```

---

## Exporting Metrics Data

### Export to CSV from Prometheus

```bash
# Use Prometheus API to export data
curl 'http://localhost:9090/api/v1/query?query=app_requests_total' | jq '.data.result[] | [.metric.application, .value[1]] | @csv' -r > requests.csv
```

### Export to Database

Create a scheduled job to periodically save aggregated metrics:

```sql
CREATE TABLE monitoring_snapshots (
    id SERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,

    -- Counters
    total_requests BIGINT,
    total_errors BIGINT,
    total_connections BIGINT,
    total_jobs_processed BIGINT,

    -- Gauges
    cpu_usage_percent DECIMAL(5,2),
    memory_usage_mb DECIMAL(10,2),
    memory_usage_percent DECIMAL(5,2),
    current_connections INTEGER,
    queue_length INTEGER,

    -- Histogram aggregates
    avg_request_latency_ms DECIMAL(10,2),
    p95_request_latency_ms DECIMAL(10,2),
    p99_request_latency_ms DECIMAL(10,2),
    avg_response_size_bytes DECIMAL(10,2),
    avg_processing_time_ms DECIMAL(10,2),
    p95_processing_time_ms DECIMAL(10,2),

    -- Derived metrics
    request_rate_per_min DECIMAL(10,2),
    error_rate_percent DECIMAL(5,2),
    success_rate_percent DECIMAL(5,2)
);

-- Create index on timestamp for fast queries
CREATE INDEX idx_monitoring_timestamp ON monitoring_snapshots(timestamp);
```

---

## Grafana Dashboard Panels

### Essential Panels for Monitoring Schema

#### Panel 1: Request Overview (Row)
- **Total Requests** (Stat) - `app_requests_total`
- **Total Errors** (Stat) - `app_errors_total`
- **Success Rate** (Gauge) - `((app_requests_total - app_errors_total) / app_requests_total) * 100`
- **Jobs Processed** (Stat) - `app_jobs_processed_total`

#### Panel 2: Resource Usage (Row)
- **CPU Usage** (Gauge) - `process_cpu_usage * 100`
- **Memory Usage** (Gauge) - `(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100`
- **Current Connections** (Stat) - `app_connections_current`
- **Queue Length** (Stat) - `app_queue_length`

#### Panel 3: Performance (Row)
- **Request Latency (P95)** (Graph) - `histogram_quantile(0.95, rate(app_request_latency_seconds_bucket[5m])) * 1000`
- **Processing Time (P95)** (Graph) - `histogram_quantile(0.95, rate(app_processing_time_seconds_bucket[5m])) * 1000`
- **Response Size (Avg)** (Graph) - `rate(app_response_size_bytes_sum[5m]) / rate(app_response_size_bytes_count[5m])`

---

## Alerting Rules

Based on the monitoring schema:

```yaml
groups:
  - name: cs4445_schema_alerts
    rules:
      # Counter alerts
      - alert: HighErrorRate
        expr: (rate(app_errors_total[5m]) / rate(app_requests_total[5m])) > 0.05
        for: 2m
        annotations:
          summary: "Error rate > 5%"

      # Gauge alerts
      - alert: HighCPU
        expr: process_cpu_usage > 0.8
        for: 5m
        annotations:
          summary: "CPU usage > 80%"

      - alert: HighMemory
        expr: (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) > 0.85
        for: 5m
        annotations:
          summary: "Memory usage > 85%"

      - alert: LargeQueue
        expr: app_queue_length > 20
        for: 3m
        annotations:
          summary: "Queue length > 20"

      # Histogram alerts
      - alert: HighLatency
        expr: histogram_quantile(0.95, rate(app_request_latency_seconds_bucket[5m])) > 2
        for: 3m
        annotations:
          summary: "P95 latency > 2s"

      - alert: SlowProcessing
        expr: histogram_quantile(0.95, rate(app_processing_time_seconds_bucket[5m])) > 5
        for: 3m
        annotations:
          summary: "P95 processing time > 5s"
```

---

## Summary Table

| Category | Metric | Type | Query Example | Dashboard Panel |
|----------|--------|------|---------------|-----------------|
| Counter | Total Requests | Counter | `app_requests_total` | Stat |
| Counter | Total Errors | Counter | `app_errors_total` | Stat |
| Counter | Total Connections | Counter | `app_connections_total` | Stat |
| Counter | Total Jobs Processed | Counter | `app_jobs_processed_total` | Stat |
| Gauge | CPU Usage | Gauge | `process_cpu_usage * 100` | Gauge |
| Gauge | RAM Usage | Gauge | `jvm_memory_used_bytes{area="heap"}` | Gauge/Graph |
| Gauge | Current Connections | Gauge | `app_connections_current` | Stat/Graph |
| Gauge | Queue Length | Gauge | `app_queue_length` | Stat/Graph |
| Histogram | Request Latency | Histogram | `histogram_quantile(0.95, rate(app_request_latency_seconds_bucket[5m])) * 1000` | Graph |
| Histogram | Response Size | Histogram | `rate(app_response_size_bytes_sum[5m]) / rate(app_response_size_bytes_count[5m])` | Graph |
| Histogram | Processing Time | Histogram | `histogram_quantile(0.95, rate(app_processing_time_seconds_bucket[5m])) * 1000` | Graph |

---

## Verification Checklist

Use this to verify the monitoring schema is correctly implemented:

### Counters
- [ ] `app_requests_total` - Increments on each request
- [ ] `app_errors_total` - Increments on errors
- [ ] `app_connections_total` - Increments on new connections
- [ ] `app_jobs_processed_total` - Increments on successful jobs

### Gauges
- [ ] `process_cpu_usage` - Shows current CPU (0.0-1.0)
- [ ] `jvm_memory_used_bytes` - Shows current memory usage
- [ ] `app_connections_current` - Goes up/down with active connections
- [ ] `app_queue_length` - Goes up/down with queue

### Histograms
- [ ] `app_request_latency_seconds` - Has _count, _sum, _bucket metrics
- [ ] `app_response_size_bytes` - Has _count, _sum, _bucket metrics
- [ ] `app_processing_time_seconds` - Has _count, _sum, _bucket metrics

---

## References

- [PROMETHEUS_METRICS_GUIDE.md](PROMETHEUS_METRICS_GUIDE.md) - Detailed Prometheus queries
- [GRAFANA_DASHBOARD_GUIDE.md](GRAFANA_DASHBOARD_GUIDE.md) - Dashboard setup
- [MONITORING_QUICK_START.md](MONITORING_QUICK_START.md) - Quick start guide
- [INSOMNIA_TEST_GUIDE.md](INSOMNIA_TEST_GUIDE.md) - API testing
- [SQL_QUERY_GUIDE.md](SQL_QUERY_GUIDE.md) - Database queries

---

✅ **Schema Complete and Documented**
