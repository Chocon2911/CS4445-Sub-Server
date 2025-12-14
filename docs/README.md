# CS4445 Sub Server Documentation

Complete documentation for the CS4445 Subscription Server project with multi-server deployment capabilities.

## 🏗️ Architecture

- **[ARCHITECTURE.md](./ARCHITECTURE.md)** ⭐ **Essential Reading**
  - Complete system architecture guide
  - How database, API, and monitoring work together
  - Data flow diagrams and real-world examples
  - Troubleshooting common issues
  - ~45 min read

## 📚 Documentation Structure

### 🚀 [1. Getting Started](./1-getting-started/)
**Start here if you're new!** Complete beginner-friendly guides with visual diagrams.

- **[BEGINNER-SSH-SETUP-GUIDE.md](./1-getting-started/BEGINNER-SSH-SETUP-GUIDE.md)** ⭐ **Start Here!**
  - Complete SSH key tutorial for absolute beginners
  - WSL (Windows Subsystem for Linux) specific instructions
  - CKey.com port configuration
  - Step-by-step with examples
  - ~30 min read

- **[VISUAL-SETUP-GUIDE.md](./1-getting-started/VISUAL-SETUP-GUIDE.md)**
  - Visual diagrams showing how everything connects
  - Complete setup flow with ASCII art
  - Where everything goes (files, keys, servers)
  - Quick test commands
  - ~15 min read

- **[CKEY-QUICKSTART.md](./1-getting-started/CKEY-QUICKSTART.md)**
  - 30-minute setup guide for CKey.com users
  - Create 5 servers and deploy automatically
  - Automated setup scripts
  - Testing and verification
  - ~30 min to complete

### 🖥️ [2. Server Setup](./2-server-setup/)
Detailed server configuration for production deployment.

- **[ckey-server-setup-guide.md](./2-server-setup/ckey-server-setup-guide.md)**
  - Complete CKey.com server setup
  - Automated configuration scripts
  - Manual setup instructions
  - Firewall and security configuration
  - ~1 hour read

### 🚢 [3. Deployment](./3-deployment/)
Multi-server deployment system (V2) with advanced strategies.

- **[V2-MULTI-SERVER-DEPLOYMENT-SUMMARY.md](./3-deployment/V2-MULTI-SERVER-DEPLOYMENT-SUMMARY.md)** ⭐ **Overview**
  - What's new in V2
  - Quick setup checklist
  - Migration from V1
  - ~15 min read

- **[multi-server-deployment-guide-v2.md](./3-deployment/multi-server-deployment-guide-v2.md)**
  - Complete multi-server deployment guide
  - Parallel, sequential, and blue-green strategies
  - Health checks and rollback
  - Advanced configuration
  - ~30 min read

- **[github-runner-setup-guide-v2.md](./3-deployment/github-runner-setup-guide-v2.md)**
  - Self-hosted GitHub runners (optional)
  - When to use self-hosted vs GitHub-hosted
  - Multi-server runner setup
  - Security best practices
  - ~45 min read

### 🔧 [4. GitHub Configuration](./4-github/)
GitHub Actions, secrets, and CI/CD setup.

- **[SERVER-CONFIGURATION.md](./4-github/SERVER-CONFIGURATION.md)** ⭐ **V2.1 (CURRENT)**
  - Combined server configuration format
  - Simplified 2-secret approach: `host:ssh_port:app_port`
  - CKey.com setup examples
  - ~15 min read

- **[github-setup-guide-v2.md](./4-github/github-setup-guide-v2.md)** ⭐ **V2**
  - GitHub secrets for multi-server deployment
  - SSH key configuration
  - Complete setup script
  - ~15 min read

- **[SSH-PORT-CONFIGURATION.md](./4-github/SSH-PORT-CONFIGURATION.md)** (V2.0 - Legacy)
  - Separate port configuration (deprecated)
  - Use SERVER-CONFIGURATION.md instead
  - ~15 min read

- **[github-setup-guide.md](./4-github/github-setup-guide.md)**
  - V1 GitHub setup (legacy)
  - Single server deployment
  - ~10 min read

- **[ci-cd-guide.md](./4-github/ci-cd-guide.md)**
  - CI/CD pipeline explanation
  - Workflow customization
  - Troubleshooting
  - ~20 min read

### 📊 [5. Monitoring](./5-monitoring/)
Prometheus and Grafana monitoring setup.

- **[monitoring-quickstart.md](./5-monitoring/monitoring-quickstart.md)**
  - Quick start for monitoring
  - Accessing dashboards
  - Basic metrics
  - ~10 min read

- **[monitoring-guide.md](./5-monitoring/monitoring-guide.md)**
  - Complete monitoring setup
  - Custom metrics and dashboards
  - Alerting configuration
  - ~30 min read

### 📡 [6. API Documentation](./6-api/)
REST API reference and usage.

- **[server-control-api.md](./6-api/server-control-api.md)**
  - Complete API reference
  - Endpoint documentation
  - Request/response examples
  - Testing procedures
  - ~20 min read

### 📦 [7. Archive](./7-archive/)
Legacy documentation (V1) for reference.

- `quick-start-guide.md` - V1 quick start
- `run-guide.md` - V1 run guide
- `project-overview.md` - Original project overview
- `summary_v1.md` - V1 summary

## 🎯 Quick Navigation

### For Complete Beginners
1. Read [ARCHITECTURE.md](./ARCHITECTURE.md) - Understand the system
2. Read [BEGINNER-SSH-SETUP-GUIDE.md](./1-getting-started/BEGINNER-SSH-SETUP-GUIDE.md)
3. Read [VISUAL-SETUP-GUIDE.md](./1-getting-started/VISUAL-SETUP-GUIDE.md)
4. Follow [CKEY-QUICKSTART.md](./1-getting-started/CKEY-QUICKSTART.md)
5. Set up [GitHub Secrets](./4-github/github-setup-guide-v2.md)
6. Deploy! 🚀

### For CKey.com Users
1. [CKEY-QUICKSTART.md](./1-getting-started/CKEY-QUICKSTART.md) (30 minutes)
2. [SERVER-CONFIGURATION.md](./4-github/SERVER-CONFIGURATION.md) ⭐ (port setup)
3. [ckey-server-setup-guide.md](./2-server-setup/ckey-server-setup-guide.md) (detailed)
4. [V2-MULTI-SERVER-DEPLOYMENT-SUMMARY.md](./3-deployment/V2-MULTI-SERVER-DEPLOYMENT-SUMMARY.md)

### For Advanced Users
1. [ARCHITECTURE.md](./ARCHITECTURE.md) - Deep dive into system architecture
2. [multi-server-deployment-guide-v2.md](./3-deployment/multi-server-deployment-guide-v2.md)
3. [github-runner-setup-guide-v2.md](./3-deployment/github-runner-setup-guide-v2.md)
4. [ci-cd-guide.md](./4-github/ci-cd-guide.md)

### For API Integration
1. [server-control-api.md](./6-api/server-control-api.md)

## 📖 Documentation Versions

### V2.1 (Current) - Combined Server Format
- ✅ Deploy to unlimited servers
- ✅ **Simplified 2-secret configuration** (`host:ssh_port:app_port`)
- ✅ Parallel, sequential, and blue-green deployment
- ✅ Per-server health checks with app ports
- ✅ Automatic rollback
- ✅ Self-hosted GitHub runners (optional)
- ✅ WSL and CKey.com support
- ✅ Backward compatible with standard VPS (defaults to `:22:8080`)

**V2.1 Guides:**
- Server Configuration (`4-github/SERVER-CONFIGURATION.md`) ⭐ **NEW!**
- Getting Started (all in `1-getting-started/`)
- Server Setup (`2-server-setup/`)
- Deployment (`3-deployment/`)
- GitHub Configuration (`4-github/github-setup-guide-v2.md`)

### V2.0 (Legacy) - Separate Port Configuration
- Separate secrets for IPs, SSH ports, and app ports
- Requires 6 secrets total (deprecated)
- See `SSH-PORT-CONFIGURATION.md` for reference

### V1 (Legacy) - Single Server
- Single server per environment
- Basic deployment
- Manual configuration

**V1 Guides:**
- Archived in `7-archive/`
- `github-setup-guide.md` (V1 GitHub setup)

## 🔑 Key Features Documented

### Multi-Server Support
- Deploy to 3, 5, 10, or unlimited servers
- Load balancing ready
- High availability setup

### CKey.com Specific
- Non-standard SSH ports
- Custom port mappings
- Root user configuration
- Panel integration

### WSL Support
- Windows Subsystem for Linux
- File system differences
- SSH key management
- Path translations

### Deployment Strategies
- **Parallel**: Deploy to all servers simultaneously (fastest)
- **Sequential**: One-by-one with fail-fast (safest)
- **Blue-Green**: Zero-downtime production deployment

### Security
- SSH key authentication
- GitHub Secrets encryption
- Firewall configuration
- Best practices

## 🆘 Getting Help

### Common Issues
1. **Can't SSH to server**: Check [BEGINNER-SSH-SETUP-GUIDE.md](./1-getting-started/BEGINNER-SSH-SETUP-GUIDE.md) Issue #6, #7
2. **WSL problems**: See WSL section in beginner guide
3. **CKey.com port issues**: Check port mapping in CKey panel
4. **Deployment fails**: See troubleshooting in [multi-server-deployment-guide-v2.md](./3-deployment/multi-server-deployment-guide-v2.md)

### Troubleshooting Guides
- SSH Issues: [BEGINNER-SSH-SETUP-GUIDE.md](./1-getting-started/BEGINNER-SSH-SETUP-GUIDE.md) § Troubleshooting
- Deployment Issues: [V2-MULTI-SERVER-DEPLOYMENT-SUMMARY.md](./3-deployment/V2-MULTI-SERVER-DEPLOYMENT-SUMMARY.md) § Troubleshooting
- CI/CD Issues: [ci-cd-guide.md](./4-github/ci-cd-guide.md) § Troubleshooting

## 📊 Estimated Reading Time

| Level | Guides | Time |
|-------|--------|------|
| **Beginner** | Getting Started (all 3) | ~1.5 hours |
| **Intermediate** | + Server Setup + Deployment | ~3 hours |
| **Advanced** | + All guides | ~5 hours |

## 🎓 Learning Path

```
START HERE
    │
    ├─► Beginner: 1-getting-started/ (all files)
    │   │
    │   ├─► Follow CKEY-QUICKSTART.md
    │   │   │
    │   │   └─► Deploy to 5 servers ✅
    │   │
    │   └─► Advanced: Learn deployment strategies
    │       │
    │       └─► 3-deployment/multi-server-deployment-guide-v2.md
    │
    └─► Expert: Self-hosted runners
        │
        └─► 3-deployment/github-runner-setup-guide-v2.md
```

## 📝 Contributing

When adding new documentation:
1. Place in appropriate numbered folder
2. Update this README.md
3. Follow existing formatting style
4. Include troubleshooting section
5. Add to Quick Navigation if important

## 🔗 External Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Docker Documentation](https://docs.docker.com/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [CKey.com](https://ckey.com/) (Vietnamese VPS provider)

---

**Version:** 2.1 (Combined Server Format)
**Created:** 2025-12-14
**Last Updated:** 2025-12-14
**Project:** CS4445 Subscription Server

**Quick Links:**
- [Main Repository](../)
- [Architecture Guide](./ARCHITECTURE.md) ⭐ **Essential Reading**
- [Server Configuration V2.1](./4-github/SERVER-CONFIGURATION.md) ⭐ **NEW!**
- [Getting Started](./1-getting-started/BEGINNER-SSH-SETUP-GUIDE.md)
- [API Docs](./6-api/server-control-api.md)
- [Troubleshooting](./1-getting-started/BEGINNER-SSH-SETUP-GUIDE.md#troubleshooting)
