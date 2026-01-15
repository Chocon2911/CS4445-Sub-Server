package com.CS445.CS4445_Sub_Server.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.CS445.CS4445_Sub_Server.dto.FakePacketRequest;
import com.CS445.CS4445_Sub_Server.dto.FakePacketResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FakePacketService {

    public FakePacketResponse processFakePacket(FakePacketRequest request) {
        long startTime = System.currentTimeMillis();

        // Handle null/empty packet ID
        String packetId = (request.getPacketId() == null || request.getPacketId().trim().isEmpty())
            ? "packet-" + UUID.randomUUID().toString()
            : request.getPacketId();

        // Default values if not provided
        int cpuIntensity = request.getCpuIntensity() != null ? request.getCpuIntensity() : 5;
        int ramIntensity = request.getRamIntensity() != null ? request.getRamIntensity() : 5;
        int minProcessingTime = request.getProcessingTimeMs() != null ? request.getProcessingTimeMs() : 1000;

        // Clamp values to valid ranges
        cpuIntensity = Math.max(1, Math.min(10, cpuIntensity));
        ramIntensity = Math.max(1, Math.min(10, ramIntensity));

        log.info("Processing packet {} with CPU intensity: {}, RAM intensity: {}",
                packetId, cpuIntensity, ramIntensity);

        try {
            // CPU-intensive operations
            long cpuCycles = performCpuIntensiveWork(cpuIntensity);

            // RAM-intensive operations
            long memoryUsed = performRamIntensiveWork(ramIntensity);

            // Ensure minimum processing time
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - startTime;
            if (elapsed < minProcessingTime) {
                try {
                    Thread.sleep(minProcessingTime - elapsed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Processing interrupted", e);
                }
            }

            long totalProcessingTime = System.currentTimeMillis() - startTime;

            String result = String.format("Packet %s processed successfully. CPU cycles: %d, Memory used: %d bytes",
                    packetId, cpuCycles, memoryUsed);

            return FakePacketResponse.builder()
                    .packetId(packetId)
                    .status("SUCCESS")
                    .processingTimeMs(totalProcessingTime)
                    .cpuCycles(cpuCycles)
                    .memoryUsedBytes(memoryUsed)
                    .result(result)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error processing packet {}", packetId, e);
            throw new RuntimeException("Failed to process packet: " + packetId, e);
        }
    }

    private long performCpuIntensiveWork(int intensity) {
        long cycles = 0;
        int iterations = intensity * 10000;  // Reduced from 100000 to 10000 (10x reduction)

        // Prime number calculation (CPU intensive)
        List<Long> primes = new ArrayList<>();
        for (long i = 2; i < iterations; i++) {
            if (isPrime(i)) {
                primes.add(i);
                cycles++;
            }
        }

        // Hash computation (CPU intensive)
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (int i = 0; i < intensity * 100; i++) {  // Reduced from 1000 to 100 (10x reduction)
                byte[] hash = digest.digest(UUID.randomUUID().toString().getBytes());
                cycles += hash.length;
            }
        } catch (NoSuchAlgorithmException e) {
            log.error("Hash algorithm not found", e);
            throw new RuntimeException("Hash algorithm not available", e);
        }

        // Complex mathematical operations
        double result = 0;
        for (int i = 0; i < intensity * 5000; i++) {  // Reduced from 50000 to 5000 (10x reduction)
            result += Math.sqrt(i) * Math.log(i + 1) / Math.cos(i * 0.1);
            cycles++;
        }

        // String manipulation (CPU intensive)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < intensity * 1000; i++) {  // Reduced from 10000 to 1000 (10x reduction)
            sb.append(UUID.randomUUID().toString());
            if (sb.length() > 100000) {
                sb = new StringBuilder(sb.substring(sb.length() - 1000));
            }
            cycles++;
        }

        log.debug("CPU work completed: {} cycles, {} primes found", cycles, primes.size());
        return cycles;
    }

    private long performRamIntensiveWork(int intensity) {
        long totalMemory = 0;

        int arraySize = intensity * 10000;  // Reduced from 100000 to 10000 (10x reduction)

        // Large ArrayList
        List<String> largeList = new ArrayList<>(arraySize);
        for (int i = 0; i < arraySize; i++) {
            largeList.add(UUID.randomUUID().toString() + "-" + i);
        }
        totalMemory += largeList.size() * 40L;

        // Large HashMap
        Map<String, Object> largeMap = new HashMap<>();
        for (int i = 0; i < intensity * 1000; i++) {  // Reduced from 10000 to 1000 (10x reduction)
            Map<String, String> nestedMap = new HashMap<>();
            nestedMap.put("key1", UUID.randomUUID().toString());
            nestedMap.put("key2", UUID.randomUUID().toString());
            nestedMap.put("key3", UUID.randomUUID().toString());
            largeMap.put("entry-" + i, nestedMap);
        }
        totalMemory += largeMap.size() * 200L;

        // Byte arrays
        List<byte[]> byteArrays = new ArrayList<>();
        for (int i = 0; i < intensity * 10; i++) {  // Reduced from 100 to 10 (10x reduction)
            byte[] arr = new byte[10000];
            new Random().nextBytes(arr);
            byteArrays.add(arr);
            totalMemory += arr.length;
        }

        // ConcurrentHashMap with complex objects
        Map<String, List<Map<String, Object>>> complexMap = new ConcurrentHashMap<>();
        for (int i = 0; i < intensity * 100; i++) {  // Reduced from 1000 to 100 (10x reduction)
            List<Map<String, Object>> list = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                Map<String, Object> obj = new HashMap<>();
                obj.put("id", UUID.randomUUID().toString());
                obj.put("timestamp", System.currentTimeMillis());
                obj.put("data", new byte[1000]);
                obj.put("index", i * 10 + j);
                list.add(obj);
            }
            complexMap.put("key-" + i, list);
        }
        totalMemory += complexMap.size() * 12000L;

        // Process the data to prevent optimization
        long sum = largeList.stream()
                .mapToLong(String::hashCode)
                .sum();
        sum += largeMap.values().stream()
                .mapToLong(Object::hashCode)
                .sum();
        sum += byteArrays.stream()
                .mapToLong(arr -> arr[0])
                .sum();

        log.debug("RAM work completed: {} bytes allocated, checksum: {}", totalMemory, sum);
        return totalMemory;
    }

    private boolean isPrime(long n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;

        for (long i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }
}
