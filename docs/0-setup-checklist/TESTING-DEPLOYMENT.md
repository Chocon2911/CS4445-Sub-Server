# Testing Deployment - Verification Guide

**Verify your deployment is working correctly on all servers.**

## 🎯 What We'll Test

1. ✅ GitHub Actions workflow runs successfully
2. ✅ Application deploys to all servers
3. ✅ Health checks pass on all servers
4. ✅ API endpoints work correctly
5. ✅ Monitoring (Grafana) shows data
6. ✅ Database stores data correctly

---

## 📋 Phase 1: Trigger Deployment

### Option A: Staging Deployment (Automatic)

**Triggers when:** You push to `main` branch

```bash
# Make a small change
echo "# Test deployment" >> README.md

# Commit and push
git add README.md
git commit -m "test: trigger staging deployment"
git push origin main
```

### Option B: Production Deployment (Manual/Tag)

**Triggers when:** You create a version tag

```bash
# Create and push tag
git tag v1.0.0
git push origin v1.0.0
```

---

## 📋 Phase 2: Monitor GitHub Actions

### Step 1: Open GitHub Actions Page

1. Go to your repository: `https://github.com/YOUR_USERNAME/CS4445-Sub-Server`
2. Click **Actions** tab (top menu)
3. You should see a new workflow run

### Step 2: Watch the Workflow

**Workflow stages:**

```
1. Build and Push Image
   ├── Checkout code
   ├── Set up Docker Buildx
   ├── Login to GitHub Container Registry
   ├── Build and push Docker image
   └── ✅ Should take ~5-8 minutes

2. Deploy to Staging
   ├── Parse server IPs
   ├── Deploy to Server 1 ─┐
   ├── Deploy to Server 2  ├─ Parallel deployment
   ├── Deploy to Server 3 ─┘
   ├── Health check Server 1 ─┐
   ├── Health check Server 2  ├─ Verify all healthy
   ├── Health check Server 3 ─┘
   └── ✅ Should take ~2-3 minutes

3. (If production) Deploy to Production
   ├── Blue Phase: Deploy to 50% of servers
   ├── Health check Blue servers
   ├── Wait 30 seconds
   ├── Green Phase: Deploy to remaining 50%
   ├── Health check Green servers
   └── ✅ Should take ~4-5 minutes
```

### Step 3: Check for Success

**✅ Success indicators:**
- All steps have green checkmarks ✅
- No red X marks ❌
- Workflow shows "Success" badge

**❌ If workflow fails:**
- Click on the failed step to see error details
- See [Troubleshooting](#troubleshooting) section below

---

## 📋 Phase 3: Verify Each Server

**Run these checks for EVERY server** (both staging and production)

### Check 1: Application Health

**What it tests:** Application is running and responsive

```bash
# Replace with your server's hostname and app port
curl http://n1.ckey.vn:3497/actuator/health

# Expected response:
{
  "status": "UP"
}
```

**✅ Pass:** Returns `{"status":"UP"}`
**❌ Fail:** No response, connection refused, or status is "DOWN"

**Run for all servers:**
```bash
# Server 1
curl http://n1.ckey.vn:3497/actuator/health

# Server 2
curl http://n2.ckey.vn:3498/actuator/health

# Server 3
curl http://n3.ckey.vn:3499/actuator/health

# ... repeat for all servers
```

---

### Check 2: Docker Containers Running

**What it tests:** All containers are up and running

```bash
# SSH to server and check containers
ssh -p 3494 -i ~/.ssh/ckey-deploy root@n1.ckey.vn "docker ps"

# Expected output shows these containers:
# - cs4445-app
# - cs4445-postgres
# - cs4445-prometheus
# - cs4445-grafana
```

**✅ Pass:** All 4 containers show "Up" status
**❌ Fail:** Any container missing or status is "Exited"

**Quick check all servers:**
```bash
# Create a simple script
for server in "n1.ckey.vn:3494" "n2.ckey.vn:3495" "n3.ckey.vn:3496"; do
  IFS=':' read -r host port <<< "$server"
  echo "=== $host ==="
  ssh -p $port -i ~/.ssh/ckey-deploy root@$host "docker ps --format 'table {{.Names}}\t{{.Status}}'"
  echo ""
done
```

---

### Check 3: Application Logs Clean

**What it tests:** No errors in application logs

```bash
# Check last 50 lines of app logs
ssh -p 3494 -i ~/.ssh/ckey-deploy root@n1.ckey.vn "docker logs cs4445-app --tail 50"
```

**✅ Pass:** Logs show normal startup messages, no ERROR or EXCEPTION
**❌ Fail:** Logs contain ERROR, EXCEPTION, or FATAL messages

**What to look for:**
```
✅ Good indicators:
- "Started Cs4445SubServerApplication in X.X seconds"
- "Tomcat started on port(s): 8080"
- "HikariPool-1 - Start completed"

❌ Bad indicators:
- "ERROR"
- "Exception"
- "Failed to"
- "Connection refused"
```

---

### Check 4: Database Connection

**What it tests:** Application can connect to PostgreSQL

```bash
# Check database container is healthy
ssh -p 3494 -i ~/.ssh/ckey-deploy root@n1.ckey.vn "docker exec cs4445-postgres pg_isready -U myuser"

# Expected response:
# /var/run/postgresql:5432 - accepting connections
```

**✅ Pass:** Shows "accepting connections"
**❌ Fail:** Shows "no response" or error

---

## 📋 Phase 4: Test API Endpoints

### Test 1: Simple Health Check

```bash
curl http://n1.ckey.vn:3497/api/v1/health

# Expected response:
Server is running
```

---

### Test 2: Create Fake Packet (Low Load)

```bash
curl -X POST http://n1.ckey.vn:3497/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{
    "packetId": "test-001",
    "cpuIntensity": 2,
    "ramIntensity": 2,
    "processingTimeMs": 500,
    "payload": "deployment test"
  }'

# Expected response (example):
{
  "packetId": "test-001",
  "status": "SUCCESS",
  "processingTimeMs": 523,
  "cpuCycles": 50000,
  "memoryUsedBytes": 20971520,
  "result": "Packet processed...",
  "timestamp": "2025-12-15T10:30:45.123"
}
```

**✅ Pass:** Returns status "SUCCESS"
**❌ Fail:** Returns error or status "FAILED"

---

### Test 3: Server Control API

```bash
# Test 1: Check server status
curl http://n1.ckey.vn:3497/api/v1/server/status

# Expected response:
{
  "open": true,
  "status": "OPEN",
  "lastStateChange": "2025-12-15T10:00:00",
  "reason": "Server started"
}

# Test 2: Close server
curl -X POST "http://n1.ckey.vn:3497/api/v1/server/close?reason=Testing"

# Expected response:
{
  "open": false,
  "status": "CLOSED",
  "lastStateChange": "2025-12-15T10:35:00",
  "reason": "Testing"
}

# Test 3: Try to send packet (should be rejected)
curl -X POST http://n1.ckey.vn:3497/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"test-closed","cpuIntensity":5,"ramIntensity":5}'

# Expected response (HTTP 503):
{
  "packetId": "test-closed",
  "status": "REJECTED",
  "result": "Server is closed",
  ...
}

# Test 4: Reopen server
curl -X POST "http://n1.ckey.vn:3497/api/v1/server/open?reason=Testing+complete"

# Expected response:
{
  "open": true,
  "status": "OPEN",
  "lastStateChange": "2025-12-15T10:36:00",
  "reason": "Testing complete"
}
```

**✅ Pass:** All 4 tests return expected responses
**❌ Fail:** Any test returns error or unexpected response

---

## 📋 Phase 5: Test Monitoring

### Test 1: Access Grafana

1. Open browser: `http://n1.ckey.vn:3495` (use your Grafana port)
2. Login:
   - Username: `admin`
   - Password: `admin` (or your `GRAFANA_PASSWORD`)
3. Should see Grafana home page

**✅ Pass:** Grafana loads and you can log in
**❌ Fail:** Connection refused or login fails

---

### Test 2: Check Dashboard

1. Click **Dashboards** (left menu)
2. Look for **CS4445 Sub Server** dashboard
3. Click to open it

**✅ Pass:** Dashboard shows data (graphs with values)
**❌ Fail:** Dashboard is empty or shows "No data"

**Expected panels:**
- CPU Usage (should show percentage)
- JVM Memory Usage (should show bytes)
- HTTP Requests Rate (should show requests/sec)
- Request Duration (should show milliseconds)
- Database Connections (should show count)
- Application Status (should show "UP")

---

### Test 3: Verify Prometheus

1. Open browser: `http://n1.ckey.vn:3498` (use your Prometheus port)
2. Should see Prometheus UI
3. Click **Status** → **Targets**
4. Look for `spring-boot-app` target

**✅ Pass:** Target shows "UP" status
**❌ Fail:** Target shows "DOWN" or missing

---

## 📋 Phase 6: Test Database Persistence

### Test 1: Create Packets

```bash
# Send 3 test packets
for i in {1..3}; do
  curl -X POST http://n1.ckey.vn:3497/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d "{
      \"packetId\": \"db-test-$i\",
      \"cpuIntensity\": 3,
      \"ramIntensity\": 3,
      \"processingTimeMs\": 500,
      \"payload\": \"database test $i\"
    }"
  echo ""
done
```

---

### Test 2: Verify Data in Database

```bash
# Connect to database
ssh -p 3494 -i ~/.ssh/ckey-deploy root@n1.ckey.vn \
  "docker exec -it cs4445-postgres psql -U myuser -d mydatabase"

# Run query
SELECT packet_id, cpu_intensity, ram_intensity, timestamp
FROM packet_logs
ORDER BY timestamp DESC
LIMIT 5;

# Should show your 3 test packets
# Exit with: \q
```

**✅ Pass:** Your test packets are in the database
**❌ Fail:** No data or query errors

---

## 📋 Phase 7: Load Test (Optional)

### Stress Test: Multiple Concurrent Requests

```bash
# Send 20 concurrent requests
for i in {1..20}; do
  curl -X POST http://n1.ckey.vn:3497/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d "{
      \"packetId\": \"load-test-$i\",
      \"cpuIntensity\": 5,
      \"ramIntensity\": 5,
      \"processingTimeMs\": 1000,
      \"payload\": \"stress test\"
    }" &
done

# Wait for all to complete
wait

echo "Load test complete!"
```

**Monitor during load test:**
1. Watch Grafana dashboard in real-time
2. Check CPU and Memory usage increase
3. Verify all requests succeed

**✅ Pass:** All 20 requests return status "SUCCESS"
**❌ Fail:** Any requests fail or timeout

---

## ✅ Final Verification Checklist

**All tests passed when:**

- [ ] **GitHub Actions workflow:** ✅ Green checkmarks on all steps
- [ ] **Health checks:** ✅ All servers return `{"status":"UP"}`
- [ ] **Docker containers:** ✅ All 4 containers running on each server
- [ ] **Application logs:** ✅ No errors, clean startup messages
- [ ] **Database:** ✅ Accepting connections
- [ ] **API endpoints:** ✅ All tests return expected responses
- [ ] **Server control:** ✅ Can open/close server correctly
- [ ] **Grafana:** ✅ Dashboard loads with data
- [ ] **Prometheus:** ✅ Targets show "UP"
- [ ] **Database persistence:** ✅ Data is saved and queryable
- [ ] **Load test:** ✅ Handles multiple concurrent requests

**🎉 If all checked:** Your deployment is working perfectly!

---

## 🐛 Troubleshooting

### Issue: GitHub Actions Fails at "Build and Push"

**Error:** "permission denied" or "authentication failed"

**Solution:**
1. Check GitHub Actions permissions:
   - Settings → Actions → General
   - Enable "Read and write permissions"
2. Verify workflow file has correct registry URL

---

### Issue: GitHub Actions Fails at "Deploy to Staging"

**Error:** "Permission denied (publickey)"

**Causes & Solutions:**

**Cause 1:** SSH key in GitHub doesn't match servers
```bash
# Re-add SSH key to GitHub
gh secret set DEPLOY_SSH_KEY < ~/.ssh/ckey-deploy

# Verify public key is on all servers
ssh -p PORT -i ~/.ssh/ckey-deploy root@HOST "cat ~/.ssh/authorized_keys"
```

**Cause 2:** Wrong format in STAGING_SERVERS
```bash
# Verify format (no spaces!)
# CORRECT:
n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498

# WRONG:
n1.ckey.vn:3494:3497, n2.ckey.vn:3495:3498  # Space after comma
```

**Cause 3:** Wrong SSH port
```bash
# Make sure you're using port that maps to 22
# Check CKey panel port mappings
```

---

### Issue: Health Check Fails

**Error:** "Connection refused" or no response

**Check 1: Is container running?**
```bash
ssh -p PORT -i ~/.ssh/ckey-deploy root@HOST "docker ps | grep cs4445-app"
# Should show container with "Up" status
```

**Check 2: Is port correct?**
```bash
# Make sure you're using APP port (maps to 8080), not SSH port!
# Check CKey panel port mappings
```

**Check 3: Check application logs**
```bash
ssh -p PORT -i ~/.ssh/ckey-deploy root@HOST "docker logs cs4445-app --tail 100"
# Look for errors
```

**Check 4: Restart application**
```bash
ssh -p PORT -i ~/.ssh/ckey-deploy root@HOST \
  "cd /app/cs4445-sub-server && docker compose restart app"

# Wait 30 seconds, then test health again
sleep 30
curl http://HOST:APP_PORT/actuator/health
```

---

### Issue: API Returns 500 Error

**Check database connection:**
```bash
# 1. Is postgres running?
ssh -p PORT -i ~/.ssh/ckey-deploy root@HOST "docker ps | grep postgres"

# 2. Can app connect to database?
ssh -p PORT -i ~/.ssh/ckey-deploy root@HOST \
  "docker logs cs4445-app | grep -i 'hikari\|database\|postgres'"

# 3. Restart database and app
ssh -p PORT -i ~/.ssh/ckey-deploy root@HOST \
  "cd /app/cs4445-sub-server && docker compose restart postgres app"
```

---

### Issue: Grafana Shows "No Data"

**Check 1: Is Prometheus scraping?**
```bash
# Open Prometheus targets page
# http://YOUR_HOST:PROMETHEUS_PORT/targets

# spring-boot-app should show "UP"
```

**Check 2: Is app exposing metrics?**
```bash
curl http://YOUR_HOST:APP_PORT/actuator/prometheus
# Should return lots of metrics in text format
```

**Check 3: Check Prometheus config**
```bash
ssh -p PORT -i ~/.ssh/ckey-deploy root@HOST \
  "docker exec cs4445-prometheus cat /etc/prometheus/prometheus.yml"

# Should show:
# - targets: ['app:8080']
```

**Solution: Restart monitoring stack**
```bash
ssh -p PORT -i ~/.ssh/ckey-deploy root@HOST \
  "cd /app/cs4445-sub-server && docker compose restart prometheus grafana"
```

---

### Issue: Database Query Fails

**Error:** "relation does not exist" or "permission denied"

**Solution: Recreate database schema**
```bash
# Connect to server
ssh -p PORT -i ~/.ssh/ckey-deploy root@HOST

# Stop app, reset database, restart
cd /app/cs4445-sub-server
docker compose stop app
docker compose down postgres
docker compose up -d postgres
sleep 10  # Wait for postgres to be ready
docker compose up -d app
```

---

## 📊 Test Results Template

**Copy this and fill in your results:**

```
# Deployment Test Results
Date: ___________
Tested by: ___________

## GitHub Actions
- [ ] Build and Push: ✅ / ❌
- [ ] Deploy to Staging: ✅ / ❌
- [ ] Deploy to Production: ✅ / ❌

## Staging Servers
Server 1 (n1.ckey.vn):
- [ ] Health check: ✅ / ❌
- [ ] Containers running: ✅ / ❌
- [ ] API works: ✅ / ❌
- [ ] Grafana accessible: ✅ / ❌

Server 2 (n2.ckey.vn):
- [ ] Health check: ✅ / ❌
- [ ] Containers running: ✅ / ❌
- [ ] API works: ✅ / ❌
- [ ] Grafana accessible: ✅ / ❌

Server 3 (n3.ckey.vn):
- [ ] Health check: ✅ / ❌
- [ ] Containers running: ✅ / ❌
- [ ] API works: ✅ / ❌
- [ ] Grafana accessible: ✅ / ❌

## Production Servers
Server 1 (n4.ckey.vn):
- [ ] Health check: ✅ / ❌
- [ ] Containers running: ✅ / ❌
- [ ] API works: ✅ / ❌

Server 2 (n5.ckey.vn):
- [ ] Health check: ✅ / ❌
- [ ] Containers running: ✅ / ❌
- [ ] API works: ✅ / ❌

## Overall Result
- [ ] All tests passed ✅
- [ ] Some tests failed ❌ (list below)

Issues found:
1. ___________
2. ___________

Notes:
___________
```

---

## 📚 Related Guides

- [NEW-SERVER-SETUP.md](./NEW-SERVER-SETUP.md) - Server setup instructions
- [GITHUB-SECRETS-SETUP.md](./GITHUB-SECRETS-SETUP.md) - GitHub configuration
- [QUICK-START-CHECKLIST.md](./QUICK-START-CHECKLIST.md) - Complete setup checklist

---

**Version:** 1.0
**Created:** 2025-12-15
**For:** CS4445 Sub Server
**Deployment:** V2.1 (Combined Server Format)
