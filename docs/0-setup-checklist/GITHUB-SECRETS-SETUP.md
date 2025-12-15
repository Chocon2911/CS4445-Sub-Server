# GitHub Secrets Setup - Complete Guide

**Configure all GitHub secrets needed for automated deployment.**

## 📋 Overview

GitHub secrets store sensitive information (SSH keys, passwords, server IPs) securely. Your CI/CD pipeline uses these to deploy automatically.

**Total secrets needed:** 4 core secrets

## 🎯 Required Secrets Summary

| Secret Name | Example Value | What It's For |
|------------|--------------|---------------|
| `DEPLOY_SSH_KEY` | `-----BEGIN OPENSSH PRIVATE KEY-----...` | SSH private key for server access |
| `STAGING_SERVERS` | `n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498` | Staging server list (host:ssh:app) |
| `PRODUCTION_SERVERS` | `n4.ckey.vn:4494:4497,n5.ckey.vn:4495:4498` | Production server list (host:ssh:app) |
| `DEPLOY_USER` | `root` | SSH username |
| `DEPLOY_PATH` | `/app/cs4445-sub-server` | App directory on servers |

---

## 🔧 Method 1: Using GitHub Web Interface (Easiest)

### Step 1: Go to Repository Settings

1. Open your repository: `https://github.com/YOUR_USERNAME/CS4445-Sub-Server`
2. Click **Settings** (top menu)
3. Click **Secrets and variables** (left sidebar)
4. Click **Actions**
5. Click **New repository secret**

### Step 2: Add Each Secret

#### Secret 1: DEPLOY_SSH_KEY

**Value:** Your SSH private key content

**How to get it:**
```bash
# On your local computer
cat ~/.ssh/ckey-deploy
```

**Copy the ENTIRE output** (including the BEGIN and END lines):
```
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
... (many lines) ...
-----END OPENSSH PRIVATE KEY-----
```

**In GitHub:**
- Name: `DEPLOY_SSH_KEY`
- Secret: [Paste the entire key]
- Click **Add secret**

---

#### Secret 2: STAGING_SERVERS

**Value:** Comma-separated list of staging servers in format `host:ssh_port:app_port`

**Example:**
```
n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498,n3.ckey.vn:3496:3499
```

**How to build it:**

For each staging server, write: `hostname:ssh_port:app_port`

**Server 1 Example:**
- Hostname: `n1.ckey.vn`
- SSH Port (maps to 22): `3494`
- App Port (maps to 8080): `3497`
- Combined: `n1.ckey.vn:3494:3497`

**Join multiple servers with commas (NO SPACES):**
```
n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498,n3.ckey.vn:3496:3499
```

**In GitHub:**
- Name: `STAGING_SERVERS`
- Secret: `n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498,n3.ckey.vn:3496:3499`
- Click **Add secret**

**If you only have 1 staging server:**
```
n1.ckey.vn:3494:3497
```

---

#### Secret 3: PRODUCTION_SERVERS

**Value:** Comma-separated list of production servers in format `host:ssh_port:app_port`

**Example:**
```
n4.ckey.vn:4494:4497,n5.ckey.vn:4495:4498
```

**Same format as staging:**
- Each server: `hostname:ssh_port:app_port`
- Multiple servers: separated by commas

**In GitHub:**
- Name: `PRODUCTION_SERVERS`
- Secret: `n4.ckey.vn:4494:4497,n5.ckey.vn:4495:4498`
- Click **Add secret**

---

#### Secret 4: DEPLOY_USER

**Value:** SSH username on your servers

**For CKey.com:** Usually `root`
**For standard VPS:** Usually `ubuntu`

**In GitHub:**
- Name: `DEPLOY_USER`
- Secret: `root`
- Click **Add secret**

---

#### Secret 5: DEPLOY_PATH

**Value:** Application directory path on servers

**Default:** `/app/cs4445-sub-server`

**In GitHub:**
- Name: `DEPLOY_PATH`
- Secret: `/app/cs4445-sub-server`
- Click **Add secret**

**⚠️ Important:** This MUST match the directory you created on your servers in [NEW-SERVER-SETUP.md](./NEW-SERVER-SETUP.md) Step 6.

---

## 🖥️ Method 2: Using GitHub CLI (Faster for Multiple Secrets)

### Prerequisites

Install GitHub CLI:
```bash
# On Ubuntu/WSL
curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
sudo apt update
sudo apt install gh

# On macOS
brew install gh

# On Windows
# Download from: https://cli.github.com/
```

### Login to GitHub

```bash
gh auth login
# Follow the prompts to authenticate
```

### Add All Secrets

```bash
# Navigate to your repository
cd "/mnt/c/Users/Admin/OneDrive - Hanoi University of Science and Technology/New folder/year 4-1/CS4445/TeamProject/CS4445-Sub-Server"

# Add DEPLOY_SSH_KEY
gh secret set DEPLOY_SSH_KEY < ~/.ssh/ckey-deploy

# Add STAGING_SERVERS
gh secret set STAGING_SERVERS -b "n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498,n3.ckey.vn:3496:3499"

# Add PRODUCTION_SERVERS
gh secret set PRODUCTION_SERVERS -b "n4.ckey.vn:4494:4497,n5.ckey.vn:4495:4498"

# Add DEPLOY_USER
gh secret set DEPLOY_USER -b "root"

# Add DEPLOY_PATH
gh secret set DEPLOY_PATH -b "/app/cs4445-sub-server"
```

---

## ✅ Verify Your Secrets

### Using GitHub CLI

```bash
# List all secrets
gh secret list

# Should show:
# DEPLOY_PATH        Updated 2024-XX-XX
# DEPLOY_SSH_KEY     Updated 2024-XX-XX
# DEPLOY_USER        Updated 2024-XX-XX
# PRODUCTION_SERVERS Updated 2024-XX-XX
# STAGING_SERVERS    Updated 2024-XX-XX
```

### Using GitHub Web Interface

1. Go to: Settings → Secrets and variables → Actions
2. You should see 5 repository secrets listed

**✅ All 5 secrets should be visible**

---

## 📝 Secret Templates

### Template for CKey.com (3 Staging + 2 Production)

```bash
# STAGING_SERVERS (3 servers)
n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498,n3.ckey.vn:3496:3499

# PRODUCTION_SERVERS (2 servers)
n4.ckey.vn:4494:4497,n5.ckey.vn:4495:4498

# DEPLOY_USER
root

# DEPLOY_PATH
/app/cs4445-sub-server

# DEPLOY_SSH_KEY
[Copy from: cat ~/.ssh/ckey-deploy]
```

### Template for Standard VPS (Default Ports)

```bash
# STAGING_SERVERS (if using standard SSH port 22 and app port 8080)
# You can omit ports (defaults to :22:8080)
192.168.1.101,192.168.1.102,192.168.1.103

# Or be explicit:
192.168.1.101:22:8080,192.168.1.102:22:8080,192.168.1.103:22:8080

# PRODUCTION_SERVERS
192.168.100.1:22:8080,192.168.100.2:22:8080

# DEPLOY_USER
ubuntu

# DEPLOY_PATH
/app/cs4445-sub-server

# DEPLOY_SSH_KEY
[Copy from: cat ~/.ssh/deploy-key]
```

---

## 🔍 Understanding the Server Format

### Combined Format (V2.1 - CURRENT)

**Format:** `hostname:ssh_port:app_port`

**Each part:**
- `hostname` - Server address (e.g., `n1.ckey.vn` or `192.168.1.101`)
- `ssh_port` - SSH access port (maps to internal port 22)
- `app_port` - Application port (maps to internal port 8080)

**Why we need both ports for CKey.com:**

CKey.com uses port forwarding:
```
Your Panel Shows:
3494 → 22    ← SSH port (for deployment scripts)
3497 → 8080  ← App port (for health checks)
```

The deployment script:
1. Uses `ssh_port` (3494) to connect and deploy
2. Uses `app_port` (3497) to check if app is healthy

**For standard VPS** (where SSH is on port 22):
```
server.com:22:8080
# Or omit and use defaults:
server.com
```

---

## 📋 Pre-Flight Checklist

Before adding secrets, make sure you have:

- [ ] SSH key pair created (`~/.ssh/ckey-deploy` and `~/.ssh/ckey-deploy.pub`)
- [ ] Public key added to ALL servers
- [ ] List of all server hostnames
- [ ] SSH port for each server (from CKey panel)
- [ ] App port for each server (from CKey panel)
- [ ] Verified SSH access to all servers works

**Test SSH access to each server:**
```bash
# Replace with your actual values
ssh -p 3494 -i ~/.ssh/ckey-deploy root@n1.ckey.vn
ssh -p 3495 -i ~/.ssh/ckey-deploy root@n2.ckey.vn
ssh -p 3496 -i ~/.ssh/ckey-deploy root@n3.ckey.vn
# ... etc for all servers
```

✅ All should connect without password!

---

## 🎯 What's Next?

After configuring GitHub secrets:

1. ✅ **Test deployment** - Push to main branch
2. ➡️ **Verify deployment** - See [TESTING-DEPLOYMENT.md](./TESTING-DEPLOYMENT.md)
3. ➡️ **Monitor workflow** - Check GitHub Actions tab

**To trigger deployment:**
```bash
# Staging deployment (automatic on push to main)
git push origin main

# Production deployment (use tags)
git tag v1.0.0
git push origin v1.0.0
```

---

## 🐛 Troubleshooting

### Issue: Secret not working in workflow

**Check:**
1. Secret name matches exactly (case-sensitive!)
2. No extra spaces in secret value
3. Workflow file uses correct secret name

**Debug in workflow:**
```yaml
# In .github/workflows/cd-v2-multi-server.yml
# You can see (but not print) secret lengths
- name: Debug secrets
  run: |
    echo "SSH key length: ${#DEPLOY_SSH_KEY}"
    echo "Staging servers: ${STAGING_SERVERS//[^,]/}"  # Shows only commas
  env:
    DEPLOY_SSH_KEY: ${{ secrets.DEPLOY_SSH_KEY }}
    STAGING_SERVERS: ${{ secrets.STAGING_SERVERS }}
```

### Issue: Deployment fails with "Permission denied"

**Cause:** SSH key not correct or not on servers

**Solution:**
```bash
# 1. Verify SSH key in GitHub matches your local key
cat ~/.ssh/ckey-deploy

# 2. Verify public key is on ALL servers
ssh -p PORT -i ~/.ssh/ckey-deploy USER@HOST "cat ~/.ssh/authorized_keys"

# 3. Re-add the SSH key to GitHub
gh secret set DEPLOY_SSH_KEY < ~/.ssh/ckey-deploy
```

### Issue: Can't find server

**Cause:** Wrong hostname or format in STAGING_SERVERS/PRODUCTION_SERVERS

**Solution:**
```bash
# Verify format is correct (no spaces, colon-separated)
# CORRECT:
n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498

# WRONG:
n1.ckey.vn:3494:3497, n2.ckey.vn:3495:3498  # Space after comma
n1.ckey.vn 3494 3497,n2.ckey.vn 3495 3498   # Spaces instead of colons
```

### Issue: Health check fails

**Cause:** Wrong app port in secret

**Solution:**
```bash
# Verify app port in CKey panel
# Make sure the port you use maps to 8080 (not 22!)

# Check health manually:
curl http://n1.ckey.vn:3497/actuator/health
# Should return: {"status":"UP"}
```

---

## 📚 Related Guides

- [New Server Setup](./NEW-SERVER-SETUP.md) - Set up servers first
- [Quick Start Checklist](./QUICK-START-CHECKLIST.md) - Complete overview
- [Testing Deployment](./TESTING-DEPLOYMENT.md) - Verify it works
- [Server Configuration Details](../4-github/SERVER-CONFIGURATION.md) - More about server format

---

**Version:** 1.0
**Created:** 2025-12-15
**For:** CS4445 Sub Server
**Deployment:** V2.1 (Combined Server Format)
