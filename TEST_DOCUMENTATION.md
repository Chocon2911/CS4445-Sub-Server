# Test Documentation

Comprehensive test suite for the CS4445 Subscription Server application.

## Table of Contents

1. [Overview](#overview)
2. [Test Structure](#test-structure)
3. [Running Tests](#running-tests)
4. [Test Coverage](#test-coverage)
5. [Test Descriptions](#test-descriptions)
6. [Prerequisites](#prerequisites)
7. [Troubleshooting](#troubleshooting)

## Overview

This test suite provides comprehensive coverage for the CS4445 Subscription Server, including:
- **Unit Tests**: Testing individual components in isolation
- **Integration Tests**: Testing API endpoints with mocked dependencies
- **Edge Case Tests**: Testing boundary conditions and error scenarios

The tests ensure that the fake packet processing API works correctly under various conditions and handles errors gracefully.

## Test Structure

```
src/test/java/com/CS445/CS4445_Sub_Server/
├── Cs4445SubServerApplicationTests.java          # Basic context load test
├── service/
│   ├── FakePacketServiceTest.java                # Unit tests for FakePacketService
│   └── ServerStateServiceTest.java               # Unit tests for ServerStateService
├── controller/
│   └── FakePacketControllerIntegrationTest.java  # Integration tests for REST API
└── EdgeCaseAndErrorHandlingTest.java             # Edge cases and error scenarios
```

## Running Tests

### Run All Tests

```bash
# Using Maven wrapper (recommended)
./mvnw test

# Or with Windows Maven wrapper
mvnw.cmd test
```

### Run Specific Test Class

```bash
# Run FakePacketService tests
./mvnw test -Dtest=FakePacketServiceTest

# Run ServerStateService tests
./mvnw test -Dtest=ServerStateServiceTest

# Run Controller integration tests
./mvnw test -Dtest=FakePacketControllerIntegrationTest

# Run edge case tests
./mvnw test -Dtest=EdgeCaseAndErrorHandlingTest
```

### Run Specific Test Method

```bash
./mvnw test -Dtest=FakePacketServiceTest#shouldProcessPacketSuccessfully
```

### Run Tests with Coverage

```bash
./mvnw verify

# Or with detailed output
./mvnw clean verify
```

### Run Tests in IntelliJ IDEA

1. Right-click on the test class or method
2. Select "Run 'TestName'" or "Debug 'TestName'"
3. View results in the Run tool window

### Run Tests in VS Code

1. Install the "Java Test Runner" extension
2. Click the play button next to the test class or method
3. View results in the Test Explorer

## Test Coverage

### FakePacketServiceTest (11 tests)

Tests the core packet processing service with various intensities and configurations.

**Key Test Scenarios:**
- ✅ Process packet successfully with valid request
- ✅ Use default values when intensities are null
- ✅ Clamp CPU intensity to valid range (1-10)
- ✅ Clamp RAM intensity to valid range (1-10)
- ✅ Respect minimum processing time
- ✅ Save packet log to database with correct values
- ✅ Query database for packet history
- ✅ Handle low intensity workload (intensity=1)
- ✅ Handle high intensity workload (intensity=10)
- ✅ Process multiple packets with same ID

**Mocked Dependencies:**
- `PacketLogRepository`: Database operations are mocked

### ServerStateServiceTest (13 tests)

Tests the server state management (open/closed) functionality.

**Key Test Scenarios:**
- ✅ Initialize with server open
- ✅ Close server successfully
- ✅ Open server successfully
- ✅ Use default reason when closing/opening without reason
- ✅ Not change state when closing already closed server
- ✅ Not change state when opening already open server
- ✅ Track state changes correctly over multiple operations
- ✅ Handle concurrent state checks safely
- ✅ Handle rapid state changes
- ✅ Update timestamp when state changes
- ✅ Handle empty string reason
- ✅ Handle very long reason strings

**Mocked Dependencies:**
- None (pure unit tests)

### FakePacketControllerIntegrationTest (14 tests)

Tests the REST API endpoints with mocked service layer.

**Key Test Scenarios:**
- ✅ POST /api/v1/fakePacket - Process packet successfully when server is open
- ✅ POST /api/v1/fakePacket - Reject packet when server is closed (503)
- ✅ POST /api/v1/fakePacket - Handle service exceptions (500)
- ✅ POST /api/v1/fakePacket - Accept minimal request
- ✅ GET /api/v1/health - Return health status
- ✅ POST /api/v1/server/close - Close server with/without reason
- ✅ POST /api/v1/server/open - Open server with/without reason
- ✅ GET /api/v1/server/status - Return current server status
- ✅ Complete server lifecycle (open → process → close → reject → open → process)

**Mocked Dependencies:**
- `FakePacketService`: Packet processing logic mocked
- `ServerStateService`: State management mocked

### EdgeCaseAndErrorHandlingTest (28 tests)

Tests boundary conditions, invalid inputs, and error scenarios.

**Key Test Scenarios:**

**Packet ID Tests:**
- ✅ Handle empty packet ID
- ✅ Handle null packet ID
- ✅ Handle very long packet ID (10,000 chars)
- ✅ Handle special characters in packet ID
- ✅ Handle Unicode characters in packet ID

**Intensity Tests:**
- ✅ Handle negative CPU intensity
- ✅ Handle extremely high CPU intensity (Integer.MAX_VALUE)
- ✅ Handle negative RAM intensity
- ✅ Handle extremely high RAM intensity (Integer.MAX_VALUE)

**Processing Time Tests:**
- ✅ Handle zero processing time
- ✅ Handle negative processing time

**Payload Tests:**
- ✅ Handle extremely large payload (100KB)
- ✅ Handle empty payload
- ✅ Handle null payload

**HTTP/JSON Tests:**
- ✅ Handle malformed JSON (400)
- ✅ Handle empty request body
- ✅ Handle request with extra unknown fields
- ✅ Handle wrong data types in request (400)
- ✅ Reject GET request to POST endpoint (405)
- ✅ Handle missing Content-Type header (415)
- ✅ Handle non-existent endpoint (404)

**Server Control Tests:**
- ✅ Handle special characters in server close reason
- ✅ Handle very long server close reason (10,000 chars)

**Concurrency Tests:**
- ✅ Handle concurrent requests to fakePacket endpoint

**Mocked Dependencies:**
- `FakePacketService`: Mocked
- `ServerStateService`: Mocked

## Test Descriptions

### Unit Tests vs Integration Tests

**Unit Tests** (`FakePacketServiceTest`, `ServerStateServiceTest`):
- Test individual components in isolation
- Use Mockito to mock dependencies
- Fast execution
- Focus on business logic

**Integration Tests** (`FakePacketControllerIntegrationTest`):
- Test the full HTTP request/response cycle
- Use MockMvc to simulate HTTP requests
- Test controller layer with mocked services
- Validate JSON serialization/deserialization

**Edge Case Tests** (`EdgeCaseAndErrorHandlingTest`):
- Test boundary conditions
- Test invalid inputs
- Test error handling
- Ensure robustness

## Prerequisites

### Required Software

1. **Java 21 or higher**
   ```bash
   java -version
   ```

2. **Maven** (or use included Maven wrapper)
   ```bash
   mvn -version
   ```

### Java Setup for WSL

If you're running in WSL and Java is not installed:

```bash
# Install OpenJDK 21
sudo apt update
sudo apt install openjdk-21-jdk

# Verify installation
java -version
```

### Environment Variables

For Windows users, ensure `JAVA_HOME` is set:

```cmd
# In Command Prompt
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

# Verify
java -version
```

For Linux/WSL users:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

## Troubleshooting

### Common Issues

#### 1. Tests Fail with "Cannot find symbol" errors

**Solution**: Clean and rebuild the project
```bash
./mvnw clean compile test
```

#### 2. Tests Fail with "Spring Boot context failed to load"

**Solution**: Check application.properties and ensure test profile is configured
```bash
# Verify test profile exists
cat src/test/resources/application-test.properties
```

If missing, create `src/test/resources/application-test.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
```

#### 3. Tests Timeout

**Solution**: Increase test timeout in pom.xml or run with more memory
```bash
export MAVEN_OPTS="-Xmx2g"
./mvnw test
```

#### 4. Database Connection Errors During Tests

**Solution**: Tests use H2 in-memory database for testing, not PostgreSQL.
Ensure `spring-boot-starter-test` and `h2` dependencies are in pom.xml.

#### 5. Lombok Compilation Errors

**Solution**: Enable annotation processing in your IDE
- IntelliJ IDEA: Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable
- VS Code: Install Lombok extension

### Running Tests Without Database

The tests are designed to work without a running PostgreSQL database:
- Unit tests mock the repository layer
- Integration tests use `@WebMvcTest` which doesn't load database components
- The H2 dependency provides in-memory database support

## Test Metrics

| Test Class | Test Count | Coverage Area |
|-----------|-----------|---------------|
| FakePacketServiceTest | 11 | Service layer logic |
| ServerStateServiceTest | 13 | State management |
| FakePacketControllerIntegrationTest | 14 | REST API endpoints |
| EdgeCaseAndErrorHandlingTest | 28 | Edge cases & errors |
| **Total** | **66** | **Comprehensive** |

## Best Practices

1. **Run tests before committing**: Always run `./mvnw test` before pushing code
2. **Write tests first**: Follow TDD when adding new features
3. **Keep tests isolated**: Each test should be independent
4. **Use descriptive names**: Test names should clearly describe what they test
5. **Test edge cases**: Always test boundary conditions and invalid inputs
6. **Mock external dependencies**: Use Mockito for unit tests
7. **Use @DisplayName**: Make test names readable in test reports

## CI/CD Integration

To integrate these tests into a CI/CD pipeline:

```yaml
# Example GitHub Actions workflow
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run tests
        run: ./mvnw clean test
      - name: Generate test report
        run: ./mvnw surefire-report:report
```

## Next Steps

1. **Add more integration tests** with real database (TestContainers)
2. **Add performance tests** to measure CPU/RAM load accuracy
3. **Add load tests** with tools like JMeter or Gatling
4. **Add mutation testing** with PITest
5. **Set up code coverage reporting** with JaCoCo
6. **Add API contract tests** with Spring Cloud Contract

## Contact

For questions or issues with the tests, please contact the development team or create an issue in the project repository.
