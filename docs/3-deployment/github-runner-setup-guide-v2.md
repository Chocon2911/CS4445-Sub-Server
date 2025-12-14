# GitHub Self-Hosted Runner Setup Guide (v2)

Complete guide for setting up GitHub self-hosted runners on your deployment servers.

## Table of Contents

1. [What Are Self-Hosted Runners?](#what-are-self-hosted-runners)
2. [When to Use Self-Hosted vs GitHub-Hosted](#when-to-use-self-hosted-vs-github-hosted)
3. [Prerequisites](#prerequisites)
4. [Single Server Setup](#single-server-setup)
5. [Multi-Server Setup](#multi-server-setup)
6. [Runner Configuration](#runner-configuration)
7. [Running as a Service](#running-as-a-service)
8. [Security Best Practices](#security-best-practices)
9. [Monitoring and Maintenance](#monitoring-and-maintenance)
10. [Troubleshooting](#troubleshooting)

## What Are Self-Hosted Runners?

**GitHub-Hosted Runners** (default):
- Provided by GitHub
- Fresh VM for each job
- Limited resources (2-core CPU, 7GB RAM)
- Can't access your private network
- Free for public repos, metered for private

**Self-Hosted Runners**:
- Run on YOUR servers
- Can access your private network
- Customizable resources
- Direct deployment (no SSH needed)
- Free for all repos
- You manage updates and security

## When to Use Self-Hosted vs GitHub-Hosted

### Use Self-Hosted Runners When:

✅ **Direct deployment** - Deploy directly to the same server
✅ **Private network access** - Need to access internal databases, APIs
✅ **Large resource requirements** - Need >7GB RAM, >2 cores
✅ **Cost optimization** - Heavy CI/CD usage on private repos
✅ **Custom environment** - Specific OS, tools, or configurations
✅ **Faster builds** - Caching between runs

### Use GitHub-Hosted Runners When:

✅ **Simple builds** - Just compile and test
✅ **Security concerns** - Don't want build code on your servers
✅ **No network requirements** - Self-contained builds
✅ **Low usage** - Occasional builds
✅ **Easy maintenance** - GitHub manages everything

### Our Multi-Server Deployment Approach

For this project, we support **BOTH**:

**Option 1: GitHub-Hosted Runners + SSH**
- Build on GitHub runners
- Deploy via SSH to multiple servers
- Good for: Most users, external servers

**Option 2: Self-Hosted Runners**
- Build and deploy on your servers directly
- Good for: Internal networks, large scale

## Prerequisites

### Server Requirements

**Minimum per runner:**
- OS: Ubuntu 20.04+ (recommended), Windows, macOS
- CPU: 2 cores
- RAM: 4GB
- Disk: 10GB free space
- Network: Internet access to GitHub

**Recommended:**
- CPU: 4+ cores
- RAM: 8GB+
- Disk: 50GB+ SSD
- Dedicated runner servers (not production app servers)

### Software Requirements

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install -y curl git jq

# Docker (required for our deployment)
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Logout and login for group changes to take effect
```

## Single Server Setup

### Step 1: Create Runner User

```bash
# Create dedicated user for runner
sudo useradd -m -s /bin/bash github-runner
sudo usermod -aG docker github-runner

# Set up SSH for deployment (optional)
sudo mkdir -p /home/github-runner/.ssh
sudo cp ~/.ssh/authorized_keys /home/github-runner/.ssh/
sudo chown -R github-runner:github-runner /home/github-runner/.ssh
```

### Step 2: Add Runner to GitHub

**Via GitHub UI:**

1. Go to your repository on GitHub
2. Click **Settings** → **Actions** → **Runners**
3. Click **"New self-hosted runner"**
4. Select your OS (Linux)
5. Follow the installation commands shown

**Commands will look like:**

```bash
# Switch to runner user
sudo su - github-runner

# Create actions-runner directory
mkdir actions-runner && cd actions-runner

# Download latest runner package
curl -o actions-runner-linux-x64-2.311.0.tar.gz -L https://github.com/actions/runner/releases/download/v2.311.0/actions-runner-linux-x64-2.311.0.tar.gz

# Extract
tar xzf ./actions-runner-linux-x64-2.311.0.tar.gz

# Configure the runner
./config.sh --url https://github.com/YOUR_USERNAME/cs4445-sub-server --token YOUR_TOKEN
```

### Step 3: Configure Runner

During `./config.sh`, you'll be asked:

```
Enter the name of the runner group [Default: Default]:
# Press Enter for Default

Enter the name of runner [hostname]:
server-1
# Give it a meaningful name (e.g., prod-server-1, staging-server-1)

Enter any additional labels []:
deployment,production,server-1
# Add labels for targeting specific runners

Enter name of work folder [_work]:
# Press Enter for default

Would you like to run the runner as a service? [Y/N]:
Y
# We'll configure this in next step
```

### Step 4: Install as System Service

```bash
# Install service (as root)
exit  # Exit from github-runner user
sudo ./actions-runner/svc.sh install github-runner

# Start service
sudo ./actions-runner/svc.sh start

# Check status
sudo ./actions-runner/svc.sh status

# Enable auto-start on boot
sudo systemctl enable actions.runner.YOUR_REPO.server-1.service
```

### Step 5: Verify Runner

1. Go to GitHub → Repository → Settings → Actions → Runners
2. You should see your runner with status **"Idle"** (green)
3. Labels should show: `self-hosted`, `Linux`, `X64`, plus your custom labels

## Multi-Server Setup

### Automated Setup Script

Create `setup-runner.sh`:

```bash
#!/bin/bash

# Multi-Server Runner Setup Script
# Usage: ./setup-runner.sh <server_ip> <server_name> <labels>

SERVER_IP=$1
SERVER_NAME=$2
LABELS=$3
GITHUB_REPO="YOUR_USERNAME/cs4445-sub-server"

if [ -z "$SERVER_IP" ] || [ -z "$SERVER_NAME" ]; then
    echo "Usage: $0 <server_ip> <server_name> [labels]"
    echo "Example: $0 192.168.1.100 prod-server-1 'production,deployment'"
    exit 1
fi

LABELS=${LABELS:-"deployment"}

echo "Setting up runner on $SERVER_IP..."
echo "Name: $SERVER_NAME"
echo "Labels: $LABELS"

# Generate registration token
echo "Generating registration token..."
TOKEN=$(gh api -X POST "repos/$GITHUB_REPO/actions/runners/registration-token" --jq .token)

if [ -z "$TOKEN" ]; then
    echo "Failed to generate token. Make sure 'gh' CLI is installed and authenticated."
    exit 1
fi

# Deploy runner setup script to server
ssh $SERVER_IP << EOF
    # Create runner user
    sudo useradd -m -s /bin/bash github-runner 2>/dev/null || echo "User exists"
    sudo usermod -aG docker github-runner

    # Setup runner
    sudo su - github-runner -c "
        mkdir -p actions-runner
        cd actions-runner

        # Download runner
        RUNNER_VERSION=\$(curl -s https://api.github.com/repos/actions/runner/releases/latest | grep 'tag_name' | cut -d'v' -f2 | cut -d'\"' -f1)
        curl -o actions-runner.tar.gz -L https://github.com/actions/runner/releases/download/v\${RUNNER_VERSION}/actions-runner-linux-x64-\${RUNNER_VERSION}.tar.gz
        tar xzf actions-runner.tar.gz
        rm actions-runner.tar.gz

        # Configure
        ./config.sh --url https://github.com/$GITHUB_REPO \\
            --token $TOKEN \\
            --name $SERVER_NAME \\
            --labels $LABELS \\
            --unattended \\
            --replace
    "

    # Install service
    cd /home/github-runner/actions-runner
    sudo ./svc.sh install github-runner
    sudo ./svc.sh start
    sudo systemctl enable actions.runner.*.service

    echo "Runner installed on $SERVER_IP"
EOF

echo "✅ Runner setup completed on $SERVER_IP"
```

### Deploy Runners to All Servers

```bash
# Make script executable
chmod +x setup-runner.sh

# Install GitHub CLI if not installed
# Ubuntu: sudo apt install gh
# Mac: brew install gh
# Login: gh auth login

# Setup runners on all servers
./setup-runner.sh 192.168.1.100 prod-server-1 "production,deployment,server-1"
./setup-runner.sh 192.168.1.101 prod-server-2 "production,deployment,server-2"
./setup-runner.sh 192.168.1.102 staging-server-1 "staging,deployment,server-1"

# Or loop through IPs
for i in {100..105}; do
    ./setup-runner.sh 192.168.1.$i prod-server-$((i-99)) "production,deployment"
done
```

## Runner Configuration

### Labels Strategy

Use labels to target specific runners:

```yaml
# .github/workflows/cd-v2-multi-server.yml

jobs:
  deploy-production:
    runs-on: [self-hosted, production, deployment]
    # This will run on ANY runner with ALL these labels

  deploy-staging:
    runs-on: [self-hosted, staging]
    # This will run on staging runners only

  deploy-to-specific-server:
    runs-on: [self-hosted, server-1]
    # This will run only on server-1
```

**Recommended Labels:**

- **Environment**: `production`, `staging`, `development`
- **Purpose**: `deployment`, `testing`, `building`
- **Server**: `server-1`, `server-2`, etc.
- **Region**: `us-east`, `eu-west`, etc. (if multi-region)

### Runner Groups (GitHub Enterprise)

If you have GitHub Enterprise, create runner groups:

1. Settings → Actions → Runner groups
2. Create group: "Production Servers"
3. Add production runners to group
4. Set repository access rules

## Running as a Service

### Systemd Service Management

```bash
# Check service status
sudo systemctl status actions.runner.*.service

# View logs
sudo journalctl -u actions.runner.*.service -f

# Restart runner
sudo systemctl restart actions.runner.*.service

# Stop runner
sudo systemctl stop actions.runner.*.service

# Disable auto-start
sudo systemctl disable actions.runner.*.service
```

### Service Configuration File

Located at: `/etc/systemd/system/actions.runner.YOUR_REPO.YOUR_RUNNER.service`

```ini
[Unit]
Description=GitHub Actions Runner (cs4445-sub-server.server-1)
After=network.target

[Service]
ExecStart=/home/github-runner/actions-runner/runsvc.sh
User=github-runner
WorkingDirectory=/home/github-runner/actions-runner
KillMode=process
KillSignal=SIGTERM
TimeoutStopSec=5min

[Install]
WantedBy=multi-user.target
```

### Auto-Update Configuration

Runners auto-update by default. To control updates:

```bash
# Disable auto-update (in config.sh)
./config.sh --url ... --token ... --disableupdate

# Manual update
cd ~/actions-runner
./config.sh remove --token YOUR_TOKEN
# Download new version
./config.sh --url ... --token ...
sudo ./svc.sh install
sudo ./svc.sh start
```

## Security Best Practices

### 1. Dedicated Runner User

✅ **Do:**
```bash
# Create dedicated user
sudo useradd -m -s /bin/bash github-runner
```

❌ **Don't:**
```bash
# Run as root (NEVER!)
sudo ./run.sh  # BAD!
```

### 2. Network Security

```bash
# Firewall rules - only allow necessary ports
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 8080/tcp  # Application
sudo ufw enable

# Runners need outbound HTTPS to:
# - github.com
# - api.github.com
# - *.actions.githubusercontent.com
```

### 3. Secrets Isolation

**Runner has NO access to secrets!**

Secrets are:
- Masked in logs
- Not accessible in runner environment
- Only available to specific workflow steps

```yaml
# Secrets are injected by GitHub
- name: Deploy
  env:
    SECRET_KEY: ${{ secrets.SECRET_KEY }}  # OK
  run: |
    echo $SECRET_KEY  # Will be masked in logs
```

### 4. Repository Access Control

**For public repositories:**
- Approve first-time contributors' workflows manually
- Settings → Actions → Fork pull request workflows → Require approval

**For private repositories:**
- Use runner groups to limit access
- Only trusted repositories

### 5. Runner Isolation

```bash
# Each runner should be isolated
# Option 1: Separate VMs
# Option 2: Docker isolation (advanced)
# Option 3: Dedicated physical servers
```

### 6. Regular Updates

```bash
# Update system packages
sudo apt update && sudo apt upgrade -y

# Update Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Runner updates automatically
```

## Monitoring and Maintenance

### Health Checks

Create `check-runners.sh`:

```bash
#!/bin/bash

# Check all runners
echo "Checking runner status..."

# Via GitHub API
gh api repos/YOUR_USERNAME/cs4445-sub-server/actions/runners --jq '.runners[] | {name, status, busy}'

# On each server
for server in 192.168.1.{100..105}; do
    echo "Checking $server..."
    ssh $server "sudo systemctl status actions.runner.* | grep Active"
done
```

### Log Monitoring

```bash
# View runner logs
sudo journalctl -u actions.runner.*.service -f

# View Docker logs
docker logs cs4445-app --tail 100 -f

# Check disk space
df -h

# Check memory
free -h

# Check CPU
top
```

### Automatic Restart on Failure

```bash
# Edit service file
sudo systemctl edit actions.runner.*.service

# Add:
[Service]
Restart=always
RestartSec=10

# Reload
sudo systemctl daemon-reload
```

### Cleanup Old Workflows

```bash
# Clean old workflow runs (via GitHub CLI)
gh run list --limit 100 --json databaseId --jq '.[].databaseId' | \
  xargs -I{} gh api -X DELETE repos/YOUR_USERNAME/cs4445-sub-server/actions/runs/{}

# Clean Docker
docker system prune -a -f --volumes
```

## Troubleshooting

### Issue 1: Runner Shows Offline

**Check:**
```bash
# Check service status
sudo systemctl status actions.runner.*.service

# Check logs
sudo journalctl -u actions.runner.*.service -n 50

# Check network connectivity
curl -I https://github.com
```

**Fix:**
```bash
# Restart service
sudo systemctl restart actions.runner.*.service

# If still offline, reconfigure
cd /home/github-runner/actions-runner
sudo ./svc.sh stop
./config.sh remove --token YOUR_TOKEN
./config.sh --url ... --token NEW_TOKEN
sudo ./svc.sh install github-runner
sudo ./svc.sh start
```

### Issue 2: Job Stuck in Queue

**Symptoms**: Workflow shows "Waiting for a runner..."

**Causes:**
- No runners online
- No runners match labels
- Runner is busy with another job

**Fix:**
```bash
# Check runner status on GitHub
# Settings → Actions → Runners

# Check labels match
# Workflow: runs-on: [self-hosted, production]
# Runner: Must have BOTH labels

# Add runner or fix labels
./config.sh --url ... --token ... --labels "self-hosted,production"
```

### Issue 3: Permission Denied Errors

**Symptoms**: `docker: permission denied`

**Fix:**
```bash
# Add user to docker group
sudo usermod -aG docker github-runner

# Runner must logout/login (or restart service)
sudo systemctl restart actions.runner.*.service
```

### Issue 4: Disk Space Issues

**Symptoms**: "No space left on device"

**Fix:**
```bash
# Clean Docker
docker system prune -a -f --volumes

# Clean old workflow artifacts
rm -rf /home/github-runner/actions-runner/_work/_temp/*

# Clean old runners
cd /home/github-runner/actions-runner
rm -rf _work/_tool/*
```

### Issue 5: Runner Version Mismatch

**Symptoms**: "Runner version is out of date"

**Fix:**
```bash
# Runners auto-update, but you can force update:
cd /home/github-runner/actions-runner
sudo ./svc.sh stop
./config.sh remove --token YOUR_TOKEN

# Download latest
RUNNER_VERSION=$(curl -s https://api.github.com/repos/actions/runner/releases/latest | grep 'tag_name' | cut -d'v' -f2 | cut -d'"' -f1)
curl -o actions-runner.tar.gz -L https://github.com/actions/runner/releases/download/v${RUNNER_VERSION}/actions-runner-linux-x64-${RUNNER_VERSION}.tar.gz
tar xzf actions-runner.tar.gz

# Reconfigure
./config.sh --url ... --token NEW_TOKEN
sudo ./svc.sh install github-runner
sudo ./svc.sh start
```

## Quick Reference

### Essential Commands

```bash
# Setup runner
./config.sh --url https://github.com/USER/REPO --token TOKEN --labels "prod,deployment"

# Install service
sudo ./svc.sh install github-runner

# Start/stop/status
sudo systemctl start actions.runner.*.service
sudo systemctl stop actions.runner.*.service
sudo systemctl status actions.runner.*.service

# View logs
sudo journalctl -u actions.runner.*.service -f

# Remove runner
./config.sh remove --token TOKEN

# Generate token (GitHub CLI)
gh api -X POST repos/USER/REPO/actions/runners/registration-token --jq .token
```

### Runner Locations

```
Configuration: /home/github-runner/actions-runner
Work directory: /home/github-runner/actions-runner/_work
Service file: /etc/systemd/system/actions.runner.*.service
Logs: journalctl -u actions.runner.*.service
```

## Summary

You now have:
- ✅ Self-hosted runners on your servers
- ✅ Automatic deployment without SSH
- ✅ Custom labels for targeting runners
- ✅ System service with auto-restart
- ✅ Security best practices
- ✅ Monitoring and maintenance procedures

**Next Steps:**
- [Multi-Server Deployment Guide v2](multi-server-deployment-guide-v2.md) - Configure multi-server deployment
- [GitHub Setup Guide v2](github-setup-guide-v2.md) - Update secrets for multiple servers

Happy running! 🏃‍♂️
