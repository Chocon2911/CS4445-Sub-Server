package com.CS445.CS4445_Sub_Server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for server health endpoint
 * Format khớp với Load-Balancer ServerHealthResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerHealthResponse {
    private double cpuUsagePercent;
    private double memoryUsagePercent;
    private double avgProcessingTimeSec;
    private int currConnections;
    private boolean isOpen;
}
