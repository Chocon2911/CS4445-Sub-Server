# CKey.com Server Setup Guide - Multi-Server Deployment

Complete guide for creating and configuring servers on CKey.com for the CS4445 Sub Server project.

## Table of Contents

1. [Overview](#overview)
2. [Planning Your Server Setup](#planning-your-server-setup)
3. [Creating Servers on CKey.com](#creating-servers-on-ckeycom)
4. [Server Configuration](#server-configuration)
5. [Automated Setup Script](#automated-setup-script)
6. [GitHub Integration](#github-integration)
7. [Testing Your Setup](#testing-your-setup)
8. [Troubleshooting](#troubleshooting)

## Overview

This guide walks you through setting up **5 servers** on CKey.com for multi-server deployment.

### What We'll Create

```
Server Setup:
├── 3 Staging Servers (for testing)
│   ├── staging-server-1 (192.168.1.101 or assigned IP)
│   ├── staging-server-2 (192.168.1.102 or assigned IP)
│   └── staging-server-3 (192.168.1.103 or assigned IP)
└── 2 Production Servers (for live deployment)
    ├── prod-server-1 (192.168.100.1 or assigned IP)
    └── prod-server-2 (192.168.100.2 or assigned IP)
```

### Time Required
- **Server creation:** 10-15 minutes (on CKey.com)
- **Server configuration:** 5 minutes per server (or 5 minutes total with automation)
- **GitHub integration:** 5 minutes
- **Total:** ~30 minutes

## Planning Your Server Setup

### Server Specifications

For the CS4445 Sub Server application, we recommend:

| Component | Minimum | Recommended | For Heavy Load |
|-----------|---------|-------------|----------------|
| **CPU** | 2 cores | 4 cores | 8 cores |
| **RAM** | 4 GB | 8 GB | 16 GB |
| **Disk** | 20 GB SSD | 50 GB SSD | 100 GB SSD |
| **OS** | Ubuntu 20.04 | Ubuntu 22.04 LTS | Ubuntu 22.04 LTS |

### Recommended Setup for 5 Servers

**Staging Servers (3):**
- CPU: 2-4 cores
- RAM: 4-8 GB
- Disk: 20 GB SSD
- Purpose: Testing, development, load testing

**Production Servers (2):**
- CPU: 4-8 cores
- RAM: 8-16 GB
- Disk: 50 GB SSD
- Purpose: Live application, high availability

### Cost Estimation

Typical CKey.com pricing (approximate):
- Small server (2 CPU, 4GB RAM): ~$10-20/month
- Medium server (4 CPU, 8GB RAM): ~$20-40/month
- Total for 5 servers: ~$100-150/month

## Creating Servers on CKey.com

### Step 1: Log in to CKey.com

1. Go to https://ckey.com
2. Log in to your account
3. Navigate to **Cloud Servers** or **VPS** section

### Step 2: Create First Server (Staging Server 1)

#### Basic Configuration

1. Click **"Create Server"** or **"New VPS"**
2. **Server Name:** `cs4445-staging-1`
3. **Region:** Choose closest to your location
   - Example: `Singapore`, `US-East`, `EU-West`
4. **Operating System:**
   - Select **Ubuntu 22.04 LTS** (recommended)
   - OR **Ubuntu 20.04 LTS**
5. **Server Size:**
   - CPU: 2-4 cores
   - RAM: 4 GB
   - Disk: 20 GB SSD

#### Network Configuration

6. **Firewall Rules:** Configure or note to configure later
   - SSH: Port 22
   - HTTP: Port 8080 (application)
   - Prometheus: Port 9090
   - Grafana: Port 3000
   - PostgreSQL: Port 5432 (only if external access needed)

7. **SSH Key:**
   - **Option A:** Upload your existing SSH public key
   - **Option B:** Generate new key pair (CKey will provide)
   - **Option C:** Use password authentication (will change to SSH key later)

#### Advanced Options

8. **Hostname:** `staging-1.cs4445.local`
9. **Enable IPv4:** Yes
10. **Enable IPv6:** Optional
11. **Backup:** Optional (recommended for production)
12. **Monitoring:** Enable if available

13. Click **"Create Server"** or **"Deploy"**

#### Note Your Server Details

After creation, note down:
```
Server Name: cs4445-staging-1
Public IP: XXX.XXX.XXX.XXX
Username: root or ubuntu
Password/SSH Key: (as configured)
Region: (your selected region)
```

### Step 3: Create Remaining Servers

Repeat Step 2 for each server:

**Staging Server 2:**
- Name: `cs4445-staging-2`
- Same specs as staging-1
- Note IP address

**Staging Server 3:**
- Name: `cs4445-staging-3`
- Same specs as staging-1
- Note IP address

**Production Server 1:**
- Name: `cs4445-prod-1`
- Higher specs (4 CPU, 8GB RAM, 50GB disk)
- Note IP address

**Production Server 2:**
- Name: `cs4445-prod-2`
- Same specs as prod-1
- Note IP address

### Step 4: Document All Server IPs

Create a file `server-inventory.txt`:

```
# CS4445 Sub Server - Server Inventory
# Created: 2025-12-14

STAGING SERVERS:
cs4445-staging-1    XXX.XXX.XXX.101    ubuntu    Singapore
cs4445-staging-2    XXX.XXX.XXX.102    ubuntu    Singapore
cs4445-staging-3    XXX.XXX.XXX.103    ubuntu    Singapore

PRODUCTION SERVERS:
cs4445-prod-1       XXX.XXX.XXX.201    ubuntu    Singapore
cs4445-prod-2       XXX.XXX.XXX.202    ubuntu    Singapore

CREDENTIALS:
SSH Key: ~/.ssh/ckey-deploy
Initial Password: (if using password auth)

GITHUB SECRETS VALUES:
STAGING_SERVER_IPS=XXX.XXX.XXX.101,XXX.XXX.XXX.102,XXX.XXX.XXX.103
PRODUCTION_SERVER_IPS=XXX.XXX.XXX.201,XXX.XXX.XXX.202
DEPLOY_USER=ubuntu
DEPLOY_PATH=/app/cs4445-sub-server
```

## Server Configuration

### Method 1: Automated Setup (Recommended)

Save this script as `setup-ckey-servers.sh`:

```bash
#!/bin/bash

# CKey Server Setup Script for CS4445 Sub Server
# This script configures all servers automatically

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_step() {
    echo -e "${BLUE}==>${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Configuration
GITHUB_REPO="YOUR_USERNAME/cs4445-sub-server"
SSH_USER="ubuntu"  # or "root" depending on your CKey setup
DEPLOY_PATH="/app/cs4445-sub-server"

# Server IPs - UPDATE THESE WITH YOUR ACTUAL IPs
STAGING_SERVERS=(
    "XXX.XXX.XXX.101"  # cs4445-staging-1
    "XXX.XXX.XXX.102"  # cs4445-staging-2
    "XXX.XXX.XXX.103"  # cs4445-staging-3
)

PRODUCTION_SERVERS=(
    "XXX.XXX.XXX.201"  # cs4445-prod-1
    "XXX.XXX.XXX.202"  # cs4445-prod-2
)

# Combine all servers
ALL_SERVERS=("${STAGING_SERVERS[@]}" "${PRODUCTION_SERVERS[@]}")

print_step "CS4445 Sub Server - CKey.com Server Setup"
echo "Total servers to configure: ${#ALL_SERVERS[@]}"
echo ""

# Generate SSH key if doesn't exist
if [ ! -f ~/.ssh/ckey-deploy ]; then
    print_step "Generating SSH deployment key..."
    ssh-keygen -t ed25519 -C "ckey-deployment" -f ~/.ssh/ckey-deploy -N ""
    print_success "SSH key generated: ~/.ssh/ckey-deploy"
    echo ""
fi

PUBLIC_KEY=$(cat ~/.ssh/ckey-deploy.pub)

# Function to setup a single server
setup_server() {
    local SERVER_IP=$1
    local SERVER_NUM=$2

    print_step "Setting up server $SERVER_NUM: $SERVER_IP"

    # Try to connect (may need password first time if not using key)
    if ! ssh -o ConnectTimeout=5 -o StrictHostKeyChecking=no $SSH_USER@$SERVER_IP "echo 'Connection test'" 2>/dev/null; then
        print_error "Cannot connect to $SERVER_IP"
        echo "Please ensure:"
        echo "  1. Server is running"
        echo "  2. You can SSH: ssh $SSH_USER@$SERVER_IP"
        echo "  3. Firewall allows SSH (port 22)"
        return 1
    fi

    # Run setup commands on server
    ssh -o StrictHostKeyChecking=no $SSH_USER@$SERVER_IP << EOF
        set -e

        # Update system
        echo "Updating system packages..."
        sudo apt update && sudo apt upgrade -y

        # Install required packages
        echo "Installing required packages..."
        sudo apt install -y curl git wget software-properties-common jq

        # Install Docker
        echo "Installing Docker..."
        if ! command -v docker &> /dev/null; then
            curl -fsSL https://get.docker.com | sudo sh
            sudo usermod -aG docker $SSH_USER
            echo "Docker installed"
        else
            echo "Docker already installed"
        fi

        # Install Docker Compose
        echo "Installing Docker Compose..."
        if ! command -v docker-compose &> /dev/null; then
            sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-\$(uname -s)-\$(uname -m)" -o /usr/local/bin/docker-compose
            sudo chmod +x /usr/local/bin/docker-compose
            echo "Docker Compose installed"
        else
            echo "Docker Compose already installed"
        fi

        # Setup SSH key
        echo "Configuring SSH key..."
        mkdir -p ~/.ssh
        chmod 700 ~/.ssh

        # Add deployment key
        echo "$PUBLIC_KEY" >> ~/.ssh/authorized_keys
        sort -u ~/.ssh/authorized_keys -o ~/.ssh/authorized_keys
        chmod 600 ~/.ssh/authorized_keys

        # Setup application directory
        echo "Setting up application directory..."
        sudo mkdir -p $DEPLOY_PATH
        sudo chown $SSH_USER:$SSH_USER $DEPLOY_PATH

        # Clone repository
        echo "Cloning repository..."
        if [ ! -d "$DEPLOY_PATH/.git" ]; then
            git clone https://github.com/$GITHUB_REPO.git $DEPLOY_PATH
        else
            cd $DEPLOY_PATH
            git fetch origin
            git checkout main
            git pull origin main
        fi

        # Create .env file
        echo "Creating .env file..."
        cat > $DEPLOY_PATH/.env << 'ENVEOF'
# Environment Configuration
POSTGRES_DB=mydatabase
POSTGRES_USER=myuser
POSTGRES_PASSWORD=changeme_secure_password_here
GRAFANA_USER=admin
GRAFANA_PASSWORD=changeme_secure_password_here
SPRING_PROFILE=production
LOG_LEVEL=INFO
APP_LOG_LEVEL=INFO
ENVEOF

        # Configure firewall
        echo "Configuring firewall..."
        sudo ufw --force enable
        sudo ufw allow 22/tcp    # SSH
        sudo ufw allow 8080/tcp  # Application
        sudo ufw allow 9090/tcp  # Prometheus
        sudo ufw allow 3000/tcp  # Grafana
        sudo ufw status

        # Verify Docker
        docker --version
        docker-compose --version

        echo "✓ Server setup complete!"
EOF

    if [ $? -eq 0 ]; then
        print_success "Server $SERVER_IP configured successfully"
    else
        print_error "Failed to configure $SERVER_IP"
        return 1
    fi

    echo ""
}

# Setup all servers
echo "Starting server configuration..."
echo ""

FAILED_SERVERS=()
for i in "${!ALL_SERVERS[@]}"; do
    if ! setup_server "${ALL_SERVERS[$i]}" "$((i+1))"; then
        FAILED_SERVERS+=("${ALL_SERVERS[$i]}")
    fi
    sleep 2
done

echo ""
print_step "Setup Summary"
echo "Total servers: ${#ALL_SERVERS[@]}"
echo "Failed servers: ${#FAILED_SERVERS[@]}"

if [ ${#FAILED_SERVERS[@]} -gt 0 ]; then
    print_error "Failed servers:"
    for server in "${FAILED_SERVERS[@]}"; do
        echo "  - $server"
    done
    exit 1
fi

print_success "All servers configured successfully!"
echo ""

print_step "Next Steps:"
echo "1. Update GitHub secrets with server IPs:"
echo "   STAGING_SERVER_IPS=${STAGING_SERVERS[0]},${STAGING_SERVERS[1]},${STAGING_SERVERS[2]}"
echo "   PRODUCTION_SERVER_IPS=${PRODUCTION_SERVERS[0]},${PRODUCTION_SERVERS[1]}"
echo ""
echo "2. Add SSH private key to GitHub:"
echo "   gh secret set DEPLOY_SSH_KEY < ~/.ssh/ckey-deploy"
echo ""
echo "3. Test deployment:"
echo "   git push origin main"
echo ""
echo "SSH Private Key Location: ~/.ssh/ckey-deploy"
echo "SSH Public Key: $(cat ~/.ssh/ckey-deploy.pub)"
```

**Usage:**

```bash
# 1. Save the script
nano setup-ckey-servers.sh

# 2. Update the IP addresses in the script with your actual IPs
# Edit these lines:
#   STAGING_SERVERS=(...)
#   PRODUCTION_SERVERS=(...)

# 3. Update GITHUB_REPO with your username
# Edit: GITHUB_REPO="YOUR_USERNAME/cs4445-sub-server"

# 4. Make executable
chmod +x setup-ckey-servers.sh

# 5. Run the script
./setup-ckey-servers.sh
```

The script will:
- ✅ Generate SSH key for deployment
- ✅ Update all servers
- ✅ Install Docker and Docker Compose
- ✅ Setup SSH keys
- ✅ Clone the repository
- ✅ Create .env files
- ✅ Configure firewall
- ✅ Setup all 5 servers automatically!

### Method 2: Manual Setup (Individual Server)

If you prefer to configure servers one by one:

```bash
# Connect to server
ssh ubuntu@XXX.XXX.XXX.101

# Update system
sudo apt update && sudo apt upgrade -y

# Install required packages
sudo apt install -y curl git wget software-properties-common jq

# Install Docker
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Logout and login for docker group to take effect
exit
ssh ubuntu@XXX.XXX.XXX.101

# Setup application directory
sudo mkdir -p /app
sudo chown ubuntu:ubuntu /app
cd /app

# Clone repository
git clone https://github.com/YOUR_USERNAME/cs4445-sub-server.git
cd cs4445-sub-server

# Create .env file
cat > .env << 'EOF'
POSTGRES_PASSWORD=your_secure_password_here
GRAFANA_PASSWORD=your_secure_password_here
SPRING_PROFILE=production
EOF

# Configure firewall
sudo ufw allow 22/tcp
sudo ufw allow 8080/tcp
sudo ufw allow 9090/tcp
sudo ufw allow 3000/tcp
sudo ufw enable

# Verify
docker --version
docker-compose --version

# Exit
exit

# Repeat for all 5 servers
```

## GitHub Integration

### Step 1: Add Server IPs to GitHub Secrets

```bash
# Install GitHub CLI if not installed
# Ubuntu: sudo apt install gh
# Mac: brew install gh

# Login
gh auth login

# Add staging server IPs (comma-separated)
gh secret set STAGING_SERVER_IPS -b "XXX.XXX.XXX.101,XXX.XXX.XXX.102,XXX.XXX.XXX.103"

# Add production server IPs
gh secret set PRODUCTION_SERVER_IPS -b "XXX.XXX.XXX.201,XXX.XXX.XXX.202"

# Add deploy user
gh secret set DEPLOY_USER -b "ubuntu"

# Add deploy path
gh secret set DEPLOY_PATH -b "/app/cs4445-sub-server"

# Add SSH private key
gh secret set DEPLOY_SSH_KEY < ~/.ssh/ckey-deploy

# Verify all secrets
gh secret list
```

Expected output:
```
DEPLOY_SSH_KEY         Updated YYYY-MM-DD
DEPLOY_USER           Updated YYYY-MM-DD
DEPLOY_PATH           Updated YYYY-MM-DD
STAGING_SERVER_IPS    Updated YYYY-MM-DD
PRODUCTION_SERVER_IPS Updated YYYY-MM-DD
```

### Step 2: Test SSH Connection

```bash
# Test all staging servers
for ip in XXX.XXX.XXX.{101..103}; do
    echo "Testing $ip..."
    ssh -i ~/.ssh/ckey-deploy ubuntu@$ip "echo '✓ Connected to $ip'"
done

# Test all production servers
for ip in XXX.XXX.XXX.{201..202}; do
    echo "Testing $ip..."
    ssh -i ~/.ssh/ckey-deploy ubuntu@$ip "echo '✓ Connected to $ip'"
done
```

All should show: `✓ Connected to XXX.XXX.XXX.XXX`

## Testing Your Setup

### Test 1: Manual Deployment to One Server

```bash
# Test deploy to first staging server
./scripts/deploy-to-server.sh \
    XXX.XXX.XXX.101 \
    ubuntu \
    /app/cs4445-sub-server \
    main \
    staging
```

Expected output:
```
ℹ️  Starting deployment to XXX.XXX.XXX.101
✅ Connected to XXX.XXX.XXX.101
✅ Backup completed
✅ Latest code and image pulled
✅ Old containers stopped
✅ New containers started
✅ Application started successfully
✅ Health check passed
=========================================
✅ Deployment to XXX.XXX.XXX.101 completed!
=========================================
```

### Test 2: Multi-Server Deployment via GitHub

```bash
# Create test commit
echo "# CKey.com setup test" >> README.md
git add README.md
git commit -m "Test CKey multi-server deployment"
git push origin main
```

**Monitor in GitHub Actions:**
1. Go to https://github.com/YOUR_USERNAME/cs4445-sub-server/actions
2. Click on the running workflow
3. Watch deployment to all 3 staging servers

Expected:
```
🚀 Starting parallel deployment to 3 staging servers...
Deploying to XXX.XXX.XXX.101 in background...
Deploying to XXX.XXX.XXX.102 in background...
Deploying to XXX.XXX.XXX.103 in background...
✅ All parallel deployments completed

🏥 Running health checks on all servers...
✅ XXX.XXX.XXX.101 is healthy
✅ XXX.XXX.XXX.102 is healthy
✅ XXX.XXX.XXX.103 is healthy
✅ All servers are healthy
```

### Test 3: Verify Applications

```bash
# Check health on all servers
for ip in XXX.XXX.XXX.{101..103}; do
    echo "Checking $ip..."
    curl http://$ip:8080/actuator/health
    echo ""
done

# Test API endpoint
for ip in XXX.XXX.XXX.{101..103}; do
    echo "Testing API on $ip..."
    curl -X POST http://$ip:8080/api/v1/fakePacket \
        -H "Content-Type: application/json" \
        -d '{"packetId":"test","cpuIntensity":3,"ramIntensity":3}'
    echo ""
done
```

### Test 4: Access Monitoring

Open in browser:
- **Staging Server 1 Grafana:** http://XXX.XXX.XXX.101:3000 (admin/admin)
- **Staging Server 2 Grafana:** http://XXX.XXX.XXX.102:3000
- **Staging Server 3 Grafana:** http://XXX.XXX.XXX.103:3000

You should see the dashboard with real-time metrics.

## Troubleshooting

### Issue 1: Cannot Connect via SSH

**Symptoms:**
```
Connection refused
Permission denied
```

**Solutions:**

1. **Check server is running on CKey.com:**
   - Log in to CKey dashboard
   - Verify server status is "Running"
   - Check public IP is correct

2. **Check firewall on CKey:**
   - CKey dashboard → Server → Firewall
   - Ensure port 22 is allowed
   - Ensure your IP is not blocked

3. **Verify SSH key:**
   ```bash
   # Test with password (if enabled)
   ssh ubuntu@XXX.XXX.XXX.101

   # Add your public key manually
   ssh-copy-id -i ~/.ssh/ckey-deploy.pub ubuntu@XXX.XXX.XXX.101
   ```

4. **Check CKey network settings:**
   - Ensure public IP is assigned
   - Check if server is in correct VPC/network
   - Verify no firewall rules blocking access

### Issue 2: Docker Installation Fails

**Symptoms:**
```
Cannot install docker
Permission denied
```

**Solutions:**

```bash
# Manual Docker installation
ssh ubuntu@XXX.XXX.XXX.101

# Remove old versions
sudo apt remove docker docker-engine docker.io containerd runc

# Install prerequisites
sudo apt update
sudo apt install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

# Add Docker GPG key
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Add Docker repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Add user to docker group
sudo usermod -aG docker ubuntu

# Test
sudo docker run hello-world
```

### Issue 3: Repository Clone Fails

**Symptoms:**
```
Permission denied (publickey)
fatal: Could not read from remote repository
```

**Solutions:**

1. **Use HTTPS instead of SSH:**
   ```bash
   # Instead of:
   git clone git@github.com:YOUR_USERNAME/cs4445-sub-server.git

   # Use:
   git clone https://github.com/YOUR_USERNAME/cs4445-sub-server.git
   ```

2. **For private repositories:**
   ```bash
   # Generate SSH key on server
   ssh-keygen -t ed25519 -C "server@cs4445"

   # Add to GitHub
   cat ~/.ssh/id_ed25519.pub
   # Copy and add to GitHub → Settings → SSH Keys
   ```

### Issue 4: Health Check Fails

**Symptoms:**
```
curl: (7) Failed to connect to XXX.XXX.XXX.101 port 8080
```

**Solutions:**

1. **Check application is running:**
   ```bash
   ssh ubuntu@XXX.XXX.XXX.101
   docker ps
   docker logs cs4445-app
   ```

2. **Check firewall:**
   ```bash
   # On server
   sudo ufw status
   sudo ufw allow 8080/tcp

   # On CKey dashboard
   # Security → Firewall → Add rule for port 8080
   ```

3. **Check application logs:**
   ```bash
   ssh ubuntu@XXX.XXX.XXX.101
   cd /app/cs4445-sub-server
   docker-compose logs app
   ```

### Issue 5: Out of Disk Space

**Symptoms:**
```
No space left on device
```

**Solutions:**

```bash
# Check disk usage
ssh ubuntu@XXX.XXX.XXX.101
df -h

# Clean Docker
docker system prune -a -f --volumes

# Clean old logs
sudo journalctl --vacuum-time=7d

# If still full, upgrade disk on CKey.com:
# CKey dashboard → Server → Resize → Increase disk
```

## CKey.com Specific Tips

### 1. Server Snapshots/Backups

**Create snapshot after initial setup:**
1. CKey dashboard → Server → Snapshots
2. Create snapshot: "cs4445-base-setup"
3. Use this to quickly create new servers

### 2. Load Balancer Setup (Optional)

If CKey offers load balancer:
1. CKey dashboard → Load Balancers
2. Create load balancer
3. Add all production servers as backends
4. Configure health check: `/actuator/health`
5. Use LB IP for production access

### 3. Monitoring and Alerts

If CKey offers monitoring:
1. Enable server monitoring
2. Set alerts for:
   - CPU > 80%
   - RAM > 90%
   - Disk > 80%
   - Server down

### 4. Scaling

**Add more servers later:**
1. Create new server on CKey
2. Run setup script with new IP
3. Add IP to `STAGING_SERVER_IPS` or `PRODUCTION_SERVER_IPS`
4. Push to deploy

**Remove server:**
1. Remove IP from GitHub secret
2. Delete server on CKey
3. Update documentation

### 5. Cost Optimization

- **Dev servers:** Stop when not in use
- **Staging:** Can use smaller instances
- **Production:** Keep running, but right-size
- **Use reserved instances** if CKey offers them

## Quick Reference

### Server Access

```bash
# SSH to servers
ssh -i ~/.ssh/ckey-deploy ubuntu@XXX.XXX.XXX.101  # staging-1
ssh -i ~/.ssh/ckey-deploy ubuntu@XXX.XXX.XXX.102  # staging-2
ssh -i ~/.ssh/ckey-deploy ubuntu@XXX.XXX.XXX.103  # staging-3
ssh -i ~/.ssh/ckey-deploy ubuntu@XXX.XXX.XXX.201  # prod-1
ssh -i ~/.ssh/ckey-deploy ubuntu@XXX.XXX.XXX.202  # prod-2
```

### Application URLs

```
Staging:
http://XXX.XXX.XXX.101:8080  # staging-1 app
http://XXX.XXX.XXX.101:3000  # staging-1 grafana
http://XXX.XXX.XXX.101:9090  # staging-1 prometheus

Production:
http://XXX.XXX.XXX.201:8080  # prod-1 app
http://XXX.XXX.XXX.201:3000  # prod-1 grafana
```

### Common Commands

```bash
# Check all servers
for ip in XXX.XXX.XXX.{101..103} XXX.XXX.XXX.{201..202}; do
    echo "Server: $ip"
    ssh ubuntu@$ip "hostname && docker ps"
done

# Update all servers
for ip in XXX.XXX.XXX.{101..103} XXX.XXX.XXX.{201..202}; do
    ssh ubuntu@$ip "cd /app/cs4445-sub-server && git pull"
done

# Check health all servers
for ip in XXX.XXX.XXX.{101..103}; do
    curl -s http://$ip:8080/actuator/health | jq .status
done
```

## Summary Checklist

### CKey.com Setup
- [ ] Created 5 servers on CKey.com
- [ ] Noted all server IPs
- [ ] Configured firewall rules (ports 22, 8080, 3000, 9090)
- [ ] Verified all servers are running

### Server Configuration
- [ ] Generated SSH deployment key
- [ ] Ran automated setup script (or manual setup)
- [ ] Installed Docker on all servers
- [ ] Installed Docker Compose on all servers
- [ ] Cloned repository to all servers
- [ ] Created .env files
- [ ] Verified SSH access to all servers

### GitHub Integration
- [ ] Added STAGING_SERVER_IPS secret
- [ ] Added PRODUCTION_SERVER_IPS secret
- [ ] Added DEPLOY_SSH_KEY secret
- [ ] Added DEPLOY_USER secret
- [ ] Added DEPLOY_PATH secret
- [ ] Verified all secrets in GitHub

### Testing
- [ ] Tested SSH to all servers
- [ ] Deployed to one server manually
- [ ] Triggered multi-server deployment
- [ ] Verified health checks pass
- [ ] Accessed Grafana dashboards
- [ ] Tested application endpoints

**You're all set!** 🎉

## Next Steps

1. **Read the deployment guide:** `docs/multi-server-deployment-guide-v2.md`
2. **Test load balancing** (if using CKey load balancer)
3. **Set up monitoring alerts**
4. **Create server snapshots** for backup
5. **Document your specific CKey configuration**

Happy deploying on CKey.com! 🚀
