# Multi-Port Configuration for CKey.com (SSH + App Ports)

> ⚠️ **DEPRECATED - V2.0 Format**
>
> This document describes the V2.0 approach using separate secrets for IPs, SSH ports, and app ports.
>
> **Please use the V2.1 combined format instead:** [SERVER-CONFIGURATION.md](./SERVER-CONFIGURATION.md)
>
> The new format is simpler and requires only 2 secrets instead of 6.
>
> This document is kept for reference only.

## 🎯 Overview

CKey.com and other VPS providers use **port forwarding** with non-standard ports. You need to configure **two types of ports**:
1. **SSH Ports** - For deployment access (maps to 22)
2. **App Ports** - For application access and health checks (maps to 8080)

## 🔑 New GitHub Secrets Required

### For Staging Servers
```bash
STAGING_SERVER_PORTS="3494,3495,3496"          # SSH ports
STAGING_SERVER_APP_PORTS="3497,3498,3499"     # App ports
```

### For Production Servers
```bash
PRODUCTION_SERVER_PORTS="4494,4495"            # SSH ports
PRODUCTION_SERVER_APP_PORTS="4497,4498"        # App ports
```

## 📝 Format

**Comma-separated lists matching server order:**
- Must match the order of `STAGING_SERVER_IPS` or `PRODUCTION_SERVER_IPS`
- No spaces (recommended)
- One SSH port + one app port per server

**Complete Example:**
```bash
# 3 staging servers
STAGING_SERVER_IPS="n1.ckey.vn,n2.ckey.vn,n3.ckey.vn"
STAGING_SERVER_PORTS="3494,3495,3496"          # SSH: port -> 22
STAGING_SERVER_APP_PORTS="3497,3498,3499"     # App: port -> 8080

# 2 production servers
PRODUCTION_SERVER_IPS="n4.ckey.vn,n5.ckey.vn"
PRODUCTION_SERVER_PORTS="4494,4495"            # SSH: port -> 22
PRODUCTION_SERVER_APP_PORTS="4497,4498"        # App: port -> 8080
```

## 🖥️ Finding Your Ports (CKey.com)

1. **Log in to CKey.com**
2. **Go to your server details**
3. **Look for "Port Mappings"** or "Connection Info"

Example from CKey panel:
```
Port Mappings:
  3494 -> 22    ← SSH port (for deployment)
  3495 -> 3000  ← Grafana
  3496 -> 7681  ← Terminal
  3497 -> 8080  ← Application port (for health checks!)
  3498 -> 9090  ← Prometheus
```

4. **Note TWO ports for each server:**
   - Port that maps to **22** → SSH port (e.g., 3494)
   - Port that maps to **8080** → App port (e.g., 3497)

## 🚀 Setup Instructions

### Step 1: Add Secrets via GitHub CLI

```bash
# Login to GitHub CLI
gh auth login

# Add staging SSH ports (replace with YOUR ports!)
gh secret set STAGING_SERVER_PORTS -b "3494,3495,3496"

# Add staging APP ports (replace with YOUR ports!)
gh secret set STAGING_SERVER_APP_PORTS -b "3497,3498,3499"

# Add production SSH ports (replace with YOUR ports!)
gh secret set PRODUCTION_SERVER_PORTS -b "4494,4495"

# Add production APP ports (replace with YOUR ports!)
gh secret set PRODUCTION_SERVER_APP_PORTS -b "4497,4498"

# Verify
gh secret list
```

### Step 2: Verify Existing Secrets

Make sure you have all required secrets:

```bash
gh secret list
```

**Required secrets:**
- ✅ `DEPLOY_SSH_KEY`
- ✅ `DEPLOY_USER`
- ✅ `DEPLOY_PATH`
- ✅ `STAGING_SERVER_IPS`
- ✅ `PRODUCTION_SERVER_IPS`
- ✅ `STAGING_SERVER_PORTS` ← NEW! (SSH)
- ✅ `STAGING_SERVER_APP_PORTS` ← NEW! (App)
- ✅ `PRODUCTION_SERVER_PORTS` ← NEW! (SSH)
- ✅ `PRODUCTION_SERVER_APP_PORTS` ← NEW! (App)

## 📖 How It Works

### Workflow Changes

The workflow now:
1. **Parses server IPs**: `n1.ckey.vn,n2.ckey.vn,n3.ckey.vn`
2. **Parses SSH ports**: `3494,3495,3496`
3. **Parses app ports**: `3497,3498,3499`
4. **Matches them by index**:
   - Server 1: `n1.ckey.vn` SSH:3494 App:3497
   - Server 2: `n2.ckey.vn` SSH:3495 App:3498
   - Server 3: `n3.ckey.vn` SSH:3496 App:3499
5. **Deploys using SSH port**: `ssh -p 3494 root@n1.ckey.vn`
6. **Health checks using app port**: `curl http://n1.ckey.vn:3497/actuator/health`

### Script Changes

The deployment script now accepts a 6th parameter for SSH port:

```bash
./scripts/deploy-to-server.sh \
    SERVER_IP \
    DEPLOY_USER \
    DEPLOY_PATH \
    IMAGE_TAG \
    ENVIRONMENT \
    SSH_PORT    ← NEW!
```

**Example:**
```bash
./scripts/deploy-to-server.sh \
    n1.ckey.vn \
    root \
    /app/cs4445-sub-server \
    main \
    staging \
    3494
```

## ⚠️ Backward Compatibility

**If you don't add the port secrets**, the system defaults to port 22:
- No `STAGING_SERVER_PORTS` → uses port 22 for all staging servers
- No `PRODUCTION_SERVER_PORTS` → uses port 22 for all production servers

This ensures **backward compatibility** with existing setups.

## 🧪 Testing

### Test SSH Connection Manually

```bash
# Replace with YOUR port and server
ssh -p 3494 -i ~/.ssh/ckey-deploy root@n1.ckey.vn

# Should connect without password
```

### Test Deployment to Single Server

```bash
./scripts/deploy-to-server.sh \
    n1.ckey.vn \
    root \
    /app/cs4445-sub-server \
    main \
    staging \
    3494
```

## 🐛 Troubleshooting

### Issue: "Connection refused"

**Cause:** Wrong SSH port

**Solution:**
1. Check CKey.com panel for correct port
2. Update `STAGING_SERVER_PORTS` secret
3. Make sure port maps to 22 (not 3000, 8080, etc.)

### Issue: "Permission denied"

**Cause:** Wrong username or SSH key

**Solution:**
```bash
# Test with verbose output
ssh -v -p 3494 -i ~/.ssh/ckey-deploy root@n1.ckey.vn

# Check:
# 1. Username is correct (root vs ubuntu)
# 2. SSH key is correct
# 3. Public key is in server's authorized_keys
```

### Issue: Deployment works for some servers, fails for others

**Cause:** Port mismatch - ports don't match server order

**Solution:**
```bash
# Make sure port order matches IP order!

# WRONG:
STAGING_SERVER_IPS="n1.ckey.vn,n2.ckey.vn,n3.ckey.vn"
STAGING_SERVER_PORTS="3495,3494,3496"  # Order doesn't match!

# CORRECT:
STAGING_SERVER_IPS="n1.ckey.vn,n2.ckey.vn,n3.ckey.vn"
STAGING_SERVER_PORTS="3494,3495,3496"  # Matches IP order
```

## 📊 Example Configurations

### Standard VPS (Port 22)

```bash
# No need to add port secrets - defaults to 22
STAGING_SERVER_IPS="192.168.1.101,192.168.1.102,192.168.1.103"
# STAGING_SERVER_PORTS not needed (defaults to 22,22,22)
```

### CKey.com (Custom Ports)

```bash
# Must add port secrets
STAGING_SERVER_IPS="n1.ckey.vn,n2.ckey.vn,n3.ckey.vn"
STAGING_SERVER_PORTS="3494,3495,3496"

PRODUCTION_SERVER_IPS="n4.ckey.vn,n5.ckey.vn"
PRODUCTION_SERVER_PORTS="4494,4495"
```

### Mixed Setup (Some Custom, Some Standard)

```bash
# Mixed ports work too!
STAGING_SERVER_IPS="192.168.1.101,n2.ckey.vn,192.168.1.103"
STAGING_SERVER_PORTS="22,3495,22"
```

## 🔗 Related Documentation

- [GitHub Setup Guide v2](./github-setup-guide-v2.md) - GitHub secrets setup
- [Beginner SSH Setup](../1-getting-started/BEGINNER-SSH-SETUP-GUIDE.md) - SSH key creation
- [CKey Quickstart](../1-getting-started/CKEY-QUICKSTART.md) - CKey.com setup

## ✅ Quick Setup Checklist

- [ ] Find SSH ports from CKey.com panel
- [ ] Note port for each server (that maps to 22)
- [ ] Create comma-separated list matching server order
- [ ] Add `STAGING_SERVER_PORTS` secret
- [ ] Add `PRODUCTION_SERVER_PORTS` secret
- [ ] Test SSH connection manually
- [ ] Verify `gh secret list` shows new secrets
- [ ] Push to trigger deployment
- [ ] Check GitHub Actions logs show correct ports

---

**Updated:** 2025-12-14
**Version:** 2.0.1
**Feature:** Custom SSH port support
