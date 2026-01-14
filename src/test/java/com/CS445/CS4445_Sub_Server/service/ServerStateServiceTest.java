package com.CS445.CS4445_Sub_Server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ServerStateService Unit Tests")
class ServerStateServiceTest {

    private ServerStateService serverStateService;

    @BeforeEach
    void setUp() {
        serverStateService = new ServerStateService();
    }

    @Test
    @DisplayName("Should initialize with server open")
    void shouldInitializeWithServerOpen() {
        // Act & Assert
        assertThat(serverStateService.isServerOpen()).isTrue();

        ServerStateService.ServerStatus status = serverStateService.getStatus();
        assertThat(status.isOpen()).isTrue();
        assertThat(status.getStatus()).isEqualTo("OPEN");
        assertThat(status.getReason()).isEqualTo("Server started");
        assertThat(status.getLastStateChange()).isNotNull();
    }

    @Test
    @DisplayName("Should close server successfully")
    void shouldCloseServerSuccessfully() {
        // Act
        serverStateService.closeServer("Maintenance");

        // Assert
        assertThat(serverStateService.isServerOpen()).isFalse();

        ServerStateService.ServerStatus status = serverStateService.getStatus();
        assertThat(status.isOpen()).isFalse();
        assertThat(status.getStatus()).isEqualTo("CLOSED");
        assertThat(status.getReason()).isEqualTo("Maintenance");
        assertThat(status.getLastStateChange()).isNotNull();
    }

    @Test
    @DisplayName("Should open server successfully")
    void shouldOpenServerSuccessfully() {
        // Arrange - First close the server
        serverStateService.closeServer("Testing");

        // Act
        serverStateService.openServer("Testing complete");

        // Assert
        assertThat(serverStateService.isServerOpen()).isTrue();

        ServerStateService.ServerStatus status = serverStateService.getStatus();
        assertThat(status.isOpen()).isTrue();
        assertThat(status.getStatus()).isEqualTo("OPEN");
        assertThat(status.getReason()).isEqualTo("Testing complete");
        assertThat(status.getLastStateChange()).isNotNull();
    }

    @Test
    @DisplayName("Should use default reason when closing without reason")
    void shouldUseDefaultReasonWhenClosingWithoutReason() {
        // Act
        serverStateService.closeServer(null);

        // Assert
        ServerStateService.ServerStatus status = serverStateService.getStatus();
        assertThat(status.getReason()).isEqualTo("Server closed");
    }

    @Test
    @DisplayName("Should use default reason when opening without reason")
    void shouldUseDefaultReasonWhenOpeningWithoutReason() {
        // Arrange
        serverStateService.closeServer("Test");

        // Act
        serverStateService.openServer(null);

        // Assert
        ServerStateService.ServerStatus status = serverStateService.getStatus();
        assertThat(status.getReason()).isEqualTo("Server opened");
    }

    @Test
    @DisplayName("Should not change state when closing already closed server")
    void shouldNotChangeStateWhenClosingAlreadyClosedServer() {
        // Arrange
        serverStateService.closeServer("First close");
        ServerStateService.ServerStatus firstStatus = serverStateService.getStatus();

        // Act - Try to close again
        serverStateService.closeServer("Second close");
        ServerStateService.ServerStatus secondStatus = serverStateService.getStatus();

        // Assert - State should remain the same
        assertThat(secondStatus.getLastStateChange()).isEqualTo(firstStatus.getLastStateChange());
        assertThat(secondStatus.getReason()).isEqualTo("First close");
    }

    @Test
    @DisplayName("Should not change state when opening already open server")
    void shouldNotChangeStateWhenOpeningAlreadyOpenServer() {
        // Arrange
        ServerStateService.ServerStatus initialStatus = serverStateService.getStatus();

        // Act - Try to open again
        serverStateService.openServer("Redundant open");
        ServerStateService.ServerStatus secondStatus = serverStateService.getStatus();

        // Assert - State should remain the same
        assertThat(secondStatus.getLastStateChange()).isEqualTo(initialStatus.getLastStateChange());
        assertThat(secondStatus.getReason()).isEqualTo("Server started");
    }

    @Test
    @DisplayName("Should track state changes correctly over multiple operations")
    void shouldTrackStateChangesCorrectly() {
        // Act & Assert - Initial state
        assertThat(serverStateService.isServerOpen()).isTrue();

        // Close
        serverStateService.closeServer("Reason 1");
        assertThat(serverStateService.isServerOpen()).isFalse();
        assertThat(serverStateService.getStatus().getReason()).isEqualTo("Reason 1");

        // Open
        serverStateService.openServer("Reason 2");
        assertThat(serverStateService.isServerOpen()).isTrue();
        assertThat(serverStateService.getStatus().getReason()).isEqualTo("Reason 2");

        // Close again
        serverStateService.closeServer("Reason 3");
        assertThat(serverStateService.isServerOpen()).isFalse();
        assertThat(serverStateService.getStatus().getReason()).isEqualTo("Reason 3");

        // Open again
        serverStateService.openServer("Reason 4");
        assertThat(serverStateService.isServerOpen()).isTrue();
        assertThat(serverStateService.getStatus().getReason()).isEqualTo("Reason 4");
    }

    @Test
    @DisplayName("Should handle concurrent state checks safely")
    void shouldHandleConcurrentStateChecksSafely() throws InterruptedException {
        // Arrange
        int numberOfThreads = 10;
        Thread[] threads = new Thread[numberOfThreads];

        // Act - Create threads that check state concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    boolean isOpen = serverStateService.isServerOpen();
                    ServerStateService.ServerStatus status = serverStateService.getStatus();
                    assertThat(status.isOpen()).isEqualTo(isOpen);
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Assert - Server should still be in consistent state
        assertThat(serverStateService.isServerOpen()).isTrue();
    }

    @Test
    @DisplayName("Should handle rapid state changes")
    void shouldHandleRapidStateChanges() {
        // Act - Rapidly toggle state
        for (int i = 0; i < 10; i++) {
            serverStateService.closeServer("Close " + i);
            assertThat(serverStateService.isServerOpen()).isFalse();

            serverStateService.openServer("Open " + i);
            assertThat(serverStateService.isServerOpen()).isTrue();
        }

        // Assert - Final state should be consistent
        ServerStateService.ServerStatus status = serverStateService.getStatus();
        assertThat(status.isOpen()).isTrue();
        assertThat(status.getStatus()).isEqualTo("OPEN");
        assertThat(status.getReason()).isEqualTo("Open 9");
    }

    @Test
    @DisplayName("Should update timestamp when state changes")
    void shouldUpdateTimestampWhenStateChanges() throws InterruptedException {
        // Arrange
        ServerStateService.ServerStatus initialStatus = serverStateService.getStatus();

        // Act
        Thread.sleep(10); // Small delay to ensure timestamp difference
        serverStateService.closeServer("Update timestamp test");
        ServerStateService.ServerStatus afterCloseStatus = serverStateService.getStatus();

        // Assert
        assertThat(afterCloseStatus.getLastStateChange())
                .isAfter(initialStatus.getLastStateChange());
    }

    @Test
    @DisplayName("Should handle empty string reason")
    void shouldHandleEmptyStringReason() {
        // Act
        serverStateService.closeServer("");

        // Assert
        ServerStateService.ServerStatus status = serverStateService.getStatus();
        assertThat(status.getReason()).isEmpty();
    }

    @Test
    @DisplayName("Should handle very long reason strings")
    void shouldHandleVeryLongReasonStrings() {
        // Arrange
        String longReason = "A".repeat(1000);

        // Act
        serverStateService.closeServer(longReason);

        // Assert
        ServerStateService.ServerStatus status = serverStateService.getStatus();
        assertThat(status.getReason()).isEqualTo(longReason);
        assertThat(status.getReason().length()).isEqualTo(1000);
    }
}
