package com.CS445.CS4445_Sub_Server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class ServerStateService {

    private final AtomicBoolean serverOpen = new AtomicBoolean(true);
    private LocalDateTime lastStateChange = LocalDateTime.now();
    private String lastStateChangeReason = "Server started";

    public boolean isServerOpen() {
        return serverOpen.get();
    }

    public void openServer(String reason) {
        if (!serverOpen.get()) {
            serverOpen.set(true);
            lastStateChange = LocalDateTime.now();
            lastStateChangeReason = reason != null ? reason : "Server opened";
            log.info("Server OPENED at {}: {}", lastStateChange, lastStateChangeReason);
        } else {
            log.warn("Attempt to open server, but it's already open");
        }
    }

    public void closeServer(String reason) {
        if (serverOpen.get()) {
            serverOpen.set(false);
            lastStateChange = LocalDateTime.now();
            lastStateChangeReason = reason != null ? reason : "Server closed";
            log.warn("Server CLOSED at {}: {}", lastStateChange, lastStateChangeReason);
        } else {
            log.warn("Attempt to close server, but it's already closed");
        }
    }

    public ServerStatus getStatus() {
        return ServerStatus.builder()
                .isOpen(serverOpen.get())
                .status(serverOpen.get() ? "OPEN" : "CLOSED")
                .lastStateChange(lastStateChange)
                .reason(lastStateChangeReason)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class ServerStatus {
        private boolean isOpen;
        private String status;
        private LocalDateTime lastStateChange;
        private String reason;
    }
}
