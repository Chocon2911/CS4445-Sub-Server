# Server Configuration with Combined Format

## 🎯 Simple 2-Secret Approach

Instead of separate secrets for IPs, SSH ports, and app ports, use **just 2 secrets** with combined format:

```bash
STAGING_SERVERS="host:ssh_port:app_port,host:ssh_port:app_port,..."
PRODUCTION_SERVERS="host:ssh_port:app_port,host:ssh_port:app_port,..."
```

## 🔑 GitHub Secrets Required

### For Staging (3 servers)
```bash
STAGING_SERVERS="n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498,n3.ckey.vn:3496:3499"
```

### For Production (2 servers)
```bash
PRODUCTION_SERVERS="n4.ckey.vn:4494:4497,n5.ckey.vn:4495:4498"
```

## 📝 Format Breakdown

Each server entry: `hostname:ssh_port:app_port`

- **hostname**: Server hostname or IP address
- **ssh_port**: Port for SSH access (maps to 22)
- **app_port**: Port for application access (maps to 8080)

Separate multiple servers with commas.

## 🖥️ Finding Your Ports on CKey.com

### Server 1 Example

**CKey.com Panel:**
```
Server: n1.ckey.vn

Port Mappings:
  3494 -> 22    ← SSH port
  3495 -> 3000  ← Grafana
  3496 -> 7681  ← Terminal
  3497 -> 8080  ← App port (for health checks!)
  3498 -> 9090  ← Prometheus
```

**Your secret entry:**
```
n1.ckey.vn:3494:3497
     ↑      ↑     ↑
   host   SSH  App
```

### Complete Example (3 staging servers)

**Server 1:** SSH=3494, App=3497
**Server 2:** SSH=3495, App=3498
**Server 3:** SSH=3496, App=3499

**GitHub Secret:**
```bash
STAGING_SERVERS="n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498,n3.ckey.vn:3496:3499"
```

## 🚀 Setup Instructions

### Step 1: Collect Server Information

For each server, note:
1. Hostname (e.g., `n1.ckey.vn`)
2. Port that maps to **22** (SSH)
3. Port that maps to **8080** (App)

### Step 2: Create Secret String

```bash
# Format: host:ssh_port:app_port,host:ssh_port:app_port
STAGING="n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498,n3.ckey.vn:3496:3499"
PRODUCTION="n4.ckey.vn:4494:4497,n5.ckey.vn:4495:4498"
```

### Step 3: Add to GitHub

```bash
# Login to GitHub CLI
gh auth login

# Add staging servers
gh secret set STAGING_SERVERS -b "n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498,n3.ckey.vn:3496:3499"

# Add production servers
gh secret set PRODUCTION_SERVERS -b "n4.ckey.vn:4494:4497,n5.ckey.vn:4495:4498"

# Verify
gh secret list
```

### Step 4: Verify Other Required Secrets

```bash
gh secret list
```

**Required secrets:**
- ✅ `DEPLOY_SSH_KEY` - Your private SSH key
- ✅ `DEPLOY_USER` - Usually "root" for CKey.com
- ✅ `DEPLOY_PATH` - e.g., "/app/cs4445-sub-server"
- ✅ `STAGING_SERVERS` ← NEW! Combined format
- ✅ `PRODUCTION_SERVERS` ← NEW! Combined format

## 📖 How It Works

The workflow automatically parses the combined format:

```bash
Input:  "n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498"

Parsed:
  Hosts:      n1.ckey.vn,n2.ckey.vn
  SSH Ports:  3494,3495
  App Ports:  3497,3498
```

Then uses:
- **SSH ports** for deployment: `ssh -p 3494 root@n1.ckey.vn`
- **App ports** for health checks: `curl http://n1.ckey.vn:3497/actuator/health`

## 🧪 Default Values

If you omit ports, they default to standard values:

```bash
# Full format
"n1.ckey.vn:3494:3497"

# SSH port default (22)
"n1.ckey.vn::3497"  → becomes "n1.ckey.vn:22:3497"

# App port default (8080)
"n1.ckey.vn:3494"   → becomes "n1.ckey.vn:3494:8080"

# Both defaults
"n1.ckey.vn"        → becomes "n1.ckey.vn:22:8080"
```

**Standard VPS (non-CKey):**
```bash
STAGING_SERVERS="192.168.1.101,192.168.1.102,192.168.1.103"
# Each becomes: IP:22:8080
```

## 📊 Complete Examples

### Example 1: CKey.com (3 staging + 2 production)

```bash
# Staging
gh secret set STAGING_SERVERS -b "n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498,n3.ckey.vn:3496:3499"

# Production
gh secret set PRODUCTION_SERVERS -b "n4.ckey.vn:4494:4497,n5.ckey.vn:4495:4498"
```

### Example 2: Standard VPS (port 22, 8080)

```bash
# Staging - defaults to :22:8080 for each
gh secret set STAGING_SERVERS -b "192.168.1.101,192.168.1.102,192.168.1.103"

# Production
gh secret set PRODUCTION_SERVERS -b "192.168.100.1,192.168.100.2"
```

### Example 3: Mixed (some custom, some standard)

```bash
# Server 1: Custom ports
# Server 2: Standard ports (defaults)
# Server 3: Custom ports
gh secret set STAGING_SERVERS -b "n1.ckey.vn:3494:3497,192.168.1.102,n3.ckey.vn:3496:3499"
```

## 🐛 Troubleshooting

### Issue: "STAGING_SERVERS secret not found"

**Fix:**
```bash
gh secret set STAGING_SERVERS -b "your:servers:here"
```

### Issue: Connection fails to one server

**Check format:**
```bash
# ❌ WRONG - missing colons
"n1.ckey.vn,n2.ckey.vn"

# ✅ CORRECT - with ports
"n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498"
```

### Issue: Health check fails

**Verify app port mapping:**
```bash
# Check CKey panel for port that maps to 8080
# Use that port in your secret

# Test manually:
curl http://n1.ckey.vn:3497/actuator/health
```

### Issue: Wrong port order

**Remember:** `hostname:ssh_port:app_port` (NOT app:ssh!)

```bash
# ❌ WRONG
"n1.ckey.vn:3497:3494"  # Swapped!

# ✅ CORRECT
"n1.ckey.vn:3494:3497"
     host   SSH  App
```

## ✅ Testing

### Test Format Parsing

```bash
# Your secret
STAGING_SERVERS="n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498"

# Should connect:
ssh -p 3494 -i ~/.ssh/ckey-deploy root@n1.ckey.vn

# Should return {"status":"UP"}:
curl http://n1.ckey.vn:3497/actuator/health
```

### Test All Servers

```bash
# Server 1
ssh -p 3494 -i ~/.ssh/ckey-deploy root@n1.ckey.vn "echo OK"
curl http://n1.ckey.vn:3497/actuator/health

# Server 2
ssh -p 3495 -i ~/.ssh/ckey-deploy root@n2.ckey.vn "echo OK"
curl http://n2.ckey.vn:3498/actuator/health

# Server 3
ssh -p 3496 -i ~/.ssh/ckey-deploy root@n3.ckey.vn "echo OK"
curl http://n3.ckey.vn:3499/actuator/health
```

## 🎯 Quick Reference

```bash
# CKey.com server configuration
hostname:ssh_port:app_port

# Examples:
"n1.ckey.vn:3494:3497"                    # Single server
"n1.ckey.vn:3494:3497,n2.ckey.vn:3495:3498"  # Multiple servers

# Defaults:
"::"        = ":22:8080"
"host"      = "host:22:8080"
"host:3494" = "host:3494:8080"
"host::3497"= "host:22:3497"
```

## 🔗 Related Documentation

- [Beginner SSH Setup](../1-getting-started/BEGINNER-SSH-SETUP-GUIDE.md)
- [CKey Quickstart](../1-getting-started/CKEY-QUICKSTART.md)
- [GitHub Setup v2](./github-setup-guide-v2.md)

---

**Updated:** 2025-12-14
**Version:** 2.1
**Format:** Combined server configuration (host:ssh:app)
