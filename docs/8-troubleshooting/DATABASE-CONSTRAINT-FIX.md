# Database Constraint Violation Fix - "Value Too Long" Error

**Date**: December 21, 2025  
**Issue**: `DataIntegrityViolationException: value too long for type character varying(5000)`  
**Status**: ✅ **RESOLVED**

---

## Table of Contents
1. [Problem Summary](#problem-summary)
2. [Root Cause Analysis](#root-cause-analysis)
3. [Solution Implementation](#solution-implementation)
4. [Docker Build Issues](#docker-build-issues)
5. [Testing & Verification](#testing--verification)
6. [Lessons Learned](#lessons-learned)

---

## Problem Summary

### Initial Error
```
org.springframework.dao.DataIntegrityViolationException: 
could not execute statement [ERROR: value too long for type character varying(5000)]
```

### Affected Tables
- `packet_logs` table:
  - `payload` VARCHAR(5000)
  - `result` VARCHAR(5000)
  
- `packet_error_logs` table:
  - `payload` VARCHAR(5000)
  - `additional_context` VARCHAR(5000)
  - `endpoint` VARCHAR(2000)
  - `request_method` VARCHAR(1000)
  - `error_message` VARCHAR(255)

### Symptoms
- 500 Internal Server Error when processing packets with large payloads (>5000 characters)
- Database insert failures
- Application crashes under load

---

## Root Cause Analysis

### Primary Issue
The application was attempting to save strings longer than the database column constraints without truncation.

### Specific Locations

#### 1. FakePacketService.java
```java
// Line 220-221: Missing truncation
PacketLog log = PacketLog.builder()
    .payload(payload)  // ❌ No truncation - could be > 5000 chars
    .result("Processed successfully")  // ✅ OK - short string
    .build();
```

#### 2. PacketErrorLogService.java
```java
// Multiple fields without truncation
PacketErrorLog errorLog = PacketErrorLog.builder()
    .errorMessage(e.getMessage())  // ❌ Could be > 255 chars
    .payload(request.getPayload())  // ❌ Could be > 5000 chars
    .endpoint(endpoint)  // ❌ Could be > 2000 chars
    .additionalContext(buildAdditionalContext(request))  // ❌ Could be > 5000 chars
    .build();
```

### Secondary Issue: Incorrect Truncation Logic
Initial implementation had a bug in the truncation calculation:

```java
// ❌ WRONG: Results in strings > maxLength
return value.substring(0, maxLength - 15) + "\n... (truncated)";
// If maxLength = 5000:
// substring(0, 4985) = 4985 chars
// + "\n... (truncated)" = 17 chars
// Total = 5002 chars > 5000 ❌
```

---

## Solution Implementation

### Step 1: Create Truncation Helper Method

Added to both `FakePacketService.java` and `PacketErrorLogService.java`:

```java
/**
 * Helper method to truncate strings to prevent database constraint violations
 * @param value The string to truncate
 * @param maxLength Maximum allowed length
 * @return Truncated string with suffix, or original if within limit
 */
private String truncateString(String value, int maxLength) {
    if (value == null) {
        return null;
    }
    if (value.length() <= maxLength) {
        return value;
    }
    String suffix = "\n... (truncated)";
    return value.substring(0, maxLength - suffix.length()) + suffix;
}
```

**Key Points:**
- ✅ Handles null values
- ✅ Returns original if within limit
- ✅ Uses `suffix.length()` for accurate calculation
- ✅ Ensures final string length ≤ maxLength

### Step 2: Apply Truncation in FakePacketService

**File**: `src/main/java/com/CS445/CS4445_Sub_Server/service/FakePacketService.java`

```java
private String performDatabaseOperations(String packetId, String payload, 
                                        long cpuCycles, long memoryUsed, 
                                        boolean errorOccurred, int cpuIntensity, 
                                        int ramIntensity) {
    try {
        // Save packet log to database
        PacketLog packetLog = PacketLog.builder()
                .packetId(packetId)
                .cpuIntensity(cpuIntensity)
                .ramIntensity(ramIntensity)
                .processingTimeMs(0L)
                .cpuCycles(cpuCycles)
                .memoryUsedBytes(memoryUsed)
                .errorOccurred(errorOccurred)
                .payload(truncateString(payload, 5000))  // ✅ Truncated
                .result(truncateString("Processed successfully", 5000))  // ✅ Truncated
                .build();

        packetLogRepository.save(packetLog);
        // ... rest of method
    } catch (Exception e) {
        log.error("Failed to save packet log", e);
        throw e;
    }
}
```

### Step 3: Apply Truncation in PacketErrorLogService

**File**: `src/main/java/com/CS445/CS4445_Sub_Server/service/PacketErrorLogService.java`

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public PacketErrorLog logError(Exception e, FakePacketRequest request, 
                               String endpoint, String method, 
                               Integer httpStatusCode) {
    try {
        PacketErrorLog errorLog = PacketErrorLog.builder()
                .errorType(e.getClass().getSimpleName())
                .errorMessage(truncateString(
                    e.getMessage() != null ? e.getMessage() : "No error message", 
                    255))  // ✅ Truncated to 255
                .stackTrace(getStackTraceAsString(e))
                .packetId(request != null ? request.getPacketId() : null)
                .cpuIntensity(request != null ? request.getCpuIntensity() : null)
                .ramIntensity(request != null ? request.getRamIntensity() : null)
                .payload(truncateString(
                    request != null ? request.getPayload() : null, 
                    5000))  // ✅ Truncated to 5000
                .endpoint(truncateString(endpoint, 2000))  // ✅ Truncated to 2000
                .requestMethod(truncateString(method, 1000))  // ✅ Truncated to 1000
                .httpStatusCode(httpStatusCode)
                .additionalContext(truncateString(
                    buildAdditionalContext(request), 
                    5000))  // ✅ Truncated to 5000
                .build();

        PacketErrorLog saved = packetErrorLogRepository.save(errorLog);
        log.info("Error logged to database with ID: {}", saved.getId());
        return saved;
    } catch (Exception dbException) {
        log.error("Failed to save error log to database", dbException);
        log.error("Original error was: ", e);
        return null;
    }
}
```

---

## Docker Build Issues

### Challenge: Docker Cache Problems

During implementation, we encountered significant issues with Docker caching old code even after rebuilding.

#### Symptoms
- Code changes not reflected in running container
- JAR file contained old compiled classes
- `docker-compose build` used cached layers

#### Investigation Process

1. **Verified source code had changes**:
   ```bash
   grep "truncateString" src/main/java/.../FakePacketService.java
   # Found: 3 matching lines ✅
   ```

2. **Checked JAR in container**:
   ```bash
   docker exec cs4445-app cat /app/app.jar | grep "truncateString"
   # Not found ❌
   ```

3. **Examined build logs**:
   ```
   #11 [build 8/9] COPY src ./src
   #11 CACHED  ❌
   #12 [build 9/9] RUN ./mvnw clean package -DskipTests
   #12 CACHED  ❌
   ```

#### Solutions Attempted

1. **❌ Adding comments to Dockerfile** - Docker still used cache
2. **❌ Touching source files** - Docker checksum unchanged
3. **❌ `docker builder prune`** - Partial success
4. **✅ `docker-compose build --no-cache`** - Full rebuild, worked!

#### Final Build Process

```bash
# 1. Build with --no-cache to force full rebuild
docker-compose -f docker-compose.dev.yml build --no-cache app

# 2. Stop and remove old container
docker-compose -f docker-compose.dev.yml stop app
docker rm cs4445-app

# 3. Start new container
docker-compose -f docker-compose.dev.yml up -d app

# 4. Wait for startup
sleep 20

# 5. Verify new code is running
docker logs cs4445-app --tail 50
```

### Dockerfile Configuration

**File**: `Dockerfile`

```dockerfile
# Multi-stage Dockerfile for CS4445 Sub Server

# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven files first (for layer caching)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies (cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Set JVM options for container environment
ENV JAVA_OPTS="-Xmx2g -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Docker Compose Configuration

**File**: `docker-compose.dev.yml`

```yaml
version: '3.8'

services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    image: cs4445-sub-server-app:latest
    container_name: cs4445-app
    restart: unless-stopped
    ports:
      - "${APP_PORT:-8080}:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/${POSTGRES_DB:-mydatabase}
      - SPRING_DATASOURCE_USERNAME=${POSTGRES_USER:-myuser}
      - SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD:-secret}
      - SPRING_JPA_HIBERNATE_DDL_AUTO=${DDL_AUTO:-update}
      - JAVA_OPTS=-Xmx${MAX_HEAP:-2g} -Xms${MIN_HEAP:-512m}
    depends_on:
      postgres:
        condition: service_healthy
```

---

## Testing & Verification

### Test Cases

#### Test 1: Normal Payload (< 5000 chars)
```bash
# PowerShell
$body = @{
    packetId='test-001'
    cpuIntensity=1
    ramIntensity=1
    payload='test'
} | ConvertTo-Json

Invoke-WebRequest -Uri 'http://localhost:8080/api/v1/fakePacket' `
    -Method POST `
    -ContentType 'application/json' `
    -Body $body `
    -UseBasicParsing
```

**Result**: ✅ Status 200 OK

#### Test 2: Large Payload (6000 chars, exceeds limit)
```bash
# PowerShell
$longPayload = 'A' * 6000

$body = @{
    packetId='test-long-002'
    cpuIntensity=1
    ramIntensity=1
    payload=$longPayload
} | ConvertTo-Json

Invoke-WebRequest -Uri 'http://localhost:8080/api/v1/fakePacket' `
    -Method POST `
    -ContentType 'application/json' `
    -Body $body `
    -UseBasicParsing
```

**Before Fix**: ❌ Status 500, "value too long" error  
**After Fix**: ✅ Status 200 OK, payload truncated to 5000 chars

#### Test 3: Verify Truncation in Database
```sql
-- Check packet_logs table
SELECT 
    packet_id,
    LENGTH(payload) as payload_length,
    LENGTH(result) as result_length,
    CASE 
        WHEN payload LIKE '%... (truncated)' THEN 'Truncated'
        ELSE 'Original'
    END as payload_status
FROM packet_logs
WHERE packet_id = 'test-long-002';
```

**Expected Result**:
```
packet_id       | payload_length | result_length | payload_status
----------------|----------------|---------------|---------------
test-long-002   | 5000          | 22            | Truncated
```

#### Test 4: Error Logging with Large Context
```bash
# Trigger an error with large payload
$longPayload = 'B' * 7000

$body = @{
    packetId='test-error-003'
    cpuIntensity=999  # Invalid intensity to trigger error
    ramIntensity=1
    payload=$longPayload
} | ConvertTo-Json

Invoke-WebRequest -Uri 'http://localhost:8080/api/v1/fakePacket' `
    -Method POST `
    -ContentType 'application/json' `
    -Body $body `
    -UseBasicParsing
```

**Result**: ✅ Error logged successfully, payload truncated in `packet_error_logs`

### Verification Commands

```bash
# 1. Check for "value too long" errors in logs
docker logs cs4445-app --tail 100 | grep "value too long"
# Expected: No results

# 2. Check application health
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}

# 3. Verify database records
docker exec -it cs4445-postgres psql -U myuser -d mydatabase -c \
  "SELECT COUNT(*) FROM packet_logs WHERE LENGTH(payload) <= 5000;"
# Expected: All records have payload <= 5000 chars

# 4. Check for truncation markers
docker exec -it cs4445-postgres psql -U myuser -d mydatabase -c \
  "SELECT COUNT(*) FROM packet_logs WHERE payload LIKE '%... (truncated)';"
# Expected: > 0 (records with truncated payloads)
```

---

## Lessons Learned

### 1. Database Constraints Must Be Enforced in Application Layer
- **Problem**: Relying solely on database constraints leads to runtime errors
- **Solution**: Validate and truncate data before persistence
- **Best Practice**: Implement validation at service layer, not just entity layer

### 2. Docker Cache Can Hide Code Changes
- **Problem**: Docker aggressively caches build layers
- **Solution**: Use `--no-cache` when code changes aren't reflected
- **Best Practice**: 
  - For development: Use `--no-cache` or volume mounts
  - For production: Rely on cache for faster builds
  - Always verify running code matches source

### 3. String Length Calculations Must Be Precise
- **Problem**: Hardcoded offset (15) didn't match actual suffix length (17)
- **Solution**: Use `suffix.length()` for dynamic calculation
- **Best Practice**: Avoid magic numbers, use constants or calculated values

### 4. Comprehensive Testing Is Essential
- **Problem**: Initial fix didn't work due to Docker cache
- **Solution**: Test with actual large payloads in containerized environment
- **Best Practice**: Test edge cases (boundary values, null, empty, very large)

### 5. Transaction Boundaries Matter
- **Problem**: Error logging could fail and lose original error
- **Solution**: Use `Propagation.REQUIRES_NEW` for error logging
- **Best Practice**: Separate error logging into independent transaction

---

## Code Changes Summary

### Files Modified

1. **src/main/java/com/CS445/CS4445_Sub_Server/service/FakePacketService.java**
   - Added `truncateString()` method
   - Applied truncation to `payload` and `result` fields
   - Lines changed: ~15 lines

2. **src/main/java/com/CS445/CS4445_Sub_Server/service/PacketErrorLogService.java**
   - Added `truncateString()` method
   - Applied truncation to multiple fields:
     - `errorMessage` (255 chars)
     - `payload` (5000 chars)
     - `endpoint` (2000 chars)
     - `requestMethod` (1000 chars)
     - `additionalContext` (5000 chars)
   - Lines changed: ~20 lines

### No Database Schema Changes Required
- Existing VARCHAR constraints remain unchanged
- Application-level truncation prevents constraint violations

---

## Maintenance Notes

### Future Considerations

1. **Consider Increasing Column Sizes**
   - If truncation causes data loss issues
   - Evaluate storage implications
   - Update both schema and truncation limits

2. **Add Metrics for Truncation Events**
   ```java
   if (value.length() > maxLength) {
       metricsService.incrementTruncationCounter(fieldName);
       log.warn("Truncated {} from {} to {} chars", fieldName, value.length(), maxLength);
   }
   ```

3. **Implement Configurable Limits**
   ```properties
   # application.properties
   app.limits.payload.max-length=5000
   app.limits.error-message.max-length=255
   ```

4. **Add Validation at Controller Layer**
   ```java
   @PostMapping("/fakePacket")
   public ResponseEntity<FakePacketResponse> processFakePacket(
           @Valid @RequestBody FakePacketRequest request) {
       // @Size(max = 5000) on FakePacketRequest.payload
   }
   ```

### Monitoring Recommendations

1. **Set up alerts for truncation events**
2. **Monitor database constraint violations** (should be zero)
3. **Track average payload sizes** to optimize limits
4. **Log truncation statistics** for capacity planning

---

## References

### Related Documentation
- [Database Schema](../9-reference/ARCHITECTURE.md#database-schema)
- [Error Handling Guide](./README.md#error-handling)
- [Docker Troubleshooting](./README.md#docker-issues)

### External Resources
- [PostgreSQL VARCHAR Documentation](https://www.postgresql.org/docs/current/datatype-character.html)
- [Spring Data JPA Exception Handling](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.exceptions)
- [Docker Build Cache](https://docs.docker.com/build/cache/)

---

**Document Version**: 1.0  
**Last Updated**: December 21, 2025  
**Author**: Development Team  
**Status**: ✅ Issue Resolved and Documented


