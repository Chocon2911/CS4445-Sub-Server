# GitHub Setup Guide v2 - Multi-Server Deployment

Complete guide for setting up GitHub repository, secrets, and configuration for multi-server deployment.

## Table of Contents

1. [What's New in v2](#whats-new-in-v2)
2. [Quick Setup Checklist](#quick-setup-checklist)
3. [Repository Setup](#repository-setup)
4. [Required Secrets Configuration](#required-secrets-configuration)
5. [Optional Secrets](#optional-secrets)
6. [Environment Configuration](#environment-configuration)
7. [Verification](#verification)
8. [Common Issues](#common-issues)

## What's New in v2

### v1 (Single Server):
- One IP per environment
- SSH to single server
- Simple deployment

### v2 (Multi-Server):
- **Multiple IPs** per environment (comma-separated)
- Deploy to **all servers** automatically
- **Parallel** or **sequential** deployment modes
- **Blue-green** deployment for production
- Individual **health checks** for each server
- Support for **self-hosted runners**

## Quick Setup Checklist

**Minimum Required (5 minutes):**
- [ ] Create GitHub repository
- [ ] Enable workflow permissions
- [ ] Generate SSH key
- [ ] Add `DEPLOY_SSH_KEY` secret
- [ ] Add `DEPLOY_USER` secret
- [ ] Add `DEPLOY_PATH` secret
- [ ] Add `STAGING_SERVER_IPS` secret (comma-separated)
- [ ] Add `PRODUCTION_SERVER_IPS` secret (comma-separated)
- [ ] Push code and test deployment

**That's it!** 🎉

## Repository Setup

### Step 1: Create Repository (If Not Done)

```bash
# Navigate to project
cd /path/to/CS4445-Sub-Server

# Initialize git
git init

# Create .gitignore (should already exist)
cat > .gitignore << 'EOF'
.env
.env.*
!.env.example
.DS_Store
node_modules/
target/
.idea/
*.iml
*.log
EOF

# Initial commit
git add .
git commit -m "Initial commit with multi-server deployment"

# Create GitHub repo (via web or CLI)
gh repo create cs4445-sub-server --public --source=. --remote=origin

# Or add remote manually
git remote add origin https://github.com/YOUR_USERNAME/cs4445-sub-server.git

# Push
git branch -M main
git push -u origin main
```

### Step 2: Enable Workflow Permissions

1. Go to **Settings** → **Actions** → **General**
2. Scroll to **"Workflow permissions"**
3. Select **"Read and write permissions"**
4. ✅ Check **"Allow GitHub Actions to create and approve pull requests"**
5. Click **"Save"**

## Required Secrets Configuration

Go to: **Repository → Settings → Secrets and variables → Actions**

### 1. DEPLOY_SSH_KEY

SSH private key for deploying to all servers.

#### Generate SSH Key

```bash
# On your local machine
cd ~/.ssh

# Generate new deployment key (press Enter for no passphrase)
ssh-keygen -t ed25519 -C "github-deployment-v2" -f github-deploy -N ""

# View private key (for GitHub secret)
cat github-deploy

# View public key (for servers)
cat github-deploy.pub
```

#### Add Private Key to GitHub

1. Copy the **entire output** of `cat github-deploy` including:
   ```
   -----BEGIN OPENSSH PRIVATE KEY-----
   ...
   -----END OPENSSH PRIVATE KEY-----
   ```
2. Go to Settings → Secrets and variables → Actions
3. Click **"New repository secret"**
4. Name: `DEPLOY_SSH_KEY`
5. Value: Paste the complete private key
6. Click **"Add secret"**

#### Add Public Key to All Servers

On **each server**:

```bash
# Create .ssh directory if needed
mkdir -p ~/.ssh
chmod 700 ~/.ssh

# Add public key (paste the content of github-deploy.pub)
cat >> ~/.ssh/authorized_keys << 'EOF'
ssh-ed25519 AAAA... github-deployment-v2
EOF

# Set permissions
chmod 600 ~/.ssh/authorized_keys

# Test connection from local machine
ssh -i ~/.ssh/github-deploy ubuntu@SERVER_IP "echo 'SSH works!'"
```

**Automation Script** for multiple servers:

```bash
#!/bin/bash
# add-ssh-key-to-servers.sh

PUBLIC_KEY=$(cat ~/.ssh/github-deploy.pub)
SERVERS="192.168.1.101 192.168.1.102 192.168.1.103"
USER="ubuntu"

for server in $SERVERS; do
    echo "Adding SSH key to $server..."
    ssh $USER@$server "mkdir -p ~/.ssh && chmod 700 ~/.ssh && echo '$PUBLIC_KEY' >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys"
    echo "✅ Done: $server"
done

echo "Testing connections..."
for server in $SERVERS; do
    ssh -i ~/.ssh/github-deploy $USER@$server "echo '✅ $server connection OK'"
done
```

### 2. DEPLOY_USER

The SSH username for deployment.

**Common values:**
- `ubuntu` (Ubuntu servers)
- `ec2-user` (Amazon Linux)
- `admin` (Debian)
- `deployer` (custom deploy user)
- Your username

**Add to GitHub:**
1. New repository secret
2. Name: `DEPLOY_USER`
3. Value: `ubuntu` (or your username)
4. Add secret

### 3. DEPLOY_PATH

Directory where the application is located on servers.

**Standard path:** `/app/cs4445-sub-server`

**Alternative paths:**
- `/home/deployer/cs4445-sub-server`
- `/opt/cs4445-sub-server`
- `/var/www/cs4445-sub-server`

**Important:** All servers must have the repository at this path!

**Prepare servers:**
```bash
# On each server
sudo mkdir -p /app
sudo chown $USER:$USER /app
cd /app
git clone https://github.com/YOUR_USERNAME/cs4445-sub-server.git
```

**Add to GitHub:**
1. New repository secret
2. Name: `DEPLOY_PATH`
3. Value: `/app/cs4445-sub-server`
4. Add secret

### 4. STAGING_SERVER_IPS ⭐ NEW!

**Comma-separated list** of staging server IP addresses.

**Format:**
```
# No spaces (recommended)
192.168.1.101,192.168.1.102,192.168.1.103

# With spaces (also works - script trims them)
192.168.1.101, 192.168.1.102, 192.168.1.103

# Single server
192.168.1.101

# Many servers
10.0.1.10,10.0.1.11,10.0.1.12,10.0.1.13,10.0.1.14,10.0.1.15
```

**Examples:**

| Scenario | Value |
|----------|-------|
| 1 staging server | `192.168.1.101` |
| 3 staging servers | `192.168.1.101,192.168.1.102,192.168.1.103` |
| 5 staging servers | `10.0.1.1,10.0.1.2,10.0.1.3,10.0.1.4,10.0.1.5` |
| 10 staging servers | `10.0.{1..10}` expanded |

**Add to GitHub:**
1. New repository secret
2. Name: `STAGING_SERVER_IPS`
3. Value: Your comma-separated IPs
4. Add secret

### 5. PRODUCTION_SERVER_IPS ⭐ NEW!

Same format as staging, but for production servers.

**Examples:**
```
# 3 production servers
192.168.100.1,192.168.100.2,192.168.100.3

# 6 production servers in two regions
us-east: 10.1.1.1,10.1.1.2,10.1.1.3
us-west: 10.2.1.1,10.2.1.2,10.2.1.3
Combined: 10.1.1.1,10.1.1.2,10.1.1.3,10.2.1.1,10.2.1.2,10.2.1.3
```

**Add to GitHub:**
1. New repository secret
2. Name: `PRODUCTION_SERVER_IPS`
3. Value: Your comma-separated production IPs
4. Add secret

## Optional Secrets

### SLACK_WEBHOOK

For Slack notifications on deployment.

**Setup:**
1. Go to your Slack workspace
2. Create Incoming Webhook: https://api.slack.com/messaging/webhooks
3. Select channel (e.g., #deployments)
4. Copy Webhook URL (looks like: `https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXX`)

**Add to GitHub:**
1. New repository secret
2. Name: `SLACK_WEBHOOK`
3. Value: Webhook URL
4. Add secret

**Enable in workflow:**
Uncomment the Slack notification section in `.github/workflows/cd-v2-multi-server.yml`

### DISCORD_WEBHOOK

For Discord notifications.

**Setup:**
1. Go to Discord Server Settings
2. Integrations → Webhooks → New Webhook
3. Choose channel, copy URL

**Add to GitHub:**
1. New repository secret
2. Name: `DISCORD_WEBHOOK`
3. Value: Webhook URL
4. Add secret

## Environment Configuration

### Create GitHub Environments

Environments allow you to:
- Require manual approval for production
- Set environment-specific secrets
- Track deployment history

#### Create Staging Environment

1. Go to **Settings** → **Environments**
2. Click **"New environment"**
3. Name: `staging`
4. Click **"Configure environment"**
5. (Optional) Protection rules:
   - Deployment branches: Select `main` only
6. Click **"Save protection rules"**

#### Create Production Environment

1. New environment: `production`
2. **Important:** Add protection rules:
   - ✅ **Required reviewers**: Select yourself or team members
   - ✅ **Wait timer**: 5 minutes (gives you time to cancel)
   - ✅ **Deployment branches**: Only allow tags matching `v*`
3. Save protection rules

**Now production deployments require manual approval!**

### Environment-Specific Secrets (Optional)

You can override repository secrets per environment:

**Example: Different deploy users per environment**

1. Go to Environments → staging
2. Scroll to "Environment secrets"
3. Add secret:
   - Name: `DEPLOY_USER`
   - Value: `staging-deployer`
4. Repeat for production with `prod-deployer`

**Priority:** Environment secrets > Repository secrets

## Verification

### Step 1: Verify Secrets Exist

```bash
# Install GitHub CLI if not installed
# Ubuntu: sudo apt install gh
# Mac: brew install gh

# Login
gh auth login

# List secrets (values are hidden)
gh secret list

# Expected output:
# DEPLOY_SSH_KEY         Updated YYYY-MM-DD
# DEPLOY_USER           Updated YYYY-MM-DD
# DEPLOY_PATH           Updated YYYY-MM-DD
# STAGING_SERVER_IPS    Updated YYYY-MM-DD
# PRODUCTION_SERVER_IPS Updated YYYY-MM-DD
```

### Step 2: Test SSH Connectivity

```bash
# Create test script
cat > test-ssh-all.sh << 'EOF'
#!/bin/bash
STAGING="192.168.1.101,192.168.1.102,192.168.1.103"
SSH_KEY="~/.ssh/github-deploy"
USER="ubuntu"

IFS=',' read -ra SERVERS <<< "$STAGING"
echo "Testing SSH to ${#SERVERS[@]} servers..."

for server in "${SERVERS[@]}"; do
    server=$(echo "$server" | xargs)
    echo -n "Testing $server... "
    if ssh -i $SSH_KEY -o ConnectTimeout=5 -o StrictHostKeyChecking=no $USER@$server "echo 'OK'" 2>/dev/null; then
        echo "✅"
    else
        echo "❌ FAILED"
    fi
done
EOF

chmod +x test-ssh-all.sh
./test-ssh-all.sh
```

Expected output:
```
Testing SSH to 3 servers...
Testing 192.168.1.101... ✅
Testing 192.168.1.102... ✅
Testing 192.168.1.103... ✅
```

### Step 3: Verify Repository Exists on Servers

```bash
STAGING="192.168.1.101,192.168.1.102,192.168.1.103"
SSH_KEY="~/.ssh/github-deploy"
USER="ubuntu"
DEPLOY_PATH="/app/cs4445-sub-server"

IFS=',' read -ra SERVERS <<< "$STAGING"
for server in "${SERVERS[@]}"; do
    server=$(echo "$server" | xargs)
    echo -n "$server: "
    ssh -i $SSH_KEY $USER@$server "if [ -d $DEPLOY_PATH ]; then echo '✅ Repo exists'; else echo '❌ Missing repo'; fi"
done
```

### Step 4: Trigger Test Deployment

**Small test push:**
```bash
echo "# Test" >> README.md
git add README.md
git commit -m "Test multi-server deployment"
git push origin main
```

**Monitor:**
1. Go to GitHub → Actions
2. Click on running workflow
3. Watch "Deploy to Staging Servers" job
4. Should see:
   ```
   🚀 Starting parallel deployment to 3 staging servers...
   ✅ All servers deployed
   ✅ All servers are healthy
   ```

## Common Issues

### Issue 1: "Permission denied (publickey)"

**Cause:** SSH key not added to servers or wrong user

**Fix:**
```bash
# Re-add public key to all servers
PUBLIC_KEY=$(cat ~/.ssh/github-deploy.pub)
for server in 192.168.1.{101..103}; do
    ssh ubuntu@$server "echo '$PUBLIC_KEY' >> ~/.ssh/authorized_keys"
done

# Verify correct user
ssh -i ~/.ssh/github-deploy ubuntu@192.168.1.101 "whoami"
# Should output: ubuntu (or your DEPLOY_USER)
```

### Issue 2: "No such file or directory" (deploy path)

**Cause:** Repository not cloned to servers

**Fix:**
```bash
# Clone repo on all servers
for server in 192.168.1.{101..103}; do
    ssh ubuntu@$server "sudo mkdir -p /app && sudo chown ubuntu:ubuntu /app && cd /app && git clone https://github.com/YOUR_USERNAME/cs4445-sub-server.git"
done
```

### Issue 3: "Invalid IP format" in workflow

**Cause:** Typo in `STAGING_SERVER_IPS` or `PRODUCTION_SERVER_IPS`

**Fix:**
1. Go to Settings → Secrets → Actions
2. Click on the secret
3. Update value
4. Format: `IP1,IP2,IP3` (no spaces recommended)
5. Save

### Issue 4: One server fails, others succeed

**Symptom:**
```
✅ 192.168.1.101 deployed
❌ 192.168.1.102 FAILED
✅ 192.168.1.103 deployed
```

**Fix failed server:**
```bash
# Manual deploy to failed server
./scripts/deploy-to-server.sh \
    192.168.1.102 \
    ubuntu \
    /app/cs4445-sub-server \
    main \
    staging

# Check why it failed
ssh ubuntu@192.168.1.102 "docker logs cs4445-app --tail 100"
```

### Issue 5: Health check fails after deployment

**Cause:** Application not started or port not accessible

**Fix:**
```bash
# Check if app is running
ssh ubuntu@SERVER "docker ps | grep cs4445"

# Check logs
ssh ubuntu@SERVER "docker logs cs4445-app --tail 50"

# Check port
curl http://SERVER:8080/actuator/health

# Check firewall
ssh ubuntu@SERVER "sudo ufw status"
# Allow port if needed: sudo ufw allow 8080/tcp
```

## Complete Setup Example

Here's a complete setup from scratch for **3 staging servers** and **3 production servers**:

```bash
#!/bin/bash
# complete-setup.sh

# Configuration
STAGING_IPS="192.168.1.101,192.168.1.102,192.168.1.103"
PRODUCTION_IPS="192.168.100.1,192.168.100.2,192.168.100.3"
USER="ubuntu"
DEPLOY_PATH="/app/cs4445-sub-server"
GITHUB_REPO="YOUR_USERNAME/cs4445-sub-server"

echo "🚀 Complete Multi-Server Setup"
echo "Staging: $STAGING_IPS"
echo "Production: $PRODUCTION_IPS"
echo ""

# 1. Generate SSH key
echo "📝 Step 1: Generating SSH key..."
ssh-keygen -t ed25519 -C "github-deploy-v2" -f ~/.ssh/github-deploy -N ""
echo "✅ SSH key generated"
echo ""

# 2. Add public key to all servers
echo "🔑 Step 2: Adding public key to servers..."
PUBLIC_KEY=$(cat ~/.ssh/github-deploy.pub)

all_servers="$STAGING_IPS,$PRODUCTION_IPS"
IFS=',' read -ra SERVERS <<< "$all_servers"

for server in "${SERVERS[@]}"; do
    server=$(echo "$server" | xargs)
    echo "  Adding to $server..."
    ssh $USER@$server "mkdir -p ~/.ssh && echo '$PUBLIC_KEY' >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys && chmod 700 ~/.ssh"
done
echo "✅ Public keys added"
echo ""

# 3. Setup repository on all servers
echo "📦 Step 3: Cloning repository on servers..."
for server in "${SERVERS[@]}"; do
    server=$(echo "$server" | xargs)
    echo "  Setting up $server..."
    ssh $USER@$server << EOF
        sudo mkdir -p /app
        sudo chown $USER:$USER /app
        cd /app
        if [ ! -d cs4445-sub-server ]; then
            git clone https://github.com/$GITHUB_REPO.git
        else
            cd cs4445-sub-server
            git pull
        fi
EOF
done
echo "✅ Repository setup on all servers"
echo ""

# 4. Add secrets to GitHub
echo "🔐 Step 4: Adding secrets to GitHub..."
echo "Private key:"
gh secret set DEPLOY_SSH_KEY < ~/.ssh/github-deploy
gh secret set DEPLOY_USER -b "$USER"
gh secret set DEPLOY_PATH -b "$DEPLOY_PATH"
gh secret set STAGING_SERVER_IPS -b "$STAGING_IPS"
gh secret set PRODUCTION_SERVER_IPS -b "$PRODUCTION_IPS"
echo "✅ Secrets added to GitHub"
echo ""

# 5. Verify
echo "✅ Setup complete!"
echo ""
echo "Verify with:"
echo "  gh secret list"
echo "  ./test-ssh-all.sh"
echo ""
echo "Deploy with:"
echo "  git push origin main  (staging)"
echo "  git tag v1.0.0 && git push origin v1.0.0  (production)"
```

## Summary

### Required Secrets

| Secret Name | Description | Example Value |
|-------------|-------------|---------------|
| `DEPLOY_SSH_KEY` | SSH private key | `-----BEGIN OPENSSH PRIVATE KEY-----...` |
| `DEPLOY_USER` | SSH username | `ubuntu` |
| `DEPLOY_PATH` | Application path | `/app/cs4445-sub-server` |
| `STAGING_SERVER_IPS` | Staging IPs | `192.168.1.101,192.168.1.102,192.168.1.103` |
| `PRODUCTION_SERVER_IPS` | Production IPs | `192.168.100.1,192.168.100.2,192.168.100.3` |

### Quick Commands

```bash
# Generate SSH key
ssh-keygen -t ed25519 -f ~/.ssh/github-deploy -N ""

# Add to servers
ssh-copy-id -i ~/.ssh/github-deploy.pub ubuntu@SERVER_IP

# Add secrets via GitHub CLI
gh secret set DEPLOY_SSH_KEY < ~/.ssh/github-deploy
gh secret set STAGING_SERVER_IPS -b "IP1,IP2,IP3"

# Test deployment
git push origin main
```

You're all set for multi-server deployment! 🎉

**Next:** [Multi-Server Deployment Guide v2](multi-server-deployment-guide-v2.md)
