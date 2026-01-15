package com.CS445.CS4445_Sub_Server.controller;

import com.CS445.CS4445_Sub_Server.dto.ServerHealthResponse;
import com.CS445.CS4445_Sub_Server.service.MetricsService;
import com.CS445.CS4445_Sub_Server.service.ServerStateService;
import com.sun.management.OperatingSystemMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;

/**
 * Controller cho các endpoint /server/*
 * Endpoint này KHÔNG có prefix /api/v1 để khớp với Load-Balancer
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ServerHealthController {

    private final MetricsService metricsService;
    private final ServerStateService serverStateService;

    // Lưu processing time gần nhất để tính average
    private double avgProcessingTimeSec = 0.0;
    private long totalProcessedCount = 0;

    /**
     * GET /server/health
     * Response format khớp với Load-Balancer ServerHealthResponse:
     * {
     *   "cpuUsagePercent": 45.5,
     *   "memoryUsagePercent": 60.2,
     *   "avgProcessingTimeSec": 1.5,
     *   "currConnections": 10,
     *   "isOpen": true
     * }
     */
    @GetMapping("/server/health")
    public ResponseEntity<ServerHealthResponse> getServerHealth() {
        // Lấy CPU usage
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double cpuUsage = osBean.getCpuLoad() * 100; // Convert to percentage
        if (cpuUsage < 0) cpuUsage = 0; // CPU load có thể trả về -1 nếu không có sẵn

        // Lấy Memory usage
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsage = (double) usedMemory / maxMemory * 100;

        // Lấy current connections từ MetricsService
        int currConnections = metricsService.getCurrentConnections();

        // Lấy server open status
        boolean isOpen = serverStateService.isServerOpen();

        ServerHealthResponse response = ServerHealthResponse.builder()
                .cpuUsagePercent(Math.round(cpuUsage * 100.0) / 100.0) // Round to 2 decimal places
                .memoryUsagePercent(Math.round(memoryUsage * 100.0) / 100.0)
                .avgProcessingTimeSec(avgProcessingTimeSec)
                .currConnections(currConnections)
                .isOpen(isOpen)
                .build();

        log.debug("Health check response: CPU={}%, Memory={}%, Connections={}, Open={}",
                response.getCpuUsagePercent(),
                response.getMemoryUsagePercent(),
                response.getCurrConnections(),
                response.isOpen());

        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật average processing time (gọi từ FakePacketController sau khi xử lý xong)
     */
    public void updateProcessingTime(long processingTimeMs) {
        totalProcessedCount++;
        // Tính moving average
        avgProcessingTimeSec = avgProcessingTimeSec +
                (processingTimeMs / 1000.0 - avgProcessingTimeSec) / totalProcessedCount;
    }

    /**
     * POST /server/open
     * Mở server để nhận request (Load-Balancer gọi endpoint này)
     */
    @PostMapping("/server/open")
    public ResponseEntity<ServerStateService.ServerStatus> openServer(
            @RequestParam(required = false) String reason) {
        log.info("Request to OPEN server from Load-Balancer. Reason: {}", reason);
        serverStateService.openServer(reason);
        return ResponseEntity.ok(serverStateService.getStatus());
    }

    /**
     * POST /server/close
     * Đóng server, từ chối request mới (Load-Balancer gọi endpoint này)
     */
    @PostMapping("/server/close")
    public ResponseEntity<ServerStateService.ServerStatus> closeServer(
            @RequestParam(required = false) String reason) {
        log.warn("Request to CLOSE server from Load-Balancer. Reason: {}", reason);
        serverStateService.closeServer(reason);
        return ResponseEntity.ok(serverStateService.getStatus());
    }
}
