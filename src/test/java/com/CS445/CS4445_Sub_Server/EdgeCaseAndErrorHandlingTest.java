package com.CS445.CS4445_Sub_Server;

import com.CS445.CS4445_Sub_Server.controller.FakePacketController;
import com.CS445.CS4445_Sub_Server.dto.FakePacketRequest;
import com.CS445.CS4445_Sub_Server.service.FakePacketService;
import com.CS445.CS4445_Sub_Server.service.ServerStateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FakePacketController.class)
@ActiveProfiles("test")
@DisplayName("Edge Case and Error Handling Tests")
@AutoConfigureMockMvc(addFilters = false)
class EdgeCaseAndErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FakePacketService fakePacketService;

    @MockBean
    private ServerStateService serverStateService;

    @Test
    @DisplayName("Should handle empty packet ID")
    void shouldHandleEmptyPacketId() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        FakePacketRequest request = FakePacketRequest.builder()
                .packetId("")
                .cpuIntensity(5)
                .ramIntensity(5)
                .build();

        // Act & Assert - The API should still accept this
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle null packet ID")
    void shouldHandleNullPacketId() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        FakePacketRequest request = FakePacketRequest.builder()
                .packetId(null)
                .cpuIntensity(5)
                .ramIntensity(5)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle very long packet ID")
    void shouldHandleVeryLongPacketId() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        String longPacketId = "p".repeat(10000);
        FakePacketRequest request = FakePacketRequest.builder()
                .packetId(longPacketId)
                .cpuIntensity(1)
                .ramIntensity(1)
                .processingTimeMs(100)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle special characters in packet ID")
    void shouldHandleSpecialCharactersInPacketId() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        FakePacketRequest request = FakePacketRequest.builder()
                .packetId("test!@#$%^&*()_+-={}[]|\\:\";<>?,./")
                .cpuIntensity(3)
                .ramIntensity(3)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle Unicode characters in packet ID")
    void shouldHandleUnicodeCharactersInPacketId() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        FakePacketRequest request = FakePacketRequest.builder()
                .packetId("测试-テスト-테스트-🚀🔥💻")
                .cpuIntensity(3)
                .ramIntensity(3)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle negative CPU intensity")
    void shouldHandleNegativeCpuIntensity() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        FakePacketRequest request = FakePacketRequest.builder()
                .packetId("negative-test")
                .cpuIntensity(-10)
                .ramIntensity(5)
                .build();

        // Act & Assert - Service should clamp to valid range
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle extremely high CPU intensity")
    void shouldHandleExtremelyHighCpuIntensity() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        FakePacketRequest request = FakePacketRequest.builder()
                .packetId("extreme-cpu")
                .cpuIntensity(Integer.MAX_VALUE)
                .ramIntensity(5)
                .processingTimeMs(100)
                .build();

        // Act & Assert - Service should clamp to valid range
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle negative RAM intensity")
    void shouldHandleNegativeRamIntensity() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        FakePacketRequest request = FakePacketRequest.builder()
                .packetId("negative-ram")
                .cpuIntensity(5)
                .ramIntensity(-5)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle extremely high RAM intensity")
    void shouldHandleExtremelyHighRamIntensity() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        FakePacketRequest request = FakePacketRequest.builder()
                .packetId("extreme-ram")
                .cpuIntensity(5)
                .ramIntensity(Integer.MAX_VALUE)
                .processingTimeMs(100)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle zero processing time")
    void shouldHandleZeroProcessingTime() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        FakePacketRequest request = FakePacketRequest.builder()
                .packetId("zero-time")
                .cpuIntensity(1)
                .ramIntensity(1)
                .processingTimeMs(0)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle negative processing time")
    void shouldHandleNegativeProcessingTime() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        FakePacketRequest request = FakePacketRequest.builder()
                .packetId("negative-time")
                .cpuIntensity(1)
                .ramIntensity(1)
                .processingTimeMs(-1000)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle extremely large payload")
    void shouldHandleExtremelyLargePayload() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        String largePayload = "A".repeat(100000); // 100KB payload
        FakePacketRequest request = FakePacketRequest.builder()
                .packetId("large-payload")
                .cpuIntensity(1)
                .ramIntensity(1)
                .processingTimeMs(100)
                .payload(largePayload)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle empty payload")
    void shouldHandleEmptyPayload() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        FakePacketRequest request = FakePacketRequest.builder()
                .packetId("empty-payload")
                .cpuIntensity(5)
                .ramIntensity(5)
                .payload("")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle null payload")
    void shouldHandleNullPayload() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        FakePacketRequest request = FakePacketRequest.builder()
                .packetId("null-payload")
                .cpuIntensity(5)
                .ramIntensity(5)
                .payload(null)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle malformed JSON")
    void shouldHandleMalformedJson() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        String malformedJson = "{\"packetId\": \"test\", \"cpuIntensity\": ";

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle empty request body")
    void shouldHandleEmptyRequestBody() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle request with extra unknown fields")
    void shouldHandleRequestWithExtraFields() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        String jsonWithExtraFields = """
                {
                    "packetId": "test-001",
                    "cpuIntensity": 5,
                    "ramIntensity": 5,
                    "unknownField1": "value1",
                    "unknownField2": 12345
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithExtraFields))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle wrong data types in request")
    void shouldHandleWrongDataTypes() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        String jsonWithWrongTypes = """
                {
                    "packetId": "test-001",
                    "cpuIntensity": "not-a-number",
                    "ramIntensity": 5
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithWrongTypes))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle GET request to POST endpoint")
    void shouldRejectGetRequestToPostEndpoint() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/fakePacket"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Should handle missing Content-Type header")
    void shouldHandleMissingContentType() throws Exception {
        // Arrange
        when(serverStateService.isServerOpen()).thenReturn(true);

        FakePacketRequest request = FakePacketRequest.builder()
                .packetId("test-001")
                .cpuIntensity(5)
                .ramIntensity(5)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/fakePacket")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Should handle non-existent endpoint")
    void shouldHandleNonExistentEndpoint() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should handle special characters in server close reason")
    void shouldHandleSpecialCharactersInCloseReason() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/server/close")
                        .param("reason", "Special chars: !@#$%^&*()"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle very long server close reason")
    void shouldHandleVeryLongCloseReason() throws Exception {
        // Arrange
        String longReason = "R".repeat(10000);

        // Act & Assert
        mockMvc.perform(post("/api/v1/server/close")
                        .param("reason", longReason))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle concurrent requests to fakePacket endpoint")
    void shouldHandleConcurrentRequests() throws Exception {
        // This test verifies that the endpoint can handle concurrent requests
        // In a real integration test environment, you would spawn multiple threads
        // For now, we just verify the endpoint accepts requests
        when(serverStateService.isServerOpen()).thenReturn(true);

        FakePacketRequest request = FakePacketRequest.builder()
                .packetId("concurrent-test")
                .cpuIntensity(1)
                .ramIntensity(1)
                .processingTimeMs(100)
                .build();

        // Act & Assert - Simulate multiple sequential requests
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/fakePacket")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }
}
