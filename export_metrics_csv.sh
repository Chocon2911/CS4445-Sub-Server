#!/bin/bash

echo "========================================="
echo "EXPORTING METRICS TO CSV FORMAT"
echo "========================================="
echo ""

# Create export directory
mkdir -p metrics_export_csv
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
OUTPUT_FILE="metrics_export_csv/all_metrics_${TIMESTAMP}.csv"

# Create CSV header
echo "metric_type,metric_name,value,timestamp" > $OUTPUT_FILE

echo "📊 Exporting metrics to CSV..."

# Helper function to extract value and add to CSV
export_to_csv() {
    local metric_type=$1
    local metric_name=$2
    local query=$3

    result=$(curl -s "http://localhost:9090/api/v1/query?query=${query}")
    value=$(echo $result | jq -r '.data.result[0].value[1] // "0"')
    timestamp=$(echo $result | jq -r '.data.result[0].value[0] // "0"')

    echo "${metric_type},${metric_name},${value},${timestamp}" >> $OUTPUT_FILE
}

# ========================================
# COUNTER METRICS
# ========================================
export_to_csv "counter" "total_requests" "app_requests_total"
export_to_csv "counter" "total_errors" "app_errors_total"
export_to_csv "counter" "total_connections" "app_connections_total"
export_to_csv "counter" "jobs_processed" "app_jobs_processed_total"

# ========================================
# GAUGE METRICS
# ========================================
export_to_csv "gauge" "cpu_usage" "process_cpu_usage"
export_to_csv "gauge" "ram_usage_bytes" "jvm_memory_used_bytes{area=\"heap\"}"
export_to_csv "gauge" "current_connections" "app_connections_current"
export_to_csv "gauge" "queue_length" "app_queue_length"

# ========================================
# HISTOGRAM METRICS (Average values)
# ========================================
# Request latency average (in milliseconds)
export_to_csv "histogram" "request_latency_avg_ms" "rate(app_request_latency_seconds_sum[5m])/rate(app_request_latency_seconds_count[5m])*1000"

# Response size average (in bytes)
export_to_csv "histogram" "response_size_avg_bytes" "rate(app_response_size_bytes_sum[5m])/rate(app_response_size_bytes_count[5m])"

# Processing time average (in milliseconds)
export_to_csv "histogram" "processing_time_avg_ms" "rate(app_processing_time_seconds_sum[5m])/rate(app_processing_time_seconds_count[5m])*1000"

echo ""
echo "========================================="
echo "✅ CSV file created: $OUTPUT_FILE"
echo ""
echo "Preview:"
cat $OUTPUT_FILE
echo "========================================="
