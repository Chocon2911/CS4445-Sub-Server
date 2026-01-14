#!/bin/bash

echo "========================================="
echo "EXPORTING METRICS TO SQL FORMAT"
echo "========================================="
echo ""

# Create export directory
mkdir -p metrics_export_sql
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
OUTPUT_FILE="metrics_export_sql/metrics_${TIMESTAMP}.sql"

# Create SQL file with table creation
cat > $OUTPUT_FILE << 'EOF'
-- Table to store metrics snapshots
CREATE TABLE IF NOT EXISTS metrics_snapshot (
    id SERIAL PRIMARY KEY,
    snapshot_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metric_type VARCHAR(50),
    metric_name VARCHAR(100),
    metric_value DOUBLE PRECISION,
    metric_timestamp BIGINT
);

-- Insert metrics data
EOF

echo "📊 Generating SQL INSERT statements..."

# Helper function to create SQL INSERT
export_to_sql() {
    local metric_type=$1
    local metric_name=$2
    local query=$3

    result=$(curl -s "http://localhost:9090/api/v1/query?query=${query}")
    value=$(echo $result | jq -r '.data.result[0].value[1] // "0"')
    timestamp=$(echo $result | jq -r '.data.result[0].value[0] // "0"')

    echo "INSERT INTO metrics_snapshot (metric_type, metric_name, metric_value, metric_timestamp) VALUES ('${metric_type}', '${metric_name}', ${value}, ${timestamp});" >> $OUTPUT_FILE
}

# ========================================
# COUNTER METRICS
# ========================================
export_to_sql "counter" "total_requests" "app_requests_total"
export_to_sql "counter" "total_errors" "app_errors_total"
export_to_sql "counter" "total_connections" "app_connections_total"
export_to_sql "counter" "jobs_processed" "app_jobs_processed_total"

# ========================================
# GAUGE METRICS
# ========================================
export_to_sql "gauge" "cpu_usage" "process_cpu_usage"
export_to_sql "gauge" "ram_usage_bytes" "jvm_memory_used_bytes{area=\"heap\"}"
export_to_sql "gauge" "current_connections" "app_connections_current"
export_to_sql "gauge" "queue_length" "app_queue_length"

# ========================================
# HISTOGRAM METRICS
# ========================================
export_to_sql "histogram" "request_latency_avg_ms" "rate(app_request_latency_seconds_sum[5m])/rate(app_request_latency_seconds_count[5m])*1000"
export_to_sql "histogram" "response_size_avg_bytes" "rate(app_response_size_bytes_sum[5m])/rate(app_response_size_bytes_count[5m])"
export_to_sql "histogram" "processing_time_avg_ms" "rate(app_processing_time_seconds_sum[5m])/rate(app_processing_time_seconds_count[5m])*1000"

echo ""
echo "========================================="
echo "✅ SQL file created: $OUTPUT_FILE"
echo ""
echo "You can now import this to your database:"
echo "  PostgreSQL: psql -h your-server -U user -d database -f $OUTPUT_FILE"
echo "  MySQL:      mysql -h your-server -u user -p database < $OUTPUT_FILE"
echo "========================================="
