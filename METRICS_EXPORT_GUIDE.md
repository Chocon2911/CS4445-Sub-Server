# Metrics Export Guide

This guide shows you how to export all monitoring metrics from your application.

## 📋 Metrics Available for Export

### **Counter (Always Increasing)**
- ✅ Tổng số request (`app_requests_total`)
- ✅ Tổng số error (`app_errors_total`)
- ✅ Tổng số connection (`app_connections_total`)
- ✅ Tổng số job đã xử lý (`app_jobs_processed_total`)

### **Gauge (Current Values)**
- ✅ CPU usage (`process_cpu_usage`)
- ✅ RAM usage (`jvm_memory_used_bytes`)
- ✅ Số connection hiện tại (`app_connections_current`)
- ✅ Queue length (`app_queue_length`)

### **Histogram (Distributions)**
- ✅ Request latency (`app_request_latency_seconds`)
- ✅ Response size (`app_response_size_bytes`)
- ✅ Processing time (`app_processing_time_seconds`)

---

## 🚀 How to Export

### **Option 1: Export as JSON** (Separate files for each metric)

```bash
./export_metrics.sh
```

**Output:** Creates `metrics_export/` folder with individual JSON files:
- `counter_total_requests_20251221_103045.json`
- `gauge_cpu_usage_20251221_103045.json`
- `histogram_request_latency_buckets_20251221_103045.json`
- etc.

---

### **Option 2: Export as CSV** (Single file with all metrics)

```bash
./export_metrics_csv.sh
```

**Output:** Creates `metrics_export_csv/all_metrics_TIMESTAMP.csv`

**CSV Format:**
```csv
metric_type,metric_name,value,timestamp
counter,total_requests,1524,1703158245
counter,total_errors,23,1703158245
gauge,cpu_usage,0.45,1703158245
gauge,ram_usage_bytes,536870912,1703158245
histogram,request_latency_avg_ms,234.5,1703158245
```

**Import to database:**
```sql
-- PostgreSQL
COPY metrics_snapshot FROM '/path/to/all_metrics.csv' DELIMITER ',' CSV HEADER;

-- MySQL
LOAD DATA LOCAL INFILE '/path/to/all_metrics.csv'
INTO TABLE metrics_snapshot
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;
```

---

### **Option 3: Export as SQL** (Ready to import to database)

```bash
./export_metrics_sql.sh
```

**Output:** Creates `metrics_export_sql/metrics_TIMESTAMP.sql`

**SQL File Contains:**
1. CREATE TABLE statement
2. INSERT statements for all metrics

**Import to your database:**
```bash
# PostgreSQL
psql -h your-server.com -U username -d database_name -f metrics_export_sql/metrics_*.sql

# MySQL
mysql -h your-server.com -u username -p database_name < metrics_export_sql/metrics_*.sql
```

---

## 📊 Manual Export (Individual Queries)

If you want to export specific metrics manually:

### **Counters:**
```bash
# Total requests
curl 'http://localhost:9090/api/v1/query?query=app_requests_total'

# Total errors
curl 'http://localhost:9090/api/v1/query?query=app_errors_total'

# Total connections
curl 'http://localhost:9090/api/v1/query?query=app_connections_total'

# Jobs processed
curl 'http://localhost:9090/api/v1/query?query=app_jobs_processed_total'
```

### **Gauges:**
```bash
# CPU usage (0-1, multiply by 100 for percentage)
curl 'http://localhost:9090/api/v1/query?query=process_cpu_usage'

# RAM usage in bytes (divide by 1024^2 for MB)
curl 'http://localhost:9090/api/v1/query?query=jvm_memory_used_bytes{area="heap"}'

# Current connections
curl 'http://localhost:9090/api/v1/query?query=app_connections_current'

# Queue length
curl 'http://localhost:9090/api/v1/query?query=app_queue_length'
```

### **Histograms (Averages):**
```bash
# Average request latency in milliseconds (last 5 minutes)
curl 'http://localhost:9090/api/v1/query?query=rate(app_request_latency_seconds_sum[5m])/rate(app_request_latency_seconds_count[5m])*1000'

# Average response size in bytes
curl 'http://localhost:9090/api/v1/query?query=rate(app_response_size_bytes_sum[5m])/rate(app_response_size_bytes_count[5m])'

# Average processing time in milliseconds
curl 'http://localhost:9090/api/v1/query?query=rate(app_processing_time_seconds_sum[5m])/rate(app_processing_time_seconds_count[5m])*1000'
```

### **Histograms (Percentiles):**
```bash
# 95th percentile request latency
curl 'http://localhost:9090/api/v1/query?query=histogram_quantile(0.95,rate(app_request_latency_seconds_bucket[5m]))*1000'

# 99th percentile request latency
curl 'http://localhost:9090/api/v1/query?query=histogram_quantile(0.99,rate(app_request_latency_seconds_bucket[5m]))*1000'

# Median (50th percentile) response size
curl 'http://localhost:9090/api/v1/query?query=histogram_quantile(0.5,rate(app_response_size_bytes_bucket[5m]))'
```

---

## 🔄 Automated Export (Scheduled)

### **Export every hour with cron:**

```bash
# Edit crontab
crontab -e

# Add this line to run every hour at minute 0
0 * * * * cd /path/to/your/project && ./export_metrics_sql.sh && psql -h your-server -U user -d db -f metrics_export_sql/metrics_*.sql
```

### **Export every 5 minutes:**
```bash
# Add to crontab
*/5 * * * * cd /path/to/your/project && ./export_metrics_csv.sh
```

---

## 📤 Send to Remote Database

### **Method 1: Direct PostgreSQL Connection**
```bash
# Export and send in one command
./export_metrics_sql.sh && \
psql -h remote-server.com -U username -d database_name -f metrics_export_sql/metrics_*.sql
```

### **Method 2: Via API (POST to your server)**
```bash
# Get all metrics as JSON and POST to your API
curl -X POST http://your-api-server.com/api/metrics \
  -H "Content-Type: application/json" \
  -d "$(curl -s http://localhost:9090/api/v1/query?query=app_requests_total)"
```

### **Method 3: Using SCP (Secure Copy)**
```bash
# Export and copy to remote server
./export_metrics_csv.sh
scp metrics_export_csv/*.csv user@remote-server:/path/to/import/
```

---

## 🎯 Quick Test

```bash
# 1. Make sure your app is running
./mvnw spring-boot:run

# 2. Export metrics
./export_metrics_csv.sh

# 3. View the exported data
cat metrics_export_csv/all_metrics_*.csv
```

---

## 📦 Database Table Schema

If you need to create a table in your database to store these metrics:

```sql
CREATE TABLE metrics_snapshot (
    id SERIAL PRIMARY KEY,
    snapshot_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metric_type VARCHAR(50),        -- 'counter', 'gauge', or 'histogram'
    metric_name VARCHAR(100),       -- 'total_requests', 'cpu_usage', etc.
    metric_value DOUBLE PRECISION,  -- The actual value
    metric_timestamp BIGINT         -- Unix timestamp from Prometheus
);

-- Add indexes for faster queries
CREATE INDEX idx_metric_name ON metrics_snapshot(metric_name);
CREATE INDEX idx_snapshot_time ON metrics_snapshot(snapshot_time);
CREATE INDEX idx_metric_type ON metrics_snapshot(metric_type);
```

---

## 💡 Tips

1. **For real-time export**: Use Prometheus Remote Write feature in `prometheus.yml`
2. **For large datasets**: Export time ranges instead of all data at once
3. **For backup**: Export daily and keep historical data
4. **For analysis**: Use CSV format - easy to import to Excel/Google Sheets
5. **For automation**: Use SQL format - ready for database import

---

## ❓ Troubleshooting

**Q: Empty or zero values?**
- Make sure your app is running and receiving requests
- Send some test requests first: `curl -X POST http://localhost:8080/api/v1/fakePacket -H "Content-Type: application/json" -d '{"id":1,"data":"test"}'`

**Q: Prometheus not responding?**
- Check if Prometheus is running: `docker ps | grep prometheus`
- Check Prometheus URL: http://localhost:9090

**Q: jq command not found?**
- Install jq: `sudo apt-get install jq` (Linux) or `brew install jq` (Mac)
- Or modify scripts to not use jq

---

## 📞 Need Help?

Check these files:
- `prometheus.yml` - Prometheus configuration
- `src/main/java/com/CS445/CS4445_Sub_Server/service/MetricsService.java` - Metrics definitions
- `compose.yaml` - Docker services configuration
