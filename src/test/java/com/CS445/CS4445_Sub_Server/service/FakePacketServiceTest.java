package com.CS445.CS4445_Sub_Server.service;

import com.CS445.CS4445_Sub_Server.dto.FakePacketRequest;
import com.CS445.CS4445_Sub_Server.dto.FakePacketResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FakePacketService Unit Tests")
class FakePacketServiceTest {

    private FakePacketService fakePacketService;
    private FakePacketRequest testRequest;

    @BeforeEach
    void setUp() {
        fakePacketService = new FakePacketService();
        testRequest = FakePacketRequest.builder()
                .packetId("test-packet-001")
                .cpuIntensity(5)
                .ramIntensity(5)
                .processingTimeMs(100)
                .payload("test payload")
                .build();
    }

    @Test
    @DisplayName("Should process packet successfully with valid request")
    void shouldProcessPacketSuccessfully() {
        // Act
        FakePacketResponse response = fakePacketService.processFakePacket(testRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getPacketId()).isEqualTo("test-packet-001");
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getProcessingTimeMs()).isGreaterThanOrEqualTo(100L);
        assertThat(response.getCpuCycles()).isGreaterThan(0L);
        assertThat(response.getMemoryUsedBytes()).isGreaterThan(0L);
        assertThat(response.getResult()).contains("processed successfully");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should use default values when intensities are null")
    void shouldUseDefaultValuesWhenNull() {
        // Arrange
        FakePacketRequest requestWithNulls = FakePacketRequest.builder()
                .packetId("test-packet-002")
                .cpuIntensity(null)
                .ramIntensity(null)
                .processingTimeMs(null)
                .build();

        // Act
        FakePacketResponse response = fakePacketService.processFakePacket(requestWithNulls);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getProcessingTimeMs()).isGreaterThanOrEqualTo(1000L);
    }

    @Test
    @DisplayName("Should clamp CPU intensity to valid range (1-10)")
    void shouldClampCpuIntensityToValidRange() {
        // Test with intensity > 10
        FakePacketRequest highIntensityRequest = FakePacketRequest.builder()
                .packetId("test-packet-003")
                .cpuIntensity(15)
                .ramIntensity(5)
                .processingTimeMs(100)
                .build();

        // Act
        FakePacketResponse response = fakePacketService.processFakePacket(highIntensityRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");

        // Test with intensity < 1
        FakePacketRequest lowIntensityRequest = FakePacketRequest.builder()
                .packetId("test-packet-004")
                .cpuIntensity(0)
                .ramIntensity(5)
                .processingTimeMs(100)
                .build();

        // Act
        response = fakePacketService.processFakePacket(lowIntensityRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("Should clamp RAM intensity to valid range (1-10)")
    void shouldClampRamIntensityToValidRange() {
        // Test with intensity > 10
        FakePacketRequest highRamRequest = FakePacketRequest.builder()
                .packetId("test-packet-005")
                .cpuIntensity(5)
                .ramIntensity(20)
                .processingTimeMs(100)
                .build();

        // Act
        FakePacketResponse response = fakePacketService.processFakePacket(highRamRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");

        // Test with intensity < 1
        FakePacketRequest lowRamRequest = FakePacketRequest.builder()
                .packetId("test-packet-006")
                .cpuIntensity(5)
                .ramIntensity(-1)
                .processingTimeMs(100)
                .build();

        // Act
        response = fakePacketService.processFakePacket(lowRamRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("Should respect minimum processing time")
    void shouldRespectMinimumProcessingTime() {
        // Arrange
        FakePacketRequest quickRequest = FakePacketRequest.builder()
                .packetId("test-packet-007")
                .cpuIntensity(1)
                .ramIntensity(1)
                .processingTimeMs(500)
                .build();

        // Act
        long startTime = System.currentTimeMillis();
        FakePacketResponse response = fakePacketService.processFakePacket(quickRequest);
        long actualTime = System.currentTimeMillis() - startTime;

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getProcessingTimeMs()).isGreaterThanOrEqualTo(500L);
        assertThat(actualTime).isGreaterThanOrEqualTo(500L);
    }

    @Test
    @DisplayName("Should handle low intensity workload (intensity=1)")
    void shouldHandleLowIntensityWorkload() {
        // Arrange
        FakePacketRequest lowIntensityRequest = FakePacketRequest.builder()
                .packetId("test-packet-low")
                .cpuIntensity(1)
                .ramIntensity(1)
                .processingTimeMs(100)
                .build();

        // Act
        FakePacketResponse response = fakePacketService.processFakePacket(lowIntensityRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getCpuCycles()).isGreaterThan(0L);
        assertThat(response.getMemoryUsedBytes()).isGreaterThan(0L);
    }

    @Test
    @DisplayName("Should handle high intensity workload (intensity=10)")
    void shouldHandleHighIntensityWorkload() {
        // Arrange
        FakePacketRequest highIntensityRequest = FakePacketRequest.builder()
                .packetId("test-packet-high")
                .cpuIntensity(10)
                .ramIntensity(10)
                .processingTimeMs(100)
                .build();

        // Act
        FakePacketResponse response = fakePacketService.processFakePacket(highIntensityRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getCpuCycles()).isGreaterThan(0L);
        assertThat(response.getMemoryUsedBytes()).isGreaterThan(0L);
    }
}
