# CKey.com Server Deployment Guide
## CS4445 Sub Server - Step-by-Step Setup

This guide provides **exact commands** to deploy the CS4445 Sub Server on a CKey.com VPS from scratch.

---

## Prerequisites

Before starting, ensure you have:
- ✅ Root access to your CKey.com VPS
- ✅ SSH connection to the server
- ✅ Your application Docker image ready (or source code to build)

---

## Part 1: Docker Installation

### Step 1.1: Update System Packages

```bash
apt-get update
```

**Expected output:** Package lists will update successfully.

---

### Step 1.2: Install Required Prerequisites

```bash
apt-get install -y ca-certificates curl gnupg lsb-release
```

**Expected output:** Packages installed successfully.

---

### Step 1.3: Add Docker's Official GPG Key

```bash
mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
```

**Expected output:** Key file created at `/etc/apt/keyrings/docker.gpg`

---

### Step 1.4: Set Up Docker Repository

```bash
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
```

**Expected output:** Repository configuration added to `/etc/apt/sources.list.d/docker.list`

---

### Step 1.5: Install Docker Engine

```bash
apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

**Expected output:** Docker components installed successfully.

**⏱️ Time:** ~2-5 minutes depending on network speed

---

### Step 1.6: Verify Docker Installation

```bash
docker --version
```

**Expected output:**
```
Docker version 27.x.x, build xxxxxxx
```

✅ **Checkpoint:** If you see the Docker version, installation succeeded!

---

## Part 2: Configure Docker for CKey.com Environment

### Step 2.1: Create Docker Configuration Directory

```bash
mkdir -p /etc/docker
```

---

### Step 2.2: Create Docker Daemon Configuration

**Copy and paste this entire block:**

```bash
cat > /etc/docker/daemon.json << 'EOF'
{
  "iptables": false,
  "ip-forward": false,
  "ip-masq": false,
  "bridge": "none",
  "storage-driver": "vfs"
}
EOF
```

---

### Step 2.3: Verify Configuration File

```bash
cat /etc/docker/daemon.json
```

**Expected output:**
```json
{
  "iptables": false,
  "ip-forward": false,
  "ip-masq": false,
  "bridge": "none",
  "storage-driver": "vfs"
}
```

✅ **Checkpoint:** Configuration file created correctly!

---

## Part 3: Start Docker Daemon

### Step 3.1: Stop Any Existing Docker Processes

```bash
pkill -9 dockerd 2>/dev/null || true
pkill -9 containerd 2>/dev/null || true
sleep 3
```

**Note:** It's normal if you see "no process found" - this just cleans up any existing processes.

---

### Step 3.2: Start Docker Daemon

```bash
dockerd > /tmp/docker.log 2>&1 &
```

**Expected output:** Process started in background (you'll see a process ID number)

---

### Step 3.3: Wait for Docker to Initialize

```bash
sleep 15
```

**⏱️ Wait:** 15 seconds for Docker to fully start up.

---

### Step 3.4: Verify Docker is Running

```bash
docker version
```

**Expected output:**
```
Client: Docker Engine - Community
 Version:           27.x.x
 ...

Server: Docker Engine - Community
 Engine:
  Version:          27.x.x
  ...
```

✅ **Checkpoint:** If you see both Client AND Server sections, Docker is running!

---

### Step 3.5: Check Docker System Info

```bash
docker info
```

**Look for these key values:**
- `Storage Driver: vfs` ✅
- `Iptables: false` ✅
- No error messages ✅

---

### Step 3.6: Test Docker with Hello World

```bash
docker run --network host hello-world
```

**Expected output:**
```
Hello from Docker!
This message shows that your installation appears to be working correctly.
```

✅ **Checkpoint:** Docker is fully functional!

---

## Part 4: Deploy PostgreSQL Database

### Step 4.1: Pull PostgreSQL Image

```bash
docker pull postgres:16-alpine
```

**⏱️ Time:** ~1-2 minutes

---

### Step 4.2: Start PostgreSQL Container

```bash
docker run -d \
  --name postgres \
  --network host \
  --restart unless-stopped \
  -e POSTGRES_USER=myuser \
  -e POSTGRES_PASSWORD=secret \
  -e POSTGRES_DB=mydatabase \
  -v /var/lib/postgresql/data:/var/lib/postgresql/data \
  postgres:16-alpine
```

**Expected output:** Container ID (long alphanumeric string)

---

### Step 4.3: Verify PostgreSQL is Running

```bash
docker ps
```

**Expected output:**
```
CONTAINER ID   IMAGE                COMMAND                  STATUS         PORTS     NAMES
xxxxxxxxxxxx   postgres:16-alpine   "docker-entrypoint.s…"   Up X seconds             postgres
```

---

### Step 4.4: Check PostgreSQL Logs

```bash
docker logs postgres
```

**Look for:** `database system is ready to accept connections`

✅ **Checkpoint:** PostgreSQL is ready!

---

## Part 5: Deploy CS4445 Sub Server Application

### Option A: Build from Source Code

#### Step 5A.1: Install Git (if needed)

```bash
apt-get install -y git
```

---

#### Step 5A.2: Clone Repository

```bash
cd /root
git clone <your-repository-url> cs4445-app
cd cs4445-app
```

**Replace** `<your-repository-url>` with your actual Git repository URL.

---

#### Step 5A.3: Build Docker Image

```bash
docker build -t cs4445-sub-server:latest .
```

**⏱️ Time:** ~5-10 minutes (first build)

**Expected output:** `Successfully tagged cs4445-sub-server:latest`

---

### Option B: Pull Pre-Built Image

```bash
docker pull <your-registry>/cs4445-sub-server:latest
docker tag <your-registry>/cs4445-sub-server:latest cs4445-sub-server:latest
```

**Replace** `<your-registry>` with your Docker registry URL (e.g., Docker Hub username).

---

### Step 5.5: Create Application Configuration

Create environment variables file:

```bash
cat > /root/app.env << 'EOF'
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mydatabase
SPRING_DATASOURCE_USERNAME=myuser
SPRING_DATASOURCE_PASSWORD=secret
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
EOF
```

---

### Step 5.6: Start Application Container

```bash
docker run -d \
  --name cs4445-sub-server \
  --network host \
  --restart unless-stopped \
  --env-file /root/app.env \
  cs4445-sub-server:latest
```

**Expected output:** Container ID

---

### Step 5.7: Monitor Application Startup

```bash
docker logs -f cs4445-sub-server
```

**Look for:**
```
Started Cs4445SubServerApplication in X.XXX seconds
```

**Press Ctrl+C to exit** the log view.

✅ **Checkpoint:** Application is running!

---

## Part 6: Verification and Testing

### Step 6.1: Check All Running Containers

```bash
docker ps
```

**Expected output:** Both `postgres` and `cs4445-sub-server` containers running.

---

### Step 6.2: Test Health Endpoint

```bash
curl http://localhost:8080/actuator/health
```

**Expected output:**
```json
{"status":"UP"}
```

---

### Step 6.3: Check Application Metrics

```bash
curl http://localhost:8080/actuator/prometheus
```

**Expected output:** Prometheus metrics data.

---

### Step 6.4: Test from External Network

From your **local machine** (not the server):

```bash
curl http://<your-server-ip>:8080/actuator/health
```

**Replace** `<your-server-ip>` with your CKey.com server's public IP address.

✅ **Success:** If you get `{"status":"UP"}`, your application is publicly accessible!

---

## Part 7: Monitoring and Maintenance

### View Application Logs

```bash
# View last 100 lines
docker logs --tail 100 cs4445-sub-server

# Follow logs in real-time
docker logs -f cs4445-sub-server

# View logs with timestamps
docker logs -t cs4445-sub-server
```

---

### View Database Logs

```bash
docker logs --tail 100 postgres
```

---

### Check Container Resource Usage

```bash
docker stats
```

**Press Ctrl+C to exit**

---

### Restart Application

```bash
docker restart cs4445-sub-server
```

---

### Restart Database

```bash
docker restart postgres
```

---

### Stop Application

```bash
docker stop cs4445-sub-server
```

---

### Start Application

```bash
docker start cs4445-sub-server
```

---

## Part 8: Server Reboot Recovery

**IMPORTANT:** After server reboot, Docker daemon does NOT auto-start on CKey.com.

### Create Startup Script

```bash
cat > /root/start-docker.sh << 'EOF'
#!/bin/bash
echo "Starting Docker daemon..."
pkill -9 dockerd 2>/dev/null || true
pkill -9 containerd 2>/dev/null || true
sleep 3
dockerd > /tmp/docker.log 2>&1 &
sleep 15
echo "Docker daemon started!"
docker ps
EOF

chmod +x /root/start-docker.sh
```

---

### After Each Reboot, Run:

```bash
/root/start-docker.sh
```

**Note:** Containers with `--restart unless-stopped` will auto-start once Docker daemon is running.

---

## Part 9: Application Updates

### Update to New Version

```bash
# Step 1: Pull/build new image
docker pull <your-registry>/cs4445-sub-server:latest
# OR
cd /root/cs4445-app && git pull && docker build -t cs4445-sub-server:latest .

# Step 2: Stop old container
docker stop cs4445-sub-server

# Step 3: Remove old container (keeps data in database)
docker rm cs4445-sub-server

# Step 4: Start new container
docker run -d \
  --name cs4445-sub-server \
  --network host \
  --restart unless-stopped \
  --env-file /root/app.env \
  cs4445-sub-server:latest

# Step 5: Check logs
docker logs -f cs4445-sub-server
```

---

## Part 10: Troubleshooting

### Problem: Docker daemon not responding

```bash
# Check if dockerd is running
ps aux | grep dockerd

# If not running, start it
pkill -9 dockerd containerd
sleep 3
dockerd > /tmp/docker.log 2>&1 &
sleep 15
```

---

### Problem: Container exits immediately

```bash
# Check exit code and logs
docker ps -a
docker logs <container-name>

# Common issues:
# - Missing environment variables
# - Database connection failure
# - Port already in use
```

---

### Problem: Cannot connect to application

```bash
# Check if container is running
docker ps

# Check what's listening on port 8080
netstat -tulpn | grep 8080

# Check application logs
docker logs cs4445-sub-server

# Test local connection
curl http://localhost:8080/actuator/health
```

---

### Problem: Database connection failed

```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check PostgreSQL logs
docker logs postgres

# Test database connection
docker exec -it postgres psql -U myuser -d mydatabase
# Type \q to exit psql
```

---

### View Docker Daemon Logs

```bash
tail -f /tmp/docker.log
```

---

## Quick Reference Commands

### Essential Commands

```bash
# Start Docker daemon
dockerd > /tmp/docker.log 2>&1 &

# Check running containers
docker ps

# View logs
docker logs -f cs4445-sub-server

# Restart application
docker restart cs4445-sub-server

# Stop application
docker stop cs4445-sub-server

# Start application
docker start cs4445-sub-server

# Remove stopped containers
docker container prune

# View system status
docker info

# View resource usage
docker stats
```

---

## Important Notes

### ⚠️ Critical Requirements for CKey.com

1. **Always use `--network host`** for all containers
2. **Database connections use `localhost`** not container names
3. **Docker daemon must be manually started** after each server reboot
4. **No bridge networking** - containers share host network namespace
5. **Ports are directly exposed** - container port 8080 = host port 8080

---

## Security Considerations

### Change Default Credentials

Before production use, update these in `/root/app.env`:

```bash
SPRING_DATASOURCE_PASSWORD=<strong-password-here>
```

And update the PostgreSQL container:

```bash
docker stop postgres
docker rm postgres
docker run -d \
  --name postgres \
  --network host \
  --restart unless-stopped \
  -e POSTGRES_USER=myuser \
  -e POSTGRES_PASSWORD=<strong-password-here> \
  -e POSTGRES_DB=mydatabase \
  -v /var/lib/postgresql/data:/var/lib/postgresql/data \
  postgres:16-alpine
```

---

## Next Steps

After successful deployment:

1. ✅ Configure SSL/TLS certificates (Let's Encrypt)
2. ✅ Set up automated backups for PostgreSQL data
3. ✅ Configure monitoring and alerting
4. ✅ Set up log rotation for `/tmp/docker.log`
5. ✅ Document your specific API endpoints
6. ✅ Create integration tests for your deployed application

---

## Support

For issues specific to:
- **CKey.com infrastructure:** Contact CKey.com support
- **Docker configuration:** See `docs/SERVER-DEPLOYMENT-TROUBLESHOOTING.md`
- **Application issues:** Check application logs with `docker logs cs4445-sub-server`

---

**Document Version:** 1.0
**Last Updated:** 2025-12-14
**Environment:** CKey.com VPS (Ubuntu 22.04, Container-based)
**Author:** CS4445 Team

---

## Completion Checklist

Use this checklist to track your deployment progress:

- [ ] Part 1: Docker Installation Complete
- [ ] Part 2: Docker Configuration Complete
- [ ] Part 3: Docker Daemon Running
- [ ] Part 4: PostgreSQL Deployed
- [ ] Part 5: Application Deployed
- [ ] Part 6: Verification Tests Passed
- [ ] Part 7: Monitoring Setup
- [ ] Part 8: Reboot Script Created
- [ ] Changed default passwords
- [ ] Application accessible from internet

**🎉 All done? Congratulations! Your CS4445 Sub Server is deployed!**
