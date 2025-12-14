# Server Deployment Troubleshooting Guide

This document contains troubleshooting steps for deploying the CS4445 Sub Server application to CKey.com VPS servers.

## Table of Contents
- [Environment Overview](#environment-overview)
- [Issue 1: Docker Command Not Found](#issue-1-docker-command-not-found)
- [Issue 2: Docker Daemon Permission Errors](#issue-2-docker-daemon-permission-errors)
- [Issue 3: iptables Permission Denied](#issue-3-iptables-permission-denied)
- [Issue 4: systemd Not Available](#issue-4-systemd-not-available)
- [Final Working Configuration](#final-working-configuration)
- [Running Containers on CKey.com](#running-containers-on-ckey-com)
- [Monitoring Commands](#monitoring-commands)

---

## Environment Overview

### CKey.com VPS Characteristics
- **Type**: Containerized VPS (container-based virtualization)
- **OS**: Ubuntu 22.04 LTS (Jammy)
- **Init System**: SysV init (not systemd)
- **Restrictions**:
  - No iptables/nftables support (permission denied)
  - No systemd
  - Limited mount propagation
  - No direct kernel access

### Why These Restrictions Exist
CKey.com uses container-based virtualization where each VPS runs inside a container. This provides isolation but limits certain privileged operations like:
- Network filtering (iptables/nftables)
- Systemd operations
- Kernel module loading
- Some filesystem operations

---

## Issue 1: Docker Command Not Found

### Problem
```bash
root@O-1472877:~# docker version
-bash: docker: command not found
```

### Root Cause
Docker was not installed on the server.

### Solution
Install Docker using the official Docker repository:

```bash
# Update package index
apt-get update

# Install prerequisites
apt-get install -y ca-certificates curl gnupg lsb-release

# Add Docker's official GPG key
mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Set up the repository
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker Engine
apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

### Verification
```bash
docker --version
# Expected: Docker version 27.x.x or higher
```

---

## Issue 2: Docker Daemon Permission Errors

### Problem
```bash
Cannot connect to the Docker daemon at unix:///var/run/docker.sock. Is the docker daemon running?
```

### Root Cause
Docker daemon was not running after installation.

### Solution Attempts

#### Attempt 1: Using systemctl (FAILED)
```bash
systemctl start docker
```

**Error:**
```
System has not been booted with systemd as init system (PID 1). Can't operate.
Failed to connect to bus: Host is down
```

**Why it failed:** CKey.com uses SysV init, not systemd.

#### Attempt 2: Using service command (FAILED)
```bash
service docker start
```

**Error:**
```
docker: unrecognized service
```

**Why it failed:** Docker service script not properly configured for SysV init.

#### Attempt 3: Manual daemon start (PARTIALLY SUCCESSFUL)
```bash
dockerd > /tmp/docker.log 2>&1 &
```

**Result:** Daemon started but crashed due to networking issues.

---

## Issue 3: iptables Permission Denied

### Problem
The most critical issue preventing Docker from running:

```
failed to start daemon: Error initializing network controller: error obtaining controller instance:
failed to register "bridge" driver: failed to create NAT chain DOCKER: iptables failed:
iptables --wait -t nat -N DOCKER: iptables v1.8.7 (nf_tables):
Could not fetch rule set generation id: Permission denied (you must be root)
(exit status 4)
```

### Full Error Log Analysis
```
time="2025-12-14T10:17:30.491224333Z" level=info msg="Starting daemon with containerd snapshotter integration enabled"
time="2025-12-14T10:17:30.492639708Z" level=info msg="Restoring containers: start."
time="2025-12-14T10:17:30.494340154Z" level=info msg="Deleting nftables IPv4 rules" error="exit status 1"
time="2025-12-14T10:17:30.495597412Z" level=info msg="Deleting nftables IPv6 rules" error="exit status 1"
time="2025-12-14T10:17:30.527391926Z" level=info msg="stopping event stream following graceful shutdown"
time="2025-12-14T10:17:30.528152090Z" level=info msg="stopping healthcheck following graceful shutdown"
failed to start daemon: Error initializing network controller...
```

### Root Cause
1. **iptables requires root permissions** - Even though we're running as root, the containerized VPS doesn't allow iptables manipulation
2. **Bridge networking requires iptables** - Docker's default bridge network driver needs iptables to set up NAT chains
3. **Nested containerization limitation** - Running Docker inside a container has networking restrictions

### Why This Happens on CKey.com
CKey.com VPS runs inside containers (likely LXC/LXD), which restricts access to:
- iptables/nftables (firewall rules)
- Network namespace creation
- Bridge device management

---

## Issue 4: systemd Not Available

### Problem
```bash
root@O-1472877:~# systemctl status docker
System has not been booted with systemd as init system (PID 1). Can't operate.
```

### Root Cause
CKey.com uses SysV init system instead of systemd.

### Solution
Use manual daemon management or init scripts instead of systemctl:

```bash
# Check what init system is running
ps -p 1 -o comm=
# Output: init (not systemd)

# Use manual start instead
dockerd > /tmp/docker.log 2>&1 &
```

---

## Final Working Configuration

### Step 1: Create Docker Daemon Configuration

Create `/etc/docker/daemon.json` with settings that bypass restricted features:

```bash
mkdir -p /etc/docker

echo '{' > /etc/docker/daemon.json
echo '  "iptables": false,' >> /etc/docker/daemon.json
echo '  "ip-forward": false,' >> /etc/docker/daemon.json
echo '  "ip-masq": false,' >> /etc/docker/daemon.json
echo '  "bridge": "none",' >> /etc/docker/daemon.json
echo '  "storage-driver": "vfs"' >> /etc/docker/daemon.json
echo '}' >> /etc/docker/daemon.json
```

**Verify the file:**
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

### Configuration Explanation

| Setting | Value | Reason |
|---------|-------|--------|
| `"iptables": false` | Disable iptables | CKey.com doesn't allow iptables manipulation |
| `"ip-forward": false` | Disable IP forwarding | Requires kernel-level permissions not available |
| `"ip-masq": false` | Disable IP masquerading | Requires iptables |
| `"bridge": "none"` | No bridge network | Bridge networking requires iptables NAT |
| `"storage-driver": "vfs"` | Use VFS storage | More compatible with nested containers (overlay2 may fail) |

### Step 2: Stop Any Running Docker Processes

```bash
pkill -9 dockerd
pkill -9 containerd
sleep 3
```

### Step 3: Start Docker Daemon

```bash
dockerd > /tmp/docker.log 2>&1 &
sleep 15
```

### Step 4: Verify Docker is Running

```bash
docker version
```

**Expected output:**
```
Client: Docker Engine - Community
 Version:           27.x.x
 API version:       1.xx
 ...

Server: Docker Engine - Community
 Engine:
  Version:          27.x.x
  API version:      1.xx (minimum version 1.24)
  ...
```

### Step 5: Test Basic Functionality

```bash
docker info
docker ps
```

---

## Running Containers on CKey.com

### IMPORTANT: Network Configuration

Because bridge networking is disabled, **all containers must use `--network host`**:

```bash
# CORRECT - Use host network
docker run --network host your-image

# WRONG - Will fail without bridge networking
docker run your-image
```

### Running the CS4445 Sub Server

```bash
# Pull or build your image
docker pull your-registry/cs4445-sub-server:latest
# OR
docker build -t cs4445-sub-server:latest .

# Run with host networking
docker run -d \
  --name cs4445-sub-server \
  --network host \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mydatabase \
  -e SPRING_DATASOURCE_USERNAME=myuser \
  -e SPRING_DATASOURCE_PASSWORD=secret \
  cs4445-sub-server:latest

# Check logs
docker logs -f cs4445-sub-server
```

### Running PostgreSQL with Docker Compose

Since bridge networking is disabled, you need to modify `compose.yaml`:

**Original (won't work):**
```yaml
services:
  db:
    image: postgres:16-alpine
    ports:
      - "5432:5432"
```

**Modified for CKey.com:**
```yaml
services:
  db:
    image: postgres:16-alpine
    network_mode: host
    environment:
      POSTGRES_USER: myuser
      POSTGRES_PASSWORD: secret
      POSTGRES_DB: mydatabase
```

**Start with:**
```bash
docker compose up -d
```

### Network Implications

With `--network host`:
- Container shares the host's network namespace
- No port mapping needed (container port 8080 is directly accessible as host:8080)
- `localhost` in container refers to the actual host
- Less network isolation (security consideration)
- Better performance (no NAT overhead)

---

## Monitoring Commands

### Check Docker Daemon Status

```bash
# Check if dockerd is running
ps aux | grep dockerd

# View daemon logs
tail -f /tmp/docker.log

# View recent logs
tail -100 /tmp/docker.log
```

### Container Management

```bash
# List all containers
docker ps -a

# View container logs
docker logs <container-name>
docker logs -f <container-name>  # Follow logs

# View container resource usage
docker stats

# Inspect container
docker inspect <container-name>

# Execute command in running container
docker exec -it <container-name> bash
```

### System Information

```bash
# Docker system overview
docker info

# Disk usage
docker system df

# List images
docker images

# List volumes
docker volume ls

# List networks (will show only 'host' and 'none')
docker network ls
```

### Cleanup Commands

```bash
# Stop all containers
docker stop $(docker ps -aq)

# Remove all stopped containers
docker container prune

# Remove unused images
docker image prune

# Remove all unused data (containers, networks, images, volumes)
docker system prune -a

# Remove specific container
docker rm -f <container-name>
```

### Troubleshooting Commands

```bash
# Check if Docker daemon is responding
docker version

# View detailed system info
docker info

# Test with hello-world
docker run --network host hello-world

# Check container exit codes
docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.ExitCode}}"

# View container processes
docker top <container-name>

# View container filesystem changes
docker diff <container-name>
```

---

## Common Issues After Setup

### Issue: Container Exits Immediately

```bash
# Check exit code and reason
docker ps -a
docker logs <container-name>
```

**Common causes:**
- Application error on startup
- Missing environment variables
- Database connection failure

### Issue: Cannot Connect to Application

```bash
# Verify container is running
docker ps

# Check what's listening on port
netstat -tulpn | grep 8080

# Check application logs
docker logs <container-name>
```

**Common causes:**
- Application not binding to 0.0.0.0 (must bind to all interfaces with host network)
- Firewall blocking port
- Application crashed on startup

### Issue: Database Connection Failed

With host networking, use `localhost` or `127.0.0.1`:

```bash
# CORRECT
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mydatabase

# WRONG (won't work with host networking)
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/mydatabase
```

---

## Quick Reference

### Start Docker Daemon
```bash
dockerd > /tmp/docker.log 2>&1 &
```

### Run Application
```bash
docker run -d --name app --network host -e ENV_VAR=value your-image
```

### View Logs
```bash
docker logs -f app
```

### Stop and Remove
```bash
docker stop app
docker rm app
```

### Restart Docker Daemon
```bash
pkill -9 dockerd containerd
sleep 3
dockerd > /tmp/docker.log 2>&1 &
sleep 15
docker ps
```

---

## Summary of Key Learnings

1. **CKey.com uses containerized VPS** - This creates nested containerization challenges
2. **No iptables/nftables support** - Must disable all iptables features in Docker
3. **No systemd** - Must manage Docker daemon manually
4. **Bridge networking doesn't work** - Must use `--network host` for all containers
5. **VFS storage driver recommended** - More reliable than overlay2 in nested containers
6. **Manual daemon management** - Cannot rely on systemd for auto-start

---

## Next Steps

1. Consider creating a startup script to auto-start Docker daemon on VPS reboot
2. Set up monitoring for the Docker daemon process
3. Configure application-specific environment variables
4. Set up log rotation for `/tmp/docker.log`
5. Plan for container updates and rollbacks
6. Document application-specific deployment procedures

---

## Additional Resources

- [Docker in LXC/LXD Containers](https://discuss.linuxcontainers.org/t/running-docker-inside-lxd/2290)
- [Docker Network Drivers](https://docs.docker.com/network/drivers/)
- [Docker Storage Drivers](https://docs.docker.com/storage/storagedriver/select-storage-driver/)
- [CKey.com Documentation](https://ckey.com/docs/) (if available)

---

**Document Version:** 1.0
**Last Updated:** 2025-12-14
**Author:** CS4445 Team
**Environment:** CKey.com VPS (Ubuntu 22.04, Container-based)
