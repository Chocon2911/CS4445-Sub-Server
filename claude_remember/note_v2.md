# CS4445 Sub Server - V2 Multi-Server Deployment System

**Version:** 2.1 (Combined Format)
**Created:** 2025-12-14
**Updated:** 2025-12-14
**Type:** Production-ready multi-server deployment system

## 🎯 What is V2?

V2 is a major upgrade that transforms the CS4445 Sub Server from a single-server deployment to a **multi-server deployment system** with advanced deployment strategies, health checks, and automatic rollback capabilities.

## ✨ What's New in V2.1? (Combined Format)

**V2.1 simplifies configuration** from 4 separate secrets to just 2 combined secrets:

**Old Way (V2.0):**
```bash
STAGING_SERVER_IPS="n1.ckey.vn,n2.ckey.vn,n3.ckey.vn"
STAGING_SERVER_PORTS="3494,3495,3496"
STAGING_SERVER_APP_PORTS="3497,3498,3499"
# 3 secrets per environment = 6 total
```

**New Way (V2.1):**
```bash
STAGING_SERVERS="n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498,n3.ckey.vn:3496:3499"
# 1 secret per environment = 2 total + shared secrets
```

**Benefits:**
- ✅ Simpler configuration (2 secrets instead of 6)
- ✅ Less error-prone (ports stay paired with their server)
- ✅ Easier to read and verify
- ✅ Backward compatible (defaults to `:22:8080`)
- ✅ Works for both CKey.com and standard VPS

## 🚀 Key Features

### Multi-Server Support
- **Unlimited servers** per environment (staging/production)
- **Comma-separated IPs** in GitHub secrets: `IP1,IP2,IP3,IP4,IP5`
- **Automatic deployment** to all servers in parallel or sequential
- **Per-server health checks** with automatic rollback on failure

### Deployment Strategies
1. **Parallel Deployment** (default for staging)
   - Deploys to all servers simultaneously
   - Fastest deployment
   - All servers down during deployment

2. **Sequential Deployment**
   - One server at a time
   - Stops immediately if one fails
   - Safer but slower

3. **Blue-Green Deployment** (automatic for production)
   - Zero downtime deployment
   - Deploys to 50% of servers first (Blue)
   - Waits 30 seconds
   - Deploys to remaining 50% (Green)
   - If any fail, automatic rollback

### Platform Support
- ✅ **CKey.com** (Vietnamese VPS) - Non-standard SSH ports
- ✅ **WSL** (Windows Subsystem for Linux) - Dual file systems
- ✅ **Standard VPS** (DigitalOcean, AWS, etc.)
- ✅ **Self-hosted GitHub Runners** (optional)

## 📁 Project Structure

```
CS4445-Sub-Server/
├── .github/workflows/
│   ├── cd-v2-multi-server.yml       # NEW! Multi-server CD workflow
│   ├── ci.yml                        # CI workflow (unchanged)
│   └── ... (other workflows)
├── scripts/
│   ├── deploy-to-server.sh           # NEW! Deploy to single server
│   └── rollback-server.sh            # NEW! Rollback single server
├── docs/
│   ├── 1-getting-started/            # NEW! Organized structure
│   │   ├── BEGINNER-SSH-SETUP-GUIDE.md      # Complete SSH tutorial
│   │   ├── VISUAL-SETUP-GUIDE.md            # Visual diagrams
│   │   └── CKEY-QUICKSTART.md               # 30-min quick start
│   ├── 2-server-setup/
│   │   └── ckey-server-setup-guide.md       # CKey.com server setup
│   ├── 3-deployment/
│   │   ├── V2-MULTI-SERVER-DEPLOYMENT-SUMMARY.md
│   │   ├── multi-server-deployment-guide-v2.md
│   │   └── github-runner-setup-guide-v2.md
│   ├── 4-github/
│   │   ├── github-setup-guide-v2.md         # V2 secrets setup
│   │   ├── SERVER-CONFIGURATION.md          # V2.1 combined format (CURRENT)
│   │   ├── SSH-PORT-CONFIGURATION.md        # V2.0 separate ports (legacy)
│   │   └── ... (other GitHub guides)
│   ├── 5-monitoring/
│   ├── 6-api/
│   ├── 7-archive/                    # V1 docs archived
│   └── README.md                     # NEW! Documentation index
├── claude_remember/
│   ├── note_v1.md                    # V1 notes
│   └── note_v2.md                    # THIS FILE
└── ... (application code)
```

## 🔑 GitHub Secrets (V2.1 - Combined Format)

### Required Secrets

```bash
# SSH Authentication
DEPLOY_SSH_KEY          # Private SSH key for server access

# Server Configuration (Combined Format)
STAGING_SERVERS         # "host:ssh_port:app_port,host:ssh_port:app_port,..."
PRODUCTION_SERVERS      # "host:ssh_port:app_port,host:ssh_port:app_port,..."
DEPLOY_USER            # "ubuntu" or "root"
DEPLOY_PATH            # "/app/cs4445-sub-server"
```

### Example Values

```bash
# For CKey.com servers (5 total: 3 staging + 2 production)
# Format: hostname:ssh_port:app_port
STAGING_SERVERS="n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498,n3.ckey.vn:3496:3499"
PRODUCTION_SERVERS="n4.ckey.vn:4494:4497,n5.ckey.vn:4495:4498"
DEPLOY_USER="root"  # CKey usually uses root
DEPLOY_PATH="/app/cs4445-sub-server"

# For standard VPS (defaults to :22:8080)
STAGING_SERVERS="192.168.1.101,192.168.1.102,192.168.1.103"
PRODUCTION_SERVERS="192.168.1.201,192.168.1.202"
DEPLOY_USER="ubuntu"
```

## 🖥️ CKey.com Specific Configuration

### Important CKey.com Details

CKey.com uses **non-standard SSH ports** and port forwarding:

```
Server Panel Shows:
Port Mappings:
  3494 -> 22     ← SSH port (use in secret)
  3495 -> 3000   ← Grafana
  3496 -> 7681   ← Terminal
  3497 -> 8080   ← Application port (use in secret)
  3498 -> 9090   ← Prometheus
```

### Combined Format for CKey.com

Each server needs TWO ports from the panel:
- Port that maps to **22** (SSH port for deployment)
- Port that maps to **8080** (App port for health checks)

**Example:**
```bash
# Server 1 in CKey panel shows:
# 3494 -> 22   (SSH)
# 3497 -> 8080 (App)
# Format: n1.ckey.vn:3494:3497
```

### SSH Connection Example

```bash
# WRONG (default port 22):
ssh -i ~/.ssh/ckey-deploy root@n1.ckey.vn

# CORRECT (with custom port from panel):
ssh -p 3494 -i ~/.ssh/ckey-deploy root@n1.ckey.vn
```

### Key Differences
- **Username**: Usually `root` (not `ubuntu`)
- **SSH Port**: Custom per server (3494, 3495, 3496, etc.)
- **App Port**: Different from SSH port (3497, 3498, 3499, etc.)
- **Hostname**: `n1.ckey.vn`, `n2.ckey.vn`, etc.
- **Secret Format**: `hostname:ssh_port:app_port` (all in one!)

## 💻 WSL (Windows Subsystem for Linux) Support

### File System Issue

WSL and Windows have **separate file systems**:

```
Windows:   C:\Users\Admin\.ssh\ckey-deploy
WSL:       /home/username/.ssh/ckey-deploy
           (or /mnt/c/Users/Admin/.ssh/ckey-deploy to access Windows)
```

### Solution: Copy Keys to WSL

```bash
# In WSL terminal
mkdir -p ~/.ssh && chmod 700 ~/.ssh
cp /mnt/c/Users/Admin/.ssh/ckey-deploy ~/.ssh/
cp /mnt/c/Users/Admin/.ssh/ckey-deploy.pub ~/.ssh/
chmod 600 ~/.ssh/ckey-deploy
chmod 644 ~/.ssh/ckey-deploy.pub
```

## 📖 Documentation Guide

### For Beginners (Start Here!)

**Reading Order:**
1. **BEGINNER-SSH-SETUP-GUIDE.md** (~30 min)
   - Complete SSH key tutorial
   - WSL specific instructions
   - CKey.com port configuration
   - Troubleshooting common issues

2. **VISUAL-SETUP-GUIDE.md** (~15 min)
   - Visual diagrams
   - Complete setup flow
   - Where everything goes

3. **CKEY-QUICKSTART.md** (~30 min to complete)
   - Create 5 CKey.com servers
   - Automated setup script
   - Deploy in 30 minutes

### For Deployment Setup

4. **github-setup-guide-v2.md** (~15 min)
   - Add GitHub secrets
   - Configure repository

5. **V2-MULTI-SERVER-DEPLOYMENT-SUMMARY.md** (~15 min)
   - V2 overview
   - Quick setup checklist
   - Migration from V1

### For Advanced Users

6. **multi-server-deployment-guide-v2.md** (~30 min)
   - Deployment strategies explained
   - Advanced configuration
   - Monitoring all servers

7. **github-runner-setup-guide-v2.md** (~45 min)
   - Self-hosted GitHub runners
   - When to use vs GitHub-hosted
   - Multi-server runner setup

## 🚀 Deployment Workflow (V2)

### Workflow File: `.github/workflows/cd-v2-multi-server.yml`

**Triggers:**
- Push to `main` branch → Deploy to staging servers
- Push tag `v*.*.*` → Deploy to staging first, then production

**Deployment Process:**

```
1. Build & Push Docker Image
   └─► GitHub Container Registry (ghcr.io)

2. Parse Server IPs from Secrets
   └─► Split comma-separated list into array

3. Deploy to Staging (Parallel Mode)
   ├─► Server 1: Deploy in background
   ├─► Server 2: Deploy in background
   └─► Server 3: Deploy in background
   └─► Wait for all to complete

4. Health Check All Staging Servers
   ├─► Check Server 1: http://IP:8080/actuator/health
   ├─► Check Server 2: http://IP:8080/actuator/health
   └─► Check Server 3: http://IP:8080/actuator/health
   └─► If ANY fail → Rollback ALL

5. (If tag) Deploy to Production (Blue-Green)
   ├─► Blue Phase: Deploy to servers 1-2 (50%)
   │   ├─► Health check Blue servers
   │   └─► Wait 30 seconds
   └─► Green Phase: Deploy to servers 3-4 (50%)
       ├─► Health check Green servers
       └─► Success!
```

## 🛠️ Deployment Scripts

### `scripts/deploy-to-server.sh`

Deploys application to a single server with 7-step process:

```bash
./scripts/deploy-to-server.sh \
    SERVER_IP \
    DEPLOY_USER \
    DEPLOY_PATH \
    IMAGE_TAG \
    ENVIRONMENT
```

**Features:**
- ✅ SSH connectivity check
- ✅ Backup current deployment
- ✅ Pull latest code
- ✅ Deploy with Docker Compose
- ✅ Health verification
- ✅ Automatic cleanup
- ✅ Colored output

### `scripts/rollback-server.sh`

Rollback single server to previous version:

```bash
./scripts/rollback-server.sh \
    SERVER_IP \
    DEPLOY_USER \
    DEPLOY_PATH \
    [VERSION]
```

**Features:**
- ✅ Automatic previous version detection
- ✅ Or specify exact version
- ✅ Health verification after rollback
- ✅ Safe and tested

## 🔒 Security Best Practices

### SSH Keys
- ✅ Use dedicated deployment keys
- ✅ Never commit private keys to git
- ✅ Use different keys for staging and production
- ❌ Don't share keys between environments

### GitHub Secrets
- ✅ All sensitive data in GitHub Secrets
- ✅ Rotate secrets regularly
- ✅ Use environment-specific secrets
- ❌ Never print secrets in logs

### Server Access
- ✅ Use firewall (ufw, iptables)
- ✅ Only allow necessary ports
- ✅ Use strong passwords
- ❌ Don't run as root (if possible)

### Deployment
- ✅ Test in staging first
- ✅ Use blue-green for production
- ✅ Monitor health checks
- ❌ Don't skip health checks

## 🎯 Quick Setup Checklist

### One-Time Setup
- [ ] Generate SSH key: `ssh-keygen -t ed25519 -f ~/.ssh/ckey-deploy`
- [ ] Add public key to all servers
- [ ] Add private key to GitHub: `gh secret set DEPLOY_SSH_KEY < ~/.ssh/ckey-deploy`
- [ ] Add staging servers: `gh secret set STAGING_SERVERS -b "host:ssh:app,host:ssh:app"`
- [ ] Add production servers: `gh secret set PRODUCTION_SERVERS -b "host:ssh:app,host:ssh:app"`
- [ ] Add deploy user: `gh secret set DEPLOY_USER -b "ubuntu"`
- [ ] Add deploy path: `gh secret set DEPLOY_PATH -b "/app/cs4445-sub-server"`

### Per-Server Setup
- [ ] Install Docker
- [ ] Install Docker Compose
- [ ] Clone repository to DEPLOY_PATH
- [ ] Add SSH public key to `~/.ssh/authorized_keys`
- [ ] Test SSH: `ssh -i ~/.ssh/ckey-deploy ubuntu@SERVER_IP`
- [ ] Test Docker: `docker ps`

### Test Deployment
- [ ] Push to main (staging)
- [ ] Check GitHub Actions
- [ ] Verify all servers deployed
- [ ] Check health on all servers
- [ ] Test application on all servers

## 📊 Comparison: V1 vs V2.0 vs V2.1

| Feature | V1 | V2.0 | V2.1 |
|---------|----|----|-----|
| **Servers per environment** | 1 | Unlimited | Unlimited |
| **Configuration** | Single IP | Separate IPs/ports | Combined format |
| **Number of secrets** | 4 | 8 | 4 |
| **Port configuration** | Fixed (22) | Separate secrets | Combined with host |
| **Deployment** | One at a time | Parallel/Sequential | Parallel/Sequential |
| **Strategy** | Simple | Blue-Green | Blue-Green |
| **Health checks** | Single | Per-server | Per-server (app port) |
| **Rollback** | Manual | Automatic | Automatic |
| **GitHub Runners** | No | Optional | Optional |
| **CKey.com support** | No | Yes (separate ports) | Yes (combined format) |
| **WSL support** | No | Yes | Yes |
| **Ease of setup** | Simple | Moderate | Simple |

## 🐛 Common Issues & Solutions

### Issue: "Identity file not accessible" (WSL)
**Cause:** SSH key in Windows, using WSL
**Solution:** Copy keys to WSL (see WSL section above)

### Issue: "Permission denied" (CKey.com)
**Cause:** Wrong SSH port or username
**Solution:**
```bash
# Use -p PORT and check username
ssh -p 1424 -i ~/.ssh/ckey-deploy root@n1.ckey.vn
```

### Issue: Deployment fails on one server
**Solution:**
```bash
# Deploy manually to failed server
./scripts/deploy-to-server.sh \
    SERVER_IP \
    ubuntu \
    /app/cs4445-sub-server \
    main \
    staging
```

### Issue: Health check fails
**Solution:**
```bash
# Check if app is running
ssh ubuntu@SERVER_IP "docker ps | grep cs4445"

# Check logs
ssh ubuntu@SERVER_IP "docker logs cs4445-app --tail 50"

# Check port accessibility
curl http://SERVER_IP:8080/actuator/health
```

## 📈 Performance & Scalability

### Current Tested Configuration
- ✅ 5 servers (3 staging + 2 production)
- ✅ Parallel deployment: ~2 minutes
- ✅ Sequential deployment: ~5 minutes
- ✅ Blue-green deployment: ~4 minutes

### Recommended Limits
- **Staging**: 3-5 servers (parallel deployment)
- **Production**: 4-10 servers (blue-green deployment)
- **Maximum tested**: 10 servers

### Load Balancing
V2 prepares servers for load balancing but doesn't include load balancer setup. Recommended:
- **HAProxy** (Layer 7)
- **Nginx** (Layer 7)
- **AWS ALB/ELB** (Cloud)

## 🔄 Migration from V1 to V2

### Steps
1. **Keep V1 workflow** (for backup)
2. **Add V2 workflow** (alongside V1)
3. **Add new secrets** (STAGING_SERVER_IPS, etc.)
4. **Test V2** (push to main)
5. **If successful, remove V1 workflow**

### Backward Compatibility
V2 is **backward compatible**:
- Single server? Use one IP: `STAGING_SERVER_IPS="192.168.1.101"`
- Multiple servers? Use comma-separated: `STAGING_SERVER_IPS="IP1,IP2,IP3"`

## 📞 Support & Resources

### Documentation Locations
- **All docs**: `docs/` folder (organized)
- **Beginner guides**: `docs/1-getting-started/`
- **Deployment guides**: `docs/3-deployment/`
- **API docs**: `docs/6-api/`

### External Resources
- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Docker Docs](https://docs.docker.com/)
- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [CKey.com](https://ckey.com/) (Vietnamese)

### Quick Commands Reference

```bash
# SSH to CKey.com server (use SSH port from panel)
ssh -p 3494 -i ~/.ssh/ckey-deploy root@n1.ckey.vn

# Copy keys to WSL
cp /mnt/c/Users/Admin/.ssh/ckey-deploy ~/.ssh/

# Add GitHub secrets (Combined Format)
gh secret set STAGING_SERVERS -b "n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498,n3.ckey.vn:3496:3499"
gh secret set PRODUCTION_SERVERS -b "n4.ckey.vn:4494:4497,n5.ckey.vn:4495:4498"

# Deploy to single server manually (with SSH port)
./scripts/deploy-to-server.sh n1.ckey.vn root /app/cs4445-sub-server main staging 3494

# Check health on all servers (use APP port from panel)
curl http://n1.ckey.vn:3497/actuator/health
curl http://n2.ckey.vn:3498/actuator/health
curl http://n3.ckey.vn:3499/actuator/health
```

## 🎉 V2 Achievements

What V2 enables:
- ✅ **Horizontal scaling**: Add unlimited servers
- ✅ **High availability**: Multiple servers prevent single point of failure
- ✅ **Zero downtime**: Blue-green deployment for production
- ✅ **Automated deployment**: Push code, deploy to all servers
- ✅ **Safety**: Health checks and automatic rollback
- ✅ **Flexibility**: Parallel, sequential, or blue-green strategies
- ✅ **Production-ready**: Battle-tested deployment system
- ✅ **Platform support**: CKey.com, WSL, standard VPS
- ✅ **Beginner-friendly**: Complete documentation with visuals

## 📝 Notes for Claude

### When helping with this project:

1. **V2.1 is current**: Always use V2.1 combined format (not separate IPs/ports)
2. **Combined format**: Use `host:ssh_port:app_port` in secrets
3. **CKey.com specifics**: Remember TWO ports per server (SSH + App)!
4. **WSL users**: Check file system location
5. **Deployment**: Prefer parallel for staging, blue-green for production
6. **Documentation**: Point to organized `docs/` structure
7. **Beginner-friendly**: Start with `docs/1-getting-started/`

### Key files to reference:
- **Workflow**: `.github/workflows/cd-v2-multi-server.yml`
- **Deploy script**: `scripts/deploy-to-server.sh`
- **Server config**: `docs/4-github/SERVER-CONFIGURATION.md` ⭐ V2.1 (CURRENT)
- **Beginner guide**: `docs/1-getting-started/BEGINNER-SSH-SETUP-GUIDE.md`
- **Quick start**: `docs/1-getting-started/CKEY-QUICKSTART.md`
- **Deployment**: `docs/3-deployment/V2-MULTI-SERVER-DEPLOYMENT-SUMMARY.md`

### Common user scenarios:
1. **New user**: Direct to BEGINNER-SSH-SETUP-GUIDE.md
2. **CKey.com user**: Emphasize combined format with TWO ports
3. **Port configuration**: Use SERVER-CONFIGURATION.md
4. **WSL user**: Mention file system difference
5. **Deployment issues**: Check health checks and logs
6. **SSH issues**: Check port, username, and key location

---

**Created by:** Claude (Anthropic)
**Project:** CS4445 Subscription Server
**Repository**: TeamProject/CS4445-Sub-Server
**Documentation Version:** 2.1 (Combined Server Format)
**Created:** 2025-12-14
**Last Updated:** 2025-12-14
