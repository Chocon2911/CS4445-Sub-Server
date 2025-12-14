# CI/CD Guide - GitHub Actions

Complete guide for Continuous Integration and Continuous Deployment using GitHub Actions.

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [CI/CD Pipeline Architecture](#cicd-pipeline-architecture)
4. [Setup Instructions](#setup-instructions)
5. [CI Workflow](#ci-workflow)
6. [CD Workflow](#cd-workflow)
7. [Deployment Process](#deployment-process)
8. [Rollback Procedure](#rollback-procedure)
9. [Troubleshooting](#troubleshooting)

## Overview

This project uses GitHub Actions for automated CI/CD with the following capabilities:

- **Continuous Integration (CI)**:
  - Automated building and testing on every push/PR
  - Code quality checks
  - Docker image build testing
  - Test reporting

- **Continuous Deployment (CD)**:
  - Automatic Docker image builds
  - Push to GitHub Container Registry
  - Automated deployment to staging
  - Manual/tag-based deployment to production
  - Automatic rollback on failure

## Prerequisites

### 1. GitHub Repository Setup

```bash
# Initialize git if not already done
git init
git add .
git commit -m "Initial commit"

# Create GitHub repository and push
git remote add origin https://github.com/YOUR_USERNAME/cs4445-sub-server.git
git branch -M main
git push -u origin main
```

### 2. GitHub Secrets Configuration

Go to your GitHub repository → Settings → Secrets and variables → Actions

**Required Secrets**: None (uses GITHUB_TOKEN automatically)

**Optional Secrets** (for production deployment):
- `DEPLOY_SSH_KEY`: SSH private key for deployment server
- `DEPLOY_HOST`: Deployment server hostname
- `DEPLOY_USER`: Deployment server username
- `SLACK_WEBHOOK`: Slack webhook for notifications
- `DISCORD_WEBHOOK`: Discord webhook for notifications

### 3. Enable GitHub Packages

1. Go to repository Settings → Actions → General
2. Scroll to "Workflow permissions"
3. Select "Read and write permissions"
4. Check "Allow GitHub Actions to create and approve pull requests"
5. Click "Save"

## CI/CD Pipeline Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Developer Workflow                           │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Push to GitHub (main/develop)                  │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────┴───────────────────┐
          │                                       │
          ▼                                       ▼
┌──────────────────────┐              ┌──────────────────────┐
│    CI Workflow       │              │   CD Workflow        │
│  (.github/workflows  │              │  (.github/workflows  │
│      /ci.yml)        │              │      /cd.yml)        │
└──────────┬───────────┘              └──────────┬───────────┘
           │                                     │
           ▼                                     ▼
    ┌──────────────┐                    ┌──────────────┐
    │ Build & Test │                    │ Build Image  │
    │ Code Quality │                    │ Push to GHCR │
    │ Docker Test  │                    │ Deploy       │
    └──────────────┘                    └──────────────┘
           │                                     │
           ▼                                     ▼
    ┌──────────────┐                    ┌──────────────┐
    │   Success/   │                    │   Staging    │
    │   Failure    │                    │   (auto)     │
    └──────────────┘                    └──────┬───────┘
                                               │
                                               ▼
                                        ┌──────────────┐
                                        │  Production  │
                                        │  (manual/tag)│
                                        └──────────────┘
```

## Setup Instructions

### Step 1: Add Workflow Files

The workflow files are already created in `.github/workflows/`:
- `ci.yml` - Continuous Integration
- `cd.yml` - Continuous Deployment

### Step 2: Configure Environment Variables

Copy `.env.example` to `.env` on your deployment server:

```bash
cp .env.example .env
vi .env  # Edit with your values
```

### Step 3: Test Local Docker Build

```bash
# Build Docker image locally
docker build -t cs4445-sub-server:test .

# Test the image
docker run -d -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/mydatabase \
  cs4445-sub-server:test

# Check health
curl http://localhost:8080/actuator/health
```

### Step 4: Push to GitHub

```bash
git add .
git commit -m "Add CI/CD configuration"
git push origin main
```

The CI workflow will automatically trigger!

## CI Workflow

### Triggered On

- Push to `main` or `develop` branches
- Pull requests to `main` or `develop`

### Jobs

#### 1. Build and Test

```yaml
- Checkout code
- Set up JDK 21
- Cache Maven dependencies
- Compile application
- Run tests (with PostgreSQL service)
- Package application
- Upload build artifacts
```

**What it does**:
- Verifies code compiles
- Runs all unit/integration tests
- Packages JAR file
- Stores artifacts for 7 days

#### 2. Code Quality Check

```yaml
- Checkout code
- Set up JDK 21
- Run static analysis
```

**What it does**:
- Checks code quality
- Can be extended with SonarQube, Checkstyle, etc.

#### 3. Docker Build Test

```yaml
- Checkout code
- Set up Docker Buildx
- Build Docker image
- Test image health
```

**What it does**:
- Verifies Dockerfile builds successfully
- Tests that application starts in container
- Checks health endpoint

### Viewing CI Results

1. Go to your GitHub repository
2. Click "Actions" tab
3. Click on the workflow run
4. View job logs and test results

## CD Workflow

### Triggered On

- **Staging**: Push to `main` branch
- **Production**:
  - Git tags (e.g., `v1.0.0`)
  - Manual workflow dispatch

### Jobs

#### 1. Build and Push Docker Image

```yaml
- Checkout code
- Set up Docker Buildx
- Log in to GHCR
- Extract metadata (tags)
- Build and push multi-platform image
- Generate attestation
```

**What it creates**:
- Docker image in GitHub Container Registry
- Tags: `latest`, `main`, `sha-xxxxx`, version tags
- Platforms: `linux/amd64`, `linux/arm64`

#### 2. Deploy to Staging

**Runs automatically** when:
- Push to `main` branch
- Manual workflow dispatch with environment=staging

**Steps**:
1. Pull latest image
2. Deploy using docker-compose
3. Run health checks
4. Run smoke tests

#### 3. Deploy to Production

**Runs** when:
- Git tag starting with `v` (e.g., `v1.0.0`)
- Manual workflow dispatch with environment=production
- After successful staging deployment

**Steps**:
1. Pull latest image
2. Deploy using docker-compose
3. Run health checks
4. Run smoke tests
5. Send notifications

#### 4. Rollback (on failure)

Automatically triggers if deployment fails:
1. Stops failed deployment
2. Reverts to previous image
3. Sends alert notification

## Deployment Process

### Method 1: Automatic Deployment (Staging)

Simply push to main:

```bash
git add .
git commit -m "Add new feature"
git push origin main
```

GitHub Actions will:
1. Run CI tests
2. Build Docker image
3. Push to registry
4. Deploy to staging automatically

### Method 2: Tagged Release (Production)

Create a version tag:

```bash
# Create and push tag
git tag v1.0.0
git push origin v1.0.0
```

GitHub Actions will:
1. Build Docker image with version tag
2. Deploy to staging
3. After staging success, deploy to production

### Method 3: Manual Deployment

1. Go to GitHub Actions
2. Select "CD - Build and Deploy" workflow
3. Click "Run workflow"
4. Select environment (staging/production)
5. Click "Run workflow"

### Deployment Script

For manual deployment on server:

```bash
# On your deployment server
cd /app/cs4445-sub-server

# Deploy
./scripts/deploy.sh staging
# or
./scripts/deploy.sh production
```

## Rollback Procedure

### Method 1: Automatic Rollback

If deployment fails, GitHub Actions automatically rolls back.

### Method 2: Manual Rollback via Script

On the deployment server:

```bash
# Rollback to previous version
./scripts/rollback.sh previous

# Rollback to specific version
./scripts/rollback.sh v1.0.0
```

### Method 3: Manual Rollback via Docker

```bash
# View available images
docker images | grep cs4445-sub-server

# Update .env with previous tag
vi .env  # Change IMAGE_TAG=v1.0.0

# Restart with previous version
docker compose -f docker-compose.prod.yml down
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```

### Method 4: Rollback via GitHub

1. Go to GitHub → Actions
2. Find the last successful deployment
3. Click "Re-run jobs"

## Docker Registry

### GitHub Container Registry (GHCR)

Images are pushed to: `ghcr.io/YOUR_USERNAME/cs4445-sub-server`

**Viewing images**:
1. Go to your GitHub profile
2. Click "Packages"
3. Find "cs4445-sub-server"

**Pulling images**:

```bash
# Login to GHCR
echo $GITHUB_TOKEN | docker login ghcr.io -u YOUR_USERNAME --password-stdin

# Pull specific version
docker pull ghcr.io/YOUR_USERNAME/cs4445-sub-server:v1.0.0

# Pull latest
docker pull ghcr.io/YOUR_USERNAME/cs4445-sub-server:latest
```

## Environment Configuration

### Staging Environment

Create `.env` on staging server:

```env
GITHUB_REPOSITORY=your-username/cs4445-sub-server
IMAGE_TAG=main
SPRING_PROFILE=staging
APP_PORT=8080
POSTGRES_PASSWORD=staging_secure_password
GRAFANA_PASSWORD=staging_secure_password
LOG_LEVEL=DEBUG
```

### Production Environment

Create `.env` on production server:

```env
GITHUB_REPOSITORY=your-username/cs4445-sub-server
IMAGE_TAG=v1.0.0
SPRING_PROFILE=production
APP_PORT=8080
POSTGRES_PASSWORD=production_very_secure_password
GRAFANA_PASSWORD=production_very_secure_password
LOG_LEVEL=INFO
DDL_AUTO=validate
```

## Monitoring Deployments

### Health Checks

```bash
# Run health check script
./scripts/health-check.sh localhost 8080

# Or manual checks
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/v1/server/status
```

### View Logs

```bash
# All services
docker compose -f docker-compose.prod.yml logs -f

# Specific service
docker compose -f docker-compose.prod.yml logs -f app

# Last 100 lines
docker compose -f docker-compose.prod.yml logs --tail=100 app
```

### Monitor Metrics

- **Grafana**: http://your-server:3000
- **Prometheus**: http://your-server:9090
- **Application**: http://your-server:8080/actuator/prometheus

## Troubleshooting

### Issue 1: Workflow Fails to Push to Registry

**Error**: `denied: permission_denied`

**Solution**:
1. Go to Settings → Actions → General
2. Enable "Read and write permissions"
3. Re-run workflow

### Issue 2: Docker Build Fails

**Error**: `error building image`

**Solution**:
1. Test local build: `docker build -t test .`
2. Check Dockerfile syntax
3. Ensure all files exist
4. Check build logs in GitHub Actions

### Issue 3: Health Check Fails After Deployment

**Error**: `Health check failed`

**Solution**:
1. Check application logs: `docker logs cs4445-app`
2. Verify database connection
3. Check environment variables in `.env`
4. Ensure PostgreSQL is running and healthy

### Issue 4: Cannot Pull Image from GHCR

**Error**: `unauthorized`

**Solution**:
```bash
# Make package public in GitHub
# Or login to GHCR
echo $GITHUB_TOKEN | docker login ghcr.io -u YOUR_USERNAME --password-stdin
```

### Issue 5: Deployment Script Permission Denied

**Error**: `Permission denied`

**Solution**:
```bash
chmod +x scripts/*.sh
```

## Best Practices

### 1. Branch Strategy

- `main` → Production-ready code
- `develop` → Development branch
- `feature/*` → Feature branches
- Use Pull Requests for code review

### 2. Versioning

Follow Semantic Versioning (SemVer):
- `v1.0.0` → Major release
- `v1.1.0` → Minor release (new features)
- `v1.1.1` → Patch release (bug fixes)

### 3. Testing

- Always run tests locally before pushing
- Write tests for new features
- Keep test coverage above 80%

### 4. Security

- Never commit secrets to git
- Use `.env` files (in `.gitignore`)
- Rotate credentials regularly
- Use strong passwords

### 5. Deployment

- Deploy to staging first
- Run smoke tests after deployment
- Monitor logs and metrics
- Have rollback plan ready

## Advanced Configuration

### Adding Notifications

Edit `.github/workflows/cd.yml` to add Slack/Discord notifications:

```yaml
- name: Notify deployment
  uses: slackapi/slack-github-action@v1
  with:
    webhook: ${{ secrets.SLACK_WEBHOOK }}
    payload: |
      {
        "text": "Deployment to ${{ github.event.inputs.environment }} completed!"
      }
```

### Adding Automated Tests

Create `.github/workflows/e2e-tests.yml`:

```yaml
name: E2E Tests
on: [deployment]
jobs:
  e2e:
    runs-on: ubuntu-latest
    steps:
      - name: Run E2E tests
        run: |
          # Your E2E test commands
```

### Multi-Environment Setup

Create separate workflow files:
- `.github/workflows/deploy-staging.yml`
- `.github/workflows/deploy-production.yml`

## Summary

You now have:
- ✅ Automated CI pipeline (build, test, quality checks)
- ✅ Automated CD pipeline (build image, deploy)
- ✅ Multi-environment deployment (staging/production)
- ✅ Automatic rollback on failure
- ✅ Health checks and monitoring
- ✅ Docker image registry (GHCR)
- ✅ Deployment scripts for manual use

**Quick Commands**:

```bash
# Trigger CI
git push origin main

# Trigger Production Deployment
git tag v1.0.0 && git push origin v1.0.0

# Manual Deployment
./scripts/deploy.sh staging

# Rollback
./scripts/rollback.sh v1.0.0

# Health Check
./scripts/health-check.sh
```

Happy deploying! 🚀
