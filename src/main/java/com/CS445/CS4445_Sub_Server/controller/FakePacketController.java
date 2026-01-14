package com.CS445.CS4445_Sub_Server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CS445.CS4445_Sub_Server.dto.FakePacketRequest;
import com.CS445.CS4445_Sub_Server.dto.FakePacketResponse;
import com.CS445.CS4445_Sub_Server.service.FakePacketService;
import com.CS445.CS4445_Sub_Server.service.MetricsService;
import com.CS445.CS4445_Sub_Server.service.ServerStateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class FakePacketController {

    private final FakePacketService fakePacketService;
    private final ServerStateService serverStateService;
    private final MetricsService metricsService;
    private final ObjectMapper objectMapper;

    @PostMapping("/fakePacket")
    public ResponseEntity<FakePacketResponse> processFakePacket(@RequestBody FakePacketRequest request) {
        long requestStartTime = System.currentTimeMillis();
        Timer.Sample latencySample = metricsService.startRequestLatencyTimer();

        // Track connection
        metricsService.incrementCurrentConnections();
        metricsService.incrementTotalConnections();
        metricsService.incrementTotalRequests();

        log.info("Received fakePacket request: {}", request.getPacketId());

        try {
            // Check if server is open
            if (!serverStateService.isServerOpen()) {
                log.warn("Server is CLOSED. Rejecting packet request: {}", request.getPacketId());
                metricsService.incrementTotalErrors("server_closed");

                FakePacketResponse rejectedResponse = FakePacketResponse.builder()
                        .packetId(request.getPacketId())
                        .status("REJECTED")
                        .result("Server is currently closed. Please open the server first using /api/v1/server/open")
                        .build();

                recordMetrics(latencySample, requestStartTime, rejectedResponse, true);

                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(rejectedResponse);
            }

            // Add to queue
            metricsService.incrementQueueLength();

            try {
                FakePacketResponse response = fakePacketService.processFakePacket(request);
                log.info("Successfully processed packet {} in {}ms",
                    response.getPacketId(), response.getProcessingTimeMs());

                // Record success metrics
                recordMetrics(latencySample, requestStartTime, response, false);
                metricsService.incrementTotalJobsProcessed();

                return ResponseEntity.ok(response);
            } finally {
                // Remove from queue
                metricsService.decrementQueueLength();
            }

        } catch (Exception e) {
            log.error("Error processing packet {}", request.getPacketId(), e);
            metricsService.incrementTotalErrors("exception");

            FakePacketResponse errorResponse = FakePacketResponse.builder()
                    .packetId(request.getPacketId())
                    .status("FAILED")
                    .result("Error: " + e.getMessage())
                    .build();

            recordMetrics(latencySample, requestStartTime, errorResponse, true);

            return ResponseEntity.internalServerError().body(errorResponse);
        } finally {
            // Always decrement connection count
            metricsService.decrementCurrentConnections();
        }
    }

    private void recordMetrics(Timer.Sample latencySample, long requestStartTime,
                                FakePacketResponse response, boolean isError) {
        // Stop latency timer
        metricsService.stopRequestLatencyTimer(latencySample);

        // Record processing time if available
        if (response.getProcessingTimeMs() != null) {
            metricsService.recordProcessingTime(response.getProcessingTimeMs(), "fake_packet");
        }

        // Calculate and record response size
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            long responseSize = responseJson.getBytes().length;
            metricsService.recordResponseSize(responseSize, "json");
        } catch (JsonProcessingException e) {
            log.warn("Failed to calculate response size", e);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Server is running");
    }

    @PostMapping("/server/open")
    public ResponseEntity<ServerStateService.ServerStatus> openServer(
            @RequestParam(required = false) String reason) {
        log.info("Request to OPEN server. Reason: {}", reason);
        serverStateService.openServer(reason);
        return ResponseEntity.ok(serverStateService.getStatus());
    }

    @PostMapping("/server/close")
    public ResponseEntity<ServerStateService.ServerStatus> closeServer(
            @RequestParam(required = false) String reason) {
        log.warn("Request to CLOSE server. Reason: {}", reason);
        serverStateService.closeServer(reason);
        return ResponseEntity.ok(serverStateService.getStatus());
    }

    @GetMapping("/server/status")
    public ResponseEntity<ServerStateService.ServerStatus> getServerStatus() {
        return ResponseEntity.ok(serverStateService.getStatus());
    }

    @GetMapping("/metrics/summary")
    public ResponseEntity<String> getMetricsSummary() {
        String summary = metricsService.getMetricsSummary();
        log.info("Metrics summary requested: {}", summary);
        return ResponseEntity.ok(summary);
    }
}
