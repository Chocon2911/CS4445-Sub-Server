package com.CS445.CS4445_Sub_Server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FakePacketRequest {
    private String packetId;
    private Integer cpuIntensity; // 1-10 scale for CPU load
    private Integer ramIntensity; // 1-10 scale for RAM load (MB)
    private Integer processingTimeMs; // Minimum processing time in milliseconds
    private String payload;
}
