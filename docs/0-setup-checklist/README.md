# Setup Checklist - Your Starting Point

**Everything you need to set up CS4445 Sub Server from scratch, in the right order.**

## 🎯 What's in This Folder?

This folder contains **4 essential guides** that take you from zero to fully deployed:

1. **Server setup** - Configure your rented servers
2. **GitHub setup** - Add deployment secrets
3. **Testing** - Verify everything works
4. **Checklist** - Track your progress

## 📚 Read in This Order

### ⭐ Start Here: Quick Start Checklist

**[QUICK-START-CHECKLIST.md](./QUICK-START-CHECKLIST.md)**

- Complete checklist format with checkboxes
- All steps in one place
- Progress tracking
- Time estimate: 2-3 hours (first time)

**Best for:** Following step-by-step and tracking your progress

---

### Step 1: Set Up Your Servers

**[NEW-SERVER-SETUP.md](./NEW-SERVER-SETUP.md)**

What you'll do:
- Find SSH and App ports in CKey panel
- Add SSH keys to servers
- Install Docker and Docker Compose
- Clone repository
- Configure environment variables
- Set up firewall

**Time:** 20-30 minutes per server

**Best for:** Detailed instructions for each server setup step

---

### Step 2: Configure GitHub

**[GITHUB-SECRETS-SETUP.md](./GITHUB-SECRETS-SETUP.md)**

What you'll do:
- Add SSH private key to GitHub
- Add server lists (STAGING_SERVERS, PRODUCTION_SERVERS)
- Add deployment configuration
- Verify secrets are correct

**Time:** 10-15 minutes

**Best for:** Understanding GitHub secrets and how to add them

---

### Step 3: Test Deployment

**[TESTING-DEPLOYMENT.md](./TESTING-DEPLOYMENT.md)**

What you'll do:
- Trigger deployment via GitHub Actions
- Verify health checks pass
- Test API endpoints
- Check monitoring (Grafana)
- Verify database works
- Run load tests

**Time:** 15-20 minutes

**Best for:** Comprehensive testing and troubleshooting

---

## 🚀 Quick Navigation by Scenario

### "I'm completely new to this"

1. Read [QUICK-START-CHECKLIST.md](./QUICK-START-CHECKLIST.md) first
2. Follow it step-by-step, checking off items as you go
3. Refer to detailed guides when you need more info

### "I need to set up a new server"

1. Go to [NEW-SERVER-SETUP.md](./NEW-SERVER-SETUP.md)
2. Fill out the server information template
3. Follow steps 1-11
4. Update GitHub secrets if this is a new environment

### "I need to add GitHub secrets"

1. Go to [GITHUB-SECRETS-SETUP.md](./GITHUB-SECRETS-SETUP.md)
2. Use Method 1 (Web UI) or Method 2 (GitHub CLI)
3. Verify secrets were added correctly

### "I need to test if deployment works"

1. Go to [TESTING-DEPLOYMENT.md](./TESTING-DEPLOYMENT.md)
2. Follow Phase 1: Trigger deployment
3. Follow Phase 2-7: Verify each component
4. Use troubleshooting section if something fails

### "Something's not working"

1. Check [TESTING-DEPLOYMENT.md](./TESTING-DEPLOYMENT.md) - Troubleshooting section
2. Check [NEW-SERVER-SETUP.md](./NEW-SERVER-SETUP.md) - Troubleshooting section
3. Check [GITHUB-SECRETS-SETUP.md](./GITHUB-SECRETS-SETUP.md) - Troubleshooting section

---

## 📊 What You'll Have When Done

After completing all guides:

✅ **Servers configured:**
- Docker installed
- SSH keys set up
- Application directory ready
- Firewall configured

✅ **GitHub configured:**
- All secrets added
- Workflow ready to deploy
- CI/CD pipeline active

✅ **Application deployed:**
- Running on all servers
- Health checks passing
- API endpoints working
- Monitoring active

✅ **Verification complete:**
- All tests passed
- Database working
- Load test successful

---

## ⏱️ Time Estimates

### First-Time Setup (5 servers example)

| Task | Time |
|------|------|
| SSH key generation | 5 min |
| Server 1 setup | 30 min |
| Servers 2-5 setup | 20 min each = 80 min |
| GitHub secrets | 15 min |
| First deployment | 10 min |
| Testing & verification | 20 min |
| **Total** | **~2.5 hours** |

### Adding More Servers Later

| Task | Time |
|------|------|
| Server setup | 20 min |
| Update GitHub secrets | 2 min |
| Test deployment | 5 min |
| **Total** | **~30 min per server** |

---

## 🎓 Learning Path

```
START
  │
  ├─► Complete Beginner
  │   └─► Read QUICK-START-CHECKLIST.md
  │       └─► Follow step-by-step
  │           └─► Refer to detailed guides as needed
  │
  ├─► Has Servers, Need GitHub
  │   └─► Skip to GITHUB-SECRETS-SETUP.md
  │       └─► Then TESTING-DEPLOYMENT.md
  │
  ├─► Has GitHub, Adding Servers
  │   └─► Read NEW-SERVER-SETUP.md
  │       └─► Update STAGING_SERVERS or PRODUCTION_SERVERS
  │           └─► Then TESTING-DEPLOYMENT.md
  │
  └─► Troubleshooting Issues
      └─► Go to TESTING-DEPLOYMENT.md → Troubleshooting
          └─► Or specific guide's troubleshooting section
```

---

## 📋 Pre-Requirements

Before starting, make sure you have:

### On Your Computer
- [ ] SSH client installed
- [ ] Terminal or command line access
- [ ] Git installed
- [ ] Text editor (notepad, VS Code, etc.)

### For CKey.com Users
- [ ] CKey.com account with credits
- [ ] Servers rented (3 staging + 2 production recommended)
- [ ] Access to server panel

### For GitHub
- [ ] GitHub account
- [ ] Repository created (or fork this one)
- [ ] Repository permissions set to "Read and write"

---

## 🔗 Related Documentation

After completing setup, you might want to read:

### Understanding the System
- [ARCHITECTURE.md](../ARCHITECTURE.md) - How everything works together
- [VISUAL-SETUP-GUIDE.md](../1-getting-started/VISUAL-SETUP-GUIDE.md) - Visual diagrams

### Advanced Topics
- [multi-server-deployment-guide-v2.md](../3-deployment/multi-server-deployment-guide-v2.md) - Deployment strategies
- [github-runner-setup-guide-v2.md](../3-deployment/github-runner-setup-guide-v2.md) - Self-hosted runners
- [ci-cd-guide.md](../4-github/ci-cd-guide.md) - CI/CD pipeline details

### API and Monitoring
- [server-control-api.md](../6-api/server-control-api.md) - API documentation
- [monitoring-guide.md](../5-monitoring/monitoring-guide.md) - Monitoring setup

---

## 💡 Tips for Success

### 1. Take Your Time
- Don't rush through the steps
- Read each instruction carefully
- Verify each step before moving to the next

### 2. Keep Notes
- Write down your server hostnames and ports
- Save SSH commands you use often
- Document any issues you encounter

### 3. Test Early, Test Often
- Test SSH connection immediately after setup
- Test health checks after deployment
- Run API tests regularly

### 4. Use the Checklist
- Check off items as you complete them
- Track which servers are done
- Know exactly where you are in the process

### 5. Don't Panic
- If something fails, check the troubleshooting section
- Most issues have simple solutions
- You can always start over on a specific step

---

## 🆘 Getting Help

### Check These First
1. Troubleshooting sections in each guide
2. Error messages in GitHub Actions logs
3. Application logs on servers

### Common Issues
- **SSH problems** → See NEW-SERVER-SETUP.md Troubleshooting
- **Deployment fails** → See TESTING-DEPLOYMENT.md Troubleshooting
- **Secrets not working** → See GITHUB-SECRETS-SETUP.md Troubleshooting

### Still Stuck?
1. Review the error message carefully
2. Check which exact step failed
3. Try the step again from the beginning
4. Verify all prerequisites are met

---

## 📝 File Summary

| File | Purpose | When to Use |
|------|---------|-------------|
| **QUICK-START-CHECKLIST.md** | Complete checklist with all steps | First time setup, tracking progress |
| **NEW-SERVER-SETUP.md** | Detailed server configuration | Setting up each new server |
| **GITHUB-SECRETS-SETUP.md** | GitHub secrets configuration | Adding deployment secrets |
| **TESTING-DEPLOYMENT.md** | Verification and testing | After deployment, troubleshooting |

---

## ✅ Success Checklist

**You're done when:**

- [ ] All servers have Docker installed and running
- [ ] SSH keys work on all servers (no password needed)
- [ ] GitHub has all 5 required secrets
- [ ] Push to `main` triggers staging deployment
- [ ] All health checks pass on all servers
- [ ] API endpoints work on all servers
- [ ] Grafana shows monitoring data
- [ ] Database saves and retrieves data
- [ ] You can confidently deploy new changes

---

**Ready to start?** → Go to [QUICK-START-CHECKLIST.md](./QUICK-START-CHECKLIST.md)

---

**Version:** 1.0
**Created:** 2025-12-15
**For:** CS4445 Sub Server
**Deployment:** V2.1 (Combined Server Format)
