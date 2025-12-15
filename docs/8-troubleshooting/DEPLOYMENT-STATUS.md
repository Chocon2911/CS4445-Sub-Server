# Deployment Status and Critical Issue - 2025-12-15

## Summary

Deployment to CKey.com server `n1.ckey.vn` has encountered a **critical blocking issue** that prevents Docker from running containers.

---

## What We Accomplished

### ✅ Fixed Issues

1. **Deployment Script Tag Extraction**
   - Fixed `deploy-to-server.sh` to extract just the tag from full image references
   - Commit: `ab8b4bc` - "fix: extract tag from full image reference in deploy script"

2. **CKey.com Detection**
   - Created `docker-compose.ckey.yml` with host networking for CKey.com compatibility
   - Modified `deploy-to-server.sh` to auto-detect CKey.com servers
   - Commit: `1acb729` - "feat: add CKey.com support with host networking"

3. **GitHub Actions Workflow**
   - Real deployment workflow (`cd-v2-multi-server.yml`) now triggers and runs
   - Server parsing works correctly: `n1.ckey.vn:2942:2945`
   - Deployment script executes successfully on server

4. **Repository Name Case**
   - Fixed `.env` file to use lowercase repository name: `chocon2911/cs4445-sub-server`

### ✅ Server Setup

- SSH connection working: `ssh -p 2942 -i ~/.ssh/ckey-deploy root@n1.ckey.vn`
- Docker installed: version 29.1.3
- Application directory created: `/app/cs4445-sub-server`
- Repository cloned successfully
- Environment files configured

---

## ❌ Critical Blocking Issue

### Error

```
failed to register layer: unshare: operation not permitted
```

### What This Means

CKey.com's containerized VPS environment **does not support the `unshare` system call**, which is required by Docker to:
- Create namespaces for containers
- Extract image layers
- Run any containers

### What We Tried

1. **VFS Storage Driver**
   ```json
   {
     "storage-driver": "vfs"
   }
   ```
   Result: Same error

2. **Overlay2 Storage Driver**
   ```json
   {
     "storage-driver": "overlay2",
     "storage-opts": ["overlay2.override_kernel_check=true"]
   }
   ```
   Result: Daemon failed to start

3. **Disabled User Namespaces**
   ```bash
   dockerd --userns-remap="" --exec-opt native.cgroupdriver=cgroupfs
   ```
   Result: Same error

4. **Test with Simple Image**
   ```bash
   docker pull alpine:latest
   ```
   Result: Same error (proves it affects all images, not just ours)

### Root Cause

CKey.com runs VPS servers **inside containers** (containerized VPS). This nested containerization has permission restrictions that prevent Docker from:
- Using mount propagation
- Creating new namespaces with `unshare`
- Setting up quotas
- Manipulating iptables

**Evidence from logs:**
```
level=warning msg="Error while setting daemon root propagation... operation not permitted"
level=warning msg="Unable to setup quota: operation not permitted"
failed to register layer: unshare: operation not permitted
```

---

## Potential Solutions

### Option 1: Deploy Without Docker ✅ **RECOMMENDED**

Deploy the Spring Boot application as a JAR file directly:

**Pros:**
- Works on any Linux VPS
- Simpler deployment
- Faster startup
- No Docker overhead

**Cons:**
- Need to install PostgreSQL, Prometheus, Grafana separately
- More manual configuration
- No isolation between services

**Implementation:**
1. Install PostgreSQL on server
2. Build JAR with Maven: `./mvnw package`
3. Upload JAR to server
4. Run with `java -jar app.jar`
5. Use systemd for auto-restart

### Option 2: Switch Hosting Provider

Use a provider with full virtualization (KVM/VMware):

**Recommended providers:**
- DigitalOcean (KVM)
- Linode (KVM)
- Vultr (KVM)
- AWS EC2 (Xen/Nitro)
- Google Cloud Compute Engine

**Pros:**
- Docker works perfectly
- Better performance
- More control

**Cons:**
- May cost more than CKey.com
- Need to migrate

### Option 3: Contact CKey.com Support

Ask if they can:
- Enable privileged containers
- Provide Docker-compatible VPS (non-containerized)
- Grant necessary Linux capabilities

**Pros:**
- Keep current provider
- Might be quick if they have solution

**Cons:**
- They may not support Docker-in-Docker
- Could take time

### Option 4: Use Podman Instead of Docker

Podman is rootless and may work better in restricted environments:

```bash
apt install -y podman
podman-compose up -d
```

**Pros:**
- Designed for rootless operation
- Might work without `unshare` in some configs

**Cons:**
- Still may hit same restrictions
- Need to modify all scripts
- Less tested than Docker

---

## Current Deployment Status

| Component | Status | Notes |
|-----------|--------|-------|
| GitHub Actions Workflow | ✅ Working | Triggers and runs successfully |
| SSH Connection | ✅ Working | Port 2942 → 22 |
| Server Setup | ✅ Complete | /app/cs4445-sub-server ready |
| Docker Installation | ⚠️ Partial | Installed but can't pull/run images |
| Container Deployment | ❌ Blocked | Cannot register layers due to unshare error |
| Application Running | ❌ Not Running | No containers can start |
| Health Check | ❌ Failing | No app to check (port 2945 → 8080) |

---

## Recommended Next Steps

### Immediate Action (Today)

**Choose one path:**

**Path A: Stay with CKey.com (No Docker)**
1. Install PostgreSQL: `apt install postgresql-16`
2. Build application locally: `./mvnw clean package -DskipTests`
3. Upload JAR to server
4. Create systemd service for auto-restart
5. Update deployment workflow to use JAR deployment

**Path B: Switch Provider**
1. Sign up for DigitalOcean/Linode ($6/month basic droplet)
2. Run server setup from `docs/0-setup-checklist/NEW-SERVER-SETUP.md`
3. Docker will work perfectly
4. Test deployment

### This Week

- Document chosen approach
- Update deployment workflows
- Test end-to-end deployment
- Update documentation for future setup

---

## Files Modified Today

1. `scripts/deploy-to-server.sh` - Added tag extraction logic
2. `docker-compose.ckey.yml` - Created CKey-specific compose file with host networking
3. `.env` - Fixed repository name to lowercase
4. Multiple documentation updates

---

## References

- CKey.com Server: `n1.ckey.vn`
- SSH Port: `2942` (maps to 22)
- App Port: `2945` (maps to 8080)
- GitHub Repository: `Chocon2911/CS4445-Sub-Server`
- Latest Working Commit: `ab8b4bc`

---

## Technical Details

### Docker Daemon Configuration Tested

```json
{
  "iptables": false,
  "ip-forward": false,
  "ip-masq": false,
  "bridge": "none",
  "storage-driver": "vfs"
}
```

### Commands That Work

- `docker version` ✅
- `docker info` ✅
- `docker ps` ✅

### Commands That Fail

- `docker pull <any-image>` ❌ "unshare: operation not permitted"
- `docker run <any-image>` ❌ (can't pull, so can't run)
- `docker compose up` ❌ (can't pull images)

---

**Generated:** 2025-12-15
**Author:** Claude Code
**Status:** BLOCKED - Awaiting decision on deployment strategy
