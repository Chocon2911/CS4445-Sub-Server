# GitHub Actions Fixes Summary

## Issues Fixed

### 1. ✅ Attestation Error (FIXED)

**Error:**
```
Error: Failed to get ID token: Error message: Unable to get ACTIONS_ID_TOKEN_REQUEST_URL env variable
```

**Root Cause:**
The `actions/attest-build-provenance@v1` action requires `id-token: write` and `attestations: write` permissions to generate OIDC tokens for build provenance.

**Files Modified:**
- `.github/workflows/cd.yml` - Added permissions
- `.github/workflows/cd-v2-multi-server.yml` - Added permissions

**Fix Applied:**
```yaml
permissions:
  contents: read
  packages: write
  id-token: write        # ← Added
  attestations: write    # ← Added
```

---

### 2. ❌ GitHub Secret Configuration (NEEDS UPDATE)

**Current Secret:**
```
STAGING_SERVERS=n1.ckey.vn:3129:8080
```

**Your Actual SSH Connection:**
```bash
ssh -p 3219 -i ~/.ssh/ckey-deploy root@n1.ckey.vn
```

**Problem:** SSH port mismatch (3129 vs 3219)

**Correct Secret Value:**
```
n1.ckey.vn:3219:8080
```

**Format:** `host:ssh_port:app_port`
- `n1.ckey.vn` - Server hostname
- `3219` - SSH port (matches your connection)
- `8080` - Application port (Spring Boot default)

---

## Actions Required

### Step 1: Commit and Push Workflow Fixes

```bash
cd /mnt/c/Users/Admin/OneDrive\ -\ Hanoi\ University\ of\ Science\ and\ Technology/New\ folder/year\ 4-1/CS4445/TeamProject/CS4445-Sub-Server

git add .github/workflows/cd.yml
git add .github/workflows/cd-v2-multi-server.yml
git commit -m "fix: add id-token and attestations permissions for build attestation"
git push origin dev
```

---

### Step 2: Update GitHub Secret

Go to your repository on GitHub:

1. Navigate to: **Settings** → **Secrets and variables** → **Actions**
2. Find `STAGING_SERVERS` secret
3. Click **Update**
4. Change value to: `n1.ckey.vn:3219:8080`
5. Click **Update secret**

---

### Step 3: Verify the Fix

After pushing the workflow changes and updating the secret:

1. Go to **Actions** tab
2. Trigger a new workflow run (push to `dev` or `main` branch)
3. Check that:
   - ✅ Build attestation step succeeds
   - ✅ No OIDC token errors
   - ✅ Deployment uses correct SSH port (3219)

---

## Additional Fixes Needed

### Missing Deployment Scripts

Your CD workflows reference scripts that may not exist:

```bash
./scripts/deploy-to-server.sh
./scripts/rollback-server.sh
```

Check if these files exist:
```bash
ls scripts/
```

If they don't exist, you'll need to create them or update the workflows to use different deployment methods.

---

## Test Reporter Permission Error

**Error:**
```
HttpError: Resource not accessible by integration
```

**Location:** CI workflow (`ci.yml` line 51-59)

**Action:** `dorny/test-reporter@v1`

**Fix Option 1 - Add Permissions:**
```yaml
code-quality:
  name: Code Quality Check
  runs-on: ubuntu-latest
  needs: build-and-test
  permissions:
    checks: write          # ← Add this
    pull-requests: write   # ← Add this
```

**Fix Option 2 - Disable Test Reporter (if not needed):**
Remove or comment out the test reporter step entirely.

---

## Summary

| Issue | Status | Action Required |
|-------|--------|-----------------|
| Attestation OIDC token error | ✅ Fixed in code | Commit and push changes |
| SSH port mismatch in secret | ❌ Needs update | Update GitHub secret |
| Test reporter permissions | ⚠️ Optional | Add permissions or remove |
| Missing deployment scripts | ⚠️ Unknown | Check if scripts exist |

---

## Quick Commands

### Push Workflow Fixes
```bash
git add .github/workflows/
git commit -m "fix: add required permissions for attestation and test reporting"
git push
```

### Check for Missing Scripts
```bash
ls -la scripts/
```

### View Workflow Runs
```bash
# Install GitHub CLI if not already installed
gh workflow list
gh run list
gh run view <run-id>
```

---

**Last Updated:** 2025-12-14
