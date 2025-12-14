# Multi-Server Deployment Guide v2

Complete guide for deploying to multiple servers with GitHub Actions.

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Prerequisites](#prerequisites)
4. [GitHub Secrets Setup](#github-secrets-setup)
5. [Deployment Strategies](#deployment-strategies)
6. [Setting Up Multiple Servers](#setting-up-multiple-servers)
7. [Running Your First Multi-Server Deployment](#running-your-first-multi-server-deployment)
8. [Monitoring Deployments](#monitoring-deployments)
9. [Rollback Procedures](#rollback-procedures)
10. [Advanced Configurations](#advanced-configurations)
11. [Troubleshooting](#troubleshooting)

## Overview

This guide covers deploying your CS4445 Sub Server to **multiple servers simultaneously** using GitHub Actions. The system supports both SSH-based deployment and self-hosted runner deployment.

### What's New in v2?

✅ **Multi-server support** - Deploy to unlimited servers
✅ **Flexible server management** - Comma-separated IP list in secrets
✅ **Parallel or sequential** - Choose deployment strategy
✅ **Blue-green deployment** - Zero-downtime production deploys
✅ **Health checks for all servers** - Verify each server individually
✅ **Automatic rollback** - Failed servers roll back automatically
✅ **Self-hosted runner support** - Optional direct deployment

### Deployment Methods

**Method 1: GitHub-Hosted + SSH** (Recommended for most users)
- Build on GitHub runners
- Deploy via SSH to all servers
- No runner setup required
- Works with external servers

**Method 2: Self-Hosted Runners**
- Build and deploy on your servers
- No SSH needed
- Better for internal networks
- Requires runner setup on each server

This guide covers **both methods**.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│              GitHub Actions Workflow                     │
│                                                          │
│  1. Build Docker Image                                  │
│  2. Push to GitHub Container Registry                   │
│  3. Parse server IPs from secrets                       │
│  4. Deploy to all servers (parallel or sequential)      │
│  5. Health check each server                            │
│  6. Rollback on failure                                 │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
        ┌────────┴────────┐
        │                 │
        ▼                 ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Server 1    │  │  Server 2    │  │  Server N    │
│  (IP 1)      │  │  (IP 2)      │  │  (IP N)      │
├──────────────┤  ├──────────────┤  ├──────────────┤
│ Docker       │  │ Docker       │  │ Docker       │
│ PostgreSQL   │  │ PostgreSQL   │  │ PostgreSQL   │
│ App          │  │ App          │  │ App          │
│ Prometheus   │  │ Prometheus   │  │ Prometheus   │
│ Grafana      │  │ Grafana      │  │ Grafana      │
└──────────────┘  └──────────────┘  └──────────────┘
```

## Prerequisites

### GitHub Side

- ✅ GitHub repository set up
- ✅ GitHub Actions enabled
- ✅ Workflow permissions: "Read and write"

### Server Side (Each Server)

- ✅ Ubuntu 20.04+ (or compatible Linux)
- ✅ Docker and Docker Compose installed
- ✅ SSH access (Method 1) OR GitHub runner installed (Method 2)
- ✅ Firewall allows: SSH (22), HTTP (8080), Prometheus (9090), Grafana (3000)
- ✅ Git installed
- ✅ Repository cloned to `/app/cs4445-sub-server` (or your path)

### Quick Server Preparation

Run this on **each server**:

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Add your user to docker group
sudo usermod -aG docker $USER
# Logout and login for this to take effect

# Install Git
sudo apt install -y git

# Clone repository
sudo mkdir -p /app
sudo chown $USER:$USER /app
cd /app
git clone https://github.com/YOUR_USERNAME/cs4445-sub-server.git
cd cs4445-sub-server

# Test Docker
docker --version
docker compose version
```

## GitHub Secrets Setup

### Required Secrets

Go to: **Repository → Settings → Secrets and variables → Actions**

#### 1. DEPLOY_SSH_KEY (Method 1: SSH Deployment)

**Generate SSH key:**
```bash
# On your local machine
ssh-keygen -t ed25519 -C "github-deployment" -f ~/.ssh/github-deploy -N ""

# View private key (this goes in GitHub secret)
cat ~/.ssh/github-deploy
```

**Add to GitHub:**
1. Copy the **entire private key** (including `-----BEGIN` and `-----END` lines)
2. Go to Settings → Secrets → Actions
3. New repository secret
4. Name: `DEPLOY_SSH_KEY`
5. Value: Paste the private key
6. Add secret

**Add public key to each server:**
```bash
# Copy public key
cat ~/.ssh/github-deploy.pub

# On each server:
mkdir -p ~/.ssh
echo "YOUR_PUBLIC_KEY_HERE" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
chmod 700 ~/.ssh
```

#### 2. STAGING_SERVER_IPS

**Format:** Comma-separated IP addresses (no spaces) OR with spaces (script handles both)

**Examples:**
```
# No spaces
192.168.1.100,192.168.1.101,192.168.1.102

# With spaces (also works)
192.168.1.100, 192.168.1.101, 192.168.1.102

# Single server
192.168.1.100

# Many servers
10.0.1.10,10.0.1.11,10.0.1.12,10.0.1.13,10.0.1.14
```

**Add to GitHub:**
1. Settings → Secrets → Actions
2. New repository secret
3. Name: `STAGING_SERVER_IPS`
4. Value: Your comma-separated IPs
5. Add secret

#### 3. PRODUCTION_SERVER_IPS

Same format as staging:
```
# Production servers
192.168.100.10,192.168.100.11,192.168.100.12
```

Add as secret: `PRODUCTION_SERVER_IPS`

#### 4. DEPLOY_USER

The SSH user for deployment (usually your username or dedicated deploy user).

```
# Example values:
ubuntu
deployer
admin
yourname
```

Add as secret: `DEPLOY_USER`

#### 5. DEPLOY_PATH

Where the application is located on servers.

```
# Usually:
/app/cs4445-sub-server

# Or:
/home/deployer/cs4445-sub-server
```

Add as secret: `DEPLOY_PATH`

### Optional Secrets

#### SLACK_WEBHOOK / DISCORD_WEBHOOK

For deployment notifications. See original guide for setup.

### Secrets Summary Checklist

```
Repository Secrets:
├── DEPLOY_SSH_KEY ............. SSH private key
├── DEPLOY_USER ................ SSH username (e.g., ubuntu)
├── DEPLOY_PATH ................ App path (e.g., /app/cs4445-sub-server)
├── STAGING_SERVER_IPS ......... Comma-separated staging IPs
├── PRODUCTION_SERVER_IPS ...... Comma-separated production IPs
└── SLACK_WEBHOOK .............. (Optional) Notification webhook
```

## Deployment Strategies

### 1. Parallel Deployment (Default)

**How it works:**
- Deploys to ALL servers simultaneously
- Fastest deployment time
- All servers deploy at the same time

**Best for:**
- Staging environments
- Small number of servers (2-10)
- When downtime is acceptable

**Workflow:**
```yaml
deployment_mode: parallel  # (default)
```

**Pros:**
- ⚡ Fastest (all servers at once)
- 🎯 Simple

**Cons:**
- ⚠️ All servers down during deployment
- ⚠️ Higher network/resource load

### 2. Sequential Deployment

**How it works:**
- Deploys to servers one by one
- Waits for each to complete before next
- Stops immediately if any server fails

**Best for:**
- Critical deployments
- Testing deployment on first server before others
- Resource-constrained environments

**Workflow:**
```yaml
deployment_mode: sequential
```

**Pros:**
- ✅ Fail fast (stops on first failure)
- ✅ Lower resource usage
- ✅ Can monitor each server

**Cons:**
- 🐌 Slower (waits for each)
- 📉 Still has downtime

### 3. Blue-Green Deployment (Production Only)

**How it works:**
- Splits servers into two groups: Blue and Green
- Deploys to Blue group first (50% of servers)
- Verifies Blue group health
- Then deploys to Green group (remaining 50%)
- Zero downtime (some servers always available)

**Best for:**
- Production environments
- High availability requirements
- Large number of servers

**Automatic in production** - no configuration needed!

**Pros:**
- ✅ Zero downtime
- ✅ Half servers always running
- ✅ Can rollback Blue before Green

**Cons:**
- ⏱️ Slower than parallel
- 📊 More complex

### Choosing a Strategy

| Scenario | Recommended Strategy |
|----------|---------------------|
| 2-5 staging servers | Parallel |
| 10+ staging servers | Sequential |
| Production (any number) | Blue-Green (automatic) |
| Testing new deployment | Sequential |
| Emergency hotfix | Sequential |

## Setting Up Multiple Servers

### Scenario 1: Fresh Multi-Server Setup

You have 3 servers and want to deploy to all:

```bash
# Server IPs
STAGING_IPS="192.168.1.101,192.168.1.102,192.168.1.103"
PRODUCTION_IPS="192.168.1.201,192.168.1.202,192.168.1.203"

# 1. Prepare each server
for ip in 192.168.1.{101..103}; do
    echo "Setting up $ip..."
    ssh ubuntu@$ip << 'EOF'
        # Install Docker
        curl -fsSL https://get.docker.com | sh
        sudo usermod -aG docker ubuntu

        # Install Docker Compose
        sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
        sudo chmod +x /usr/local/bin/docker-compose

        # Clone repo
        sudo mkdir -p /app && sudo chown ubuntu:ubuntu /app
        cd /app
        git clone https://github.com/YOUR_USERNAME/cs4445-sub-server.git
        cd cs4445-sub-server

        # Create .env file
        cat > .env << 'ENVEOF'
POSTGRES_PASSWORD=your_secure_password
GRAFANA_PASSWORD=your_grafana_password
SPRING_PROFILE=staging
ENVEOF

        echo "Server $ip setup complete"
EOF
done

# 2. Add SSH key to all servers
for ip in 192.168.1.{101..103}; do
    ssh-copy-id -i ~/.ssh/github-deploy.pub ubuntu@$ip
done

# 3. Add secrets to GitHub
# Go to Settings → Secrets → Actions
# Add: STAGING_SERVER_IPS = 192.168.1.101,192.168.1.102,192.168.1.103
```

### Scenario 2: Adding New Servers

You have 3 servers, want to add 2 more:

```bash
# New servers: 192.168.1.104, 192.168.1.105

# 1. Setup new servers (same as above)
for ip in 192.168.1.{104..105}; do
    # ... run setup script
done

# 2. Update GitHub secret
# Old: 192.168.1.101,192.168.1.102,192.168.1.103
# New: 192.168.1.101,192.168.1.102,192.168.1.103,192.168.1.104,192.168.1.105
```

### Scenario 3: Removing Servers

```bash
# Remove 192.168.1.102 from deployment

# Update GitHub secret
# Old: 192.168.1.101,192.168.1.102,192.168.1.103
# New: 192.168.1.101,192.168.1.103

# Next deployment will skip .102
```

## Running Your First Multi-Server Deployment

### Step 1: Verify Secrets

```bash
# Use GitHub CLI to check (secrets are masked, but you can verify they exist)
gh secret list

# Should show:
# DEPLOY_SSH_KEY
# DEPLOY_USER
# DEPLOY_PATH
# STAGING_SERVER_IPS
# PRODUCTION_SERVER_IPS
```

### Step 2: Test SSH Connectivity

```bash
# Test each server manually
IPS="192.168.1.101,192.168.1.102,192.168.1.103"
IFS=',' read -ra SERVERS <<< "$IPS"

for server in "${SERVERS[@]}"; do
    echo "Testing $server..."
    ssh -i ~/.ssh/github-deploy ubuntu@$server "echo 'Connected to $server'"
done
```

### Step 3: Trigger Staging Deployment

**Option A: Push to main branch**
```bash
git add .
git commit -m "Deploy to staging"
git push origin main
```

The workflow will automatically:
1. Build Docker image
2. Parse `STAGING_SERVER_IPS`
3. Deploy to all staging servers in parallel
4. Run health checks
5. Report results

**Option B: Manual trigger**
1. Go to GitHub → Actions
2. Select "CD v2 - Multi-Server Deployment"
3. Click "Run workflow"
4. Select:
   - Branch: `main`
   - Environment: `staging`
   - Deployment mode: `parallel`
5. Click "Run workflow"

### Step 4: Monitor Deployment

1. Go to GitHub → Actions
2. Click on the running workflow
3. Watch the deploy job expand
4. You'll see:
   ```
   🚀 Starting parallel deployment to 3 staging servers...
   Deploying to 192.168.1.101 in background...
   Deploying to 192.168.1.102 in background...
   Deploying to 192.168.1.103 in background...
   ✅ All parallel deployments completed

   🏥 Running health checks on all servers...
   ✅ 192.168.1.101 is healthy
   ✅ 192.168.1.102 is healthy
   ✅ 192.168.1.103 is healthy
   ✅ All servers are healthy
   ```

### Step 5: Verify Deployment

```bash
# Check each server
for ip in 192.168.1.{101..103}; do
    echo "Checking $ip..."
    curl http://$ip:8080/actuator/health
    echo ""
done

# Or all at once
IPS="192.168.1.101,192.168.1.102,192.168.1.103"
IFS=',' read -ra SERVERS <<< "$IPS"
for server in "${SERVERS[@]}"; do
    echo "Server: $server"
    curl -s http://$server:8080/api/v1/server/status | jq
done
```

### Step 6: Production Deployment

**For production, use version tags:**

```bash
# Create version tag
git tag v1.0.0
git push origin v1.0.0
```

This will trigger:
1. Build image with `v1.0.0` tag
2. Deploy to staging first
3. **Blue-green deployment to production** (automatic)
   - Deploy to first 50% of servers
   - Health check
   - Wait 30 seconds
   - Deploy to remaining 50%
   - Final health check

## Monitoring Deployments

### Real-Time Monitoring

**GitHub Actions UI:**
1. Actions tab
2. Running workflow
3. Expand job
4. Live log stream

**Server-Side Monitoring:**
```bash
# Watch deployment on server
ssh ubuntu@192.168.1.101
tail -f /var/log/syslog | grep docker

# Watch container logs
docker logs cs4445-app -f
```

### Health Dashboard

After deployment, monitor all servers:

```bash
# Create a quick monitoring script
cat > check-all-servers.sh << 'EOF'
#!/bin/bash
IPS="192.168.1.101,192.168.1.102,192.168.1.103"
IFS=',' read -ra SERVERS <<< "$IPS"

for server in "${SERVERS[@]}"; do
    server=$(echo "$server" | xargs)
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "Server: $server"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    # Health
    echo -n "Health: "
    curl -s http://$server:8080/actuator/health | jq -r '.status' || echo "FAILED"

    # Status
    echo -n "Server Status: "
    curl -s http://$server:8080/api/v1/server/status | jq -r '.status' || echo "FAILED"

    # Version (from Docker image)
    echo -n "Version: "
    ssh ubuntu@$server "docker inspect cs4445-app --format='{{.Config.Image}}'" | cut -d':' -f2

    echo ""
done
EOF

chmod +x check-all-servers.sh
./check-all-servers.sh
```

### Aggregate Metrics

Access Grafana on each server:
- Server 1: http://192.168.1.101:3000
- Server 2: http://192.168.1.102:3000
- Server 3: http://192.168.1.103:3000

## Rollback Procedures

### Automatic Rollback

If deployment fails on ANY server:
- Workflow automatically triggers rollback job
- Rolls back ALL servers to previous version
- Sends notification

### Manual Rollback (All Servers)

```bash
# Rollback all staging servers
./scripts/rollback-all-servers.sh staging

# Rollback all production servers
./scripts/rollback-all-servers.sh production
```

Create `scripts/rollback-all-servers.sh`:
```bash
#!/bin/bash
ENV=$1

if [ "$ENV" == "production" ]; then
    IPS="192.168.1.201,192.168.1.202,192.168.1.203"
else
    IPS="192.168.1.101,192.168.1.102,192.168.1.103"
fi

echo "Rolling back $ENV servers..."
IFS=',' read -ra SERVERS <<< "$IPS"

for server in "${SERVERS[@]}"; do
    server=$(echo "$server" | xargs)
    echo "Rolling back $server..."
    ./scripts/rollback-server.sh "$server" ubuntu /app/cs4445-sub-server
done
```

### Rollback Single Server

```bash
# Rollback specific server only
./scripts/rollback-server.sh 192.168.1.102 ubuntu /app/cs4445-sub-server v1.0.0
```

## Advanced Configurations

### Load Balancer Integration

If using a load balancer (e.g., nginx, HAProxy):

**Pre-deployment:**
```bash
# Remove server from load balancer
ssh lb-server "nginx -s reload" # after editing upstream config
```

**Post-deployment:**
```bash
# Add server back to load balancer
# after health check passes
```

### Database Migrations

For schema changes:

```bash
# Run migration on one server first
ssh ubuntu@192.168.1.101 "cd /app/cs4445-sub-server && docker compose exec app ./mvnw flyway:migrate"

# Then deploy to all servers
```

### Canary Deployment

Deploy to subset first:

```bash
# Create canary secret
STAGING_CANARY_IPS="192.168.1.101"  # Just first server

# After canary success, deploy to all
STAGING_SERVER_IPS="192.168.1.101,192.168.1.102,192.168.1.103"
```

## Troubleshooting

### Issue 1: Deployment Fails on One Server

**Symptom:**
```
✅ 192.168.1.101 deployed
❌ 192.168.1.102 failed
✅ 192.168.1.103 deployed
```

**Solution:**
```bash
# Check logs on failed server
ssh ubuntu@192.168.1.102
docker logs cs4445-app
journalctl -u docker

# Fix issue, then redeploy manually
./scripts/deploy-to-server.sh 192.168.1.102 ubuntu /app/cs4445-sub-server main staging
```

### Issue 2: Health Check Fails

**Symptom:** "Health check failed after 5 attempts"

**Solution:**
```bash
# Check if app started
ssh ubuntu@SERVER_IP "docker ps"

# Check app logs
ssh ubuntu@SERVER_IP "docker logs cs4445-app --tail 100"

# Check port is open
curl http://SERVER_IP:8080/actuator/health

# Check firewall
ssh ubuntu@SERVER_IP "sudo ufw status"
```

### Issue 3: SSH Connection Refused

**Symptom:** "Permission denied" or "Connection refused"

**Solution:**
```bash
# Test SSH manually
ssh -i ~/.ssh/github-deploy ubuntu@SERVER_IP

# Check SSH key
cat ~/.ssh/github-deploy.pub

# Verify key is in authorized_keys on server
ssh ubuntu@SERVER_IP "cat ~/.ssh/authorized_keys"

# Re-add key if needed
ssh-copy-id -i ~/.ssh/github-deploy.pub ubuntu@SERVER_IP
```

### Issue 4: Some Servers Out of Sync

**Symptom:** Servers running different versions

**Solution:**
```bash
# Check version on all servers
for ip in 192.168.1.{101..103}; do
    echo -n "$ip: "
    ssh ubuntu@$ip "docker inspect cs4445-app --format='{{.Config.Image}}'"
done

# Force redeploy to specific servers
./scripts/deploy-to-server.sh 192.168.1.102 ubuntu /app/cs4445-sub-server latest staging
```

## Summary

You now have:
- ✅ Multi-server deployment working
- ✅ Comma-separated IP configuration in secrets
- ✅ Parallel and sequential deployment modes
- ✅ Blue-green deployment for production
- ✅ Health checks for all servers
- ✅ Automatic and manual rollback
- ✅ Comprehensive monitoring

**Quick Commands:**
```bash
# Deploy to staging (automatic)
git push origin main

# Deploy to production
git tag v1.0.0 && git push origin v1.0.0

# Check all servers
./check-all-servers.sh

# Rollback all servers
./scripts/rollback-all-servers.sh staging
```

**Next Steps:**
- Set up [GitHub Runners](github-runner-setup-guide-v2.md) for direct deployment (optional)
- Configure load balancer for high availability
- Set up monitoring alerts
- Automate server provisioning

Happy multi-server deploying! 🚀
