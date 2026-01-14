package com.CS445.CS4445_Sub_Server.controller;

import com.CS445.CS4445_Sub_Server.dto.FakePacketRequest;
import com.CS445.CS4445_Sub_Server.dto.FakePacketResponse;
import com.CS445.CS4445_Sub_Server.service.FakePacketService;
import com.CS445.CS4445_Sub_Server.service.MetricsService;
import com.CS445.CS4445_Sub_Server.service.ServerStateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FakePacketController.class)
@ActiveProfiles("test")
@DisplayName("FakePacketController Integration Tests")
@AutoConfigureMockMvc(addFilters = false)
class FakePacketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FakePacketService fakePacketService;

    @MockBean
    private ServerStateService serverStateService;

    @MockBean
    private MetricsService metricsService;

    private FakePacketRequest testRequest;
    private FakePacketResponse testResponse;

    @BeforeEach
    void setUp() {
        testRequest = FakePacketRequest.builder()
                .packetId("test-packet-001")
                .cpuIntensity(5)
                .ramIntensity(5)
                .processingTimeMs(1000)
                .payload("test payload")
                .build();

        testResponse = FakePacketResponse.builder()
                .packetId("test-packet-001")
                .status("SUCCESS")
                .processingTimeMs(1500L)
                .cpuCycles(150000L)
                .memoryUsedBytes(52428800L)
                .result("Packet processed successfully")
                .timestamp(LocalDateTime.now())
                .build();

        // Default: server is open
        when(serverStateService.isServerOpen()).thenReturn(true);
    }

    @Test
    @DisplayName("POST /api/v1/fakePacket - Should process packet successfully when server is open")
    void shouldProcessPacketSuccessfully() throws Exception {
        // Arrange
        when(fakePacketService.processFakePacket(any(FakePacketRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packetId").value("test-packet-001"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.processingTimeMs").value(1500))
                .andExpect(jsonPath("$.cpuCycles").value(150000))
                .andExpect(jsonPath("$.memoryUsedBytes").value(52428800))
                .andExpect(jsonPath("$.result").value("Packet processed successfully"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(fakePacketService, times(1)).processFakePacket(any(FakePacketRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/fakePacket - Should reject packet when server is closed")
    void shouldRejectPacketWhenServerClosed() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.packetId").value("test-packet-001"))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.result").value(containsString("Server is currently closed")));

        verify(fakePacketService, never()).processFakePacket(any(FakePacketRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/fakePacket - Should handle service exceptions")
    void shouldHandleServiceExceptions() throws Exception {
        // Arrange
        when(fakePacketService.processFakePacket(any(FakePacketRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.packetId").value("test-packet-001"))
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.result").value(containsString("Error: Database connection failed")));
    }

    @Test
    @DisplayName("POST /api/v1/fakePacket - Should accept minimal request")
    void shouldAcceptMinimalRequest() throws Exception {
        // Arrange
        FakePacketRequest minimalRequest = FakePacketRequest.builder()
                .packetId("minimal-001")
                .build();

        FakePacketResponse minimalResponse = FakePacketResponse.builder()
                .packetId("minimal-001")
                .status("SUCCESS")
                .processingTimeMs(1000L)
                .cpuCycles(50000L)
                .memoryUsedBytes(10000000L)
                .result("Processed")
                .timestamp(LocalDateTime.now())
                .build();

        when(fakePacketService.processFakePacket(any(FakePacketRequest.class))).thenReturn(minimalResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("GET /api/v1/health - Should return health status")
    void shouldReturnHealthStatus() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Server is running"));
    }

    @Test
    @DisplayName("POST /api/v1/server/close - Should close server with reason")
    void shouldCloseServerWithReason() throws Exception {
        // Arrange
        ServerStateService.ServerStatus closedStatus = ServerStateService.ServerStatus.builder()
                .isOpen(false)
                .status("CLOSED")
                .lastStateChange(LocalDateTime.now())
                .reason("Maintenance")
                .build();

        doNothing().when(serverStateService).closeServer("Maintenance");
        when(serverStateService.getStatus()).thenReturn(closedStatus);

        // Act & Assert
        mockMvc.perform(post("/api/v1/server/close")
                        .param("reason", "Maintenance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open").value(false))
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.reason").value("Maintenance"))
                .andExpect(jsonPath("$.lastStateChange").exists());

        verify(serverStateService).closeServer("Maintenance");
        verify(serverStateService).getStatus();
    }

    @Test
    @DisplayName("POST /api/v1/server/close - Should close server without reason")
    void shouldCloseServerWithoutReason() throws Exception {
        // Arrange
        ServerStateService.ServerStatus closedStatus = ServerStateService.ServerStatus.builder()
                .isOpen(false)
                .status("CLOSED")
                .lastStateChange(LocalDateTime.now())
                .reason("Server closed")
                .build();

        doNothing().when(serverStateService).closeServer(null);
        when(serverStateService.getStatus()).thenReturn(closedStatus);

        // Act & Assert
        mockMvc.perform(post("/api/v1/server/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open").value(false))
                .andExpect(jsonPath("$.status").value("CLOSED"));

        verify(serverStateService).closeServer(null);
    }

    @Test
    @DisplayName("POST /api/v1/server/open - Should open server with reason")
    void shouldOpenServerWithReason() throws Exception {
        // Arrange
        ServerStateService.ServerStatus openStatus = ServerStateService.ServerStatus.builder()
                .isOpen(true)
                .status("OPEN")
                .lastStateChange(LocalDateTime.now())
                .reason("Maintenance complete")
                .build();

        doNothing().when(serverStateService).openServer("Maintenance complete");
        when(serverStateService.getStatus()).thenReturn(openStatus);

        // Act & Assert
        mockMvc.perform(post("/api/v1/server/open")
                        .param("reason", "Maintenance complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open").value(true))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.reason").value("Maintenance complete"))
                .andExpect(jsonPath("$.lastStateChange").exists());

        verify(serverStateService).openServer("Maintenance complete");
        verify(serverStateService).getStatus();
    }

    @Test
    @DisplayName("POST /api/v1/server/open - Should open server without reason")
    void shouldOpenServerWithoutReason() throws Exception {
        // Arrange
        ServerStateService.ServerStatus openStatus = ServerStateService.ServerStatus.builder()
                .isOpen(true)
                .status("OPEN")
                .lastStateChange(LocalDateTime.now())
                .reason("Server opened")
                .build();

        doNothing().when(serverStateService).openServer(null);
        when(serverStateService.getStatus()).thenReturn(openStatus);

        // Act & Assert
        mockMvc.perform(post("/api/v1/server/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open").value(true))
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(serverStateService).openServer(null);
    }

    @Test
    @DisplayName("GET /api/v1/server/status - Should return current server status when open")
    void shouldReturnServerStatusWhenOpen() throws Exception {
        // Arrange
        ServerStateService.ServerStatus status = ServerStateService.ServerStatus.builder()
                .isOpen(true)
                .status("OPEN")
                .lastStateChange(LocalDateTime.now())
                .reason("Server started")
                .build();

        when(serverStateService.getStatus()).thenReturn(status);

        // Act & Assert
        mockMvc.perform(get("/api/v1/server/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open").value(true))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.reason").value("Server started"))
                .andExpect(jsonPath("$.lastStateChange").exists());
    }

    @Test
    @DisplayName("GET /api/v1/server/status - Should return current server status when closed")
    void shouldReturnServerStatusWhenClosed() throws Exception {
        // Arrange
        ServerStateService.ServerStatus status = ServerStateService.ServerStatus.builder()
                .isOpen(false)
                .status("CLOSED")
                .lastStateChange(LocalDateTime.now())
                .reason("Emergency maintenance")
                .build();

        when(serverStateService.getStatus()).thenReturn(status);

        // Act & Assert
        mockMvc.perform(get("/api/v1/server/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open").value(false))
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.reason").value("Emergency maintenance"))
                .andExpect(jsonPath("$.lastStateChange").exists());
    }

    @Test
    @DisplayName("Should handle complete server lifecycle")
    void shouldHandleCompleteServerLifecycle() throws Exception {
        // 1. Check initial status (open)
        ServerStateService.ServerStatus openStatus = ServerStateService.ServerStatus.builder()
                .isOpen(true)
                .status("OPEN")
                .lastStateChange(LocalDateTime.now())
                .reason("Server started")
                .build();
        when(serverStateService.getStatus()).thenReturn(openStatus);

        mockMvc.perform(get("/api/v1/server/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open").value(true));

        // 2. Process packet successfully
        when(fakePacketService.processFakePacket(any(FakePacketRequest.class))).thenReturn(testResponse);

        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk());

        // 3. Close server
        ServerStateService.ServerStatus closedStatus = ServerStateService.ServerStatus.builder()
                .isOpen(false)
                .status("CLOSED")
                .lastStateChange(LocalDateTime.now())
                .reason("Maintenance")
                .build();
        when(serverStateService.getStatus()).thenReturn(closedStatus);
        when(serverStateService.isServerOpen()).thenReturn(false);

        mockMvc.perform(post("/api/v1/server/close")
                        .param("reason", "Maintenance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open").value(false));

        // 4. Try to process packet (should be rejected)
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        // 5. Reopen server
        when(serverStateService.getStatus()).thenReturn(openStatus);
        when(serverStateService.isServerOpen()).thenReturn(true);

        mockMvc.perform(post("/api/v1/server/open")
                        .param("reason", "Maintenance complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open").value(true));

        // 6. Process packet again (should succeed)
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}
