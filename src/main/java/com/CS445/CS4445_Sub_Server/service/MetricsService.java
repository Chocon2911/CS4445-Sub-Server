package com.CS445.CS4445_Sub_Server.service;

import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for managing custom Prometheus metrics
 * Implements the monitoring schema with Counters, Gauges, and Histograms
 */
@Service
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // ===== COUNTERS =====
    private final Counter totalRequestsCounter;
    private final Counter totalErrorsCounter;
    private final Counter totalConnectionsCounter;
    private final Counter totalJobsProcessedCounter;

    // ===== GAUGES =====
    private final AtomicInteger currentConnections;
    private final AtomicInteger currentQueueLength;

    // ===== HISTOGRAMS (implemented as Timers/DistributionSummary) =====
    private final Timer requestLatencyTimer;
    private final Timer processingTimeTimer;
    private final DistributionSummary responseSizeDistribution;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize Counters
        this.totalRequestsCounter = Counter.builder("app.requests.total")
                .description("Total number of requests received")
                .tag("type", "all")
                .register(meterRegistry);

        this.totalErrorsCounter = Counter.builder("app.errors.total")
                .description("Total number of errors occurred")
                .tag("type", "all")
                .register(meterRegistry);

        this.totalConnectionsCounter = Counter.builder("app.connections.total")
                .description("Total number of connections established")
                .tag("type", "all")
                .register(meterRegistry);

        this.totalJobsProcessedCounter = Counter.builder("app.jobs.processed.total")
                .description("Total number of jobs processed successfully")
                .tag("type", "all")
                .register(meterRegistry);

        // Initialize Gauges
        this.currentConnections = new AtomicInteger(0);
        Gauge.builder("app.connections.current", currentConnections, AtomicInteger::get)
                .description("Current number of active connections")
                .tag("type", "active")
                .register(meterRegistry);

        this.currentQueueLength = new AtomicInteger(0);
        Gauge.builder("app.queue.length", currentQueueLength, AtomicInteger::get)
                .description("Current queue length")
                .tag("type", "pending")
                .register(meterRegistry);

        // Register JVM CPU and Memory Gauges (automatically collected by Micrometer)
        // These are available as:
        // - process.cpu.usage (Gauge for CPU usage)
        // - jvm.memory.used (Gauge for memory usage)

        // Initialize Histograms
        this.requestLatencyTimer = Timer.builder("app.request.latency")
                .description("Request latency in milliseconds")
                .tag("type", "http")
                .publishPercentileHistogram()
                .register(meterRegistry);

        this.processingTimeTimer = Timer.builder("app.processing.time")
                .description("Job processing time in milliseconds")
                .tag("type", "job")
                .publishPercentileHistogram()
                .register(meterRegistry);

        this.responseSizeDistribution = DistributionSummary.builder("app.response.size")
                .description("Response size in bytes")
                .tag("type", "http")
                .baseUnit("bytes")
                .publishPercentileHistogram()
                .register(meterRegistry);

        log.info("MetricsService initialized with custom Prometheus metrics");
    }

    // ===== COUNTER METHODS =====

    public void incrementTotalRequests() {
        totalRequestsCounter.increment();
    }

    public void incrementTotalRequests(double amount) {
        totalRequestsCounter.increment(amount);
    }

    public void incrementTotalErrors() {
        totalErrorsCounter.increment();
    }

    public void incrementTotalErrors(String errorType) {
        Counter.builder("app.errors.total")
                .description("Total number of errors occurred")
                .tag("type", errorType)
                .register(meterRegistry)
                .increment();
    }

    public void incrementTotalConnections() {
        totalConnectionsCounter.increment();
    }

    public void incrementTotalJobsProcessed() {
        totalJobsProcessedCounter.increment();
    }

    // ===== GAUGE METHODS =====

    public void incrementCurrentConnections() {
        currentConnections.incrementAndGet();
    }

    public void decrementCurrentConnections() {
        currentConnections.decrementAndGet();
    }

    public int getCurrentConnections() {
        return currentConnections.get();
    }

    public void setCurrentConnections(int value) {
        currentConnections.set(value);
    }

    public void incrementQueueLength() {
        currentQueueLength.incrementAndGet();
    }

    public void decrementQueueLength() {
        currentQueueLength.decrementAndGet();
    }

    public int getQueueLength() {
        return currentQueueLength.get();
    }

    public void setQueueLength(int value) {
        currentQueueLength.set(value);
    }

    // ===== HISTOGRAM/TIMER METHODS =====

    /**
     * Record request latency
     * @param latencyMs latency in milliseconds
     */
    public void recordRequestLatency(long latencyMs) {
        requestLatencyTimer.record(latencyMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Start recording request latency, returns a sample that should be stopped
     */
    public Timer.Sample startRequestLatencyTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Stop and record request latency
     */
    public void stopRequestLatencyTimer(Timer.Sample sample) {
        sample.stop(requestLatencyTimer);
    }

    /**
     * Record processing time
     * @param processingTimeMs processing time in milliseconds
     */
    public void recordProcessingTime(long processingTimeMs) {
        processingTimeTimer.record(processingTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Record processing time with tags
     */
    public void recordProcessingTime(long processingTimeMs, String jobType) {
        Timer.builder("app.processing.time")
                .description("Job processing time in milliseconds")
                .tag("type", jobType)
                .publishPercentileHistogram()
                .register(meterRegistry)
                .record(processingTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Record response size
     * @param sizeBytes response size in bytes
     */
    public void recordResponseSize(long sizeBytes) {
        responseSizeDistribution.record(sizeBytes);
    }

    /**
     * Record response size with tags
     */
    public void recordResponseSize(long sizeBytes, String responseType) {
        DistributionSummary.builder("app.response.size")
                .description("Response size in bytes")
                .tag("type", responseType)
                .baseUnit("bytes")
                .publishPercentileHistogram()
                .register(meterRegistry)
                .record(sizeBytes);
    }

    // ===== UTILITY METHODS =====

    /**
     * Record a complete request lifecycle
     */
    public void recordRequest(long latencyMs, long processingTimeMs, long responseSizeBytes, boolean isError) {
        incrementTotalRequests();
        recordRequestLatency(latencyMs);
        recordProcessingTime(processingTimeMs);
        recordResponseSize(responseSizeBytes);

        if (isError) {
            incrementTotalErrors();
        } else {
            incrementTotalJobsProcessed();
        }
    }

    /**
     * Get current metric values for logging/debugging
     */
    public String getMetricsSummary() {
        return String.format(
                "Metrics Summary - Total Requests: %.0f, Total Errors: %.0f, " +
                "Total Connections: %.0f, Jobs Processed: %.0f, " +
                "Current Connections: %d, Queue Length: %d",
                totalRequestsCounter.count(),
                totalErrorsCounter.count(),
                totalConnectionsCounter.count(),
                totalJobsProcessedCounter.count(),
                getCurrentConnections(),
                getQueueLength()
        );
    }
}
