# Quick Start Checklist - Complete Setup

**Your complete setup checklist from zero to deployed! Check off each item as you go.**

## 🎯 Total Time Estimate

- **First time:** ~2-3 hours
- **Additional servers:** ~20 minutes each
- **GitHub setup:** ~15 minutes

---

## 📋 Phase 1: Prepare Your Local Machine

### 1.1 Install Required Tools

- [ ] **SSH client installed**
  - Windows: OpenSSH (built-in) or Git Bash
  - Mac/Linux: Built-in
  - Test: `ssh -V` in terminal

- [ ] **Git installed**
  - Test: `git --version`
  - Download: https://git-scm.com/

- [ ] **GitHub CLI installed** (optional but helpful)
  - Test: `gh --version`
  - Download: https://cli.github.com/

### 1.2 Create SSH Keys

- [ ] **Generate SSH key pair**
  ```bash
  ssh-keygen -t ed25519 -f ~/.ssh/ckey-deploy -N ""
  ```

- [ ] **Verify keys created**
  ```bash
  ls -la ~/.ssh/ckey-deploy*
  # Should show:
  # ckey-deploy      (private key)
  # ckey-deploy.pub  (public key)
  ```

- [ ] **Set correct permissions**
  ```bash
  chmod 600 ~/.ssh/ckey-deploy
  chmod 644 ~/.ssh/ckey-deploy.pub
  ```

- [ ] **Copy public key** (you'll need this for servers)
  ```bash
  cat ~/.ssh/ckey-deploy.pub
  # Copy the entire output!
  ```

**📖 Need help?** See [BEGINNER-SSH-SETUP-GUIDE.md](../1-getting-started/BEGINNER-SSH-SETUP-GUIDE.md)

---

## 📋 Phase 2: Set Up Each Server

**Repeat this section for EACH server** (staging and production)

### 2.1 Collect Server Information

Server #___ Information:

- [ ] **Server hostname:** `_______________` (e.g., n1.ckey.vn)
- [ ] **SSH port:** `_______________` (the one mapping to port 22)
- [ ] **App port:** `_______________` (the one mapping to port 8080)
- [ ] **Purpose:** Staging ☐  Production ☐

**Where to find ports:**
1. Log in to CKey.com panel
2. Click your server
3. Look for "Port Mappings" or "Cổng"
4. Find port mapping to **22** (SSH port)
5. Find port mapping to **8080** (App port)

### 2.2 First SSH Connection

- [ ] **Test SSH connection**
  ```bash
  ssh -p YOUR_SSH_PORT -i ~/.ssh/ckey-deploy root@YOUR_HOSTNAME
  ```

- [ ] **Add SSH public key to server**
  ```bash
  # On server:
  mkdir -p ~/.ssh
  chmod 700 ~/.ssh

  # Use echo to add your key (minimal Ubuntu doesn't have nano)
  echo "YOUR_PUBLIC_KEY_HERE" >> ~/.ssh/authorized_keys

  # Set permissions
  chmod 600 ~/.ssh/authorized_keys
  ```

- [ ] **Test passwordless SSH**
  ```bash
  # Reconnect - should NOT ask for password
  ssh -p YOUR_SSH_PORT -i ~/.ssh/ckey-deploy root@YOUR_HOSTNAME
  ```

### 2.3 Install Dependencies

- [ ] **Update system**
  ```bash
  apt update && apt upgrade -y
  ```

- [ ] **Install Docker**
  ```bash
  apt install -y ca-certificates curl gnupg lsb-release
  mkdir -p /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
  apt update
  apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
  ```

- [ ] **Verify Docker installation**
  ```bash
  docker --version
  docker compose version
  ```

### 2.4 Set Up Application Directory

- [ ] **Create app directory**
  ```bash
  mkdir -p /app/cs4445-sub-server
  cd /app/cs4445-sub-server
  ```

- [ ] **Clone repository**
  ```bash
  # Replace with your repo URL
  git clone https://github.com/YOUR_USERNAME/CS4445-Sub-Server.git .
  ```

- [ ] **Configure environment**
  ```bash
  # Create .env file using cat (minimal Ubuntu doesn't have nano)
  cat > .env << 'EOF'
  GITHUB_REPOSITORY=YOUR_USERNAME/CS4445-Sub-Server
  IMAGE_TAG=latest
  APP_PORT=8080
  POSTGRES_PASSWORD=YOUR_SECURE_PASSWORD_HERE
  GRAFANA_PASSWORD=YOUR_SECURE_PASSWORD_HERE
  EOF

  # Verify
  cat .env
  ```

### 2.5 Optional: Set Up Firewall

- [ ] **Install and configure UFW**
  ```bash
  apt install -y ufw
  ufw allow YOUR_SSH_PORT/tcp  # ⚠️ Use YOUR port!
  ufw allow YOUR_APP_PORT/tcp
  ufw --force enable
  ufw status
  ```

### 2.6 Verify Server Setup

- [ ] **All checks pass**
  ```bash
  docker --version          # Shows Docker version
  pwd                       # Shows /app/cs4445-sub-server
  ls -la                    # Shows project files
  cat .env                  # Shows configuration
  git status                # Shows clean working tree
  ```

**✅ Server is ready!**

**📖 Need help?** See [NEW-SERVER-SETUP.md](./NEW-SERVER-SETUP.md)

---

## 📋 Phase 3: Configure GitHub

### 3.1 Organize Your Server Information

**Staging Servers:**

| # | Hostname | SSH Port | App Port | Combined Format |
|---|----------|----------|----------|-----------------|
| 1 | ________ | ________ | ________ | host:ssh:app |
| 2 | ________ | ________ | ________ | host:ssh:app |
| 3 | ________ | ________ | ________ | host:ssh:app |

**Production Servers:**

| # | Hostname | SSH Port | App Port | Combined Format |
|---|----------|----------|----------|-----------------|
| 1 | ________ | ________ | ________ | host:ssh:app |
| 2 | ________ | ________ | ________ | host:ssh:app |

**Example:**
```
Staging:  n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498,n3.ckey.vn:3496:3499
Production: n4.ckey.vn:4494:4497,n5.ckey.vn:4495:4498
```

### 3.2 Add GitHub Secrets

**Method A: Using GitHub CLI** (Recommended - Faster)

- [ ] **Login to GitHub**
  ```bash
  gh auth login
  ```

- [ ] **Add all secrets**
  ```bash
  # Navigate to repo
  cd CS4445-Sub-Server

  # Add secrets
  gh secret set DEPLOY_SSH_KEY < ~/.ssh/ckey-deploy
  gh secret set STAGING_SERVERS -b "YOUR_STAGING_SERVERS_HERE"
  gh secret set PRODUCTION_SERVERS -b "YOUR_PRODUCTION_SERVERS_HERE"
  gh secret set DEPLOY_USER -b "root"
  gh secret set DEPLOY_PATH -b "/app/cs4445-sub-server"
  ```

- [ ] **Verify secrets added**
  ```bash
  gh secret list
  # Should show 5 secrets
  ```

**Method B: Using GitHub Web Interface**

- [ ] Go to: Repository → Settings → Secrets and variables → Actions
- [ ] Add these secrets one by one:
  - [ ] `DEPLOY_SSH_KEY` - Your SSH private key
  - [ ] `STAGING_SERVERS` - Staging server list
  - [ ] `PRODUCTION_SERVERS` - Production server list
  - [ ] `DEPLOY_USER` - `root` (or `ubuntu`)
  - [ ] `DEPLOY_PATH` - `/app/cs4445-sub-server`

**📖 Need help?** See [GITHUB-SECRETS-SETUP.md](./GITHUB-SECRETS-SETUP.md)

---

## 📋 Phase 4: Test Deployment

### 4.1 Test Staging Deployment

- [ ] **Push to main branch**
  ```bash
  git add .
  git commit -m "test: trigger staging deployment"
  git push origin main
  ```

- [ ] **Watch GitHub Actions**
  - Open: https://github.com/YOUR_USERNAME/CS4445-Sub-Server/actions
  - Wait for workflow to complete (~5-10 minutes)
  - Check status: ✅ All steps should be green

### 4.2 Verify Staging Deployment

**For each staging server:**

- [ ] **Check application health**
  ```bash
  curl http://n1.ckey.vn:3497/actuator/health
  # Should return: {"status":"UP"}
  ```

- [ ] **Check app is running**
  ```bash
  ssh -p 3494 -i ~/.ssh/ckey-deploy root@n1.ckey.vn "docker ps"
  # Should show cs4445-app container running
  ```

- [ ] **Test API endpoint**
  ```bash
  curl -X POST http://n1.ckey.vn:3497/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d '{"packetId":"test-001","cpuIntensity":5,"ramIntensity":5}'
  # Should return JSON with status: "SUCCESS"
  ```

- [ ] **Access Grafana dashboard**
  - Open: http://n1.ckey.vn:3495 (use your Grafana port)
  - Login: admin / admin (or your GRAFANA_PASSWORD)
  - Should show CS4445 dashboard

### 4.3 Test Production Deployment (Optional)

- [ ] **Create and push version tag**
  ```bash
  git tag v1.0.0
  git push origin v1.0.0
  ```

- [ ] **Watch GitHub Actions**
  - Workflow runs for production
  - Blue-green deployment strategy
  - Check all steps are green

- [ ] **Verify production servers** (same as staging verification)

**📖 Need help?** See [TESTING-DEPLOYMENT.md](./TESTING-DEPLOYMENT.md)

---

## ✅ Final Verification

### Everything Working Checklist

- [ ] **All servers accessible via SSH** (without password)
- [ ] **All GitHub secrets added** (5 total)
- [ ] **Staging deployment successful** (GitHub Actions green)
- [ ] **All staging servers healthy** (health check returns UP)
- [ ] **API endpoints working** (can create fake packets)
- [ ] **Monitoring accessible** (Grafana shows data)
- [ ] **Production deployment tested** (if applicable)

---

## 🎉 Success Criteria

**You're done when:**

✅ You can push code to GitHub
✅ GitHub Actions automatically deploys to all servers
✅ Health checks pass on all servers
✅ You can access the API on all servers
✅ Grafana shows monitoring data

---

## 📊 Quick Reference

### Your Server List

**Staging:**
```
Server 1: ssh -p ____ -i ~/.ssh/ckey-deploy root@____________
Server 2: ssh -p ____ -i ~/.ssh/ckey-deploy root@____________
Server 3: ssh -p ____ -i ~/.ssh/ckey-deploy root@____________
```

**Production:**
```
Server 1: ssh -p ____ -i ~/.ssh/ckey-deploy root@____________
Server 2: ssh -p ____ -i ~/.ssh/ckey-deploy root@____________
```

### Common Commands

```bash
# Deploy to staging
git push origin main

# Deploy to production
git tag v1.0.0 && git push origin v1.0.0

# Check health
curl http://SERVER:APP_PORT/actuator/health

# Check logs
ssh -p SSH_PORT -i ~/.ssh/ckey-deploy root@SERVER "docker logs cs4445-app --tail 50"

# Restart app
ssh -p SSH_PORT -i ~/.ssh/ckey-deploy root@SERVER "cd /app/cs4445-sub-server && docker compose restart"
```

---

## 🆘 Common Issues

### "Permission denied" when SSH

**Solution:**
```bash
chmod 600 ~/.ssh/ckey-deploy
# Make sure you're using the correct SSH port (the one mapping to 22!)
```

### "Connection refused" when SSH

**Solution:**
- Check server is running in CKey panel
- Verify SSH port is correct
- Check firewall allows the port

### GitHub Actions fails on deployment

**Solution:**
- Check all secrets are added correctly
- Verify SSH key in GitHub matches your local key
- Check server format: `host:ssh_port:app_port` (no spaces!)

### Health check fails

**Solution:**
```bash
# Check if containers are running
ssh -p PORT -i ~/.ssh/ckey-deploy root@HOST "docker ps"

# Check app logs
ssh -p PORT -i ~/.ssh/ckey-deploy root@HOST "docker logs cs4445-app"

# Restart if needed
ssh -p PORT -i ~/.ssh/ckey-deploy root@HOST "cd /app/cs4445-sub-server && docker compose restart"
```

---

## 📚 Detailed Guides

If you need more details on any step:

- [NEW-SERVER-SETUP.md](./NEW-SERVER-SETUP.md) - Complete server setup guide
- [GITHUB-SECRETS-SETUP.md](./GITHUB-SECRETS-SETUP.md) - GitHub secrets explained
- [TESTING-DEPLOYMENT.md](./TESTING-DEPLOYMENT.md) - Testing and verification
- [BEGINNER-SSH-SETUP-GUIDE.md](../1-getting-started/BEGINNER-SSH-SETUP-GUIDE.md) - SSH keys explained

---

**Version:** 1.0
**Created:** 2025-12-15
**For:** CS4445 Sub Server
**Deployment:** V2.1 (Combined Server Format)

**Time to complete:** 2-3 hours (first time)
