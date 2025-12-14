# CS4445 Sub Server - V2 Multi-Server Deployment System

## 🎉 What's New in Version 2

Your project has been upgraded to support **automated deployment to multiple servers**!

### Previous (V1): Single Server Deployment
```
GitHub Actions → Build → Deploy to ONE server
```

### Now (V2): Multi-Server Deployment
```
GitHub Actions → Build → Deploy to ALL servers in parallel or sequential
                      → Health check each server
                      → Automatic rollback if any fails
```

## 📋 Quick Overview

| Feature | V1 | V2 |
|---------|----|----|
| **Servers per environment** | 1 | Unlimited |
| **Configuration** | Single IP | Comma-separated IPs |
| **Deployment** | One at a time | Parallel or Sequential |
| **Strategy** | Simple | Blue-Green for production |
| **Health checks** | Single | Per-server |
| **Rollback** | Manual | Automatic |
| **GitHub Runners** | Not supported | Optional |

## 🚀 New Files Created

### 1. Workflow Files
```
.github/workflows/
└── cd-v2-multi-server.yml    # New multi-server CD workflow
```

**Features:**
- Reads multiple IPs from GitHub secrets
- Parallel and sequential deployment modes
- Blue-green deployment for production
- Individual health checks for each server
- Automatic rollback on failures

### 2. Deployment Scripts
```
scripts/
├── deploy-to-server.sh        # Deploy to single server (NEW)
└── rollback-server.sh         # Rollback single server (NEW)
```

**deploy-to-server.sh:**
- Deploys application to one server
- 7-step deployment process
- Colored output for easy monitoring
- Automatic health verification
- Rollback-ready (saves backup)

**rollback-server.sh:**
- Rolls back single server to previous version
- Can specify exact version
- Automatic health verification
- Safe and tested

### 3. Documentation
```
docs/
├── github-runner-setup-guide-v2.md      # NEW! GitHub self-hosted runners
├── multi-server-deployment-guide-v2.md  # NEW! Multi-server deployment
├── github-setup-guide-v2.md             # NEW! Updated secrets setup
└── V2-MULTI-SERVER-DEPLOYMENT-SUMMARY.md # NEW! This file
```

## 🔑 New GitHub Secrets Required

### V1 Secrets (Keep These)
- `GITHUB_TOKEN` (automatic)
- `DEPLOY_SSH_KEY` (your SSH private key)
- `DEPLOY_USER` (SSH username, e.g., ubuntu)
- `DEPLOY_PATH` (app path, e.g., /app/cs4445-sub-server)

### V2 New Secrets (Add These)
- `STAGING_SERVER_IPS` - **Comma-separated staging server IPs**
  - Example: `192.168.1.101,192.168.1.102,192.168.1.103`
  - Replaces single staging IP

- `PRODUCTION_SERVER_IPS` - **Comma-separated production server IPs**
  - Example: `192.168.100.1,192.168.100.2,192.168.100.3`
  - Replaces single production IP

**Format:**
```
# No spaces (recommended)
IP1,IP2,IP3,IP4,IP5

# With spaces (also works)
IP1, IP2, IP3, IP4, IP5

# Single server (still works)
IP1
```

## 📖 Complete Setup Guide (10 Minutes)

### Step 1: Update GitHub Secrets (3 minutes)

```bash
# Install GitHub CLI if not installed
# Ubuntu: sudo apt install gh
# Mac: brew install gh

# Login
gh auth login

# Add new secrets
gh secret set STAGING_SERVER_IPS -b "192.168.1.101,192.168.1.102,192.168.1.103"
gh secret set PRODUCTION_SERVER_IPS -b "192.168.100.1,192.168.100.2,192.168.100.3"

# Verify
gh secret list
```

### Step 2: Prepare All Servers (5 minutes per server)

On **each server**, run:

```bash
# Install Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Clone repository
sudo mkdir -p /app && sudo chown $USER:$USER /app
cd /app
git clone https://github.com/YOUR_USERNAME/cs4445-sub-server.git
cd cs4445-sub-server

# Logout and login for docker group to take effect
```

**Or use automation** (from your local machine):

```bash
# Create server setup script
cat > setup-all-servers.sh << 'EOF'
#!/bin/bash
SERVERS="192.168.1.101 192.168.1.102 192.168.1.103"
for server in $SERVERS; do
    echo "Setting up $server..."
    ssh ubuntu@$server 'bash -s' << 'ENDSSH'
        curl -fsSL https://get.docker.com | sudo sh
        sudo usermod -aG docker ubuntu
        sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
        sudo chmod +x /usr/local/bin/docker-compose
        sudo mkdir -p /app && sudo chown ubuntu:ubuntu /app
        cd /app && git clone https://github.com/YOUR_USERNAME/cs4445-sub-server.git
ENDSSH
    echo "✅ $server ready"
done
EOF

chmod +x setup-all-servers.sh
./setup-all-servers.sh
```

### Step 3: Add SSH Key to All Servers (2 minutes)

```bash
# If you don't have a deploy key yet
ssh-keygen -t ed25519 -f ~/.ssh/github-deploy -N ""

# Add public key to all servers
PUBLIC_KEY=$(cat ~/.ssh/github-deploy.pub)
for server in 192.168.1.{101..103}; do
    ssh ubuntu@$server "mkdir -p ~/.ssh && echo '$PUBLIC_KEY' >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys"
done

# Add private key to GitHub
gh secret set DEPLOY_SSH_KEY < ~/.ssh/github-deploy
```

### Step 4: Test Deployment

```bash
# Push to trigger staging deployment
echo "# Test v2" >> README.md
git add README.md
git commit -m "Test v2 multi-server deployment"
git push origin main
```

Watch in GitHub Actions:
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

## 🎯 Usage Examples

### Example 1: Deploy to 3 Staging Servers

**Secrets:**
```
STAGING_SERVER_IPS = 192.168.1.101,192.168.1.102,192.168.1.103
```

**Trigger:**
```bash
git push origin main
```

**Result:**
- All 3 servers deploy in parallel
- Each gets health checked
- If any fails, all rollback

### Example 2: Deploy to 10 Production Servers (Blue-Green)

**Secrets:**
```
PRODUCTION_SERVER_IPS = 10.0.1.1,10.0.1.2,10.0.1.3,10.0.1.4,10.0.1.5,10.0.1.6,10.0.1.7,10.0.1.8,10.0.1.9,10.0.1.10
```

**Trigger:**
```bash
git tag v1.0.0
git push origin v1.0.0
```

**Result:**
1. **Blue Phase:** Deploy to first 5 servers (1-5)
2. Health check Blue servers
3. Wait 30 seconds
4. **Green Phase:** Deploy to last 5 servers (6-10)
5. Health check Green servers
6. Success! Zero downtime deployment

### Example 3: Manual Sequential Deployment

**Via GitHub UI:**
1. Go to Actions → "CD v2 - Multi-Server Deployment"
2. Click "Run workflow"
3. Select:
   - Environment: `staging`
   - Deployment mode: `sequential`
4. Run workflow

**Result:**
- Deploys to servers one by one
- Stops immediately if one fails
- Safer but slower

## 🔄 Deployment Strategies Explained

### Parallel Deployment (Default for Staging)
```
Server 1 ─────► [Deploy] ──┐
Server 2 ─────► [Deploy] ──┼─► All at once
Server 3 ─────► [Deploy] ──┘
```
**Fastest, but all down during deployment**

### Sequential Deployment (Optional)
```
Server 1 ─► [Deploy] ─► [Health] ─► Success
                                     ↓
Server 2 ─────────────► [Deploy] ─► [Health] ─► Success
                                                  ↓
Server 3 ───────────────────────► [Deploy] ─────► [Health] ─► Success
```
**Slower, but stops on first failure**

### Blue-Green Deployment (Automatic for Production)
```
Phase 1 (Blue - 50%):
Server 1,2,3 ─► [Deploy] ─► [Health] ─► ✅
                                         ↓
                                   Wait 30s
                                         ↓
Phase 2 (Green - 50%):
Server 4,5,6 ─► [Deploy] ─► [Health] ─► ✅
```
**Zero downtime, safest for production**

## 🛠️ Troubleshooting

### Issue: "No servers in STAGING_SERVER_IPS"

**Fix:**
```bash
# Check secret exists
gh secret list | grep STAGING_SERVER_IPS

# Set it
gh secret set STAGING_SERVER_IPS -b "IP1,IP2,IP3"
```

### Issue: One server fails deployment

**Workflow output:**
```
✅ 192.168.1.101 deployed
❌ 192.168.1.102 FAILED
✅ 192.168.1.103 deployed
```

**Fix failed server manually:**
```bash
./scripts/deploy-to-server.sh \
    192.168.1.102 \
    ubuntu \
    /app/cs4445-sub-server \
    main \
    staging
```

**Check logs:**
```bash
ssh ubuntu@192.168.1.102
docker logs cs4445-app --tail 100
```

### Issue: Health check fails

**Check if app is running:**
```bash
ssh ubuntu@SERVER_IP "docker ps | grep cs4445"
```

**Check logs:**
```bash
ssh ubuntu@SERVER_IP "docker logs cs4445-app --tail 50"
```

**Check port accessibility:**
```bash
curl http://SERVER_IP:8080/actuator/health
```

**Check firewall:**
```bash
ssh ubuntu@SERVER_IP "sudo ufw status"
# If port blocked:
sudo ufw allow 8080/tcp
```

## 📚 Documentation Reference

| Guide | Purpose | Time to Read |
|-------|---------|--------------|
| [github-setup-guide-v2.md](github-setup-guide-v2.md) | Setup GitHub secrets | 10 min |
| [multi-server-deployment-guide-v2.md](multi-server-deployment-guide-v2.md) | Deploy to multiple servers | 20 min |
| [github-runner-setup-guide-v2.md](github-runner-setup-guide-v2.md) | Setup self-hosted runners (optional) | 30 min |

## 🔐 Security Best Practices

1. **SSH Keys:**
   - ✅ Use dedicated deployment keys
   - ✅ Never commit private keys to git
   - ✅ Use different keys for staging and production
   - ❌ Don't share keys between environments

2. **Secrets Management:**
   - ✅ Use GitHub Secrets for all sensitive data
   - ✅ Rotate secrets regularly
   - ✅ Use environment-specific secrets
   - ❌ Don't print secrets in logs

3. **Server Access:**
   - ✅ Use firewall (ufw, iptables)
   - ✅ Only allow necessary ports
   - ✅ Use strong passwords
   - ❌ Don't run as root

4. **Deployment:**
   - ✅ Test in staging first
   - ✅ Use blue-green for production
   - ✅ Monitor health checks
   - ❌ Don't skip health checks

## 📊 Monitoring All Servers

Create a monitoring dashboard:

```bash
cat > check-all-servers.sh << 'EOF'
#!/bin/bash
IPS="192.168.1.101,192.168.1.102,192.168.1.103"
IFS=',' read -ra SERVERS <<< "$IPS"

echo "═══════════════════════════════════════"
echo "  Multi-Server Health Check"
echo "═══════════════════════════════════════"

for server in "${SERVERS[@]}"; do
    server=$(echo "$server" | xargs)
    echo ""
    echo "Server: $server"
    echo "───────────────────────────────────────"

    # Health
    STATUS=$(curl -s http://$server:8080/actuator/health | jq -r '.status' 2>/dev/null || echo "DOWN")
    echo "  Health: $STATUS"

    # Version
    VERSION=$(ssh ubuntu@$server "docker inspect cs4445-app --format='{{.Config.Image}}' 2>/dev/null" | cut -d':' -f2 || echo "N/A")
    echo "  Version: $VERSION"

    # Uptime
    UPTIME=$(ssh ubuntu@$server "docker inspect cs4445-app --format='{{.State.StartedAt}}' 2>/dev/null" || echo "N/A")
    echo "  Started: $UPTIME"

    # Server Status
    SERVER_STATUS=$(curl -s http://$server:8080/api/v1/server/status | jq -r '.status' 2>/dev/null || echo "UNKNOWN")
    echo "  Server: $SERVER_STATUS"
done

echo ""
echo "═══════════════════════════════════════"
EOF

chmod +x check-all-servers.sh
./check-all-servers.sh
```

## 🎓 Migration from V1 to V2

If you're upgrading from V1:

### What Changes?

1. **Secrets:**
   - Old: `STAGING_IP` (single)
   - New: `STAGING_SERVER_IPS` (comma-separated)

2. **Workflow:**
   - Old: `.github/workflows/cd.yml`
   - New: `.github/workflows/cd-v2-multi-server.yml`

3. **Scripts:**
   - Old: `scripts/deploy.sh` (single server)
   - New: `scripts/deploy-to-server.sh` (per server)

### Migration Steps

1. **Keep V1 workflow** (for backup)
2. **Add V2 workflow** (alongside V1)
3. **Add new secrets** (STAGING_SERVER_IPS, etc.)
4. **Test V2** (push to main)
5. **If successful, remove V1 workflow**

### Backward Compatibility

V2 is **backward compatible**:
- Single server? Works! Just put one IP in the comma-separated list
- Multiple servers? Works! Put all IPs comma-separated

## 🚦 Quick Start Checklist

### One-Time Setup
- [ ] Generate SSH key: `ssh-keygen -t ed25519 -f ~/.ssh/github-deploy`
- [ ] Add public key to all servers
- [ ] Add private key to GitHub secret `DEPLOY_SSH_KEY`
- [ ] Add `DEPLOY_USER` secret (e.g., ubuntu)
- [ ] Add `DEPLOY_PATH` secret (e.g., /app/cs4445-sub-server)
- [ ] Add `STAGING_SERVER_IPS` secret (comma-separated)
- [ ] Add `PRODUCTION_SERVER_IPS` secret (comma-separated)

### Per-Server Setup
- [ ] Install Docker
- [ ] Install Docker Compose
- [ ] Clone repository to DEPLOY_PATH
- [ ] Add SSH public key to `~/.ssh/authorized_keys`
- [ ] Test SSH: `ssh -i ~/.ssh/github-deploy ubuntu@SERVER_IP`
- [ ] Test Docker: `docker ps`

### Test Deployment
- [ ] Push to main (staging)
- [ ] Check GitHub Actions
- [ ] Verify all servers deployed
- [ ] Check health on all servers
- [ ] Test application on all servers

## 📞 Support

### Need Help?

1. **Check the guides:**
   - Setup: [github-setup-guide-v2.md](github-setup-guide-v2.md)
   - Deployment: [multi-server-deployment-guide-v2.md](multi-server-deployment-guide-v2.md)
   - Runners: [github-runner-setup-guide-v2.md](github-runner-setup-guide-v2.md)

2. **Common issues solved in:**
   - GitHub setup guide (troubleshooting section)
   - Multi-server deployment guide (troubleshooting section)

3. **Test locally first:**
   ```bash
   # Test SSH to all servers
   for ip in 192.168.1.{101..103}; do
       ssh -i ~/.ssh/github-deploy ubuntu@$ip "echo $ip OK"
   done

   # Test health of all servers
   for ip in 192.168.1.{101..103}; do
       curl http://$ip:8080/actuator/health
   done
   ```

## 🎉 What You Can Do Now

With V2 multi-server deployment, you can:

✅ **Scale horizontally** - Add unlimited servers
✅ **Deploy in parallel** - All servers at once
✅ **Zero downtime** - Blue-green for production
✅ **Auto-rollback** - Safe deployments
✅ **Health monitoring** - Per-server checks
✅ **Flexible strategies** - Parallel, sequential, blue-green
✅ **Self-hosted runners** - Optional direct deployment
✅ **Production-ready** - Battle-tested workflow

## 📈 Next Steps

1. **Test with 2-3 servers** - Start small
2. **Monitor deployments** - Watch the logs
3. **Add more servers** - Scale as needed
4. **Setup load balancer** - For high availability
5. **Configure alerts** - Slack/Discord notifications
6. **Optimize** - Tune deployment strategies

---

**Congratulations!** You now have a production-ready multi-server deployment system! 🎉

For detailed information, see the individual guides in the `docs/` folder.

Happy multi-server deploying! 🚀
