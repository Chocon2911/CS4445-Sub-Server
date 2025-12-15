# System Architecture - CS4445 Sub Server

**Complete guide to understanding how the database, API, and monitoring components work together**

## 📚 Table of Contents

- [System Overview](#system-overview)
- [Component Architecture](#component-architecture)
- [Data Flow](#data-flow)
- [Network Communication](#network-communication)
- [Monitoring System](#monitoring-system)
- [Real-World Examples](#real-world-examples)
- [Troubleshooting](#troubleshooting)

## 🏗️ System Overview

The CS4445 Sub Server is a microservices-based application with four main components running in Docker containers:

```
┌─────────────────────────────────────────────────────────────────┐
│                          Your Server                             │
│                                                                  │
│  ┌──────────┐      ┌──────────┐      ┌──────────┐              │
│  │          │      │          │      │          │              │
│  │ Grafana  │─────▶│Prometheus│◀─────│Spring App│              │
│  │  :3000   │      │  :9090   │      │  :8080   │              │
│  │          │      │          │      │          │              │
│  └──────────┘      └──────────┘      └────┬─────┘              │
│       ▲                                    │                    │
│       │                                    │                    │
│       │                                    ▼                    │
│       │                            ┌──────────┐                │
│       │                            │PostgreSQL│                │
│       │                            │  :5432   │                │
│       │                            └──────────┘                │
│       │                                                         │
└───────┼─────────────────────────────────────────────────────────┘
        │
        ▼
    👤 User's Browser
```

### Components Summary

| Component | Port | Technology | Purpose |
|-----------|------|------------|---------|
| Spring Boot App | 8080 | Java 25, Spring Boot 3.5.8 | REST API and business logic |
| PostgreSQL | 5432 | PostgreSQL 16 | Data persistence |
| Prometheus | 9090 | Prometheus | Metrics collection |
| Grafana | 3000 | Grafana | Metrics visualization |

## 🔧 Component Architecture

### 1. Spring Boot Application (Port 8080)

**Purpose:** The main application that handles all business logic and API requests.

#### Technology Stack

```
Spring Boot 3.5.8
├── Spring Web         (REST API)
├── Spring Data JPA    (Database access)
├── Spring Security    (Authentication/Authorization)
├── Spring Actuator    (Health checks & metrics)
└── Lombok            (Reduce boilerplate)
```

#### Key Responsibilities

1. **Handle HTTP Requests**
   ```
   Client Request → Controller → Service → Repository → Database
   ```

2. **Expose REST API**
   ```bash
   # Business endpoints
   GET  /api/v1/fakePacket          # Retrieve packets
   POST /api/v1/fakePacket          # Create packet
   PUT  /api/v1/fakePacket/{id}     # Update packet
   DELETE /api/v1/fakePacket/{id}   # Delete packet
   ```

3. **Provide Health Checks**
   ```bash
   GET /actuator/health             # Application health status
   GET /actuator/info               # Application information
   ```

4. **Expose Metrics**
   ```bash
   GET /actuator/prometheus         # Metrics for Prometheus
   GET /actuator/metrics            # Available metrics list
   ```

#### Application Layers

```
┌─────────────────────────────────────┐
│         Controller Layer            │  ← Handles HTTP requests
│  @RestController, @RequestMapping   │
└───────────────┬─────────────────────┘
                │
                ▼
┌─────────────────────────────────────┐
│          Service Layer              │  ← Business logic
│  @Service, @Transactional          │
└───────────────┬─────────────────────┘
                │
                ▼
┌─────────────────────────────────────┐
│        Repository Layer             │  ← Database access
│  @Repository, JpaRepository         │
└───────────────┬─────────────────────┘
                │
                ▼
┌─────────────────────────────────────┐
│         PostgreSQL Database         │  ← Data storage
└─────────────────────────────────────┘
```

#### Code Example

```java
// Controller Layer
@RestController
@RequestMapping("/api/v1")
public class FakePacketController {

    @Autowired
    private FakePacketService service;

    @PostMapping("/fakePacket")
    public ResponseEntity<FakePacket> create(@RequestBody FakePacketRequest request) {
        FakePacket packet = service.createPacket(request);
        return ResponseEntity.ok(packet);
    }
}

// Service Layer
@Service
public class FakePacketService {

    @Autowired
    private FakePacketRepository repository;

    @Transactional
    public FakePacket createPacket(FakePacketRequest request) {
        // Business logic here
        FakePacket packet = new FakePacket();
        packet.setData(request.getData());
        return repository.save(packet);  // → Database
    }
}

// Repository Layer
@Repository
public interface FakePacketRepository extends JpaRepository<FakePacket, Long> {
    // Spring Data JPA provides implementation automatically
}
```

### 2. PostgreSQL Database (Port 5432)

**Purpose:** Persistent data storage for all application data.

#### Configuration

```yaml
# From docker-compose.prod.yml
postgres:
  image: postgres:16-alpine
  environment:
    POSTGRES_DB: mydatabase
    POSTGRES_USER: myuser
    POSTGRES_PASSWORD: secret
  ports:
    - "5432:5432"
  volumes:
    - postgres-data:/var/lib/postgresql/data
```

#### Connection from Spring Boot

```properties
# application.properties
spring.datasource.url=jdbc:postgresql://postgres:5432/mydatabase
spring.datasource.username=myuser
spring.datasource.password=secret

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

#### Data Storage

```sql
-- Example schema (auto-generated by JPA)
CREATE TABLE fake_packets (
    id BIGSERIAL PRIMARY KEY,
    data VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Example queries executed by Spring Data JPA
-- When you call repository.save(packet):
INSERT INTO fake_packets (data, created_at)
VALUES ('test_data', NOW());

-- When you call repository.findById(1):
SELECT * FROM fake_packets WHERE id = 1;
```

#### Health Check

```bash
# Docker health check
pg_isready -U myuser
# Returns: postgres:5432 - accepting connections
```

### 3. Prometheus (Port 9090)

**Purpose:** Collect and store time-series metrics data from the application.

#### How It Works

Prometheus uses a **pull model** - it periodically scrapes (pulls) metrics from your application:

```
Every 15 seconds:
Prometheus → GET http://app:8080/actuator/prometheus
         ← Metrics data in text format

Prometheus stores this data in time-series database
```

#### Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s          # How often to scrape
  evaluation_interval: 15s      # How often to evaluate rules

scrape_configs:
  - job_name: 'spring-boot-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['app:8080']   # Where to scrape from
```

#### Metrics Collected

```
# HTTP Request Metrics
http_server_requests_seconds_count{uri="/api/v1/fakePacket"} 1234
http_server_requests_seconds_sum{uri="/api/v1/fakePacket"} 56.78

# JVM Metrics
jvm_memory_used_bytes{area="heap"} 268435456
jvm_threads_live 42
jvm_gc_pause_seconds_count 150

# System Metrics
system_cpu_usage 0.45
system_load_average_1m 2.5

# Database Metrics
hikaricp_connections_active 5
hikaricp_connections_idle 10
```

#### Querying Metrics

```promql
# Prometheus Query Language (PromQL)

# Average response time over 5 minutes
rate(http_server_requests_seconds_sum[5m]) /
rate(http_server_requests_seconds_count[5m])

# CPU usage percentage
system_cpu_usage * 100

# Requests per second
rate(http_server_requests_seconds_count[1m])
```

### 4. Grafana (Port 3000)

**Purpose:** Visualize metrics from Prometheus with beautiful dashboards.

#### How It Works

```
User opens browser → http://server:3000
                  ↓
              Login to Grafana
                  ↓
          Open dashboard
                  ↓
    Dashboard sends PromQL queries to Prometheus
                  ↓
    Prometheus returns time-series data
                  ↓
    Grafana renders graphs and charts
```

#### Configuration

```yaml
# From docker-compose.prod.yml
grafana:
  image: grafana/grafana:latest
  environment:
    GF_SECURITY_ADMIN_USER: admin
    GF_SECURITY_ADMIN_PASSWORD: admin
    GF_SERVER_ROOT_URL: http://localhost:3000
  ports:
    - "3000:3000"
```

#### Dashboard Examples

**1. Application Performance Dashboard**
```
┌─────────────────────────────────────┐
│  Request Rate: 150 req/s            │
│  [████████████████░░░░] 80%         │
├─────────────────────────────────────┤
│  Average Response Time              │
│  [Line graph showing last 24h]      │
├─────────────────────────────────────┤
│  Error Rate: 0.5%                   │
│  [████░░░░░░░░░░░░░░░] 0.5%        │
└─────────────────────────────────────┘
```

**2. System Resources Dashboard**
```
┌─────────────────────────────────────┐
│  CPU Usage: 45%                     │
│  [Line graph]                       │
├─────────────────────────────────────┤
│  Memory Usage: 512MB / 2GB          │
│  [Gauge chart]                      │
├─────────────────────────────────────┤
│  Database Connections: 5/20         │
│  [Bar chart]                        │
└─────────────────────────────────────┘
```

## 🔄 Data Flow

### Example: Creating a Fake Packet

Let's trace the complete data flow when a user creates a fake packet:

#### Step 1: Client Makes Request

```bash
curl -X POST http://server:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{
    "data": "test_packet_123",
    "timestamp": "2025-12-14T10:30:00Z"
  }'
```

#### Step 2: Spring Boot Receives Request

```
HTTP Request arrives at port 8080
         ↓
DispatcherServlet routes to Controller
         ↓
FakePacketController.create() is called
```

#### Step 3: Controller Processes Request

```java
@PostMapping("/fakePacket")
public ResponseEntity<FakePacket> create(@RequestBody FakePacketRequest request) {
    // 1. Validate input
    if (request.getData() == null) {
        throw new ValidationException("Data is required");
    }

    // 2. Call service layer
    FakePacket packet = fakePacketService.createPacket(request);

    // 3. Return response
    return ResponseEntity.ok(packet);
}
```

#### Step 4: Service Layer Executes Business Logic

```java
@Service
@Transactional
public class FakePacketService {

    public FakePacket createPacket(FakePacketRequest request) {
        // 1. Create entity
        FakePacket packet = new FakePacket();
        packet.setData(request.getData());
        packet.setTimestamp(LocalDateTime.now());

        // 2. Additional business logic
        // - Validate data format
        // - Check permissions
        // - Transform data if needed

        // 3. Save to database
        FakePacket saved = repository.save(packet);

        // 4. Maybe publish event, send notification, etc.

        return saved;
    }
}
```

#### Step 5: Repository Saves to Database

```java
// Spring Data JPA automatically generates this SQL:
INSERT INTO fake_packets (data, timestamp, created_at)
VALUES ('test_packet_123', '2025-12-14 10:30:00', NOW())
RETURNING id, data, timestamp, created_at;
```

#### Step 6: PostgreSQL Stores Data

```
PostgreSQL receives SQL command
         ↓
Validates and executes INSERT
         ↓
Stores data in table
         ↓
Returns generated ID and row data
         ↓
Transaction committed
```

#### Step 7: Response Sent to Client

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 123,
  "data": "test_packet_123",
  "timestamp": "2025-12-14T10:30:00Z",
  "createdAt": "2025-12-14T10:30:00.123Z"
}
```

#### Step 8: Metrics Recorded

Spring Boot automatically records:
```
http_server_requests_seconds_count{
  uri="/api/v1/fakePacket",
  method="POST",
  status="200"
} = 1235  (incremented by 1)

http_server_requests_seconds_sum{
  uri="/api/v1/fakePacket",
  method="POST",
  status="200"
} = 56.823  (added 0.045 seconds)
```

#### Step 9: Prometheus Scrapes Metrics

```
[15 seconds later]
Prometheus: GET http://app:8080/actuator/prometheus
         ← Receives updated metrics

Prometheus stores:
  timestamp: 2025-12-14T10:30:15Z
  metric: http_server_requests_seconds_count = 1235
```

#### Step 10: Grafana Updates Dashboard

```
[Real-time dashboard update]
User's browser queries Grafana every 5 seconds
         ↓
Grafana queries Prometheus:
  rate(http_server_requests_seconds_count[1m])
         ↓
Prometheus returns: 2.5 requests/second
         ↓
Grafana updates graph in user's browser
```

### Complete Flow Diagram

```
┌─────────┐
│  User   │
└────┬────┘
     │ 1. POST /api/v1/fakePacket
     ▼
┌─────────────────────┐
│  Spring Boot App    │
│  :8080              │
│                     │
│  ┌──────────────┐   │
│  │ Controller   │   │ 2. Receive request
│  └──────┬───────┘   │
│         │           │
│  ┌──────▼───────┐   │
│  │  Service     │   │ 3. Business logic
│  └──────┬───────┘   │
│         │           │
│  ┌──────▼───────┐   │
│  │ Repository   │   │ 4. Database query
│  └──────┬───────┘   │
└─────────┼───────────┘
          │
          ▼
┌─────────────────────┐
│   PostgreSQL        │ 5. Store data
│   :5432             │
└─────────────────────┘
          │
          │ 6. Return saved data
          ▼
┌─────────────────────┐
│  Spring Boot App    │ 7. Send response
└─────────┬───────────┘
          │
          │ 8. Expose metrics
          ▼
┌─────────────────────┐
│   Prometheus        │ 9. Scrape metrics
│   :9090             │
└─────────┬───────────┘
          │
          │ 10. Query metrics
          ▼
┌─────────────────────┐
│    Grafana          │ 11. Display dashboard
│    :3000            │
└─────────────────────┘
          │
          ▼
      👤 User views dashboard
```

## 🌐 Network Communication

### Docker Network Architecture

All services run in the same Docker network (`app-network`), allowing them to communicate using service names:

```yaml
# docker-compose.prod.yml
networks:
  app-network:
    driver: bridge

services:
  app:
    networks:
      - app-network
  postgres:
    networks:
      - app-network
  prometheus:
    networks:
      - app-network
  grafana:
    networks:
      - app-network
```

### Internal Communication (Inside Docker)

```
Service Discovery by Name:

App → PostgreSQL:     postgres:5432
Prometheus → App:     app:8080
Grafana → Prometheus: prometheus:9090
```

### External Access (From Outside)

On CKey.com, internal ports are mapped to external ports:

```
External Access via CKey Ports:

User → App:        http://n1.ckey.vn:3497
User → Grafana:    http://n1.ckey.vn:3495
User → Prometheus: http://n1.ckey.vn:3498

Port Mapping:
3497 → 8080  (App)
3495 → 3000  (Grafana)
3498 → 9090  (Prometheus)
```

### Communication Matrix

| From | To | Port | Protocol | Purpose |
|------|------|------|----------|---------|
| User | App | 8080 | HTTP | API requests |
| App | PostgreSQL | 5432 | TCP | Database queries |
| Prometheus | App | 8080 | HTTP | Scrape metrics |
| Grafana | Prometheus | 9090 | HTTP | Query metrics |
| User | Grafana | 3000 | HTTP | View dashboards |
| Deployment Script | Server | 22 | SSH | Deploy application |

## 📊 Monitoring System

### How Monitoring Works

```
┌─────────────────────────────────────────────────────────┐
│                    Monitoring Flow                       │
└─────────────────────────────────────────────────────────┘

1. Application Records Metrics
   ↓
   Spring Boot Actuator collects:
   - HTTP requests
   - JVM stats
   - Database connections
   - Custom business metrics

2. Prometheus Scrapes Metrics
   ↓
   Every 15 seconds:
   GET http://app:8080/actuator/prometheus

3. Prometheus Stores Data
   ↓
   Time-series database:
   timestamp | metric_name | value

4. Grafana Queries Prometheus
   ↓
   PromQL: rate(http_requests[5m])

5. Grafana Displays Dashboard
   ↓
   Beautiful graphs and charts

6. Alerts (if configured)
   ↓
   If CPU > 80%, send email/Slack
```

### What You Can Monitor

#### Application Metrics

```yaml
HTTP Requests:
  - Total requests count
  - Requests per second
  - Response time (avg, p95, p99)
  - Error rate (4xx, 5xx)
  - Requests by endpoint
  - Requests by status code

Business Metrics:
  - Packets created per hour
  - Active users
  - Data processing rate
  - Custom counters/gauges
```

#### JVM Metrics

```yaml
Memory:
  - Heap memory used/max
  - Non-heap memory
  - Memory pools
  - Garbage collection count/time

Threads:
  - Live threads
  - Daemon threads
  - Peak threads
  - Thread states

Classes:
  - Loaded classes
  - Unloaded classes
```

#### System Metrics

```yaml
CPU:
  - Process CPU usage
  - System CPU usage
  - Load average

Disk:
  - Free space
  - Total space
  - I/O operations
```

#### Database Metrics

```yaml
Connection Pool (HikariCP):
  - Active connections
  - Idle connections
  - Pending threads
  - Connection timeout
  - Max lifetime

Query Performance:
  - Query execution time
  - Slow queries
  - Transaction count
```

### Example Grafana Queries

```promql
# Request rate (requests per second)
rate(http_server_requests_seconds_count[1m])

# Average response time
rate(http_server_requests_seconds_sum[5m]) /
rate(http_server_requests_seconds_count[5m])

# Error rate percentage
(rate(http_server_requests_seconds_count{status=~"5.."}[5m]) /
rate(http_server_requests_seconds_count[5m])) * 100

# JVM heap usage percentage
(jvm_memory_used_bytes{area="heap"} /
jvm_memory_max_bytes{area="heap"}) * 100

# Database connection pool usage
hikaricp_connections_active / hikaricp_connections_max * 100
```

## 🎯 Real-World Examples

### Example 1: Detecting Slow API Endpoint

**Problem:** Users complain that the API is slow.

**Solution using monitoring:**

```
1. Open Grafana dashboard
   ↓
2. Check "API Response Time" graph
   ↓
   Notice /api/v1/fakePacket endpoint shows 5 second avg

3. Check database metrics
   ↓
   See "Query Execution Time" spiking

4. Check PostgreSQL slow query log
   ↓
   Find: SELECT * FROM fake_packets WHERE data LIKE '%test%'

5. Optimize query:
   - Add database index
   - Change to exact match instead of LIKE

6. Deploy fix
   ↓
7. Watch response time drop to 50ms ✅
```

### Example 2: Memory Leak Detection

**Problem:** Application crashes after running for a few days.

**Solution using monitoring:**

```
1. Open Grafana "JVM Memory" dashboard
   ↓
2. See heap memory usage steadily increasing
   ↓
   Pattern: Sawtooth that doesn't drop after GC

3. Take heap dump for analysis
   ssh server "docker exec cs4445-app jcmd 1 GC.heap_dump /tmp/heap.hprof"

4. Analyze with tools (VisualVM, Eclipse MAT)
   ↓
5. Find leaked objects (e.g., unclosed connections)
   ↓
6. Fix code (add try-with-resources, close connections)
   ↓
7. Deploy fix
   ↓
8. Watch memory pattern normalize ✅
```

### Example 3: High Traffic Alert

**Problem:** Need to know when traffic spikes.

**Solution using Prometheus alerting:**

```yaml
# prometheus-alerts.yml
groups:
  - name: traffic_alerts
    rules:
      - alert: HighTrafficVolume
        expr: rate(http_server_requests_seconds_count[5m]) > 100
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High traffic detected"
          description: "Request rate is {{ $value }} req/s"

      - alert: HighErrorRate
        expr: |
          (rate(http_server_requests_seconds_count{status=~"5.."}[5m]) /
          rate(http_server_requests_seconds_count[5m])) * 100 > 5
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }}%"
```

## 🔍 Troubleshooting

### Issue 1: Database Connection Refused

**Symptom:**
```
ERROR: Connection to postgres:5432 refused
```

**Check:**
```bash
# 1. Is PostgreSQL running?
docker ps | grep postgres

# 2. Check PostgreSQL logs
docker logs cs4445-postgres

# 3. Test connection manually
docker exec -it cs4445-app psql -h postgres -U myuser -d mydatabase

# 4. Check network
docker network inspect app-network
```

### Issue 2: Metrics Not Appearing in Prometheus

**Symptom:** Grafana shows "No data"

**Check:**
```bash
# 1. Is Prometheus scraping?
# Open http://server:9090/targets
# Should show app:8080 as "UP"

# 2. Check app exposes metrics
curl http://localhost:8080/actuator/prometheus

# 3. Check Prometheus config
docker exec cs4445-prometheus cat /etc/prometheus/prometheus.yml

# 4. Check Prometheus logs
docker logs cs4445-prometheus
```

### Issue 3: Slow API Response

**Symptom:** API takes > 1 second to respond

**Debug:**
```bash
# 1. Check application logs
docker logs cs4445-app --tail 100

# 2. Check database query time
# Look at Grafana "Database Performance" dashboard

# 3. Enable SQL logging (temporarily)
# In application.properties:
spring.jpa.show-sql=true

# 4. Check slow queries in PostgreSQL
docker exec -it cs4445-postgres psql -U myuser -d mydatabase
SELECT * FROM pg_stat_statements ORDER BY mean_exec_time DESC LIMIT 10;
```

### Issue 4: High Memory Usage

**Symptom:** Application using too much memory

**Debug:**
```bash
# 1. Check current memory usage
docker stats cs4445-app

# 2. Check JVM heap settings
docker exec cs4445-app java -XX:+PrintFlagsFinal -version | grep HeapSize

# 3. Get heap dump
docker exec cs4445-app jcmd 1 GC.heap_dump /tmp/heap.hprof
docker cp cs4445-app:/tmp/heap.hprof ./heap.hprof

# 4. Analyze with VisualVM or Eclipse MAT
```

## 📝 Best Practices

### Application Development

```java
✅ DO:
- Use proper exception handling
- Close database connections (use try-with-resources)
- Use connection pooling (HikariCP)
- Add appropriate indexes to database tables
- Validate input data
- Use pagination for large datasets
- Implement caching where appropriate

❌ DON'T:
- Load entire table into memory
- Use SELECT * (specify columns)
- Ignore database connection leaks
- Skip input validation
- Log sensitive data
- Block threads with long operations
```

### Monitoring

```yaml
✅ DO:
- Set up alerts for critical metrics
- Monitor error rates
- Track response times
- Monitor database performance
- Check disk space regularly
- Review dashboards weekly

❌ DON'T:
- Ignore warning signs
- Wait for users to report issues
- Collect too many metrics (affects performance)
- Set alert thresholds too sensitive
```

### Database

```sql
✅ DO:
- Use indexes on frequently queried columns
- Regularly vacuum and analyze
- Monitor connection pool usage
- Use transactions appropriately
- Back up data regularly

❌ DON'T:
- Use N+1 queries
- Skip migrations
- Ignore slow query warnings
- Overuse indexes (slows writes)
```

## 🔗 Related Documentation

- [API Documentation](./6-api/server-control-api.md) - Complete API reference
- [Monitoring Guide](./5-monitoring/monitoring-guide.md) - Detailed monitoring setup
- [Deployment Guide](./3-deployment/V2-MULTI-SERVER-DEPLOYMENT-SUMMARY.md) - How to deploy

## 📖 Further Reading

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Docker Networking](https://docs.docker.com/network/)

---

**Version:** 1.0
**Created:** 2025-12-14
**Last Updated:** 2025-12-14
**Author:** CS4445 Team
