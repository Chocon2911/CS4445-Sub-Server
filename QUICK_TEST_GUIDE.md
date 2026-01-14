# Quick Test Guide

Fast reference for running tests on the CS4445 Subscription Server.

## 🚀 Quick Start

```bash
# Navigate to project directory
cd "/path/to/CS4445-Sub-Server"

# Run all tests
./mvnw test

# On Windows
mvnw.cmd test
```

## 📊 Test Summary

| Test Suite | Tests | Purpose |
|-----------|-------|---------|
| **FakePacketServiceTest** | 11 | Core packet processing logic |
| **ServerStateServiceTest** | 13 | Server open/close state management |
| **FakePacketControllerIntegrationTest** | 14 | REST API endpoints |
| **EdgeCaseAndErrorHandlingTest** | 28 | Boundary conditions & errors |
| **TOTAL** | **66** | Comprehensive coverage |

## 🎯 Common Commands

```bash
# Run all tests
./mvnw test

# Run tests with output
./mvnw test -Dorg.slf4j.simpleLogger.defaultLogLevel=debug

# Run specific test class
./mvnw test -Dtest=FakePacketServiceTest

# Run specific test method
./mvnw test -Dtest=FakePacketServiceTest#shouldProcessPacketSuccessfully

# Clean and test
./mvnw clean test

# Run tests with coverage
./mvnw verify

# Skip tests (for quick builds)
./mvnw clean install -DskipTests
```

## ✅ What's Tested

### Service Layer Tests
- ✅ Packet processing with various CPU/RAM intensities (1-10)
- ✅ Default value handling (null inputs)
- ✅ Intensity clamping (negative/extreme values)
- ✅ Minimum processing time enforcement
- ✅ Database operations (save & query)
- ✅ Multiple packets with same ID

### State Management Tests
- ✅ Server initialization (starts open)
- ✅ Open/close server operations
- ✅ State change tracking with timestamps
- ✅ Concurrent access safety
- ✅ Rapid state transitions
- ✅ Reason string handling (empty, null, long)

### REST API Tests
- ✅ `POST /api/v1/fakePacket` - Success (200)
- ✅ `POST /api/v1/fakePacket` - Server closed (503)
- ✅ `POST /api/v1/fakePacket` - Service error (500)
- ✅ `GET /api/v1/health` - Health check
- ✅ `POST /api/v1/server/close` - Close server
- ✅ `POST /api/v1/server/open` - Open server
- ✅ `GET /api/v1/server/status` - Get status
- ✅ Complete lifecycle testing

### Edge Cases & Error Handling
- ✅ Empty/null/long packet IDs
- ✅ Special characters & Unicode in IDs
- ✅ Negative/extreme CPU intensities
- ✅ Negative/extreme RAM intensities
- ✅ Zero/negative processing times
- ✅ Large payloads (100KB+)
- ✅ Malformed JSON (400)
- ✅ Wrong HTTP methods (405)
- ✅ Missing headers (415)
- ✅ Non-existent endpoints (404)
- ✅ Concurrent requests

## 🔧 Setup Requirements

### Java Installation
```bash
# Check Java version (need 21+)
java -version

# Install Java 21 (Ubuntu/Debian)
sudo apt install openjdk-21-jdk

# Set JAVA_HOME (Linux)
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Set JAVA_HOME (Windows)
set JAVA_HOME=C:\Program Files\Java\jdk-21
```

### Database Note
Tests **do NOT require PostgreSQL** to be running:
- Unit tests mock the database
- Integration tests use `@WebMvcTest` (no database)
- H2 in-memory database is used for full integration tests

## 📁 Test File Locations

```
src/test/java/com/CS445/CS4445_Sub_Server/
├── Cs4445SubServerApplicationTests.java
├── service/
│   ├── FakePacketServiceTest.java
│   └── ServerStateServiceTest.java
├── controller/
│   └── FakePacketControllerIntegrationTest.java
└── EdgeCaseAndErrorHandlingTest.java
```

## 🐛 Troubleshooting

### Test Failures

**Problem**: "Cannot find symbol" errors
**Solution**: `./mvnw clean compile test`

**Problem**: Tests timeout
**Solution**: `export MAVEN_OPTS="-Xmx2g" && ./mvnw test`

**Problem**: Lombok errors
**Solution**: Enable annotation processing in IDE

**Problem**: Spring context fails to load
**Solution**: Check `src/test/resources/application-test.properties` exists

### Running in IDE

**IntelliJ IDEA**:
1. Right-click test class → "Run 'TestName'"
2. Or click green arrow next to test method

**VS Code**:
1. Install "Java Test Runner" extension
2. Click play button next to test

## 📈 Expected Results

When all tests pass, you should see:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.CS445.CS4445_Sub_Server.Cs4445SubServerApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.CS445.CS4445_Sub_Server.service.FakePacketServiceTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.CS445.CS4445_Sub_Server.service.ServerStateServiceTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.CS445.CS4445_Sub_Server.controller.FakePacketControllerIntegrationTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.CS445.CS4445_Sub_Server.EdgeCaseAndErrorHandlingTest
[INFO] Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 67, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## 🎓 Test Types Explained

| Type | What It Tests | Dependencies |
|------|--------------|--------------|
| **Unit** | Individual methods/classes | Mocked |
| **Integration** | API endpoints with mocked services | Partially mocked |
| **Edge Case** | Boundary conditions & errors | Mocked |

## 💡 Best Practices

1. ✅ Run tests before every commit
2. ✅ Write tests for new features (TDD)
3. ✅ Keep tests fast and isolated
4. ✅ Use descriptive test names
5. ✅ Mock external dependencies
6. ✅ Test happy path + edge cases
7. ✅ Maintain 80%+ code coverage

## 📚 More Information

For detailed information, see: [TEST_DOCUMENTATION.md](TEST_DOCUMENTATION.md)

## 🤝 Contributing

When adding new features:
1. Write unit tests for service logic
2. Write integration tests for API endpoints
3. Add edge case tests for error scenarios
4. Update this guide if needed
5. Ensure all tests pass: `./mvnw test`

---

**Quick Health Check**: `./mvnw clean test` - Should complete in ~30-60 seconds with all 66+ tests passing ✅
