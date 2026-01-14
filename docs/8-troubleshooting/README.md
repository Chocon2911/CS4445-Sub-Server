# 8. Troubleshooting Guide

This folder contains comprehensive troubleshooting documentation for common issues and fixes.

## 📁 Contents

### Current Deployment Issues

- **[DEPLOYMENT-STATUS.md](./DEPLOYMENT-STATUS.md)** - Current deployment status and critical CKey.com Docker issue
  - Documents the blocking `unshare: operation not permitted` error
  - Includes root cause analysis and potential solutions
  - **READ THIS FIRST** if experiencing deployment issues

### Server & Docker Troubleshooting

- **[SERVER-DEPLOYMENT-TROUBLESHOOTING.md](./SERVER-DEPLOYMENT-TROUBLESHOOTING.md)** - Comprehensive server deployment troubleshooting
  - Docker daemon issues on CKey.com
  - Network configuration problems
  - Container startup issues
  - Permission errors

- **[DATABASE-CONSTRAINT-FIX.md](./DATABASE-CONSTRAINT-FIX.md)** - Database constraint violation fix (NEW!)
  - "Value too long for type character varying" error resolution
  - String truncation implementation
  - Docker build cache troubleshooting
  - Complete testing and verification process

### GitHub Actions & CI/CD Troubleshooting

- **[GITHUB-ACTIONS-FIXES.md](./GITHUB-ACTIONS-FIXES.md)** - GitHub Actions workflow fixes
  - Workflow syntax errors
  - Secret configuration issues
  - Deployment failures

- **[TROUBLESHOOTING-CI-CD-FIXES.md](./TROUBLESHOOTING-CI-CD-FIXES.md)** - CI/CD pipeline troubleshooting
  - Build failures
  - Image push issues
  - Deployment pipeline problems

## 🔍 Common Issues Quick Links

### Cannot Deploy to CKey.com
→ See [DEPLOYMENT-STATUS.md](./DEPLOYMENT-STATUS.md) for the current Docker limitation issue

### Docker Daemon Not Starting
→ See [SERVER-DEPLOYMENT-TROUBLESHOOTING.md](./SERVER-DEPLOYMENT-TROUBLESHOOTING.md) - Section on Docker daemon configuration

### GitHub Actions Failing
→ See [GITHUB-ACTIONS-FIXES.md](./GITHUB-ACTIONS-FIXES.md) and [TROUBLESHOOTING-CI-CD-FIXES.md](./TROUBLESHOOTING-CI-CD-FIXES.md)

### Health Check Failures
→ See [SERVER-DEPLOYMENT-TROUBLESHOOTING.md](./SERVER-DEPLOYMENT-TROUBLESHOOTING.md) - Health check section

### Database "Value Too Long" Error
→ See [DATABASE-CONSTRAINT-FIX.md](./DATABASE-CONSTRAINT-FIX.md) - Complete fix for VARCHAR constraint violations

### Docker Build Not Picking Up Code Changes
→ See [DATABASE-CONSTRAINT-FIX.md](./DATABASE-CONSTRAINT-FIX.md#docker-build-issues) - Docker cache troubleshooting

## 🆘 Getting Help

1. **Check the relevant troubleshooting guide** above
2. **Search for your error message** in the files using Ctrl+F
3. **Follow the step-by-step solutions** provided
4. **Check recent commit history** for related fixes

## 📚 Related Documentation

- [Server Setup Guide](../2-server-setup/) - Initial server configuration
- [Deployment Guide](../3-deployment/) - Deployment workflows
- [GitHub Setup](../4-github/) - GitHub Actions configuration

---

**Last Updated:** 2025-12-21
