# Prometheus Metrics Guide for CS4445 Subscription Server

## Overview

This guide documents all Prometheus metrics exposed by the CS4445 Subscription Server, organized by type according to the monitoring schema.

## Accessing Metrics

**Prometheus Endpoint:** `http://localhost:8080/actuator/prometheus`
**Prometheus UI:** `http://localhost:9090`
**Grafana UI:** `http://localhost:3000` (admin/admin)

---

## Metric Types and Schema

### 1. COUNTER Metrics

Counters are cumulative metrics that only increase (or reset to zero on restart).

#### 1.1 Total Requests

**Metric Name:** `app_requests_total`

**Description:** Total number of requests received by the server

**Type:** Counter

**Tags:**
- `type="all"` - All request types
- `application="CS4445-Sub-Server"`
- `environment="development"`

**Prometheus Queries:**

```promql
# Total requests
app_requests_total

# Request rate (requests per second)
rate(app_requests_total[1m])

# Request rate over 5 minutes
rate(app_requests_total[5m])

# Total requests in last hour
increase(app_requests_total[1h])

# Total requests today
increase(app_requests_total[24h])
```

---

#### 1.2 Total Errors

**Metric Name:** `app_errors_total`

**Description:** Total number of errors occurred

**Type:** Counter

**Tags:**
- `type="all"` - All error types
- `type="server_closed"` - Errors due to server being closed
- `type="exception"` - Errors due to exceptions
- `application="CS4445-Sub-Server"`

**Prometheus Queries:**

```promql
# Total errors
app_errors_total

# Error rate per second
rate(app_errors_total[1m])

# Errors by type
sum by (type) (app_errors_total)

# Error rate by type
sum by (type) (rate(app_errors_total[5m]))

# Error percentage
(app_errors_total / app_requests_total) * 100

# Success rate percentage
((app_requests_total - app_errors_total) / app_requests_total) * 100
```

---

#### 1.3 Total Connections

**Metric Name:** `app_connections_total`

**Description:** Total number of connections established since startup

**Type:** Counter

**Tags:**
- `type="all"`
- `application="CS4445-Sub-Server"`

**Prometheus Queries:**

```promql
# Total connections
app_connections_total

# Connection rate per second
rate(app_connections_total[1m])

# Connections in last hour
increase(app_connections_total[1h])

# Average connections per minute
rate(app_connections_total[1m]) * 60
```

---

#### 1.4 Total Jobs Processed

**Metric Name:** `app_jobs_processed_total`

**Description:** Total number of jobs processed successfully

**Type:** Counter

**Tags:**
- `type="all"`
- `application="CS4445-Sub-Server"`

**Prometheus Queries:**

```promql
# Total jobs processed
app_jobs_processed_total

# Job processing rate
rate(app_jobs_processed_total[1m])

# Jobs processed in last hour
increase(app_jobs_processed_total[1h])

# Job success rate
(app_jobs_processed_total / app_requests_total) * 100

# Job failure rate
((app_requests_total - app_jobs_processed_total) / app_requests_total) * 100
```

---

### 2. GAUGE Metrics

Gauges are metrics that can go up or down (current state).

#### 2.1 CPU Usage

**Metric Names:**
- `process_cpu_usage` - Current CPU usage (0.0 to 1.0)
- `system_cpu_usage` - System-wide CPU usage
- `system_cpu_count` - Number of CPUs available

**Type:** Gauge

**Prometheus Queries:**

```promql
# Current process CPU usage (as percentage)
process_cpu_usage * 100

# System CPU usage (as percentage)
system_cpu_usage * 100

# Average CPU usage over 5 minutes
avg_over_time(process_cpu_usage[5m]) * 100

# Maximum CPU usage in last hour
max_over_time(process_cpu_usage[1h]) * 100

# CPU usage per core
(process_cpu_usage / system_cpu_count) * 100
```

---

#### 2.2 RAM Usage

**Metric Names:**
- `jvm_memory_used_bytes` - Memory currently used
- `jvm_memory_max_bytes` - Maximum memory available
- `jvm_memory_committed_bytes` - Memory guaranteed to be available

**Type:** Gauge

**Tags:**
- `area="heap"` - Heap memory
- `area="nonheap"` - Non-heap memory
- `id="<memory pool name>"`

**Prometheus Queries:**

```promql
# Current heap memory usage (bytes)
jvm_memory_used_bytes{area="heap"}

# Current heap memory usage (MB)
jvm_memory_used_bytes{area="heap"} / 1024 / 1024

# Current heap memory usage (GB)
jvm_memory_used_bytes{area="heap"} / 1024 / 1024 / 1024

# Memory usage percentage
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# Available memory
jvm_memory_max_bytes{area="heap"} - jvm_memory_used_bytes{area="heap"}

# Average memory usage over 5 minutes
avg_over_time(jvm_memory_used_bytes{area="heap"}[5m])

# Memory usage by pool
sum by (id) (jvm_memory_used_bytes)

# Total memory used (heap + non-heap)
sum(jvm_memory_used_bytes)
```

---

#### 2.3 Current Connections

**Metric Name:** `app_connections_current`

**Description:** Number of currently active connections

**Type:** Gauge

**Tags:**
- `type="active"`
- `application="CS4445-Sub-Server"`

**Prometheus Queries:**

```promql
# Current active connections
app_connections_current

# Average concurrent connections over 5 minutes
avg_over_time(app_connections_current[5m])

# Maximum concurrent connections in last hour
max_over_time(app_connections_current[1h])

# Minimum concurrent connections
min_over_time(app_connections_current[5m])

# Connection utilization (if you have a max connection limit)
# Assuming max 100 connections:
(app_connections_current / 100) * 100
```

---

#### 2.4 Queue Length

**Metric Name:** `app_queue_length`

**Description:** Current number of jobs waiting in queue

**Type:** Gauge

**Tags:**
- `type="pending"`
- `application="CS4445-Sub-Server"`

**Prometheus Queries:**

```promql
# Current queue length
app_queue_length

# Average queue length over 5 minutes
avg_over_time(app_queue_length[5m])

# Maximum queue length in last hour
max_over_time(app_queue_length[1h])

# Queue depth indicator (alerts when > 10)
app_queue_length > 10

# Queue growing rate
deriv(app_queue_length[5m])
```

---

### 3. HISTOGRAM Metrics

Histograms track distributions of values and calculate percentiles.

#### 3.1 Request Latency

**Metric Names:**
- `app_request_latency_seconds_count` - Total count
- `app_request_latency_seconds_sum` - Total sum
- `app_request_latency_seconds_bucket` - Histogram buckets
- `app_request_latency_seconds_max` - Maximum value

**Type:** Histogram/Timer

**Tags:**
- `type="http"`
- `application="CS4445-Sub-Server"`

**Prometheus Queries:**

```promql
# Average request latency (seconds)
rate(app_request_latency_seconds_sum[5m]) / rate(app_request_latency_seconds_count[5m])

# Average request latency (milliseconds)
(rate(app_request_latency_seconds_sum[5m]) / rate(app_request_latency_seconds_count[5m])) * 1000

# 50th percentile (median) latency
histogram_quantile(0.5, rate(app_request_latency_seconds_bucket[5m]))

# 95th percentile latency
histogram_quantile(0.95, rate(app_request_latency_seconds_bucket[5m])) * 1000

# 99th percentile latency
histogram_quantile(0.99, rate(app_request_latency_seconds_bucket[5m])) * 1000

# Maximum latency in last 5 minutes
max_over_time(app_request_latency_seconds_max[5m]) * 1000

# Request latency distribution
sum by (le) (rate(app_request_latency_seconds_bucket[5m]))
```

---

#### 3.2 Response Size

**Metric Names:**
- `app_response_size_bytes_count` - Total count
- `app_response_size_bytes_sum` - Total sum
- `app_response_size_bytes_bucket` - Histogram buckets
- `app_response_size_bytes_max` - Maximum value

**Type:** Histogram/Distribution Summary

**Tags:**
- `type="json"` - JSON responses
- `type="http"` - HTTP responses
- `application="CS4445-Sub-Server"`

**Prometheus Queries:**

```promql
# Average response size (bytes)
rate(app_response_size_bytes_sum[5m]) / rate(app_response_size_bytes_count[5m])

# Average response size (KB)
(rate(app_response_size_bytes_sum[5m]) / rate(app_response_size_bytes_count[5m])) / 1024

# 50th percentile response size
histogram_quantile(0.5, rate(app_response_size_bytes_bucket[5m]))

# 95th percentile response size
histogram_quantile(0.95, rate(app_response_size_bytes_bucket[5m]))

# 99th percentile response size
histogram_quantile(0.99, rate(app_response_size_bytes_bucket[5m]))

# Maximum response size in last 5 minutes
max_over_time(app_response_size_bytes_max[5m])

# Total bandwidth used (bytes per second)
rate(app_response_size_bytes_sum[5m])

# Total bandwidth used (MB per second)
rate(app_response_size_bytes_sum[5m]) / 1024 / 1024

# Response size by type
sum by (type) (rate(app_response_size_bytes_sum[5m]))
```

---

#### 3.3 Processing Time

**Metric Names:**
- `app_processing_time_seconds_count` - Total count
- `app_processing_time_seconds_sum` - Total sum
- `app_processing_time_seconds_bucket` - Histogram buckets
- `app_processing_time_seconds_max` - Maximum value

**Type:** Histogram/Timer

**Tags:**
- `type="job"` - Generic job type
- `type="fake_packet"` - Fake packet processing
- `application="CS4445-Sub-Server"`

**Prometheus Queries:**

```promql
# Average processing time (milliseconds)
(rate(app_processing_time_seconds_sum[5m]) / rate(app_processing_time_seconds_count[5m])) * 1000

# 50th percentile processing time
histogram_quantile(0.5, rate(app_processing_time_seconds_bucket[5m])) * 1000

# 95th percentile processing time
histogram_quantile(0.95, rate(app_processing_time_seconds_bucket[5m])) * 1000

# 99th percentile processing time
histogram_quantile(0.99, rate(app_processing_time_seconds_bucket[5m])) * 1000

# Maximum processing time
max_over_time(app_processing_time_seconds_max[5m]) * 1000

# Processing time by job type
sum by (type) (rate(app_processing_time_seconds_sum[5m])) / sum by (type) (rate(app_processing_time_seconds_count[5m]))

# Slow requests (> 2 seconds)
count(app_processing_time_seconds_bucket{le="2.0"} > 0)
```

---

## Standard Spring Boot Metrics

### HTTP Server Requests

```promql
# Request count by status
http_server_requests_seconds_count

# Request rate by endpoint
rate(http_server_requests_seconds_count[5m])

# Requests by status code
sum by (status) (http_server_requests_seconds_count)

# 4xx error rate
sum(rate(http_server_requests_seconds_count{status=~"4.."}[5m]))

# 5xx error rate
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))

# Success rate (2xx and 3xx)
sum(rate(http_server_requests_seconds_count{status=~"[23].."}[5m]))
```

### Database Metrics

```promql
# Active database connections
hikaricp_connections_active

# Idle database connections
hikaricp_connections_idle

# Total database connections
hikaricp_connections

# Connection timeout count
hikaricp_connections_timeout_total

# Connection acquire time
hikaricp_connections_acquire_seconds
```

### JVM Metrics

```promql
# Thread count
jvm_threads_live

# Daemon thread count
jvm_threads_daemon

# Peak thread count
jvm_threads_peak

# GC pause time
jvm_gc_pause_seconds_sum

# GC count
jvm_gc_pause_seconds_count

# Classes loaded
jvm_classes_loaded

# Classes unloaded
jvm_classes_unloaded_total
```

---

## Complex Queries and Dashboards

### Service Level Indicators (SLIs)

#### Request Success Rate

```promql
# Success rate (%)
(sum(rate(app_requests_total[5m])) - sum(rate(app_errors_total[5m]))) / sum(rate(app_requests_total[5m])) * 100
```

#### Request Latency SLI (95% of requests < 500ms)

```promql
# Check if 95th percentile latency is under 500ms
histogram_quantile(0.95, rate(app_request_latency_seconds_bucket[5m])) < 0.5
```

#### Error Rate SLI (< 1% errors)

```promql
# Error rate (%)
(sum(rate(app_errors_total[5m])) / sum(rate(app_requests_total[5m]))) * 100 < 1
```

### System Health

```promql
# Overall system health score (0-100)
(
  (100 - (sum(rate(app_errors_total[5m])) / sum(rate(app_requests_total[5m])) * 100)) * 0.4 +  # Error rate weight 40%
  (100 - (process_cpu_usage * 100)) * 0.3 +  # CPU weight 30%
  (100 - ((jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100)) * 0.3  # Memory weight 30%
)
```

### Throughput

```promql
# Requests per minute
rate(app_requests_total[1m]) * 60

# Jobs processed per minute
rate(app_jobs_processed_total[1m]) * 60

# Peak throughput in last hour
max_over_time(rate(app_requests_total[1m])[1h:]) * 60
```

### Load and Capacity

```promql
# Current load percentage (connections/max)
# Assuming max 100 connections:
(app_connections_current / 100) * 100

# Queue saturation
(app_queue_length / 50) * 100  # Assuming max queue size of 50

# Resource utilization score
(process_cpu_usage + (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"})) / 2 * 100
```

---

## Alerting Rules

### Example Prometheus Alert Rules

```yaml
groups:
  - name: cs4445_alerts
    interval: 30s
    rules:
      # High error rate
      - alert: HighErrorRate
        expr: (rate(app_errors_total[5m]) / rate(app_requests_total[5m])) > 0.05
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value | humanizePercentage }} (threshold: 5%)"

      # High CPU usage
      - alert: HighCPUUsage
        expr: process_cpu_usage > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High CPU usage"
          description: "CPU usage is {{ $value | humanizePercentage }}"

      # High memory usage
      - alert: HighMemoryUsage
        expr: (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) > 0.85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage"
          description: "Memory usage is {{ $value | humanizePercentage }}"

      # Queue growing too large
      - alert: LargeQueue
        expr: app_queue_length > 20
        for: 3m
        labels:
          severity: warning
        annotations:
          summary: "Queue length is growing"
          description: "Current queue length: {{ $value }}"

      # High latency
      - alert: HighLatency
        expr: histogram_quantile(0.95, rate(app_request_latency_seconds_bucket[5m])) > 2
        for: 3m
        labels:
          severity: warning
        annotations:
          summary: "High request latency"
          description: "95th percentile latency: {{ $value }}s"

      # No requests (service might be down)
      - alert: NoRequests
        expr: rate(app_requests_total[5m]) == 0
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "No requests received"
          description: "Service might be down - no requests in 5 minutes"
```

---

## Recording Rules (for performance)

```yaml
groups:
  - name: cs4445_recordings
    interval: 30s
    rules:
      # Pre-calculate request rate
      - record: job:app_requests:rate5m
        expr: rate(app_requests_total[5m])

      # Pre-calculate error rate
      - record: job:app_errors:rate5m
        expr: rate(app_errors_total[5m])

      # Pre-calculate success percentage
      - record: job:app_success:percentage
        expr: ((app_requests_total - app_errors_total) / app_requests_total) * 100

      # Pre-calculate average latency
      - record: job:app_latency:avg
        expr: rate(app_request_latency_seconds_sum[5m]) / rate(app_request_latency_seconds_count[5m])

      # Pre-calculate p95 latency
      - record: job:app_latency:p95
        expr: histogram_quantile(0.95, rate(app_request_latency_seconds_bucket[5m]))

      # Pre-calculate average processing time
      - record: job:app_processing_time:avg
        expr: rate(app_processing_time_seconds_sum[5m]) / rate(app_processing_time_seconds_count[5m])
```

---

## Testing Metrics Collection

### Verify Metrics Endpoint

```bash
# Check if metrics are exposed
curl http://localhost:8080/actuator/prometheus | grep "app_"

# Check specific metric
curl http://localhost:8080/actuator/prometheus | grep "app_requests_total"
```

### Generate Test Load

```bash
# Send 100 requests
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d "{\"packetId\":\"test-$i\",\"cpuIntensity\":5,\"ramIntensity\":5}"
done

# Then check Prometheus:
# http://localhost:9090/graph?g0.range_input=5m&g0.expr=app_requests_total
```

---

## Best Practices

1. **Use Recording Rules** for frequently used complex queries
2. **Set up Alerts** for critical metrics
3. **Monitor Trends** not just current values
4. **Use Percentiles** (p50, p95, p99) instead of averages for latency
5. **Tag Metrics** appropriately for better filtering
6. **Keep Cardinality Low** - avoid high-cardinality labels
7. **Set Retention Policy** in Prometheus for storage management
8. **Create Dashboards** in Grafana for visualization
9. **Document Queries** and their business meaning
10. **Regular Review** of metrics and alerts

---

## Next Steps

1. Review the [Grafana Dashboard Guide](GRAFANA_DASHBOARD_GUIDE.md) for visualization
2. Check [Monitoring Best Practices](MONITORING_BEST_PRACTICES.md)
3. Set up alerts based on your SLOs
4. Create custom dashboards for your team
5. Export metrics data for long-term analysis

---

Happy Monitoring! 📊
