# Visual Setup Guide - Complete Picture

**See how everything connects together**

## 🎯 The Big Picture

```
┌─────────────────────────────────────────────────────────────────┐
│                     YOUR COMPUTER                                │
│                                                                  │
│  ~/.ssh/                                                         │
│  ├── ckey-deploy          🔑 PRIVATE KEY (secret!)              │
│  └── ckey-deploy.pub      🔓 PUBLIC KEY (share this)            │
│                                                                  │
│  This is YOU                                                     │
└────────────┬────────────────────────────────────────────────────┘
             │
             │ Copy public key to →
             │
    ┌────────┴─────────┐
    │                  │
    ▼                  ▼
┌──────────┐      ┌──────────────────────────────────────────┐
│ GITHUB   │      │         CKEY.COM SERVERS                 │
│ SECRETS  │      │                                          │
│          │      │  Server 1 (~/.ssh/authorized_keys)      │
│ Private  │      │  ├── Your PUBLIC key here                │
│ Key →    │      │  └── ✅ Can connect without password!    │
│ Encrypted│      │                                          │
│          │      │  Server 2, 3, 4, 5 (same setup)         │
└────┬─────┘      └──────────────────────────────────────────┘
     │
     │ When you push code →
     │
     ▼
┌──────────────────────────────────────┐
│    GITHUB ACTIONS                    │
│                                      │
│  Uses private key from secrets       │
│  Connects to all 5 servers          │
│  Deploys automatically! 🚀          │
└──────────────────────────────────────┘
```

## 📖 Step-by-Step Visual Guide

### Step 1: Create SSH Keys

```
┌─────────────────────────────────────────────────┐
│  ON YOUR COMPUTER                               │
│                                                 │
│  Open Terminal                                  │
│  Type: ssh-keygen -t ed25519 -f ckey-deploy    │
│  Press Enter (no passphrase)                    │
│  Press Enter again                              │
│                                                 │
│  ✅ Created two files:                          │
│     ckey-deploy      ← Private (secret!)       │
│     ckey-deploy.pub  ← Public (share)          │
└─────────────────────────────────────────────────┘
```

**Commands to run:**
```bash
cd ~/.ssh
ssh-keygen -t ed25519 -C "ckey-deployment" -f ckey-deploy
# Press Enter twice (no passphrase)
ls -la
# You'll see: ckey-deploy and ckey-deploy.pub
```

#### ⚠️ Special Note for WSL (Windows Subsystem for Linux) Users

```
┌─────────────────────────────────────────────────┐
│  WSL USERS: TWO FILE SYSTEMS!                  │
│                                                 │
│  Windows:  C:\Users\Admin\.ssh\                 │
│            (/mnt/c/Users/Admin/.ssh/ in WSL)    │
│                                                 │
│  WSL:      /home/username/.ssh/                 │
│            (~/.ssh/ in WSL)                     │
│                                                 │
│  ⚠️ These are DIFFERENT locations!             │
│                                                 │
│  RECOMMENDATION: Create keys in WSL            │
│  OR: Copy from Windows to WSL                   │
│                                                 │
│  Copy command (if needed):                      │
│  mkdir -p ~/.ssh && chmod 700 ~/.ssh            │
│  cp /mnt/c/Users/Admin/.ssh/ckey-deploy ~/.ssh/ │
│  cp /mnt/c/Users/Admin/.ssh/ckey-deploy.pub ~/.ssh/ │
│  chmod 600 ~/.ssh/ckey-deploy                   │
└─────────────────────────────────────────────────┘
```

### Step 2: View Your Keys

```
┌─────────────────────────────────────────────────┐
│  PUBLIC KEY (safe to share)                    │
│                                                 │
│  Command: cat ~/.ssh/ckey-deploy.pub           │
│                                                 │
│  Output:                                        │
│  ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAA...       │
│  ckey-deployment                                │
│                                                 │
│  👆 THIS goes on ALL 5 servers!                │
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│  PRIVATE KEY (secret!)                          │
│                                                 │
│  Command: cat ~/.ssh/ckey-deploy                │
│                                                 │
│  Output:                                        │
│  -----BEGIN OPENSSH PRIVATE KEY-----            │
│  b3BlbnNzaC1rZXktdjEAAAAA...                    │
│  ... many lines ...                             │
│  -----END OPENSSH PRIVATE KEY-----              │
│                                                 │
│  👆 THIS goes to GitHub Secrets!                │
└─────────────────────────────────────────────────┘
```

### Step 3: Add Public Key to Servers

#### Option A: Via CKey.com Dashboard

```
┌─────────────────────────────────────────────────┐
│  STEP 1: Copy Public Key                       │
│                                                 │
│  cat ~/.ssh/ckey-deploy.pub                     │
│  (Select all and copy)                          │
└─────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────┐
│  STEP 2: On CKey.com                           │
│                                                 │
│  1. Click "Create Server"                       │
│  2. Find "SSH Keys" section                     │
│  3. Click "Add SSH Key"                         │
│  4. Name: deployment-key                        │
│  5. Paste your public key                       │
│  6. Save                                        │
│  7. Select this key when creating servers       │
└─────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────┐
│  RESULT: All 5 servers have your public key!   │
│                                                 │
│  Server 1 ✅                                    │
│  Server 2 ✅                                    │
│  Server 3 ✅                                    │
│  Server 4 ✅                                    │
│  Server 5 ✅                                    │
└─────────────────────────────────────────────────┘
```

#### Option B: Manually (If Servers Already Created)

**⚠️ IMPORTANT FOR CKEY.COM USERS:**

```
┌─────────────────────────────────────────────────┐
│  CKEY.COM USES CUSTOM SSH PORTS!               │
│                                                 │
│  Check your CKey.com panel for:                 │
│  - SSH port (NOT 22!)                           │
│  - Username (usually "root", not "ubuntu")      │
│  - Connection command                           │
│                                                 │
│  Example from CKey panel:                       │
│  Port Mapping: 1424 -> 22                       │
│  Connection: ssh -p 1424 root@n1.ckey.vn        │
│                                                 │
│  Use -p PORT when connecting!                   │
└─────────────────────────────────────────────────┘
```

```
┌─────────────────────────────────────────────────┐
│  FOR EACH CKEY.COM SERVER:                     │
│                                                 │
│  1. SSH with password (first time)              │
│     ssh -p 1424 root@n1.ckey.vn                 │
│     (Use YOUR port from CKey panel!)            │
│     (enter password from CKey)                  │
│                                                 │
│  2. Create .ssh directory                       │
│     mkdir -p ~/.ssh                             │
│     chmod 700 ~/.ssh                            │
│                                                 │
│  3. Edit authorized_keys                        │
│     nano ~/.ssh/authorized_keys                 │
│                                                 │
│  4. Paste your PUBLIC key                       │
│     (Right-click to paste)                      │
│     Save: Ctrl+O, Enter, Ctrl+X                 │
│                                                 │
│  5. Set permissions                             │
│     chmod 600 ~/.ssh/authorized_keys            │
│                                                 │
│  6. Exit                                        │
│     exit                                        │
│                                                 │
│  ✅ Now test: ssh -i ~/.ssh/ckey-deploy \      │
│               ubuntu@45.XXX.XXX.101             │
│     Should connect WITHOUT password!            │
└─────────────────────────────────────────────────┘
```

### Step 4: Add Private Key to GitHub

```
┌─────────────────────────────────────────────────┐
│  STEP 1: Install GitHub CLI                    │
│                                                 │
│  Windows: winget install GitHub.cli             │
│  Mac:     brew install gh                       │
│  Linux:   sudo apt install gh                   │
└─────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────┐
│  STEP 2: Login                                  │
│                                                 │
│  gh auth login                                  │
│  - Select: GitHub.com                           │
│  - Select: HTTPS                                │
│  - Select: Login with web browser               │
│  - Copy code and authenticate in browser        │
└─────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────┐
│  STEP 3: Add Private Key to GitHub Secret      │
│                                                 │
│  gh secret set DEPLOY_SSH_KEY < \               │
│      ~/.ssh/ckey-deploy                         │
│                                                 │
│  ✓ Set secret DEPLOY_SSH_KEY                   │
└─────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────┐
│  STEP 4: Add Server IPs                        │
│                                                 │
│  gh secret set STAGING_SERVER_IPS -b \          │
│    "45.XXX.XXX.101,45.XXX.XXX.102,45.XXX.XXX.103" │
│                                                 │
│  gh secret set PRODUCTION_SERVER_IPS -b \       │
│    "45.XXX.XXX.201,45.XXX.XXX.202"              │
│                                                 │
│  gh secret set DEPLOY_USER -b "ubuntu"          │
│  gh secret set DEPLOY_PATH -b \                 │
│    "/app/cs4445-sub-server"                     │
└─────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────┐
│  STEP 5: Verify                                 │
│                                                 │
│  gh secret list                                 │
│                                                 │
│  Output:                                        │
│  DEPLOY_SSH_KEY         ✅                      │
│  DEPLOY_USER           ✅                      │
│  DEPLOY_PATH           ✅                      │
│  STAGING_SERVER_IPS    ✅                      │
│  PRODUCTION_SERVER_IPS ✅                      │
└─────────────────────────────────────────────────┘
```

### Step 5: Deploy!

```
┌─────────────────────────────────────────────────┐
│  ON YOUR COMPUTER                               │
│                                                 │
│  git add .                                      │
│  git commit -m "Ready to deploy"                │
│  git push origin main                           │
└─────────────────────────────────────────────────┘
              │
              │ Triggers GitHub Actions
              ▼
┌─────────────────────────────────────────────────┐
│  GITHUB ACTIONS WORKFLOW                        │
│                                                 │
│  1. Build Docker image                          │
│  2. Push to registry                            │
│  3. Read STAGING_SERVER_IPS secret              │
│     → "45.XXX.XXX.101,45.XXX.XXX.102,45.XXX.XXX.103" │
│  4. For each server in parallel:                │
│     - Use DEPLOY_SSH_KEY to connect             │
│     - Pull latest code                          │
│     - Deploy with Docker                        │
│     - Run health check                          │
└─────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────┐
│  RESULT                                         │
│                                                 │
│  🚀 Deploying to 3 staging servers...           │
│  ✅ 45.XXX.XXX.101 deployed and healthy         │
│  ✅ 45.XXX.XXX.102 deployed and healthy         │
│  ✅ 45.XXX.XXX.103 deployed and healthy         │
│                                                 │
│  All servers running latest code! 🎉           │
└─────────────────────────────────────────────────┘
```

## 🔐 Security Visual

```
┌────────────────────────────────────────────────────────┐
│                  PRIVATE KEY                           │
│                                                        │
│  Location on your computer:                            │
│  ~/.ssh/ckey-deploy                                    │
│                                                        │
│  ❌ NEVER share this                                   │
│  ❌ NEVER commit to git                                │
│  ❌ NEVER email or paste publicly                      │
│  ❌ NEVER screenshot and share                         │
│                                                        │
│  ✅ Keep on your computer only                         │
│  ✅ Add to GitHub Secrets (encrypted)                  │
│  ✅ Backup securely (password manager)                 │
│                                                        │
│  Think of it as: YOUR HOUSE KEY 🔑                    │
└────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────┐
│                  PUBLIC KEY                            │
│                                                        │
│  Location on your computer:                            │
│  ~/.ssh/ckey-deploy.pub                                │
│                                                        │
│  ✅ Safe to share                                      │
│  ✅ Put on all servers                                 │
│  ✅ Add to CKey.com dashboard                          │
│  ✅ Email if needed                                    │
│  ✅ Paste in server authorized_keys                    │
│                                                        │
│  Think of it as: LOCK FOR YOUR HOUSE 🔓               │
└────────────────────────────────────────────────────────┘
```

## 📍 Where Things Go - Complete Map

```
╔══════════════════════════════════════════════════════════╗
║                    YOUR LOCAL COMPUTER                   ║
╠══════════════════════════════════════════════════════════╣
║                                                          ║
║  ~/.ssh/                                                 ║
║  ├── ckey-deploy          🔑 Private Key                ║
║  │   └── Used to: Connect to servers                    ║
║  │                Add to GitHub Secrets                  ║
║  │                                                       ║
║  └── ckey-deploy.pub      🔓 Public Key                 ║
║      └── Used to: Add to all 5 servers                  ║
║                   Add to CKey.com dashboard              ║
║                                                          ║
╠══════════════════════════════════════════════════════════╣
║                  CKEY.COM - SERVER 1                     ║
╠══════════════════════════════════════════════════════════╣
║                                                          ║
║  Hostname: n1.ckey.vn (or IP: 46.32.XXX.XXX)             ║
║  SSH Port: 1424 (NOT 22! Check YOUR panel)               ║
║  User: root (usually, check panel)                       ║
║  Connection: ssh -p 1424 root@n1.ckey.vn                 ║
║                                                          ║
║  /root/.ssh/authorized_keys                              ║
║  └── Contains: Your PUBLIC key 🔓                       ║
║                                                          ║
║  /app/cs4445-sub-server/                                 ║
║  ├── Your application code (from GitHub)                 ║
║  ├── docker-compose.prod.yml                             ║
║  └── .env (configuration)                                ║
║                                                          ║
║  Repeat for Servers 2, 3, 4, 5                          ║
║  (Each has different port! Check CKey panel)             ║
║                                                          ║
╠══════════════════════════════════════════════════════════╣
║             GITHUB.COM - Repository Secrets              ║
╠══════════════════════════════════════════════════════════╣
║                                                          ║
║  Settings → Secrets → Actions                            ║
║                                                          ║
║  DEPLOY_SSH_KEY                                          ║
║  └── Contains: Your PRIVATE key 🔑 (encrypted)          ║
║                                                          ║
║  STAGING_SERVER_IPS                                      ║
║  └── "45.XXX.XXX.101,45.XXX.XXX.102,45.XXX.XXX.103"      ║
║                                                          ║
║  PRODUCTION_SERVER_IPS                                   ║
║  └── "45.XXX.XXX.201,45.XXX.XXX.202"                     ║
║                                                          ║
║  DEPLOY_USER                                             ║
║  └── "ubuntu"                                            ║
║                                                          ║
║  DEPLOY_PATH                                             ║
║  └── "/app/cs4445-sub-server"                            ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
```

## 🎬 Complete Setup Flow

```
START
  │
  ▼
┌─────────────────────┐
│ 1. Create 5 Servers │ ← On CKey.com dashboard
│    on CKey.com      │   (10 minutes)
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ 2. Generate SSH Key │ ← On your computer
│    Pair             │   ssh-keygen -t ed25519 ...
│                     │   (2 minutes)
└──────────┬──────────┘
           │
           ├──────────────────┬──────────────┐
           ▼                  ▼              ▼
    ┌─────────────┐    ┌─────────┐   ┌──────────┐
    │ Public Key  │    │ Public  │   │ Private  │
    │ to Server 1 │    │ Key to  │   │ Key to   │
    │             │    │ Server  │   │ GitHub   │
    │ Add to:     │    │ 2,3,4,5 │   │ Secrets  │
    │ ~/.ssh/     │    │         │   │          │
    │ authorized_ │    │ Same    │   │ gh secret│
    │ keys        │    │ process │   │ set ...  │
    └─────────────┘    └─────────┘   └──────────┘
           │                  │              │
           └──────────────────┴──────────────┘
                      │
                      ▼
           ┌─────────────────────┐
           │ 3. Test SSH         │
           │    Connections      │
           │                     │
           │ CKey.com:           │
           │ ssh -p PORT -i      │
           │ ~/.ssh/ckey-deploy  │
           │ root@n1.ckey.vn     │
           │                     │
           │ (Use port from      │
           │  CKey panel!)       │
           │                     │
           │ Should work without │
           │ password!           │
           └──────────┬──────────┘
                      │
                      ▼
           ┌─────────────────────┐
           │ 4. Configure        │
           │    Servers          │
           │                     │
           │ Install Docker      │
           │ Install Docker      │
           │ Compose             │
           │ Clone repo          │
           │                     │
           │ (Use automated      │
           │  script or manual)  │
           └──────────┬──────────┘
                      │
                      ▼
           ┌─────────────────────┐
           │ 5. Add GitHub       │
           │    Secrets          │
           │                     │
           │ DEPLOY_SSH_KEY      │
           │ STAGING_SERVER_IPS  │
           │ PRODUCTION_...IPS   │
           │ DEPLOY_USER         │
           │ DEPLOY_PATH         │
           └──────────┬──────────┘
                      │
                      ▼
           ┌─────────────────────┐
           │ 6. Deploy!          │
           │                     │
           │ git push origin main│
           │                     │
           │ GitHub Actions runs │
           │ Deploys to all      │
           │ servers!            │
           └──────────┬──────────┘
                      │
                      ▼
                 SUCCESS! 🎉
        ┌──────────────────────┐
        │ All 5 servers        │
        │ running your app!    │
        │                      │
        │ Access:              │
        │ http://IP:8080       │
        │ http://IP:3000       │
        │ (Grafana)            │
        └──────────────────────┘
```

## ✅ Final Checklist with Locations

```
┌────────────────────────────────────────────────────────┐
│ ☐ 1. SSH Keys Created                                 │
│     Location: ~/.ssh/ckey-deploy and ckey-deploy.pub  │
│     Command: ssh-keygen -t ed25519 -f ckey-deploy     │
│     WSL Users: Keys in WSL (~/.ssh/) or Windows?      │
│                                                        │
│ ☐ 2. Public Key on Server 1                           │
│     Location: /root/.ssh/authorized_keys (CKey)       │
│     Contains: ssh-ed25519 AAAAC3...                   │
│     CKey: Note your SSH port from panel (e.g., 1424)  │
│                                                        │
│ ☐ 3. Public Key on Servers 2, 3, 4, 5                │
│     Same as above for each server                     │
│     CKey: Each server has different port!             │
│                                                        │
│ ☐ 4. Private Key in GitHub                            │
│     Location: GitHub Secrets → DEPLOY_SSH_KEY         │
│     Command: gh secret set DEPLOY_SSH_KEY < key       │
│                                                        │
│ ☐ 5. Server IPs/Hostnames in GitHub                   │
│     Location: GitHub Secrets                          │
│     - STAGING_SERVER_IPS                               │
│     - PRODUCTION_SERVER_IPS                            │
│     CKey: Use hostnames or IPs from panel             │
│                                                        │
│ ☐ 6. Can SSH to All Servers Without Password         │
│     Test: ssh -p PORT -i ~/.ssh/ckey-deploy root@HOST │
│     CKey: Use YOUR port from panel!                   │
│                                                        │
│ ☐ 7. Docker Installed on All Servers                  │
│     Test: ssh -p PORT root@HOST "docker --version"    │
│                                                        │
│ ☐ 8. Repository Cloned on All Servers                 │
│     Location: /app/cs4445-sub-server                  │
│                                                        │
│ ☐ 9. Can Deploy Successfully                          │
│     Test: git push origin main                        │
│     Check: GitHub Actions → See deployment succeed    │
│                                                        │
│ ☐ 10. All Servers Healthy                             │
│     Test: curl http://HOST:APP_PORT/actuator/health   │
│     Response: {"status":"UP"}                         │
│     CKey: Use app port from panel (e.g., 1427)        │
│                                                        │
│ ☐ 11. Grafana Accessible                              │
│     Open: http://HOST:GRAFANA_PORT                    │
│     Login: admin / admin                              │
│     CKey: Use Grafana port from panel (e.g., 1425)    │
│     See: Dashboard with metrics                       │
└────────────────────────────────────────────────────────┘
```

## 🎯 Quick Test Commands

### For CKey.com Servers (with custom ports)

**⚠️ Update ports and hostnames to match YOUR CKey panel!**

```bash
# 1. Test SSH to all CKey.com servers (should NOT ask for password)
# Replace ports (1424, 2424, etc.) with YOUR ports from CKey panel!
ssh -p 1424 -i ~/.ssh/ckey-deploy root@n1.ckey.vn "hostname && echo '✓ Server 1 Connected!'"
ssh -p 2424 -i ~/.ssh/ckey-deploy root@n2.ckey.vn "hostname && echo '✓ Server 2 Connected!'"
ssh -p 3424 -i ~/.ssh/ckey-deploy root@n3.ckey.vn "hostname && echo '✓ Server 3 Connected!'"
ssh -p 4424 -i ~/.ssh/ckey-deploy root@n4.ckey.vn "hostname && echo '✓ Server 4 Connected!'"
ssh -p 5424 -i ~/.ssh/ckey-deploy root@n5.ckey.vn "hostname && echo '✓ Server 5 Connected!'"

# 2. Verify GitHub secrets
gh secret list

# 3. Test deployment
echo "# Test" >> README.md
git add README.md
git commit -m "Test deployment"
git push origin main

# 4. Check health on all servers
# Note: CKey maps different ports to 8080, check your panel!
# Example: If panel shows "1427 -> 8080", use port 1427
curl -s http://n1.ckey.vn:1427/actuator/health | jq .status
curl -s http://n2.ckey.vn:2427/actuator/health | jq .status
curl -s http://n3.ckey.vn:3427/actuator/health | jq .status

# 5. Test API on servers
# Replace ports with YOUR app ports from CKey panel
curl -X POST http://n1.ckey.vn:1427/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d '{"packetId":"test","cpuIntensity":3,"ramIntensity":3}' | jq .status
```

### For Standard Servers (port 22, standard setup)

```bash
# 1. Test SSH to all servers (should NOT ask for password)
for ip in 45.XXX.XXX.{101..103} 45.XXX.XXX.{201..202}; do
    echo "Testing $ip..."
    ssh -i ~/.ssh/ckey-deploy ubuntu@$ip "hostname && echo '✓ Connected!'"
done

# 2. Check health on all staging servers
for ip in 45.XXX.XXX.{101..103}; do
    echo "Server $ip:"
    curl -s http://$ip:8080/actuator/health | jq .status
done

# 3. Test API on all servers
for ip in 45.XXX.XXX.{101..103}; do
    echo "Testing API on $ip..."
    curl -X POST http://$ip:8080/api/v1/fakePacket \
        -H "Content-Type: application/json" \
        -d '{"packetId":"test","cpuIntensity":3,"ramIntensity":3}' | jq .status
done
```

**If all tests pass:** ✅ You're 100% ready!

---

## 📚 Related Guides

1. **[BEGINNER-SSH-SETUP-GUIDE.md](BEGINNER-SSH-SETUP-GUIDE.md)** - Detailed SSH key tutorial
2. **[CKEY-QUICKSTART.md](CKEY-QUICKSTART.md)** - 30-minute setup guide
3. **[ckey-server-setup-guide.md](ckey-server-setup-guide.md)** - Complete server setup

**Start here, then use other guides for details!**
