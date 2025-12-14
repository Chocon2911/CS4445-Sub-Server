package com.CS445.CS4445_Sub_Server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "packet_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacketLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String packetId;

    private Integer cpuIntensity;
    private Integer ramIntensity;
    private Long processingTimeMs;
    private Long cpuCycles;
    private Long memoryUsedBytes;

    @Column(length = 5000)
    private String payload;

    @Column(length = 5000)
    private String result;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
