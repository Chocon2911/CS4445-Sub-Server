package com.CS445.CS4445_Sub_Server.controller;

import com.CS445.CS4445_Sub_Server.dto.FakePacketRequest;
import com.CS445.CS4445_Sub_Server.dto.FakePacketResponse;
import com.CS445.CS4445_Sub_Server.service.FakePacketService;
import com.CS445.CS4445_Sub_Server.service.ServerStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class FakePacketController {

    private final FakePacketService fakePacketService;
    private final ServerStateService serverStateService;

    @PostMapping("/fakePacket")
    public ResponseEntity<FakePacketResponse> processFakePacket(@RequestBody FakePacketRequest request) {
        log.info("Received fakePacket request: {}", request.getPacketId());

        // Check if server is open
        if (!serverStateService.isServerOpen()) {
            log.warn("Server is CLOSED. Rejecting packet request: {}", request.getPacketId());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(FakePacketResponse.builder()
                            .packetId(request.getPacketId())
                            .status("REJECTED")
                            .result("Server is currently closed. Please open the server first using /api/v1/server/open")
                            .build());
        }

        try {
            FakePacketResponse response = fakePacketService.processFakePacket(request);
            log.info("Successfully processed packet {} in {}ms",
                    response.getPacketId(), response.getProcessingTimeMs());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing packet {}", request.getPacketId(), e);
            return ResponseEntity.internalServerError()
                    .body(FakePacketResponse.builder()
                            .packetId(request.getPacketId())
                            .status("FAILED")
                            .result("Error: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Server is running");
    }

    @PostMapping("/server/open")
    public ResponseEntity<ServerStateService.ServerStatus> openServer(
            @RequestParam(required = false) String reason) {
        log.info("Request to OPEN server. Reason: {}", reason);
        serverStateService.openServer(reason);
        return ResponseEntity.ok(serverStateService.getStatus());
    }

    @PostMapping("/server/close")
    public ResponseEntity<ServerStateService.ServerStatus> closeServer(
            @RequestParam(required = false) String reason) {
        log.warn("Request to CLOSE server. Reason: {}", reason);
        serverStateService.closeServer(reason);
        return ResponseEntity.ok(serverStateService.getStatus());
    }

    @GetMapping("/server/status")
    public ResponseEntity<ServerStateService.ServerStatus> getServerStatus() {
        return ResponseEntity.ok(serverStateService.getStatus());
    }
}
