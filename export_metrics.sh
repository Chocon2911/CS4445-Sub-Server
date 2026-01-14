#!/bin/bash

echo "========================================="
echo "EXPORTING MONITORING METRICS"
echo "========================================="
echo ""

# Create export directory
mkdir -p metrics_export
cd metrics_export

# Timestamp for the export
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# ========================================
# COUNTER METRICS
# ========================================
echo "📊 Exporting Counter Metrics..."

# Tổng số request
curl -s 'http://localhost:9090/api/v1/query?query=app_requests_total' > counter_total_requests_${TIMESTAMP}.json
echo "✓ Total Requests exported"

# Tổng số error
curl -s 'http://localhost:9090/api/v1/query?query=app_errors_total' > counter_total_errors_${TIMESTAMP}.json
echo "✓ Total Errors exported"

# Tổng số connection
curl -s 'http://localhost:9090/api/v1/query?query=app_connections_total' > counter_total_connections_${TIMESTAMP}.json
echo "✓ Total Connections exported"

# Tổng số job đã xử lý
curl -s 'http://localhost:9090/api/v1/query?query=app_jobs_processed_total' > counter_jobs_processed_${TIMESTAMP}.json
echo "✓ Total Jobs Processed exported"

echo ""

# ========================================
# GAUGE METRICS
# ========================================
echo "📈 Exporting Gauge Metrics..."

# CPU usage
curl -s 'http://localhost:9090/api/v1/query?query=process_cpu_usage' > gauge_cpu_usage_${TIMESTAMP}.json
echo "✓ CPU Usage exported"

# RAM usage
curl -s 'http://localhost:9090/api/v1/query?query=jvm_memory_used_bytes{area="heap"}' > gauge_ram_usage_${TIMESTAMP}.json
echo "✓ RAM Usage exported"

# Số connection hiện tại
curl -s 'http://localhost:9090/api/v1/query?query=app_connections_current' > gauge_current_connections_${TIMESTAMP}.json
echo "✓ Current Connections exported"

# Queue length
curl -s 'http://localhost:9090/api/v1/query?query=app_queue_length' > gauge_queue_length_${TIMESTAMP}.json
echo "✓ Queue Length exported"

echo ""

# ========================================
# HISTOGRAM METRICS
# ========================================
echo "📉 Exporting Histogram Metrics..."

# Request latency (get all percentiles)
curl -s 'http://localhost:9090/api/v1/query?query=app_request_latency_seconds_bucket' > histogram_request_latency_buckets_${TIMESTAMP}.json
curl -s 'http://localhost:9090/api/v1/query?query=app_request_latency_seconds_sum' > histogram_request_latency_sum_${TIMESTAMP}.json
curl -s 'http://localhost:9090/api/v1/query?query=app_request_latency_seconds_count' > histogram_request_latency_count_${TIMESTAMP}.json
echo "✓ Request Latency exported"

# Response size
curl -s 'http://localhost:9090/api/v1/query?query=app_response_size_bytes_bucket' > histogram_response_size_buckets_${TIMESTAMP}.json
curl -s 'http://localhost:9090/api/v1/query?query=app_response_size_bytes_sum' > histogram_response_size_sum_${TIMESTAMP}.json
curl -s 'http://localhost:9090/api/v1/query?query=app_response_size_bytes_count' > histogram_response_size_count_${TIMESTAMP}.json
echo "✓ Response Size exported"

# Processing time
curl -s 'http://localhost:9090/api/v1/query?query=app_processing_time_seconds_bucket' > histogram_processing_time_buckets_${TIMESTAMP}.json
curl -s 'http://localhost:9090/api/v1/query?query=app_processing_time_seconds_sum' > histogram_processing_time_sum_${TIMESTAMP}.json
curl -s 'http://localhost:9090/api/v1/query?query=app_processing_time_seconds_count' > histogram_processing_time_count_${TIMESTAMP}.json
echo "✓ Processing Time exported"

echo ""
echo "========================================="
echo "✅ All metrics exported to: metrics_export/"
echo "Files created: $(ls -1 | wc -l)"
echo "========================================="

cd ..
