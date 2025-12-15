# New Server Setup - Complete Guide

**Follow these steps to set up a brand new server for CS4445 Sub Server deployment.**

## 📋 What You'll Need

Before starting, gather:
- ✅ CKey.com server access (panel login)
- ✅ Server hostname (e.g., `n1.ckey.vn`)
- ✅ SSH port (from CKey panel, maps to port 22)
- ✅ App port (from CKey panel, maps to port 8080)
- ✅ Your SSH key pair (`ckey-deploy` and `ckey-deploy.pub`)

## 🎯 Server Information Template

**Fill this out before you start:**

```
Server Name: _______________  (e.g., Server 1 - Staging)
Hostname:    _______________  (e.g., n1.ckey.vn)
SSH Port:    _______________  (e.g., 3494 - the one that maps to 22)
App Port:    _______________  (e.g., 3497 - the one that maps to 8080)
Username:    root            (CKey.com usually uses root)
```

## 🚀 Step-by-Step Setup

### Step 1: Find Your Ports in CKey Panel

1. Log in to [CKey.com](https://ckey.com/)
2. Go to your server panel
3. Look for "Port Mappings" or "Cổng" section

**Example port mapping:**
```
External Port → Internal Port
3494 → 22      ← This is your SSH PORT (write it down!)
3495 → 3000    (Grafana)
3496 → 7681    (Terminal)
3497 → 8080    ← This is your APP PORT (write it down!)
3498 → 9090    (Prometheus)
```

**Write down:**
- SSH Port: `_______` (the one mapping to 22)
- App Port: `_______` (the one mapping to 8080)

---

### Step 2: Test SSH Connection

Open your terminal and test the connection:

```bash
# Replace with YOUR ports and hostname
ssh -p 3494 -i ~/.ssh/ckey-deploy root@n1.ckey.vn
```

**If you see "Permission denied":**
- Check if key is in the right location: `ls -la ~/.ssh/ckey-deploy`
- Check key permissions: `chmod 600 ~/.ssh/ckey-deploy`
- Verify you're using the correct port (the one mapping to 22!)

**If you see "Connection refused":**
- Check if the SSH port is correct
- Make sure server is running in CKey panel

**Success looks like:**
```
Welcome to Ubuntu 22.04.3 LTS
root@server:~#
```

---

### Step 3: Add Your SSH Public Key to Server

**Get your public key first:**

On your **local computer**, get your public key:
```bash
cat ~/.ssh/ckey-deploy.pub
# Copy the ENTIRE output (starts with ssh-ed25519 or ssh-rsa)
```

**Example output:**
```
ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIGx... your@email.com
```

**On the server** (after SSH connection):

```bash
# Create .ssh directory if it doesn't exist
mkdir -p ~/.ssh
chmod 700 ~/.ssh

# Add your public key using echo
# IMPORTANT: Replace YOUR_PUBLIC_KEY_HERE with your actual key!
echo "YOUR_PUBLIC_KEY_HERE" >> ~/.ssh/authorized_keys

# Example (use YOUR key, not this one):
# echo "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIGx... your@email.com" >> ~/.ssh/authorized_keys

# Set correct permissions
chmod 600 ~/.ssh/authorized_keys
```

**Alternative method using cat (for multiple keys):**
```bash
# Create authorized_keys with cat heredoc
cat >> ~/.ssh/authorized_keys << 'EOF'
ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIGx... your@email.com
EOF

# Set permissions
chmod 600 ~/.ssh/authorized_keys
```

**Test the connection again:**
```bash
# Exit and reconnect (should work without password)
exit
ssh -p 3494 -i ~/.ssh/ckey-deploy root@n1.ckey.vn
```

✅ **Success:** You should connect without being asked for a password!

---

### Step 4: Update System Packages

```bash
# Update package list
apt update

# Upgrade installed packages (optional but recommended)
apt upgrade -y
```

**This takes:** ~2-5 minutes

---

### Step 5: Install Docker

```bash
# Install prerequisites
apt install -y ca-certificates curl gnupg lsb-release

# Add Docker's official GPG key
mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Set up Docker repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker
apt update
apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Verify Docker installation
docker --version
# Should show: Docker version 24.x.x or higher

docker compose version
# Should show: Docker Compose version v2.x.x or higher
```

**This takes:** ~3-5 minutes

---

### Step 5b: Configure Docker for CKey.com (IMPORTANT!)

**CKey.com servers run in containers**, so Docker needs special configuration:

```bash
# Create Docker daemon configuration
mkdir -p /etc/docker

cat > /etc/docker/daemon.json << 'EOF'
{
  "iptables": false,
  "ip-forward": false,
  "ip-masq": false,
  "bridge": "none",
  "storage-driver": "vfs"
}
EOF

# Verify the configuration
cat /etc/docker/daemon.json
```

**Why this is needed:**
- CKey.com doesn't allow iptables manipulation
- Bridge networking requires iptables
- Must use host networking instead

---

### Step 5c: Start Docker Daemon

```bash
# Kill any existing Docker processes
pkill -9 dockerd
pkill -9 containerd
sleep 3

# Start Docker daemon in background
dockerd > /tmp/docker.log 2>&1 &

# Wait for Docker to start
sleep 15

# Verify Docker is running
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

✅ **Success:** Docker is running!

**If Docker fails to start**, check logs:
```bash
cat /tmp/docker.log
```

See [SERVER-DEPLOYMENT-TROUBLESHOOTING.md](../SERVER-DEPLOYMENT-TROUBLESHOOTING.md) for detailed troubleshooting.

---

### Step 6: Create Application Directory

```bash
# Create app directory (this should match DEPLOY_PATH in GitHub secrets)
mkdir -p /app/cs4445-sub-server
cd /app/cs4445-sub-server

# Verify you're in the right place
pwd
# Should show: /app/cs4445-sub-server
```

---

### Step 7: Clone Repository

**Option A: If repo is public**
```bash
git clone https://github.com/YOUR_USERNAME/CS4445-Sub-Server.git .
```

**Option B: If repo is private (use SSH)**
```bash
# First, add deploy key to server
ssh-keygen -t ed25519 -f ~/.ssh/github-deploy -N ""

# Show the public key
cat ~/.ssh/github-deploy.pub
# Copy this and add it to GitHub → Settings → Deploy keys

# Clone with SSH
git clone git@github.com:YOUR_USERNAME/CS4445-Sub-Server.git .
```

**Option C: Use HTTPS with token**
```bash
# Clone with personal access token
git clone https://YOUR_TOKEN@github.com/YOUR_USERNAME/CS4445-Sub-Server.git .
```

**Verify clone:**
```bash
ls -la
# You should see: docker-compose.prod.yml, Dockerfile, src/, etc.
```

---

### Step 8: Configure Environment Variables

**Option A: Create .env file using cat (Recommended for minimal Ubuntu)**

```bash
# Create .env file with cat heredoc
cat > .env << 'EOF'
# Repository info
GITHUB_REPOSITORY=YOUR_USERNAME/CS4445-Sub-Server

# Image tag (will be updated by CI/CD)
IMAGE_TAG=latest

# Ports
APP_PORT=8080

# Database password
POSTGRES_PASSWORD=YOUR_SECURE_PASSWORD_HERE

# Grafana password
GRAFANA_PASSWORD=YOUR_SECURE_PASSWORD_HERE
EOF
```

**IMPORTANT:** Replace these values before running:
- `YOUR_USERNAME` → Your actual GitHub username
- `YOUR_SECURE_PASSWORD_HERE` → Strong passwords (different for each)

**Option B: Copy and edit manually (if you have vi/vim)**

```bash
# Copy example env file
cp .env.example .env

# Edit with vi (press 'i' to insert, 'Esc' then ':wq' to save)
vi .env

# Or install nano first if you prefer
apt install -y nano
nano .env
```

**Verify .env file created:**
```bash
cat .env
# Should show your configuration
```

---

### Step 9: Modify Docker Compose for CKey.com

**IMPORTANT:** Because CKey.com requires host networking, you need to modify `docker-compose.prod.yml`:

```bash
# Backup original file
cp docker-compose.prod.yml docker-compose.prod.yml.backup

# Add network_mode: host to all services
# You'll need to do this manually or use sed
```

**Quick fix with sed:**
```bash
# Add network_mode: host to each service
# (This is a simplified example - manual editing is recommended)
# See SERVER-DEPLOYMENT-TROUBLESHOOTING.md for complete examples
```

**Or manually edit** to add `network_mode: host` to each service:
```yaml
services:
  app:
    image: ...
    network_mode: host  # Add this line
    # Remove or comment out 'ports:' section (not needed with host mode)
```

**Test configuration:**
```bash
docker compose -f docker-compose.prod.yml config
# Should show the full configuration without errors
```

✅ **Success:** Configuration is valid!

**Note:** With host networking:
- No port mapping needed (app runs directly on server ports)
- Use `localhost` instead of service names for inter-service communication
- See [SERVER-DEPLOYMENT-TROUBLESHOOTING.md](../SERVER-DEPLOYMENT-TROUBLESHOOTING.md) for complete examples

---

### Step 10: Set Up Firewall (Optional but Recommended)

```bash
# Install UFW (Uncomplicated Firewall)
apt install -y ufw

# Allow SSH (IMPORTANT: Use your SSH port!)
ufw allow 3494/tcp  # Replace 3494 with YOUR SSH port!

# Allow application ports
ufw allow 3497/tcp  # App port
ufw allow 3495/tcp  # Grafana
ufw allow 3498/tcp  # Prometheus

# Enable firewall
ufw --force enable

# Check status
ufw status
```

**⚠️ IMPORTANT:** Make sure to allow your SSH port BEFORE enabling the firewall, or you'll lock yourself out!

---

### Step 11: Verify Server Setup

**Check everything is ready:**

```bash
# 1. Check Docker daemon is running
docker ps
# Should show: CONTAINER ID   IMAGE   ... (empty list is fine)
# If error: "Cannot connect to daemon" - restart Docker daemon

# 2. Check Docker version
docker --version
docker compose version

# 3. Check Docker configuration
cat /etc/docker/daemon.json
# Should show: iptables: false, bridge: none, etc.

# 4. Check directory
pwd  # Should be: /app/cs4445-sub-server
ls -la  # Should show project files

# 5. Check environment file
cat .env  # Should show your configurations

# 6. Check SSH key
ls -la ~/.ssh/authorized_keys  # Should exist

# 7. Check git
git status  # Should show clean working tree
```

**If Docker daemon not running:**
```bash
dockerd > /tmp/docker.log 2>&1 &
sleep 15
docker ps
```

✅ **All checks passed!** Server is ready for deployment!

---

## 📝 Quick Reference Card

**Save this for your server:**

```
Server: _______________ (e.g., n1.ckey.vn)
SSH Port: _______________ (maps to 22)
App Port: _______________ (maps to 8080)

SSH Command:
ssh -p YOUR_SSH_PORT -i ~/.ssh/ckey-deploy root@YOUR_HOSTNAME

App Directory:
/app/cs4445-sub-server

Health Check URL:
http://YOUR_HOSTNAME:YOUR_APP_PORT/actuator/health

Docker Commands:
docker ps                    # Show running containers
docker compose logs -f app   # Show app logs
docker compose restart       # Restart all services
```

---

## 🎯 What's Next?

After completing this server setup:

1. ✅ **Repeat for all servers** (if you have multiple)
2. ➡️ **Configure GitHub Secrets** - See [GITHUB-SECRETS-SETUP.md](./GITHUB-SECRETS-SETUP.md)
3. ➡️ **Test deployment** - See [TESTING-DEPLOYMENT.md](./TESTING-DEPLOYMENT.md)

---

## 🐛 Troubleshooting

### 📚 Complete Troubleshooting Guide

**For comprehensive Docker troubleshooting on CKey.com**, see:
**[SERVER-DEPLOYMENT-TROUBLESHOOTING.md](../SERVER-DEPLOYMENT-TROUBLESHOOTING.md)**

This guide covers:
- Docker daemon permission errors
- iptables issues
- Network configuration
- Container startup problems
- Complete working examples

---

### Issue: "nano: command not found" or "sudo: command not found"

**Cause:** Your server uses minimal Ubuntu Jammy (22.04) without these tools pre-installed

**Solution 1: Use alternatives (no installation needed)**
```bash
# Instead of nano, use echo or cat
# For authorized_keys:
echo "YOUR_SSH_PUBLIC_KEY" >> ~/.ssh/authorized_keys

# For .env file:
cat > .env << 'EOF'
YOUR_CONFIG_HERE
EOF

# Instead of sudo, login as root directly
# CKey.com uses root by default, so sudo is not needed
```

**Solution 2: Install nano if you prefer (optional)**
```bash
apt update
apt install -y nano

# Now you can use nano
nano ~/.ssh/authorized_keys
```

**Solution 3: Use vi/vim (pre-installed)**
```bash
vi ~/.ssh/authorized_keys
# Press 'i' to enter insert mode
# Paste your content
# Press 'Esc' then type ':wq' and press Enter to save
```

---

### Issue: "Cannot connect to the Docker daemon" or Docker not running

**Error:**
```
Cannot connect to the Docker daemon at unix:///var/run/docker.sock. Is the docker daemon running?
```

**Cause:** Docker daemon not started or crashed on CKey.com servers

**Solution:**

```bash
# Step 1: Create Docker configuration (if not already done)
mkdir -p /etc/docker
cat > /etc/docker/daemon.json << 'EOF'
{
  "iptables": false,
  "ip-forward": false,
  "ip-masq": false,
  "bridge": "none",
  "storage-driver": "vfs"
}
EOF

# Step 2: Kill any stuck processes
pkill -9 dockerd
pkill -9 containerd
sleep 3

# Step 3: Start Docker daemon
dockerd > /tmp/docker.log 2>&1 &

# Step 4: Wait for startup
sleep 15

# Step 5: Verify it's running
docker ps
```

**If still failing:**
```bash
# Check the logs
cat /tmp/docker.log | tail -50

# Look for errors about iptables or permissions
# See SERVER-DEPLOYMENT-TROUBLESHOOTING.md for detailed solutions
```

**Quick verification:**
```bash
# Check if dockerd process is running
ps aux | grep dockerd
```

---

### Issue: Can't SSH to server

**Check:**
```bash
# 1. Is key in the right place?
ls -la ~/.ssh/ckey-deploy

# 2. Are permissions correct?
chmod 600 ~/.ssh/ckey-deploy

# 3. Are you using the correct port?
# Check CKey panel for port mapping to 22

# 4. Try verbose mode
ssh -v -p YOUR_PORT -i ~/.ssh/ckey-deploy root@YOUR_HOST
```

### Issue: Docker installation fails

**Solution:**
```bash
# Remove old Docker versions
apt remove -y docker docker-engine docker.io containerd runc

# Try installation again from Step 5
```

### Issue: Permission denied when cloning repo

**Solution:**
```bash
# Make sure you're root or have permissions
whoami  # Should show: root

# Or use sudo
sudo git clone ...
```

### Issue: Can't access server after enabling firewall

**Solution:**
```bash
# You'll need to contact CKey support or use their web console
# Always make sure SSH port is allowed BEFORE enabling firewall!

# If you have web console access:
ufw disable
ufw allow YOUR_SSH_PORT/tcp
ufw enable
```

---

## 📚 Related Guides

- [GitHub Secrets Setup](./GITHUB-SECRETS-SETUP.md) - Configure GitHub for deployment
- [Quick Start Checklist](./QUICK-START-CHECKLIST.md) - Overview checklist
- [Testing Deployment](./TESTING-DEPLOYMENT.md) - Verify everything works
- [CKey Server Setup (Detailed)](../2-server-setup/ckey-server-setup-guide.md) - More advanced setup

---

**Version:** 1.0
**Created:** 2025-12-15
**For:** CS4445 Sub Server
**Deployment:** V2.1 (Combined Server Format)
