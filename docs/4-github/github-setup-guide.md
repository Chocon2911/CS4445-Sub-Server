# GitHub Setup Guide for CI/CD

Complete guide for setting up GitHub repository, secrets, tokens, and container registry for the CI/CD pipeline.

## Table of Contents

1. [Repository Setup](#repository-setup)
2. [GitHub Actions Permissions](#github-actions-permissions)
3. [GitHub Container Registry](#github-container-registry)
4. [GitHub Secrets Configuration](#github-secrets-configuration)
5. [Personal Access Tokens](#personal-access-tokens)
6. [Environment Configuration](#environment-configuration)
7. [Verification Steps](#verification-steps)
8. [Troubleshooting](#troubleshooting)

## Repository Setup

### Step 1: Create GitHub Repository

#### Option A: Via GitHub Website

1. Go to https://github.com
2. Click the **"+"** icon in the top-right corner
3. Select **"New repository"**
4. Fill in details:
   - **Repository name**: `cs4445-sub-server` (or your preferred name)
   - **Description**: "CS4445 Subscription Server - Load Testing API"
   - **Visibility**:
     - **Public**: Anyone can see (recommended for learning)
     - **Private**: Only you and collaborators can see
   - **Initialize**:
     - ❌ Don't check "Add a README file" (we already have one)
     - ❌ Don't add .gitignore (we already have one)
     - ❌ Don't choose a license (optional)
5. Click **"Create repository"**

#### Option B: Via GitHub CLI

```bash
# Install GitHub CLI if not installed
# Windows: winget install GitHub.cli
# Mac: brew install gh
# Linux: See https://github.com/cli/cli#installation

# Login to GitHub
gh auth login

# Create repository
gh repo create cs4445-sub-server --public --source=. --remote=origin
```

### Step 2: Push Local Code to GitHub

```bash
# Navigate to your project directory
cd /path/to/CS4445-Sub-Server

# Initialize git (if not already done)
git init

# Add all files
git add .

# Create initial commit
git commit -m "Initial commit with CI/CD setup"

# Add remote (replace YOUR_USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/cs4445-sub-server.git

# Verify remote
git remote -v

# Push to GitHub
git branch -M main
git push -u origin main
```

**Expected output**:
```
Enumerating objects: 100, done.
Counting objects: 100% (100/100), done.
...
To https://github.com/YOUR_USERNAME/cs4445-sub-server.git
 * [new branch]      main -> main
Branch 'main' set up to track remote branch 'main' from 'origin'.
```

## GitHub Actions Permissions

### Step 1: Enable Read and Write Permissions

1. Go to your repository on GitHub
2. Click **"Settings"** tab
3. In the left sidebar, click **"Actions"** → **"General"**
4. Scroll down to **"Workflow permissions"**
5. Select **"Read and write permissions"**
6. ✅ Check **"Allow GitHub Actions to create and approve pull requests"**
7. Click **"Save"**

**Screenshot reference**:
```
Settings → Actions → General → Workflow permissions
  ○ Read repository contents and packages permissions
  ● Read and write permissions  ← SELECT THIS

  ☑ Allow GitHub Actions to create and approve pull requests
```

**Why this is needed**:
- Allows workflows to push Docker images to GitHub Container Registry
- Enables workflows to create tags and releases
- Permits workflows to update deployment status

### Step 2: Allow Actions to Access Secrets

This is enabled by default, but verify:

1. Same page (Settings → Actions → General)
2. Scroll to **"Fork pull request workflows"**
3. Ensure settings match your security requirements

## GitHub Container Registry

### What is GitHub Container Registry (GHCR)?

GitHub Container Registry (ghcr.io) is GitHub's Docker image registry. Your CI/CD pipeline will automatically push Docker images here.

### Step 1: No Setup Required!

Good news: **GitHub Container Registry is automatically available** for your repository. The `GITHUB_TOKEN` used in workflows has automatic access.

### Step 2: Understanding Image URLs

Your images will be stored at:
```
ghcr.io/YOUR_USERNAME/cs4445-sub-server:latest
ghcr.io/YOUR_USERNAME/cs4445-sub-server:main
ghcr.io/YOUR_USERNAME/cs4445-sub-server:v1.0.0
```

**Format**: `ghcr.io/OWNER/REPOSITORY:TAG`

### Step 3: Make Package Public (Optional)

By default, packages are private. To make them public:

**After first workflow run**:

1. Go to your GitHub profile
2. Click **"Packages"** tab
3. Click on **"cs4445-sub-server"** package
4. Click **"Package settings"** (on the right)
5. Scroll down to **"Danger Zone"**
6. Click **"Change visibility"**
7. Select **"Public"**
8. Type the package name to confirm
9. Click **"I understand, change package visibility"**

**Why make it public?**
- Easier to pull without authentication
- Good for open-source projects
- Anyone can use your Docker images

**Keep it private if**:
- It's a production application
- Contains proprietary code
- You want to control access

### Step 4: Verify Package Access

After your first successful CD workflow run:

1. Go to `https://github.com/YOUR_USERNAME?tab=packages`
2. You should see **"cs4445-sub-server"**
3. Click on it to view all image tags

## GitHub Secrets Configuration

### What Are GitHub Secrets?

Secrets are encrypted environment variables used in workflows. They're never exposed in logs.

### Required Secrets: NONE! 🎉

Good news: The CI/CD pipeline uses `GITHUB_TOKEN` which is **automatically provided** by GitHub Actions. You don't need to create it!

### Optional Secrets (For Production)

If you want advanced features, add these optional secrets:

#### 1. SSH Deployment Keys

**For**: Deploying to remote servers via SSH

**Steps to create**:

```bash
# On your local machine, generate SSH key
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/github-deploy

# Copy the PRIVATE key
cat ~/.ssh/github-deploy

# Copy the PUBLIC key to your server
ssh-copy-id -i ~/.ssh/github-deploy.pub user@your-server.com
```

**Add to GitHub**:

1. Copy the **PRIVATE** key content
2. Go to repository **Settings** → **Secrets and variables** → **Actions**
3. Click **"New repository secret"**
4. Name: `DEPLOY_SSH_KEY`
5. Value: Paste the private key
6. Click **"Add secret"**

Also add:
- `DEPLOY_HOST`: Your server hostname/IP
- `DEPLOY_USER`: Your server username
- `DEPLOY_PATH`: Deployment path (e.g., `/app/cs4445-sub-server`)

#### 2. Notification Webhooks

**For**: Sending deployment notifications

**Slack Webhook**:
1. Go to your Slack workspace
2. Create an incoming webhook: https://api.slack.com/messaging/webhooks
3. Copy the webhook URL
4. Add to GitHub Secrets as `SLACK_WEBHOOK`

**Discord Webhook**:
1. Go to your Discord server settings
2. Integrations → Webhooks → New Webhook
3. Copy the webhook URL
4. Add to GitHub Secrets as `DISCORD_WEBHOOK`

#### 3. Docker Hub (Alternative to GHCR)

**For**: Using Docker Hub instead of GHCR

1. Go to https://hub.docker.com
2. Create access token: Account Settings → Security → New Access Token
3. Add to GitHub Secrets:
   - `DOCKERHUB_USERNAME`: Your Docker Hub username
   - `DOCKERHUB_TOKEN`: The access token

### How to Add Secrets

**Step-by-step**:

1. Go to your repository on GitHub
2. Click **"Settings"** tab
3. In left sidebar: **"Secrets and variables"** → **"Actions"**
4. Click **"New repository secret"** button
5. Enter **"Name"** (e.g., `SLACK_WEBHOOK`)
6. Enter **"Secret"** (the actual value)
7. Click **"Add secret"**

**Visual guide**:
```
Repository → Settings → Secrets and variables → Actions

Secrets:
├── Repository secrets
│   ├── DEPLOY_SSH_KEY       (optional)
│   ├── DEPLOY_HOST          (optional)
│   ├── DEPLOY_USER          (optional)
│   ├── SLACK_WEBHOOK        (optional)
│   └── DISCORD_WEBHOOK      (optional)
└── Environment secrets (see next section)
```

## Personal Access Tokens

### Do You Need a PAT?

**No, not for the basic CI/CD pipeline!** The `GITHUB_TOKEN` is automatically provided.

### When You Might Need a PAT

- Accessing private repositories in workflows
- Triggering workflows from other workflows
- GitHub API operations beyond workflow scope

### Creating a Personal Access Token

**If you need one**:

1. Go to GitHub → Click your profile picture → **Settings**
2. Scroll down to **"Developer settings"** (bottom left)
3. Click **"Personal access tokens"** → **"Tokens (classic)"**
4. Click **"Generate new token"** → **"Generate new token (classic)"**
5. Settings:
   - **Note**: "CS4445 CI/CD"
   - **Expiration**: 90 days (or custom)
   - **Scopes**: Select these:
     - ✅ `repo` (Full control of private repositories)
     - ✅ `write:packages` (Upload packages to GitHub Package Registry)
     - ✅ `read:packages` (Download packages from GitHub Package Registry)
     - ✅ `workflow` (Update GitHub Action workflows)
6. Click **"Generate token"**
7. **IMPORTANT**: Copy the token immediately (you won't see it again!)
8. Save it securely (password manager)

**Add to repository**:
1. Go to Settings → Secrets and variables → Actions
2. New repository secret
3. Name: `GH_PAT`
4. Value: Your token
5. Add secret

### Using PAT in Workflows

```yaml
- name: Checkout
  uses: actions/checkout@v4
  with:
    token: ${{ secrets.GH_PAT }}
```

## Environment Configuration

### What Are Environments?

Environments (staging, production) allow you to:
- Require manual approval before deployment
- Set environment-specific secrets
- Add protection rules
- Track deployment history

### Step 1: Create Environments

1. Go to repository **Settings**
2. Click **"Environments"** (left sidebar)
3. Click **"New environment"**
4. Name: `staging`
5. Click **"Configure environment"**
6. (Optional) Add protection rules:
   - ✅ **Required reviewers**: Select team members
   - ✅ **Wait timer**: Delay before deployment (e.g., 5 minutes)
   - ✅ **Deployment branches**: Specify which branches can deploy
7. Click **"Save protection rules"**
8. Repeat for `production` environment

### Step 2: Add Environment Secrets

Environment-specific secrets (optional):

1. In the environment configuration page
2. Scroll to **"Environment secrets"**
3. Click **"Add secret"**
4. Add secrets specific to this environment:

**Staging secrets**:
```
DEPLOY_HOST=staging.example.com
POSTGRES_PASSWORD=staging_password
GRAFANA_PASSWORD=staging_password
```

**Production secrets**:
```
DEPLOY_HOST=production.example.com
POSTGRES_PASSWORD=production_secure_password
GRAFANA_PASSWORD=production_secure_password
```

### Step 3: Require Manual Approval (Production)

For production environment:

1. Go to Settings → Environments → production
2. Under **"Deployment protection rules"**:
3. ✅ Check **"Required reviewers"**
4. Select reviewers (yourself or team members)
5. Save

Now production deployments will require approval!

## Verification Steps

### Step 1: Verify Repository Access

```bash
# Clone your repository
git clone https://github.com/YOUR_USERNAME/cs4445-sub-server.git
cd cs4445-sub-server

# Check remote
git remote -v
```

**Expected**:
```
origin  https://github.com/YOUR_USERNAME/cs4445-sub-server.git (fetch)
origin  https://github.com/YOUR_USERNAME/cs4445-sub-server.git (push)
```

### Step 2: Trigger CI Workflow

```bash
# Make a small change
echo "# Test" >> README.md

# Commit and push
git add README.md
git commit -m "Test CI workflow"
git push origin main
```

**Verify**:
1. Go to https://github.com/YOUR_USERNAME/cs4445-sub-server/actions
2. You should see a workflow running
3. Click on it to view progress
4. All jobs should succeed ✅

### Step 3: Verify Permissions

After first workflow run, check:

1. Go to Actions tab
2. If you see errors like "Permission denied" or "403 Forbidden":
   - Check Settings → Actions → General → Workflow permissions
   - Ensure "Read and write permissions" is selected

### Step 4: Verify Container Registry

After first successful CD workflow:

1. Go to your profile → Packages
2. Click on "cs4445-sub-server"
3. You should see image tags: `latest`, `main`, etc.

### Step 5: Test Image Pull

```bash
# Login to GitHub Container Registry
echo $YOUR_GITHUB_PAT | docker login ghcr.io -u YOUR_USERNAME --password-stdin

# Pull the image
docker pull ghcr.io/YOUR_USERNAME/cs4445-sub-server:latest

# Verify
docker images | grep cs4445-sub-server
```

### Step 6: Test Deployment (Optional)

If you have a server:

```bash
# SSH to your server
ssh user@your-server.com

# Clone repository
git clone https://github.com/YOUR_USERNAME/cs4445-sub-server.git
cd cs4445-sub-server

# Setup environment
cp .env.example .env
vi .env  # Edit with your values

# Run deployment
./scripts/deploy.sh staging
```

## Troubleshooting

### Issue 1: "Permission denied" in Workflow

**Error**:
```
Error: failed to push: denied: permission_denied
```

**Solution**:
1. Settings → Actions → General
2. Workflow permissions → "Read and write permissions"
3. Re-run the workflow

### Issue 2: Can't See Packages Tab

**Solution**:
- Packages appear after first successful CD workflow
- Go to your profile (not repository) → Packages

### Issue 3: Image Pull Denied

**Error**:
```
Error response from daemon: unauthorized
```

**Solution**:

**If package is private**:
```bash
# Create PAT with read:packages scope
# Then login
echo YOUR_PAT | docker login ghcr.io -u YOUR_USERNAME --password-stdin
```

**Or make package public**:
- Go to Package settings → Change visibility → Public

### Issue 4: Workflow Not Triggering

**Check**:
1. Ensure `.github/workflows/` directory exists
2. Verify YAML syntax: https://www.yamllint.com/
3. Check branch name matches trigger (e.g., `main` vs `master`)

**Force trigger**:
1. Go to Actions tab
2. Select workflow
3. Click "Run workflow"
4. Select branch
5. Run workflow

### Issue 5: Secrets Not Working

**Common issues**:
- Typo in secret name
- Using `${{ secrets.SECRET_NAME }}` not `$SECRET_NAME`
- Secret value has extra spaces/newlines
- Secret is in wrong scope (repository vs environment)

**Fix**:
1. Delete secret
2. Re-create with exact name
3. Ensure no extra whitespace in value
4. Re-run workflow

### Issue 6: Environment Protection Rules Blocking

**Error**: Deployment waiting for approval

**Solution**:
1. Go to Actions tab
2. Click on the deployment
3. Click "Review deployments"
4. Select environment
5. Click "Approve and deploy"

## Security Best Practices

### 1. Never Commit Secrets

✅ **Good**:
```bash
# .env file (in .gitignore)
POSTGRES_PASSWORD=secret123
```

❌ **Bad**:
```java
// In code
String password = "secret123";  // DON'T DO THIS!
```

### 2. Use Environment-Specific Secrets

Different passwords for staging and production:

```
Staging: POSTGRES_PASSWORD=staging_pass
Production: POSTGRES_PASSWORD=very_secure_prod_pass
```

### 3. Rotate Secrets Regularly

- Change passwords every 90 days
- Rotate after team member changes
- Update all environments

### 4. Limit Secret Scope

- Use environment secrets when possible (not repository-wide)
- Only give secrets to workflows that need them

### 5. Review Workflow Permissions

- Use least privilege
- Only enable "write" if necessary
- Review third-party actions

### 6. Monitor Access Logs

1. Go to Settings → Logs → Audit log
2. Review secret access
3. Check for unusual activity

## Quick Setup Checklist

Use this checklist to ensure everything is set up:

### Repository Setup
- [ ] Created GitHub repository
- [ ] Pushed code to repository
- [ ] Verified `.github/workflows/` files exist

### GitHub Actions
- [ ] Enabled "Read and write permissions"
- [ ] Allowed Actions to create PRs
- [ ] Verified workflows can run

### Container Registry
- [ ] Understood GHCR URL format
- [ ] Know how to view packages
- [ ] Decided on public/private visibility

### Secrets (Optional)
- [ ] Decided which secrets are needed
- [ ] Created necessary secrets
- [ ] Tested secret access in workflow

### Environments (Optional)
- [ ] Created staging environment
- [ ] Created production environment
- [ ] Set up approval rules (production)
- [ ] Added environment-specific secrets

### Verification
- [ ] Triggered CI workflow successfully
- [ ] CD workflow completed successfully
- [ ] Found package in container registry
- [ ] Tested image pull (if needed)

## Summary

### Required Setup (Minimum)

1. **Create GitHub repository**
   ```bash
   gh repo create cs4445-sub-server --public
   git push origin main
   ```

2. **Enable workflow permissions**
   - Settings → Actions → General
   - "Read and write permissions"
   - Save

3. **That's it!** 🎉

### Optional Setup (Production)

4. **Create environments**
   - Settings → Environments
   - Add: staging, production
   - Configure protection rules

5. **Add deployment secrets**
   - SSH keys
   - Server details
   - Notification webhooks

6. **Make package public** (if desired)
   - After first workflow
   - Package settings → Change visibility

### What You Get

- ✅ Automatic Docker image builds
- ✅ Free container registry (GHCR)
- ✅ No PAT needed (uses GITHUB_TOKEN)
- ✅ Multi-environment deployment
- ✅ Deployment approval workflow
- ✅ Secure secret management

### Quick Reference

| What | Where | Required? |
|------|-------|-----------|
| Repository | https://github.com/new | ✅ Yes |
| Workflow permissions | Settings → Actions → General | ✅ Yes |
| Container registry | Automatic (ghcr.io) | ✅ Auto |
| GITHUB_TOKEN | Automatic | ✅ Auto |
| Personal Access Token | Settings → Developer settings | ❌ Optional |
| Deployment secrets | Settings → Secrets → Actions | ❌ Optional |
| Environments | Settings → Environments | ❌ Optional |

**Next Steps**: [CI/CD Guide](ci-cd-guide.md) for using the pipeline!

Happy deploying! 🚀
