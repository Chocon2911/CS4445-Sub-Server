"""
Prometheus Data Logger Scheduler
Tự động log dữ liệu từ Prometheus mỗi 24 giờ kể từ lúc chạy.

Usage:
    python prometheus_scheduler.py

Dependencies:
    pip install requests pandas schedule
"""

import requests
import pandas as pd
from datetime import datetime, timedelta
import os
import time
import schedule
import logging

# Cấu hình logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('prometheus_scheduler.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# Configuration
PROMETHEUS_URL = "http://localhost:9090"
OUTPUT_DIR = "./exported_data"

# Metrics configuration
METRICS = {
    # Counter metrics
    "counters": [
        {"name": "app_requests_total", "description": "Total requests"},
        {"name": "app_errors_total", "description": "Total errors"},
        {"name": "app_connections_total", "description": "Total connections"},
        {"name": "app_jobs_processed_total", "description": "Total jobs processed"},
    ],
    # Gauge metrics
    "gauges": [
        {"name": "process_cpu_usage", "description": "CPU usage (0-1)"},
        {"name": "jvm_memory_used_bytes", "query": 'jvm_memory_used_bytes{area="heap"}', "description": "RAM used bytes"},
        {"name": "jvm_memory_max_bytes", "query": 'jvm_memory_max_bytes{area="heap"}', "description": "RAM max bytes"},
        {"name": "app_connections_current", "description": "Current connections"},
        {"name": "app_queue_length", "description": "Queue length"},
    ],
    # Histogram metrics (need special handling)
    "histograms": [
        {"name": "app_request_latency_seconds", "description": "Request latency"},
        {"name": "app_response_size_bytes", "description": "Response size"},
        {"name": "app_processing_time_seconds", "description": "Processing time"},
    ],
}


def query_prometheus(query, start_time, end_time, step="15s"):
    """Query Prometheus range API"""
    url = f"{PROMETHEUS_URL}/api/v1/query_range"
    params = {
        "query": query,
        "start": start_time.isoformat() + "Z",
        "end": end_time.isoformat() + "Z",
        "step": step,
    }

    try:
        response = requests.get(url, params=params, timeout=30)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        logger.error(f"Error querying {query}: {e}")
        return None


def parse_range_result(result):
    """Parse Prometheus range query result to DataFrame"""
    if not result or result.get("status") != "success":
        return pd.DataFrame()

    data = result.get("data", {}).get("result", [])
    if not data:
        return pd.DataFrame()

    rows = []
    for series in data:
        labels = series.get("metric", {})
        values = series.get("values", [])

        for timestamp, value in values:
            row = {
                "timestamp": datetime.fromtimestamp(timestamp),
                "value": float(value) if value != "NaN" else None,
            }
            row.update(labels)
            rows.append(row)

    return pd.DataFrame(rows)


def export_counters(start_time, end_time, step="15s"):
    """Export counter metrics"""
    logger.info("=== Exporting Counter Metrics ===")
    all_data = []

    for metric in METRICS["counters"]:
        name = metric["name"]
        query = metric.get("query", name)
        logger.info(f"  Querying {name}...")

        result = query_prometheus(query, start_time, end_time, step)
        df = parse_range_result(result)

        if not df.empty:
            df["metric_name"] = name
            df["metric_type"] = "counter"
            all_data.append(df)
            logger.info(f"    -> {len(df)} records")
        else:
            logger.info(f"    -> No data")

    if all_data:
        combined = pd.concat(all_data, ignore_index=True)
        return combined
    return pd.DataFrame()


def export_gauges(start_time, end_time, step="15s"):
    """Export gauge metrics"""
    logger.info("=== Exporting Gauge Metrics ===")
    all_data = []

    for metric in METRICS["gauges"]:
        name = metric["name"]
        query = metric.get("query", name)
        logger.info(f"  Querying {name}...")

        result = query_prometheus(query, start_time, end_time, step)
        df = parse_range_result(result)

        if not df.empty:
            df["metric_name"] = name
            df["metric_type"] = "gauge"
            all_data.append(df)
            logger.info(f"    -> {len(df)} records")
        else:
            logger.info(f"    -> No data")

    if all_data:
        combined = pd.concat(all_data, ignore_index=True)
        return combined
    return pd.DataFrame()


def export_histograms(start_time, end_time, step="15s"):
    """Export histogram metrics with percentiles"""
    logger.info("=== Exporting Histogram Metrics ===")
    all_data = []

    percentiles = [0.50, 0.90, 0.95, 0.99]

    for metric in METRICS["histograms"]:
        name = metric["name"]
        logger.info(f"  Querying {name}...")

        # Query count
        count_query = f"{name}_count"
        result = query_prometheus(count_query, start_time, end_time, step)
        df = parse_range_result(result)
        if not df.empty:
            df["metric_name"] = f"{name}_count"
            df["metric_type"] = "histogram_count"
            all_data.append(df)
            logger.info(f"    -> count: {len(df)} records")

        # Query sum
        sum_query = f"{name}_sum"
        result = query_prometheus(sum_query, start_time, end_time, step)
        df = parse_range_result(result)
        if not df.empty:
            df["metric_name"] = f"{name}_sum"
            df["metric_type"] = "histogram_sum"
            all_data.append(df)
            logger.info(f"    -> sum: {len(df)} records")

        # Query percentiles
        for p in percentiles:
            p_query = f'histogram_quantile({p}, rate({name}_bucket[1m]))'
            result = query_prometheus(p_query, start_time, end_time, step)
            df = parse_range_result(result)
            if not df.empty:
                df["metric_name"] = f"{name}_p{int(p*100)}"
                df["metric_type"] = "histogram_percentile"
                df["percentile"] = p
                all_data.append(df)
                logger.info(f"    -> p{int(p*100)}: {len(df)} records")

    if all_data:
        combined = pd.concat(all_data, ignore_index=True)
        return combined
    return pd.DataFrame()


def export_rate_metrics(start_time, end_time, step="15s"):
    """Export rate-based metrics (requests/sec, errors/sec)"""
    logger.info("=== Exporting Rate Metrics ===")
    all_data = []

    rate_queries = [
        {"name": "request_rate_per_sec", "query": "rate(app_requests_total[1m])"},
        {"name": "error_rate_per_sec", "query": "rate(app_errors_total[1m])"},
        {"name": "jobs_rate_per_sec", "query": "rate(app_jobs_processed_total[1m])"},
        {"name": "avg_latency_seconds", "query": "rate(app_request_latency_seconds_sum[1m]) / rate(app_request_latency_seconds_count[1m])"},
        {"name": "avg_processing_time_seconds", "query": "rate(app_processing_time_seconds_sum[1m]) / rate(app_processing_time_seconds_count[1m])"},
    ]

    for metric in rate_queries:
        name = metric["name"]
        query = metric["query"]
        logger.info(f"  Querying {name}...")

        result = query_prometheus(query, start_time, end_time, step)
        df = parse_range_result(result)

        if not df.empty:
            df["metric_name"] = name
            df["metric_type"] = "rate"
            all_data.append(df)
            logger.info(f"    -> {len(df)} records")
        else:
            logger.info(f"    -> No data")

    if all_data:
        combined = pd.concat(all_data, ignore_index=True)
        return combined
    return pd.DataFrame()


def create_combined_timeseries(start_time, end_time, step="15s"):
    """Create a combined timeseries with all metrics as columns"""
    logger.info("=== Creating Combined Timeseries ===")

    queries = {
        "requests_total": "app_requests_total",
        "errors_total": "app_errors_total",
        "connections_total": "app_connections_total",
        "jobs_processed": "app_jobs_processed_total",
        "cpu_usage": "process_cpu_usage",
        "memory_used_bytes": 'jvm_memory_used_bytes{area="heap"}',
        "memory_max_bytes": 'jvm_memory_max_bytes{area="heap"}',
        "connections_current": "app_connections_current",
        "queue_length": "app_queue_length",
        "request_rate": "rate(app_requests_total[1m])",
        "error_rate": "rate(app_errors_total[1m])",
        "latency_p50": "histogram_quantile(0.50, rate(app_request_latency_seconds_bucket[1m]))",
        "latency_p95": "histogram_quantile(0.95, rate(app_request_latency_seconds_bucket[1m]))",
        "latency_p99": "histogram_quantile(0.99, rate(app_request_latency_seconds_bucket[1m]))",
        "avg_latency": "rate(app_request_latency_seconds_sum[1m]) / rate(app_request_latency_seconds_count[1m])",
        "avg_processing_time": "rate(app_processing_time_seconds_sum[1m]) / rate(app_processing_time_seconds_count[1m])",
    }

    all_series = {}

    for col_name, query in queries.items():
        logger.info(f"  Querying {col_name}...")
        result = query_prometheus(query, start_time, end_time, step)
        df = parse_range_result(result)

        if not df.empty:
            # Use first series if multiple
            series = df.groupby("timestamp")["value"].first()
            all_series[col_name] = series
            logger.info(f"    -> {len(series)} records")
        else:
            logger.info(f"    -> No data")

    if all_series:
        combined = pd.DataFrame(all_series)
        combined = combined.reset_index()

        # Add derived metrics
        if "memory_used_bytes" in combined.columns and "memory_max_bytes" in combined.columns:
            combined["memory_usage_percent"] = (combined["memory_used_bytes"] / combined["memory_max_bytes"]) * 100

        if "cpu_usage" in combined.columns:
            combined["cpu_usage_percent"] = combined["cpu_usage"] * 100

        return combined

    return pd.DataFrame()


def check_prometheus_connection():
    """Check connection to Prometheus"""
    try:
        response = requests.get(f"{PROMETHEUS_URL}/api/v1/status/config", timeout=5)
        response.raise_for_status()
        return True
    except Exception as e:
        logger.error(f"Failed to connect to Prometheus: {e}")
        return False


def log_prometheus_data():
    """Main job function - logs all Prometheus data"""
    logger.info("=" * 60)
    logger.info("Starting scheduled Prometheus data export...")
    logger.info("=" * 60)

    # Create output directory
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # Time range (last 24 hours)
    end_time = datetime.utcnow()
    start_time = end_time - timedelta(hours=24)
    step = "15s"  # 15 second intervals

    logger.info(f"Exporting metrics from {start_time} to {end_time}")
    logger.info(f"Step: {step}")
    logger.info(f"Output directory: {OUTPUT_DIR}")

    # Check connection
    if not check_prometheus_connection():
        logger.error(f"Cannot connect to Prometheus at {PROMETHEUS_URL}")
        logger.error("Skipping this export cycle. Will retry in 24 hours.")
        return

    logger.info("Connected to Prometheus successfully!")

    # Export each category
    counters_df = export_counters(start_time, end_time, step)
    gauges_df = export_gauges(start_time, end_time, step)
    histograms_df = export_histograms(start_time, end_time, step)
    rates_df = export_rate_metrics(start_time, end_time, step)
    combined_df = create_combined_timeseries(start_time, end_time, step)

    # Save to CSV
    timestamp_str = datetime.now().strftime("%Y%m%d_%H%M%S")

    if not counters_df.empty:
        path = f"{OUTPUT_DIR}/counters_{timestamp_str}.csv"
        counters_df.to_csv(path, index=False)
        logger.info(f"Saved: {path} ({len(counters_df)} rows)")

    if not gauges_df.empty:
        path = f"{OUTPUT_DIR}/gauges_{timestamp_str}.csv"
        gauges_df.to_csv(path, index=False)
        logger.info(f"Saved: {path} ({len(gauges_df)} rows)")

    if not histograms_df.empty:
        path = f"{OUTPUT_DIR}/histograms_{timestamp_str}.csv"
        histograms_df.to_csv(path, index=False)
        logger.info(f"Saved: {path} ({len(histograms_df)} rows)")

    if not rates_df.empty:
        path = f"{OUTPUT_DIR}/rates_{timestamp_str}.csv"
        rates_df.to_csv(path, index=False)
        logger.info(f"Saved: {path} ({len(rates_df)} rows)")

    # Combined timeseries (best for ML training)
    if not combined_df.empty:
        path = f"{OUTPUT_DIR}/combined_timeseries_{timestamp_str}.csv"
        combined_df.to_csv(path, index=False)
        logger.info(f"Saved: {path} ({len(combined_df)} rows)")
        logger.info("*** combined_timeseries is recommended for AI training ***")
        logger.info(f"Columns: {list(combined_df.columns)}")

    logger.info("=== Export Complete ===")
    logger.info(f"Next export scheduled in 24 hours")
    logger.info("=" * 60)


def run_scheduler():
    """Run the scheduler"""
    logger.info("=" * 60)
    logger.info("Prometheus Data Logger Scheduler Started")
    logger.info(f"Prometheus URL: {PROMETHEUS_URL}")
    logger.info(f"Output Directory: {OUTPUT_DIR}")
    logger.info("Schedule: Every 24 hours from now")
    logger.info("=" * 60)

    # Run immediately on start
    logger.info("Running initial export...")
    log_prometheus_data()

    # Schedule to run every 24 hours
    schedule.every(24).hours.do(log_prometheus_data)

    logger.info("Scheduler is running. Press Ctrl+C to stop.")

    # Keep the scheduler running
    while True:
        schedule.run_pending()
        time.sleep(60)  # Check every minute


if __name__ == "__main__":
    try:
        run_scheduler()
    except KeyboardInterrupt:
        logger.info("\nScheduler stopped by user.")
    except Exception as e:
        logger.error(f"Scheduler error: {e}")
        raise
