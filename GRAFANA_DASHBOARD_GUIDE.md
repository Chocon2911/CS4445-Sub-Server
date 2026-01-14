# Grafana Dashboard Guide for CS4445 Subscription Server

## Quick Start

**Grafana URL:** http://localhost:3000
**Default Credentials:** admin / admin

## Initial Setup

### 1. Add Prometheus Data Source

1. Navigate to **Configuration** → **Data Sources**
2. Click **Add data source**
3. Select **Prometheus**
4. Configure:
   - **Name:** Prometheus
   - **URL:** `http://prometheus:9090` (Docker network) or `http://localhost:9090` (host)
   - **Access:** Server (default)
5. Click **Save & Test**

### 2. Import Dashboard JSON

You can import the ready-made dashboard JSON (see below) or create custom dashboards.

---

## Dashboard Layout

### Dashboard 1: Overview Dashboard

**Purpose:** High-level view of system health and performance

#### Row 1: Key Metrics (Single Stats)
- **Total Requests** - `app_requests_total`
- **Total Errors** - `app_errors_total`
- **Success Rate** - `((app_requests_total - app_errors_total) / app_requests_total) * 100`
- **Current Connections** - `app_connections_current`

#### Row 2: Throughput (Time Series)
- **Request Rate** - `rate(app_requests_total[5m]) * 60`
- **Job Processing Rate** - `rate(app_jobs_processed_total[5m]) * 60`
- **Error Rate** - `rate(app_errors_total[5m]) * 60`

#### Row 3: Latency (Time Series)
- **Request Latency (P50, P95, P99)**
  ```promql
  histogram_quantile(0.50, rate(app_request_latency_seconds_bucket[5m])) * 1000 # P50
  histogram_quantile(0.95, rate(app_request_latency_seconds_bucket[5m])) * 1000 # P95
  histogram_quantile(0.99, rate(app_request_latency_seconds_bucket[5m])) * 1000 # P99
  ```

#### Row 4: Resource Usage (Time Series + Gauge)
- **CPU Usage** - `process_cpu_usage * 100`
- **Memory Usage** - `(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100`
- **Current Queue Length** - `app_queue_length`

---

### Dashboard 2: Performance Dashboard

**Purpose:** Detailed performance metrics

#### Panel Configurations:

**1. Processing Time Distribution (Heatmap)**
```promql
sum(rate(app_processing_time_seconds_bucket[5m])) by (le)
```

**2. Request Latency Percentiles (Graph)**
```promql
histogram_quantile(0.50, rate(app_request_latency_seconds_bucket[5m])) * 1000 # Median
histogram_quantile(0.75, rate(app_request_latency_seconds_bucket[5m])) * 1000 # 75th
histogram_quantile(0.90, rate(app_request_latency_seconds_bucket[5m])) * 1000 # 90th
histogram_quantile(0.95, rate(app_request_latency_seconds_bucket[5m])) * 1000 # 95th
histogram_quantile(0.99, rate(app_request_latency_seconds_bucket[5m])) * 1000 # 99th
```

**3. Response Size Distribution (Graph)**
```promql
histogram_quantile(0.50, rate(app_response_size_bytes_bucket[5m])) / 1024 # KB
histogram_quantile(0.95, rate(app_response_size_bytes_bucket[5m])) / 1024
histogram_quantile(0.99, rate(app_response_size_bytes_bucket[5m])) / 1024
```

**4. Throughput by Type (Bar Gauge)**
```promql
sum by (type) (rate(app_requests_total[5m])) * 60
```

**5. Error Rate by Type (Pie Chart)**
```promql
sum by (type) (app_errors_total)
```

---

### Dashboard 3: System Resources Dashboard

**Purpose:** Monitor system resource utilization

**1. CPU Usage Over Time (Graph)**
```promql
process_cpu_usage * 100
system_cpu_usage * 100
```

**2. Memory Usage (Graph with Stacking)**
```promql
jvm_memory_used_bytes{area="heap"} / 1024 / 1024 # Heap MB
jvm_memory_used_bytes{area="nonheap"} / 1024 / 1024 # Non-Heap MB
```

**3. Memory by Pool (Stacked Area)**
```promql
sum by (id) (jvm_memory_used_bytes) / 1024 / 1024
```

**4. Thread Count (Graph)**
```promql
jvm_threads_live
jvm_threads_daemon
jvm_threads_peak
```

**5. GC Activity (Graph)**
```promql
rate(jvm_gc_pause_seconds_count[5m])
rate(jvm_gc_pause_seconds_sum[5m]) * 1000 # ms
```

**6. Database Connections (Graph)**
```promql
hikaricp_connections_active
hikaricp_connections_idle
hikaricp_connections
```

---

### Dashboard 4: Business Metrics Dashboard

**Purpose:** Business-oriented metrics

**1. Total Jobs Processed Today (Stat)**
```promql
increase(app_jobs_processed_total[24h])
```

**2. Total Requests This Hour (Stat)**
```promql
increase(app_requests_total[1h])
```

**3. Average Processing Time (Gauge)**
```promql
(rate(app_processing_time_seconds_sum[5m]) / rate(app_processing_time_seconds_count[5m])) * 1000
```

**4. Success vs Failure (Pie Chart)**
```promql
app_jobs_processed_total # Success
app_errors_total # Failure
```

**5. Request Volume by Hour (Bar Graph)**
```promql
sum_over_time(increase(app_requests_total[1h])[24h:1h])
```

**6. SLA Compliance (Gauge)**
- Target: 95% success rate, <500ms P95 latency
```promql
# Success Rate SLA
((app_requests_total - app_errors_total) / app_requests_total) * 100 > 95

# Latency SLA
histogram_quantile(0.95, rate(app_request_latency_seconds_bucket[5m])) < 0.5
```

---

### Dashboard 5: Queue & Connections Dashboard

**Purpose:** Monitor queue and connection metrics

**1. Current Connections (Stat + Graph)**
```promql
app_connections_current
```

**2. Total Connections (Graph)**
```promql
app_connections_total
increase(app_connections_total[1h])
```

**3. Connection Rate (Graph)**
```promql
rate(app_connections_total[1m]) * 60
```

**4. Queue Length (Graph with Alert Threshold)**
```promql
app_queue_length
```

**5. Queue Depth Over Time (Heatmap)**
```promql
app_queue_length
```

**6. Concurrent Users Estimate (Stat)**
```promql
avg_over_time(app_connections_current[5m])
```

---

## Ready-to-Import Dashboard JSON

### Overview Dashboard JSON

```json
{
  "dashboard": {
    "title": "CS4445 Overview Dashboard",
    "tags": ["cs4445", "overview"],
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "Total Requests",
        "type": "stat",
        "targets": [
          {
            "expr": "app_requests_total",
            "legendFormat": "Requests"
          }
        ],
        "gridPos": {"h": 4, "w": 6, "x": 0, "y": 0}
      },
      {
        "id": 2,
        "title": "Total Errors",
        "type": "stat",
        "targets": [
          {
            "expr": "app_errors_total",
            "legendFormat": "Errors"
          }
        ],
        "gridPos": {"h": 4, "w": 6, "x": 6, "y": 0},
        "fieldConfig": {
          "defaults": {
            "thresholds": {
              "steps": [
                {"color": "green", "value": 0},
                {"color": "yellow", "value": 10},
                {"color": "red", "value": 50}
              ]
            }
          }
        }
      },
      {
        "id": 3,
        "title": "Success Rate (%)",
        "type": "gauge",
        "targets": [
          {
            "expr": "((app_requests_total - app_errors_total) / app_requests_total) * 100",
            "legendFormat": "Success Rate"
          }
        ],
        "gridPos": {"h": 4, "w": 6, "x": 12, "y": 0},
        "fieldConfig": {
          "defaults": {
            "min": 0,
            "max": 100,
            "thresholds": {
              "steps": [
                {"color": "red", "value": 0},
                {"color": "yellow", "value": 90},
                {"color": "green", "value": 95}
              ]
            },
            "unit": "percent"
          }
        }
      },
      {
        "id": 4,
        "title": "Current Connections",
        "type": "stat",
        "targets": [
          {
            "expr": "app_connections_current",
            "legendFormat": "Connections"
          }
        ],
        "gridPos": {"h": 4, "w": 6, "x": 18, "y": 0}
      },
      {
        "id": 5,
        "title": "Request Rate (req/min)",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(app_requests_total[5m]) * 60",
            "legendFormat": "Requests/min"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 4}
      },
      {
        "id": 6,
        "title": "Request Latency Percentiles (ms)",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.50, rate(app_request_latency_seconds_bucket[5m])) * 1000",
            "legendFormat": "P50"
          },
          {
            "expr": "histogram_quantile(0.95, rate(app_request_latency_seconds_bucket[5m])) * 1000",
            "legendFormat": "P95"
          },
          {
            "expr": "histogram_quantile(0.99, rate(app_request_latency_seconds_bucket[5m])) * 1000",
            "legendFormat": "P99"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 4}
      },
      {
        "id": 7,
        "title": "CPU Usage (%)",
        "type": "graph",
        "targets": [
          {
            "expr": "process_cpu_usage * 100",
            "legendFormat": "Process CPU"
          }
        ],
        "gridPos": {"h": 6, "w": 8, "x": 0, "y": 12}
      },
      {
        "id": 8,
        "title": "Memory Usage (%)",
        "type": "graph",
        "targets": [
          {
            "expr": "(jvm_memory_used_bytes{area=\"heap\"} / jvm_memory_max_bytes{area=\"heap\"}) * 100",
            "legendFormat": "Heap Memory"
          }
        ],
        "gridPos": {"h": 6, "w": 8, "x": 8, "y": 12}
      },
      {
        "id": 9,
        "title": "Queue Length",
        "type": "graph",
        "targets": [
          {
            "expr": "app_queue_length",
            "legendFormat": "Queue"
          }
        ],
        "gridPos": {"h": 6, "w": 8, "x": 16, "y": 12}
      }
    ],
    "refresh": "5s",
    "time": {
      "from": "now-1h",
      "to": "now"
    },
    "version": 1
  }
}
```

---

## Panel Creation Guide

### Creating a Graph Panel

1. Click **Add Panel** → **Add new panel**
2. In **Query**, select **Prometheus** as data source
3. Enter your PromQL query
4. Configure **Legend** format
5. Under **Panel options**:
   - Set title
   - Choose visualization type (Graph, Gauge, Stat, etc.)
6. Under **Field options**:
   - Set units (percent, bytes, ms, etc.)
   - Configure thresholds for color coding
7. Click **Apply**

### Creating a Stat Panel

1. Add Panel → Choose **Stat** visualization
2. Enter PromQL query
3. Under **Field options**:
   - Set **Unit** (e.g., "short", "percent", "ms")
   - Configure **Thresholds**:
     - Green: Normal range
     - Yellow: Warning range
     - Red: Critical range
4. Under **Graph mode**: Choose "None", "Area", or "Line"
5. Under **Text mode**: Choose "Value", "Value and name", or "Name"

### Creating a Gauge Panel

1. Add Panel → Choose **Gauge** visualization
2. Enter PromQL query (should return a single value)
3. Set **Min** and **Max** values
4. Configure **Thresholds** for color zones
5. Choose **Show threshold labels** and **Show threshold markers**

---

## Common Panel Configurations

### Time Series with Multiple Queries

**Example: Request and Error Rates**

```
Query A: rate(app_requests_total[5m]) * 60
Legend: Requests/min

Query B: rate(app_errors_total[5m]) * 60
Legend: Errors/min
```

**Visualization:** Graph
**Y-Axis Unit:** req/s or req/min
**Color Scheme:** Classic palette

### Pie Chart for Distribution

**Example: Error Types**

```
Query: sum by (type) (app_errors_total)
```

**Visualization:** Pie Chart
**Legend:** Show
**Values:** Show percentage

### Heatmap for Distribution Over Time

**Example:** Processing Time Distribution

```
Query: sum(rate(app_processing_time_seconds_bucket[5m])) by (le)
Format: Heatmap
```

**Visualization:** Heatmap
**Data format:** Time series buckets

---

## Variables and Templating

### Create Time Range Variable

1. Dashboard Settings → Variables → Add variable
2. **Name:** `time_range`
3. **Type:** Interval
4. **Values:** `1m,5m,10m,30m,1h,6h,12h,24h`
5. **Auto Option:** Enabled

### Create Metric Aggregation Variable

1. **Name:** `aggregation`
2. **Type:** Custom
3. **Values:** `avg,min,max,sum`

### Use Variables in Queries

```promql
${aggregation}_over_time(app_connections_current[$time_range])
```

---

## Alert Configuration

### Setting Up Alerts in Grafana

1. Edit Panel → Alert tab
2. Click **Create Alert**
3. Configure:
   - **Name:** High Error Rate
   - **Evaluate every:** 1m
   - **For:** 5m
   - **Conditions:**
     ```
     WHEN avg() OF query(A, 5m, now) IS ABOVE 0.05
     ```
4. **No Data & Error Handling:** Set to "Alerting"
5. **Notifications:** Choose notification channel

### Example Alerts

**High Error Rate Alert:**
- Condition: `(rate(app_errors_total[5m]) / rate(app_requests_total[5m])) > 0.05`
- For: 2 minutes
- Severity: Warning

**High Memory Usage Alert:**
- Condition: `(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) > 0.85`
- For: 5 minutes
- Severity: Warning

**Queue Backup Alert:**
- Condition: `app_queue_length > 20`
- For: 3 minutes
- Severity: Warning

---

## Dashboard Best Practices

1. **Organize by Purpose:** Create separate dashboards for different audiences (DevOps, Business, Debug)
2. **Use Consistent Time Ranges:** Default to "Last 1 hour" or "Last 6 hours"
3. **Color Coding:** Use consistent color schemes (Green=Good, Yellow=Warning, Red=Critical)
4. **Add Descriptions:** Include panel descriptions for complex metrics
5. **Use Variables:** Make dashboards flexible with variables
6. **Group Related Panels:** Use rows to organize related metrics
7. **Set Refresh Rate:** Auto-refresh every 5-30 seconds for real-time monitoring
8. **Add Links:** Link related dashboards together
9. **Export Regularly:** Save dashboard JSON as backup
10. **Document Queries:** Add comments in PromQL queries

---

## Keyboard Shortcuts

- **`d` + `k`:** Toggle kiosk mode
- **`Ctrl/Cmd + S`:** Save dashboard
- **`Ctrl/Cmd + H`:** Hide/show all panels
- **`e`:** Edit panel
- **`v`:** View panel
- **`p` + `s`:** Share panel/dashboard

---

## Troubleshooting

### No Data in Panels

1. Check Prometheus data source connection
2. Verify metrics are being exported: `curl http://localhost:8080/actuator/prometheus`
3. Check PromQL query syntax in Prometheus UI first
4. Verify time range includes data

### Slow Dashboard Loading

1. Reduce number of panels
2. Increase scrape interval
3. Use recording rules for complex queries
4. Limit time range

### Alert Not Triggering

1. Check alert rule conditions
2. Verify "For" duration isn't too long
3. Test query in Explore view
4. Check notification channel configuration

---

## Next Steps

1. Create your custom dashboards based on these templates
2. Set up alerting rules
3. Configure notification channels (Email, Slack, PagerDuty)
4. Export dashboards for backup
5. Share dashboards with team

Happy Visualizing! 📈
