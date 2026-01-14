# SQL Query Guide for CS4445 Subscription Server

## Quick Access to PostgreSQL

### Method 1: Using Docker Exec (Recommended)

```bash
# Connect to PostgreSQL directly
docker exec -it cs4445-postgres psql -U myuser -d mydatabase
```

### Method 2: Using psql from Host (if installed)

```bash
psql -h localhost -p 5432 -U myuser -d mydatabase
# Password: secret
```

### Method 3: Using GUI Tools

**DBeaver / pgAdmin / DataGrip:**
- Host: `localhost`
- Port: `5432`
- Database: `mydatabase`
- Username: `myuser`
- Password: `secret`

---

## Database Schema

### View All Tables

```sql
-- List all tables in the database
\dt

-- Or using SQL
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public';
```

### View Table Structure

```sql
-- Describe packet_logs table
\d packet_logs

-- Or using SQL
SELECT column_name, data_type, character_maximum_length, is_nullable
FROM information_schema.columns
WHERE table_name = 'packet_logs'
ORDER BY ordinal_position;
```

**Expected Columns:**
- `id` - BIGINT (Primary Key, Auto-increment)
- `packet_id` - VARCHAR (NOT NULL)
- `cpu_intensity` - INTEGER
- `ram_intensity` - INTEGER
- `processing_time_ms` - BIGINT
- `cpu_cycles` - BIGINT
- `memory_used_bytes` - BIGINT
- `payload` - TEXT
- `result` - TEXT
- `timestamp` - TIMESTAMP
- `created_at` - TIMESTAMP
- `updated_at` - TIMESTAMP

---

## Basic Queries

### 1. View All Packet Logs

```sql
-- View all records (limit to 100 for safety)
SELECT * FROM packet_logs LIMIT 100;
```

### 2. View Recent Logs

```sql
-- Last 20 packets processed
SELECT
    id,
    packet_id,
    cpu_intensity,
    ram_intensity,
    processing_time_ms,
    timestamp
FROM packet_logs
ORDER BY timestamp DESC
LIMIT 20;
```

### 3. Count Total Logs

```sql
-- Total number of packet logs
SELECT COUNT(*) as total_logs FROM packet_logs;
```

### 4. View Specific Packet by ID

```sql
-- Find logs for a specific packet
SELECT * FROM packet_logs
WHERE packet_id = 'test-packet-001'
ORDER BY timestamp DESC;
```

### 5. View Logs from Today

```sql
-- All logs from today
SELECT * FROM packet_logs
WHERE DATE(timestamp) = CURRENT_DATE
ORDER BY timestamp DESC;
```

### 6. View Logs from Last Hour

```sql
-- Logs from the last hour
SELECT * FROM packet_logs
WHERE timestamp >= NOW() - INTERVAL '1 hour'
ORDER BY timestamp DESC;
```

---

## Analytical Queries

### 7. Group by Packet ID

```sql
-- Count logs per packet ID
SELECT
    packet_id,
    COUNT(*) as log_count,
    MIN(timestamp) as first_seen,
    MAX(timestamp) as last_seen
FROM packet_logs
GROUP BY packet_id
ORDER BY log_count DESC;
```

### 8. Average Processing Time by Intensity

```sql
-- Average processing time grouped by CPU and RAM intensity
SELECT
    cpu_intensity,
    ram_intensity,
    COUNT(*) as request_count,
    AVG(processing_time_ms) as avg_processing_ms,
    MIN(processing_time_ms) as min_processing_ms,
    MAX(processing_time_ms) as max_processing_ms
FROM packet_logs
GROUP BY cpu_intensity, ram_intensity
ORDER BY cpu_intensity, ram_intensity;
```

### 9. CPU Cycles Statistics

```sql
-- Statistics on CPU cycles
SELECT
    cpu_intensity,
    COUNT(*) as request_count,
    AVG(cpu_cycles) as avg_cycles,
    MIN(cpu_cycles) as min_cycles,
    MAX(cpu_cycles) as max_cycles,
    SUM(cpu_cycles) as total_cycles
FROM packet_logs
GROUP BY cpu_intensity
ORDER BY cpu_intensity;
```

### 10. Memory Usage Statistics

```sql
-- Statistics on memory usage
SELECT
    ram_intensity,
    COUNT(*) as request_count,
    AVG(memory_used_bytes) as avg_memory_bytes,
    MIN(memory_used_bytes) as min_memory_bytes,
    MAX(memory_used_bytes) as max_memory_bytes,
    SUM(memory_used_bytes) as total_memory_bytes,
    pg_size_pretty(AVG(memory_used_bytes)::bigint) as avg_memory_readable
FROM packet_logs
GROUP BY ram_intensity
ORDER BY ram_intensity;
```

### 11. Hourly Request Distribution

```sql
-- Count of requests per hour
SELECT
    DATE_TRUNC('hour', timestamp) as hour,
    COUNT(*) as request_count,
    AVG(processing_time_ms) as avg_processing_ms
FROM packet_logs
GROUP BY DATE_TRUNC('hour', timestamp)
ORDER BY hour DESC;
```

### 12. Top 10 Slowest Requests

```sql
-- Find the slowest packet processing requests
SELECT
    packet_id,
    cpu_intensity,
    ram_intensity,
    processing_time_ms,
    timestamp
FROM packet_logs
ORDER BY processing_time_ms DESC
LIMIT 10;
```

### 13. Top 10 Most CPU-Intensive Requests

```sql
-- Highest CPU cycles
SELECT
    packet_id,
    cpu_intensity,
    cpu_cycles,
    processing_time_ms,
    timestamp
FROM packet_logs
ORDER BY cpu_cycles DESC
LIMIT 10;
```

### 14. Top 10 Most Memory-Intensive Requests

```sql
-- Highest memory usage
SELECT
    packet_id,
    ram_intensity,
    memory_used_bytes,
    pg_size_pretty(memory_used_bytes) as memory_readable,
    timestamp
FROM packet_logs
ORDER BY memory_used_bytes DESC
LIMIT 10;
```

### 15. Duplicate Packet IDs

```sql
-- Find packet IDs that were processed multiple times
SELECT
    packet_id,
    COUNT(*) as process_count,
    STRING_AGG(id::text, ', ') as log_ids
FROM packet_logs
GROUP BY packet_id
HAVING COUNT(*) > 1
ORDER BY process_count DESC;
```

---

## Data Validation Queries

### 16. Check for NULL Values

```sql
-- Find records with NULL packet_id (should be none if validation works)
SELECT COUNT(*) as null_packet_ids
FROM packet_logs
WHERE packet_id IS NULL;

-- Find records with NULL intensities
SELECT
    COUNT(*) as total_records,
    SUM(CASE WHEN cpu_intensity IS NULL THEN 1 ELSE 0 END) as null_cpu_intensity,
    SUM(CASE WHEN ram_intensity IS NULL THEN 1 ELSE 0 END) as null_ram_intensity
FROM packet_logs;
```

### 17. Check Intensity Ranges

```sql
-- Verify intensities are within valid range (1-10)
SELECT
    COUNT(*) as total_records,
    SUM(CASE WHEN cpu_intensity < 1 OR cpu_intensity > 10 THEN 1 ELSE 0 END) as invalid_cpu,
    SUM(CASE WHEN ram_intensity < 1 OR ram_intensity > 10 THEN 1 ELSE 0 END) as invalid_ram
FROM packet_logs;

-- Show any invalid records
SELECT * FROM packet_logs
WHERE cpu_intensity NOT BETWEEN 1 AND 10
   OR ram_intensity NOT BETWEEN 1 AND 10;
```

### 18. Check for Negative Values

```sql
-- Find any negative values (should be none)
SELECT * FROM packet_logs
WHERE processing_time_ms < 0
   OR cpu_cycles < 0
   OR memory_used_bytes < 0;
```

### 19. Check for Zero Processing Time

```sql
-- Find requests with zero or very low processing time
SELECT
    packet_id,
    cpu_intensity,
    ram_intensity,
    processing_time_ms,
    timestamp
FROM packet_logs
WHERE processing_time_ms <= 100
ORDER BY processing_time_ms;
```

---

## Performance Analysis Queries

### 20. Correlation Between Intensity and Processing Time

```sql
-- Analyze if higher intensity correlates with longer processing time
SELECT
    cpu_intensity + ram_intensity as total_intensity,
    COUNT(*) as request_count,
    AVG(processing_time_ms) as avg_processing_ms,
    AVG(cpu_cycles) as avg_cpu_cycles,
    AVG(memory_used_bytes) as avg_memory_bytes
FROM packet_logs
GROUP BY cpu_intensity + ram_intensity
ORDER BY total_intensity;
```

### 21. Processing Time Distribution

```sql
-- Categorize processing times into buckets
SELECT
    CASE
        WHEN processing_time_ms < 500 THEN '< 500ms'
        WHEN processing_time_ms < 1000 THEN '500-1000ms'
        WHEN processing_time_ms < 2000 THEN '1-2s'
        WHEN processing_time_ms < 5000 THEN '2-5s'
        ELSE '> 5s'
    END as time_bucket,
    COUNT(*) as request_count,
    AVG(cpu_intensity) as avg_cpu_intensity,
    AVG(ram_intensity) as avg_ram_intensity
FROM packet_logs
GROUP BY time_bucket
ORDER BY
    CASE
        WHEN processing_time_ms < 500 THEN 1
        WHEN processing_time_ms < 1000 THEN 2
        WHEN processing_time_ms < 2000 THEN 3
        WHEN processing_time_ms < 5000 THEN 4
        ELSE 5
    END;
```

### 22. Daily Statistics Summary

```sql
-- Complete daily summary
SELECT
    DATE(timestamp) as date,
    COUNT(*) as total_requests,
    AVG(processing_time_ms) as avg_processing_ms,
    AVG(cpu_cycles) as avg_cpu_cycles,
    AVG(memory_used_bytes) as avg_memory_bytes,
    AVG(cpu_intensity) as avg_cpu_intensity,
    AVG(ram_intensity) as avg_ram_intensity,
    COUNT(DISTINCT packet_id) as unique_packets
FROM packet_logs
GROUP BY DATE(timestamp)
ORDER BY date DESC;
```

---

## Payload Analysis Queries

### 23. Payload Size Analysis

```sql
-- Analyze payload sizes
SELECT
    packet_id,
    LENGTH(payload) as payload_length,
    cpu_intensity,
    ram_intensity,
    processing_time_ms
FROM packet_logs
WHERE payload IS NOT NULL
ORDER BY payload_length DESC
LIMIT 20;
```

### 24. Empty or NULL Payloads

```sql
-- Count empty/null payloads
SELECT
    COUNT(*) as total_records,
    SUM(CASE WHEN payload IS NULL THEN 1 ELSE 0 END) as null_payloads,
    SUM(CASE WHEN payload = '' THEN 1 ELSE 0 END) as empty_payloads,
    SUM(CASE WHEN payload IS NOT NULL AND payload != '' THEN 1 ELSE 0 END) as with_data
FROM packet_logs;
```

### 25. Search in Payloads

```sql
-- Search for specific text in payloads
SELECT
    packet_id,
    payload,
    timestamp
FROM packet_logs
WHERE payload LIKE '%test%'
ORDER BY timestamp DESC
LIMIT 20;
```

---

## Special Character Handling

### 26. Packets with Special Characters

```sql
-- Find packet IDs with special characters
SELECT
    packet_id,
    timestamp
FROM packet_logs
WHERE packet_id ~ '[^a-zA-Z0-9\-_]'
ORDER BY timestamp DESC;
```

### 27. Packets with Unicode Characters

```sql
-- Find packet IDs with non-ASCII characters
SELECT
    packet_id,
    timestamp
FROM packet_logs
WHERE packet_id !~ '^[ -~]+$'
ORDER BY timestamp DESC;
```

---

## Time-Based Queries

### 28. Requests in Last N Minutes

```sql
-- Last 5 minutes
SELECT * FROM packet_logs
WHERE timestamp >= NOW() - INTERVAL '5 minutes'
ORDER BY timestamp DESC;

-- Last 30 minutes
SELECT * FROM packet_logs
WHERE timestamp >= NOW() - INTERVAL '30 minutes'
ORDER BY timestamp DESC;
```

### 29. Time Between Requests

```sql
-- Calculate time gaps between consecutive requests
SELECT
    packet_id,
    timestamp,
    LAG(timestamp) OVER (ORDER BY timestamp) as previous_timestamp,
    EXTRACT(EPOCH FROM (timestamp - LAG(timestamp) OVER (ORDER BY timestamp))) as seconds_since_previous
FROM packet_logs
ORDER BY timestamp DESC
LIMIT 20;
```

### 30. Busiest Time Periods

```sql
-- Find the busiest hours
SELECT
    EXTRACT(HOUR FROM timestamp) as hour,
    COUNT(*) as request_count
FROM packet_logs
GROUP BY EXTRACT(HOUR FROM timestamp)
ORDER BY request_count DESC;
```

---

## Data Cleanup Queries

### 31. Delete Old Records

```sql
-- Delete records older than 7 days (BE CAREFUL!)
-- First, check what would be deleted:
SELECT COUNT(*) FROM packet_logs
WHERE timestamp < NOW() - INTERVAL '7 days';

-- Then delete (uncomment to execute):
-- DELETE FROM packet_logs
-- WHERE timestamp < NOW() - INTERVAL '7 days';
```

### 32. Delete Test Data

```sql
-- Delete test packets (BE CAREFUL!)
-- First, check what would be deleted:
SELECT COUNT(*) FROM packet_logs
WHERE packet_id LIKE 'test-%';

-- Then delete (uncomment to execute):
-- DELETE FROM packet_logs
-- WHERE packet_id LIKE 'test-%';
```

### 33. Truncate All Data

```sql
-- Remove ALL data from table (BE VERY CAREFUL!)
-- TRUNCATE TABLE packet_logs;
```

---

## Database Maintenance

### 34. Table Size and Row Count

```sql
-- Check table size
SELECT
    pg_size_pretty(pg_total_relation_size('packet_logs')) as total_size,
    pg_size_pretty(pg_relation_size('packet_logs')) as table_size,
    pg_size_pretty(pg_indexes_size('packet_logs')) as indexes_size,
    (SELECT COUNT(*) FROM packet_logs) as row_count;
```

### 35. Index Information

```sql
-- List all indexes on packet_logs table
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'packet_logs';
```

### 36. Vacuum and Analyze

```sql
-- Optimize table (reclaim space and update statistics)
VACUUM ANALYZE packet_logs;

-- Just update statistics
ANALYZE packet_logs;
```

---

## Export Data

### 37. Export to CSV

```sql
-- Export all data to CSV (run from psql)
\copy (SELECT * FROM packet_logs ORDER BY timestamp DESC) TO '/tmp/packet_logs.csv' WITH CSV HEADER;

-- Export specific columns
\copy (SELECT packet_id, cpu_intensity, ram_intensity, processing_time_ms, timestamp FROM packet_logs) TO '/tmp/packet_summary.csv' WITH CSV HEADER;

-- Export filtered data
\copy (SELECT * FROM packet_logs WHERE DATE(timestamp) = CURRENT_DATE) TO '/tmp/today_logs.csv' WITH CSV HEADER;
```

---

## Useful PostgreSQL Commands

### Database Management

```sql
-- List all databases
\l

-- Connect to different database
\c mydatabase

-- List all schemas
\dn

-- List all roles/users
\du
```

### Session Information

```sql
-- Current database and user
SELECT current_database(), current_user;

-- Current timestamp
SELECT NOW();

-- PostgreSQL version
SELECT version();
```

### Help Commands

```sql
-- List all psql commands
\?

-- SQL command help
\h SELECT
\h INSERT
\h UPDATE
\h DELETE
```

### Output Formatting

```sql
-- Toggle expanded display (vertical format)
\x

-- Toggle timing of commands
\timing

-- Set null display string
\pset null '[NULL]'

-- Pretty format
\pset format wrapped
```

---

## Common Testing Scenarios

### Scenario 1: Verify API Test Results

After running Insomnia tests, verify the data was saved:

```sql
-- 1. Check total count matches expected
SELECT COUNT(*) FROM packet_logs;

-- 2. Check for your test packet IDs
SELECT packet_id, COUNT(*)
FROM packet_logs
GROUP BY packet_id
ORDER BY COUNT(*) DESC;

-- 3. Verify intensity clamping worked
SELECT packet_id, cpu_intensity, ram_intensity
FROM packet_logs
WHERE packet_id IN ('extreme-cpu', 'extreme-ram', 'negative-test')
ORDER BY timestamp DESC;

-- 4. Check auto-generated packet IDs
SELECT packet_id
FROM packet_logs
WHERE packet_id LIKE 'packet-%'
ORDER BY timestamp DESC;
```

### Scenario 2: Performance Testing Verification

After running high-intensity tests:

```sql
-- Check if processing time increased with intensity
SELECT
    cpu_intensity,
    ram_intensity,
    AVG(processing_time_ms) as avg_time,
    MAX(processing_time_ms) as max_time
FROM packet_logs
GROUP BY cpu_intensity, ram_intensity
ORDER BY cpu_intensity DESC, ram_intensity DESC;
```

### Scenario 3: Concurrent Request Testing

After sending multiple concurrent requests:

```sql
-- Check for requests processed around the same time
SELECT
    packet_id,
    timestamp,
    processing_time_ms
FROM packet_logs
WHERE timestamp >= NOW() - INTERVAL '5 minutes'
ORDER BY timestamp;

-- Check for any gaps or issues
SELECT
    COUNT(DISTINCT packet_id) as unique_packets,
    COUNT(*) as total_logs,
    MIN(timestamp) as first_request,
    MAX(timestamp) as last_request,
    EXTRACT(EPOCH FROM (MAX(timestamp) - MIN(timestamp))) as duration_seconds
FROM packet_logs
WHERE timestamp >= NOW() - INTERVAL '5 minutes';
```

### Scenario 4: Data Integrity Check

Complete data validation:

```sql
-- Comprehensive integrity check
SELECT
    'Total Records' as metric,
    COUNT(*)::text as value
FROM packet_logs

UNION ALL

SELECT
    'NULL packet_id',
    COUNT(*)::text
FROM packet_logs
WHERE packet_id IS NULL

UNION ALL

SELECT
    'Invalid CPU intensity',
    COUNT(*)::text
FROM packet_logs
WHERE cpu_intensity NOT BETWEEN 1 AND 10

UNION ALL

SELECT
    'Invalid RAM intensity',
    COUNT(*)::text
FROM packet_logs
WHERE ram_intensity NOT BETWEEN 1 AND 10

UNION ALL

SELECT
    'Negative processing time',
    COUNT(*)::text
FROM packet_logs
WHERE processing_time_ms < 0

UNION ALL

SELECT
    'Records last hour',
    COUNT(*)::text
FROM packet_logs
WHERE timestamp >= NOW() - INTERVAL '1 hour';
```

---

## Quick Reference Sheet

```sql
-- ===== MOST USED QUERIES =====

-- View recent logs
SELECT * FROM packet_logs ORDER BY timestamp DESC LIMIT 20;

-- Count all logs
SELECT COUNT(*) FROM packet_logs;

-- Find specific packet
SELECT * FROM packet_logs WHERE packet_id = 'your-packet-id';

-- Group by packet ID
SELECT packet_id, COUNT(*) FROM packet_logs GROUP BY packet_id;

-- Average processing time by intensity
SELECT cpu_intensity, ram_intensity, AVG(processing_time_ms)
FROM packet_logs
GROUP BY cpu_intensity, ram_intensity;

-- Last hour's activity
SELECT * FROM packet_logs
WHERE timestamp >= NOW() - INTERVAL '1 hour'
ORDER BY timestamp DESC;

-- Delete test data (BE CAREFUL!)
DELETE FROM packet_logs WHERE packet_id LIKE 'test-%';

-- Exit psql
\q
```

---

## Pro Tips

1. **Use `\x` for better readability** when viewing single records:
   ```sql
   \x
   SELECT * FROM packet_logs LIMIT 1;
   \x
   ```

2. **Enable timing** to measure query performance:
   ```sql
   \timing
   SELECT COUNT(*) FROM packet_logs;
   ```

3. **Save frequently used queries** in a `.sql` file and execute:
   ```sql
   \i /path/to/queries.sql
   ```

4. **Use EXPLAIN** to understand query performance:
   ```sql
   EXPLAIN ANALYZE
   SELECT * FROM packet_logs WHERE cpu_intensity > 8;
   ```

5. **Create views** for complex queries you run often:
   ```sql
   CREATE VIEW recent_high_intensity AS
   SELECT * FROM packet_logs
   WHERE cpu_intensity >= 8 OR ram_intensity >= 8
   ORDER BY timestamp DESC;

   -- Then use it:
   SELECT * FROM recent_high_intensity LIMIT 10;
   ```

---

## Troubleshooting SQL Issues

### Can't connect to database

```bash
# Check if PostgreSQL container is running
docker compose ps postgres

# Check PostgreSQL logs
docker compose logs postgres

# Restart PostgreSQL
docker compose restart postgres
```

### Query too slow

```sql
-- Check if indexes exist
\di

-- Analyze query plan
EXPLAIN ANALYZE your_query_here;

-- Update table statistics
ANALYZE packet_logs;
```

### Out of memory

```sql
-- Limit result size
SELECT * FROM packet_logs LIMIT 100;

-- Use pagination
SELECT * FROM packet_logs
ORDER BY id
LIMIT 100 OFFSET 0;  -- Next page: OFFSET 100
```

---

## Safety Reminders

⚠️ **ALWAYS** preview DELETE queries with SELECT first:
```sql
-- Preview what would be deleted
SELECT * FROM packet_logs WHERE condition;

-- Only then delete
DELETE FROM packet_logs WHERE condition;
```

⚠️ **BACKUP** before bulk operations:
```sql
-- Create backup table
CREATE TABLE packet_logs_backup AS
SELECT * FROM packet_logs;

-- Then perform risky operation

-- If needed, restore:
-- DROP TABLE packet_logs;
-- ALTER TABLE packet_logs_backup RENAME TO packet_logs;
```

⚠️ **Use transactions** for multi-step operations:
```sql
BEGIN;
-- Your queries here
-- If something wrong: ROLLBACK;
-- If all good: COMMIT;
```

---

Happy Querying! 📊
