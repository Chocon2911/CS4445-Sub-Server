package com.CS445.CS4445_Sub_Server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FakePacketResponse {
    private String packetId;
    private String status;
    private Long processingTimeMs;
    private Long cpuCycles;
    private Long memoryUsedBytes;
    private String result;
    private LocalDateTime timestamp;
}
