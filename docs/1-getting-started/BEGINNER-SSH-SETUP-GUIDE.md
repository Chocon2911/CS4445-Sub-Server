# Complete Beginner's Guide to SSH Keys and Server Setup

**For absolute beginners** - Every step explained with screenshots and examples.

## Table of Contents

1. [What Are SSH Keys?](#what-are-ssh-keys)
2. [Installing Required Tools](#installing-required-tools)
3. [Creating Your SSH Key](#creating-your-ssh-key)
4. [Understanding Public vs Private Keys](#understanding-public-vs-private-keys)
5. [Adding Public Key to CKey.com Servers](#adding-public-key-to-ckeycom-servers)
6. [Adding Private Key to GitHub](#adding-private-key-to-github)
7. [Testing Your Setup](#testing-your-setup)
8. [Complete Step-by-Step Server Setup](#complete-step-by-step-server-setup)
9. [Troubleshooting](#troubleshooting)

## What Are SSH Keys?

### Simple Explanation

**SSH keys are like a lock and key for your servers:**

- 🔑 **Private Key** = Your physical key (NEVER share this!)
- 🔓 **Public Key** = The lock you put on doors (safe to share)

**How it works:**
1. You put the **public key** (lock) on your server
2. You keep the **private key** (key) on your computer
3. When connecting, your private key "unlocks" the public key
4. You get access to the server!

**Why use SSH keys instead of passwords?**
- ✅ More secure than passwords
- ✅ Can't be guessed or brute-forced
- ✅ Needed for automated deployments (GitHub Actions)
- ✅ Industry standard

### What We'll Create

```
Your Computer:
└── ~/.ssh/
    ├── ckey-deploy          ← PRIVATE KEY (keep secret!)
    └── ckey-deploy.pub      ← PUBLIC KEY (put on servers)

Your 5 CKey Servers:
├── Server 1 (~/.ssh/authorized_keys) ← Add public key here
├── Server 2 (~/.ssh/authorized_keys) ← Add public key here
├── Server 3 (~/.ssh/authorized_keys) ← Add public key here
├── Server 4 (~/.ssh/authorized_keys) ← Add public key here
└── Server 5 (~/.ssh/authorized_keys) ← Add public key here

GitHub (Secrets):
└── DEPLOY_SSH_KEY ← Add PRIVATE KEY here (for automated deployment)
```

## Installing Required Tools

### For Windows Users

#### Option 1: Use Windows Terminal with PowerShell (Recommended)

1. **Install Windows Terminal** (if you don't have it):
   - Open Microsoft Store
   - Search "Windows Terminal"
   - Click "Get" or "Install"
   - Open Windows Terminal

2. **SSH is already included** in Windows 10/11!
   - Open PowerShell
   - Type: `ssh -V`
   - You should see: `OpenSSH_for_Windows_8.x.x.x`

#### Option 2: Use Git Bash

1. **Download Git for Windows**:
   - Go to: https://git-scm.com/download/win
   - Download and install
   - During installation, check "Git Bash Here"

2. **Open Git Bash**:
   - Right-click in any folder
   - Select "Git Bash Here"
   - A terminal window opens

### For Mac Users

1. **Open Terminal**:
   - Press `Cmd + Space`
   - Type "Terminal"
   - Press Enter

2. **SSH is pre-installed!**
   - Type: `ssh -V`
   - You should see version number

### For Linux Users

1. **Open Terminal**:
   - Press `Ctrl + Alt + T`
   - Or search for "Terminal" in applications

2. **Install SSH** (if not installed):
   ```bash
   sudo apt update
   sudo apt install openssh-client
   ```

### For Windows WSL (Windows Subsystem for Linux) Users

**Important:** If you use WSL, you need to understand where SSH keys are created:

#### Understanding WSL and Windows Paths

WSL and Windows have **separate file systems**:

```
Windows:   C:\Users\Admin\.ssh\
WSL:       /home/username/.ssh/
```

**Problem:** If you create SSH keys on Windows but use WSL (or vice versa), you'll get "No such file or directory" errors!

#### Solution: Choose ONE Approach

**Option 1: Create Keys in WSL (Recommended)**

```bash
# In WSL terminal
cd ~/.ssh
ssh-keygen -t ed25519 -C "ckey-deployment" -f ckey-deploy

# Keys will be at: /home/username/.ssh/ckey-deploy
```

**Option 2: Create Keys in Windows, Copy to WSL**

```bash
# If you already created keys in Windows PowerShell
# Copy them to WSL:

# In WSL terminal
mkdir -p ~/.ssh
chmod 700 ~/.ssh

# Copy from Windows to WSL
cp /mnt/c/Users/Admin/.ssh/ckey-deploy ~/.ssh/
cp /mnt/c/Users/Admin/.ssh/ckey-deploy.pub ~/.ssh/

# Set permissions
chmod 600 ~/.ssh/ckey-deploy
chmod 644 ~/.ssh/ckey-deploy.pub

# Verify
ls -la ~/.ssh/
```

**Option 3: Use Windows Path in WSL**

```bash
# When connecting from WSL, use Windows path
ssh -i /mnt/c/Users/Admin/.ssh/ckey-deploy root@server
```

**Recommendation:** If you primarily use WSL, create and keep keys in WSL (`~/.ssh/`).

## Creating Your SSH Key

### Step 1: Open Terminal

**Windows:** Open PowerShell or Git Bash
**Mac/Linux:** Open Terminal

### Step 2: Navigate to SSH Directory

```bash
# Go to your home directory
cd ~

# Create .ssh folder if it doesn't exist
mkdir -p .ssh

# Go into .ssh folder
cd .ssh

# Check what's already there
ls -la
```

**What you might see:**
```
# If empty (first time):
total 0

# If you have existing keys:
id_rsa
id_rsa.pub
known_hosts
```

### Step 3: Generate SSH Key

**Copy and paste this exact command:**

```bash
ssh-keygen -t ed25519 -C "ckey-deployment" -f ckey-deploy
```

**Let me explain each part:**
- `ssh-keygen` = program to create SSH keys
- `-t ed25519` = type of encryption (modern and secure)
- `-C "ckey-deployment"` = comment/label for this key
- `-f ckey-deploy` = filename for the key

**What happens next:**

```
Generating public/private ed25519 key pair.
Enter passphrase (empty for no passphrase):
```

**What to do:**
1. **Press Enter** (don't type anything)
2. You'll see: `Enter same passphrase again:`
3. **Press Enter again**

**Why no passphrase?**
- For automated deployment, we need no passphrase
- The key will still be secure because:
  - Only you have access to your computer
  - Private key stays on your computer
  - GitHub Secrets encrypts it

**You'll see:**

```
Your identification has been saved in ckey-deploy
Your public key has been saved in ckey-deploy.pub
The key fingerprint is:
SHA256:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx ckey-deployment
The key's randomart image is:
+--[ED25519 256]--+
|        .o.      |
|       .  +      |
|      . .. o     |
|       o  .      |
|        S        |
|       o         |
|      .          |
|     .           |
|    .            |
+----[SHA256]-----+
```

**Success!** ✅ You created SSH keys!

### Step 4: View Your Keys

```bash
# List files in .ssh directory
ls -la
```

You should see:
```
ckey-deploy       ← PRIVATE KEY (secret!)
ckey-deploy.pub   ← PUBLIC KEY (share this)
```

## Understanding Public vs Private Keys

### View Your Public Key

```bash
# Display public key
cat ckey-deploy.pub
```

**You'll see something like:**
```
ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIJqBz... ckey-deployment
```

**This is your PUBLIC KEY:**
- ✅ Safe to share
- ✅ Put on all your servers
- ✅ Like a lock you can give to anyone
- **This goes in `~/.ssh/authorized_keys` on servers**

### View Your Private Key (Don't Share!)

```bash
# Display private key
cat ckey-deploy
```

**You'll see:**
```
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
QyNTUxOQAAACCagcxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
... (many lines) ...
-----END OPENSSH PRIVATE KEY-----
```

**This is your PRIVATE KEY:**
- ❌ NEVER share this
- ❌ NEVER commit to git
- ✅ Keep on your computer
- ✅ Add to GitHub Secrets (encrypted)
- **Like your house key - keep it secret!**

## Adding Public Key to CKey.com Servers

You need to add your **public key** to ALL 5 servers. Here's how:

### Method 1: During Server Creation (Easiest)

When creating servers on CKey.com:

1. **Copy your public key:**
   ```bash
   # Display public key
   cat ~/.ssh/ckey-deploy.pub

   # Copy the entire output (Ctrl+C or Cmd+C)
   ```

2. **In CKey.com dashboard:**
   - When creating server
   - Look for "SSH Keys" section
   - Click "Add SSH Key"
   - **Name:** `deployment-key`
   - **Key:** Paste your public key
   - Save

3. **Select this key** when creating each of your 5 servers

4. **Done!** The public key is automatically added to each server

### Method 2: After Server Creation (Manual)

If servers are already created:

#### Step 1: Get Server IP and Login Info

From CKey.com dashboard, note:
```
Server 1 IP: 45.XXX.XXX.101
Username: ubuntu (or root)
Password: (temporary password from CKey)
```

#### Step 2: Copy Public Key to Clipboard

**Windows (PowerShell):**
```powershell
# Copy to clipboard
Get-Content ~/.ssh/ckey-deploy.pub | clip

# Or display it
cat ~/.ssh/ckey-deploy.pub
# Then manually select and copy (Ctrl+C)
```

**Mac:**
```bash
# Copy to clipboard
cat ~/.ssh/ckey-deploy.pub | pbcopy

# Or display it
cat ~/.ssh/ckey-deploy.pub
# Then manually select and copy (Cmd+C)
```

**Linux:**
```bash
# Copy to clipboard (if xclip installed)
cat ~/.ssh/ckey-deploy.pub | xclip -selection clipboard

# Or display it
cat ~/.ssh/ckey-deploy.pub
# Then manually select and copy (Ctrl+Shift+C)
```

#### Step 3: Connect to Server (First Time - Using Password)

```bash
# Replace with YOUR server IP
ssh ubuntu@45.XXX.XXX.101
```

**What happens:**
```
The authenticity of host '45.XXX.XXX.101' can't be established.
ED25519 key fingerprint is SHA256:xxxxxxxxxxxxxxxxxxxxxxx.
Are you sure you want to continue connecting (yes/no/[fingerprint])?
```

**Type:** `yes` and press Enter

```
Warning: Permanently added '45.XXX.XXX.101' (ED25519) to the list of known hosts.
ubuntu@45.XXX.XXX.101's password:
```

**Type the password** from CKey.com (you won't see it as you type)
**Press Enter**

**You're now on the server!** You'll see:
```
ubuntu@cs4445-staging-1:~$
```

#### Step 4: Add Public Key to Server

**On the server, type these commands:**

```bash
# Create .ssh directory if doesn't exist
mkdir -p ~/.ssh

# Set correct permissions
chmod 700 ~/.ssh

# Add your public key (REPLACE with your actual public key!)
echo "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIO5mdLly... ckey-deployment" >> ~/.ssh/authorized_keys

# Set correct permissions
chmod 600 ~/.ssh/authorized_keys
```

**⚠️ IMPORTANT:**
- Use **quotes** around your public key: `"ssh-ed25519..."`
- Use **two greater-than signs**: `>>` (not one!)
- Replace with YOUR actual public key from `cat ~/.ssh/ckey-deploy.pub`

**Common mistakes:**
```bash
# ❌ WRONG (no quotes):
echo ssh-ed25519 AAAAC3... >> ~/.ssh/authorized_keys

# ❌ WRONG (missing >>):
echo "ssh-ed25519 AAAAC3..." ~/.ssh/authorized_keys

# ✅ CORRECT:
echo "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIO5... ckey-deployment" >> ~/.ssh/authorized_keys
```

**Verify it was added:**
```bash
cat ~/.ssh/authorized_keys
# Should display your public key
```

**Exit from server:**
```bash
exit
```

#### Step 5: Test SSH Key Connection

**From your computer:**

```bash
# Connect using SSH key (no password needed!)
ssh -i ~/.ssh/ckey-deploy ubuntu@45.XXX.XXX.101
```

**If it works:**
- ✅ You connect WITHOUT asking for password
- ✅ You see: `ubuntu@cs4445-staging-1:~$`
- **Success!** SSH key is working!

**If it asks for password:**
- ❌ Public key not added correctly
- Go back to Step 4 and try again

#### Step 6: Repeat for All 5 Servers

Repeat Steps 3-5 for each server:
- Server 2: `ssh ubuntu@45.XXX.XXX.102`
- Server 3: `ssh ubuntu@45.XXX.XXX.103`
- Server 4: `ssh ubuntu@45.XXX.XXX.201`
- Server 5: `ssh ubuntu@45.XXX.XXX.202`

### Method 3: Use Automated Script (After Manual Setup on First Server)

**Create a script to add key to all servers at once:**

```bash
# Create script
cat > add-key-to-servers.sh << 'EOF'
#!/bin/bash

# Your public key
PUBLIC_KEY=$(cat ~/.ssh/ckey-deploy.pub)

# Your servers (UPDATE THESE!)
SERVERS=(
    "45.XXX.XXX.101"
    "45.XXX.XXX.102"
    "45.XXX.XXX.103"
    "45.XXX.XXX.201"
    "45.XXX.XXX.202"
)

echo "Adding SSH key to all servers..."

for server in "${SERVERS[@]}"; do
    echo "Processing $server..."

    # This will ask for password for each server
    ssh ubuntu@$server "mkdir -p ~/.ssh && chmod 700 ~/.ssh && echo '$PUBLIC_KEY' >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys && sort -u ~/.ssh/authorized_keys -o ~/.ssh/authorized_keys"

    echo "✓ Done: $server"
done

echo ""
echo "Testing connections..."
for server in "${SERVERS[@]}"; do
    echo -n "Testing $server... "
    ssh -i ~/.ssh/ckey-deploy -o ConnectTimeout=5 ubuntu@$server "echo 'OK'" 2>/dev/null && echo "✓" || echo "✗"
done
EOF

# Make executable
chmod +x add-key-to-servers.sh

# Run it
./add-key-to-servers.sh
```

## Adding Private Key to GitHub

Your **private key** needs to be added to GitHub Secrets so GitHub Actions can deploy to your servers.

### Step 1: Install GitHub CLI

**Windows (PowerShell as Administrator):**
```powershell
winget install GitHub.cli
```

**Mac:**
```bash
brew install gh
```

**Linux (Ubuntu/Debian):**
```bash
curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
sudo apt update
sudo apt install gh
```

**Verify installation:**
```bash
gh --version
```

### Step 2: Login to GitHub

```bash
gh auth login
```

**Follow the prompts:**

```
? What account do you want to log into?
> GitHub.com

? What is your preferred protocol for Git operations?
> HTTPS

? Authenticate Git with your GitHub credentials?
> Yes

? How would you like to authenticate GitHub CLI?
> Login with a web browser
```

**You'll see:**
```
! First copy your one-time code: XXXX-XXXX
Press Enter to open github.com in your browser...
```

1. **Copy the code** (e.g., `AB12-CD34`)
2. **Press Enter**
3. Browser opens → **Paste the code**
4. **Authorize GitHub CLI**

**Success:**
```
✓ Authentication complete.
✓ Logged in as YOUR_USERNAME
```

### Step 3: Add Private Key to GitHub Secret

```bash
# Navigate to your project
cd /path/to/CS4445-Sub-Server

# Add private key as secret
gh secret set DEPLOY_SSH_KEY < ~/.ssh/ckey-deploy
```

**You'll see:**
```
✓ Set secret DEPLOY_SSH_KEY for YOUR_USERNAME/cs4445-sub-server
```

**What just happened:**
- ✅ Your **private key** was encrypted and stored in GitHub Secrets
- ✅ GitHub Actions can now use it to connect to servers
- ✅ Nobody can see the actual key (encrypted)

### Step 4: Add Other Required Secrets

```bash
# Add your server IPs (UPDATE WITH YOUR ACTUAL IPs!)
gh secret set STAGING_SERVER_IPS -b "45.XXX.XXX.101,45.XXX.XXX.102,45.XXX.XXX.103"

gh secret set PRODUCTION_SERVER_IPS -b "45.XXX.XXX.201,45.XXX.XXX.202"

# Add deployment user (usually "ubuntu" or "root")
gh secret set DEPLOY_USER -b "ubuntu"

# Add deployment path
gh secret set DEPLOY_PATH -b "/app/cs4445-sub-server"
```

### Step 5: Verify All Secrets

```bash
gh secret list
```

**You should see:**
```
DEPLOY_SSH_KEY         Updated YYYY-MM-DD
DEPLOY_USER           Updated YYYY-MM-DD
DEPLOY_PATH           Updated YYYY-MM-DD
STAGING_SERVER_IPS    Updated YYYY-MM-DD
PRODUCTION_SERVER_IPS Updated YYYY-MM-DD
```

**Perfect!** ✅ All secrets are configured

## Testing Your Setup

### ⚠️ IMPORTANT: CKey.com Uses Non-Standard SSH Ports!

**Before testing, check your CKey.com server control panel!**

CKey.com servers use **custom SSH ports** (not the default port 22). Each server has different port mappings.

#### Example from CKey.com Panel:

```
Server: n1.ckey.vn
Port Mappings:
  1424 -> 22    ← SSH port is 1424 (not 22!)
  1425 -> 3000
  1426 -> 7681
  1427 -> 8080
  1428 -> 9090
```

#### How to Find Your SSH Port:

1. **Log in to CKey.com**
2. **Go to your server details**
3. **Look for "Port Mappings" or "Connection Info"**
4. **Find which port maps to 22** (SSH)
   - Example: `1424 -> 22` means use port 1424
5. **Note the connection command** shown in panel:
   - Example: `ssh -p 1424 root@n1.ckey.vn`

#### Correct SSH Command Format for CKey.com:

```bash
# Generic format:
ssh -p PORT -i ~/.ssh/ckey-deploy USERNAME@HOSTNAME

# Real example (adjust to YOUR values):
ssh -p 1424 -i ~/.ssh/ckey-deploy root@n1.ckey.vn
```

**Important Notes:**
- **Username:** Usually `root` on CKey.com (check your panel!)
- **Port:** Check your server's port mapping
- **Hostname:** Can be IP address or `n1.ckey.vn`, `n2.ckey.vn`, etc.

#### Quick Port Reference:

If you have 5 CKey servers, your ports might look like:

```
Server 1: ssh -p 1424 root@n1.ckey.vn
Server 2: ssh -p 2424 root@n2.ckey.vn
Server 3: ssh -p 3424 root@n3.ckey.vn
Server 4: ssh -p 4424 root@n4.ckey.vn
Server 5: ssh -p 5424 root@n5.ckey.vn

Note: These are examples! Check YOUR panel for actual values!
```

### Test 1: SSH to Each Server

**Update the commands with YOUR server details from CKey.com panel:**

```bash
# Test server 1 (REPLACE with your actual port and hostname!)
ssh -p 1424 -i ~/.ssh/ckey-deploy root@n1.ckey.vn

# You should connect WITHOUT password
# Type 'exit' to disconnect

# Test all servers (UPDATE ports and hostnames!)
ssh -p 1424 -i ~/.ssh/ckey-deploy root@n1.ckey.vn "echo 'Server 1 OK'"
ssh -p 2424 -i ~/.ssh/ckey-deploy root@n2.ckey.vn "echo 'Server 2 OK'"
ssh -p 3424 -i ~/.ssh/ckey-deploy root@n3.ckey.vn "echo 'Server 3 OK'"
ssh -p 4424 -i ~/.ssh/ckey-deploy root@n4.ckey.vn "echo 'Server 4 OK'"
ssh -p 5424 -i ~/.ssh/ckey-deploy root@n5.ckey.vn "echo 'Server 5 OK'"
```

**If using IP addresses instead:**

```bash
# Example with IP addresses
ssh -p 1424 -i ~/.ssh/ckey-deploy root@46.32.184.182 "echo 'Server 1 OK'"
```

**Expected output:**
```
Testing 45.XXX.XXX.101...
Connected successfully to 45.XXX.XXX.101
Testing 45.XXX.XXX.102...
Connected successfully to 45.XXX.XXX.102
... (all 5 servers)
```

### Test 2: Verify GitHub Secrets

```bash
# In your project directory
cd /path/to/CS4445-Sub-Server

# Check secrets
gh secret list

# Push a test commit (will trigger deployment)
echo "# SSH setup test" >> README.md
git add README.md
git commit -m "Test SSH setup"
git push origin main
```

**Go to GitHub:**
1. https://github.com/YOUR_USERNAME/cs4445-sub-server/actions
2. Click on the running workflow
3. Watch "Deploy to Staging Servers" job
4. Should deploy to all 3 staging servers successfully!

## Complete Step-by-Step Server Setup

Now that SSH keys are configured, let's set up all 5 servers:

### Automated Setup (Recommended)

**Create and run the setup script:**

```bash
# Create setup script
cat > setup-all-servers.sh << 'EOF'
#!/bin/bash

# UPDATE THESE WITH YOUR ACTUAL IPs!
STAGING_SERVERS=(
    "45.XXX.XXX.101"
    "45.XXX.XXX.102"
    "45.XXX.XXX.103"
)

PRODUCTION_SERVERS=(
    "45.XXX.XXX.201"
    "45.XXX.XXX.202"
)

ALL_SERVERS=("${STAGING_SERVERS[@]}" "${PRODUCTION_SERVERS[@]}")

# Your GitHub repository (UPDATE THIS!)
GITHUB_REPO="YOUR_USERNAME/cs4445-sub-server"

echo "Setting up ${#ALL_SERVERS[@]} servers..."

for server in "${ALL_SERVERS[@]}"; do
    echo ""
    echo "═══════════════════════════════════"
    echo "Setting up: $server"
    echo "═══════════════════════════════════"

    ssh -i ~/.ssh/ckey-deploy ubuntu@$server << ENDSSH
        set -e

        echo "→ Updating system..."
        sudo apt update && sudo apt upgrade -y

        echo "→ Installing Docker..."
        if ! command -v docker &> /dev/null; then
            curl -fsSL https://get.docker.com | sudo sh
            sudo usermod -aG docker ubuntu
        fi

        echo "→ Installing Docker Compose..."
        if ! command -v docker-compose &> /dev/null; then
            sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-\$(uname -s)-\$(uname -m)" -o /usr/local/bin/docker-compose
            sudo chmod +x /usr/local/bin/docker-compose
        fi

        echo "→ Setting up application directory..."
        sudo mkdir -p /app
        sudo chown ubuntu:ubuntu /app

        echo "→ Cloning repository..."
        if [ ! -d /app/cs4445-sub-server ]; then
            git clone https://github.com/$GITHUB_REPO.git /app/cs4445-sub-server
        else
            cd /app/cs4445-sub-server && git pull
        fi

        echo "→ Creating .env file..."
        cat > /app/cs4445-sub-server/.env << 'ENVEOF'
POSTGRES_PASSWORD=change_this_password_123
GRAFANA_PASSWORD=change_this_password_456
SPRING_PROFILE=production
LOG_LEVEL=INFO
ENVEOF

        echo "→ Configuring firewall..."
        sudo ufw --force enable
        sudo ufw allow 22/tcp
        sudo ufw allow 8080/tcp
        sudo ufw allow 9090/tcp
        sudo ufw allow 3000/tcp

        echo "✓ Server $server setup complete!"
ENDSSH

    echo "✓ Finished: $server"
done

echo ""
echo "═══════════════════════════════════"
echo "✓ All servers configured!"
echo "═══════════════════════════════════"
echo ""
echo "Next steps:"
echo "1. Verify GitHub secrets are set"
echo "2. Push to main branch to deploy"
EOF

# Make executable
chmod +x setup-all-servers.sh

# Update the IPs and GitHub repo in the script
nano setup-all-servers.sh
# Edit the IP addresses and GITHUB_REPO line

# Run it!
./setup-all-servers.sh
```

**This will take about 10-15 minutes for all 5 servers.**

## Troubleshooting

### Issue 1: "Permission denied (publickey)"

**Problem:** Can't connect to server with SSH key

**Solutions:**

1. **Check if public key is on server:**
   ```bash
   # Connect with password first
   ssh ubuntu@45.XXX.XXX.101

   # Check authorized_keys file
   cat ~/.ssh/authorized_keys

   # Should contain your public key starting with: ssh-ed25519 AAAAC3...
   ```

2. **Check file permissions on server:**
   ```bash
   # On server
   chmod 700 ~/.ssh
   chmod 600 ~/.ssh/authorized_keys
   ```

3. **Re-add public key:**
   ```bash
   # From your computer
   cat ~/.ssh/ckey-deploy.pub

   # Copy the output
   # SSH to server with password
   # Add to authorized_keys again
   ```

### Issue 2: "No such file or directory" (can't find key)

**Problem:** SSH can't find your private key

**Solutions:**

1. **Check if key exists:**
   ```bash
   ls -la ~/.ssh/ckey-deploy
   ```

2. **If not found, create it again:**
   ```bash
   cd ~/.ssh
   ssh-keygen -t ed25519 -C "ckey-deployment" -f ckey-deploy
   ```

3. **Use full path when connecting:**
   ```bash
   # Instead of: ssh -i ckey-deploy ...
   # Use full path:
   ssh -i ~/.ssh/ckey-deploy ubuntu@SERVER_IP
   ```

### Issue 3: GitHub CLI not working

**Problem:** `gh` command not found

**Solutions:**

1. **Verify installation:**
   ```bash
   which gh
   # Should show: /usr/bin/gh or similar
   ```

2. **Reinstall GitHub CLI** (see installation section above)

3. **Use web interface instead:**
   - Go to: https://github.com/YOUR_USERNAME/cs4445-sub-server/settings/secrets/actions
   - Click "New repository secret"
   - Name: `DEPLOY_SSH_KEY`
   - Value: Copy entire private key from `cat ~/.ssh/ckey-deploy`
   - Click "Add secret"

### Issue 4: Can't copy SSH key

**Problem:** Clipboard doesn't work

**Solutions:**

1. **Display and manually copy:**
   ```bash
   cat ~/.ssh/ckey-deploy.pub
   ```

   Then manually select all text and copy (Ctrl+C / Cmd+C)

2. **Save to file:**
   ```bash
   cat ~/.ssh/ckey-deploy.pub > ~/Desktop/public_key.txt
   ```

   Open file from Desktop and copy

### Issue 5: Server asks for password even with SSH key

**Problem:** SSH key not working

**Solutions:**

1. **Check SSH config on server:**
   ```bash
   # On server (connect with password)
   sudo nano /etc/ssh/sshd_config

   # Make sure these lines exist and are uncommented:
   PubkeyAuthentication yes
   AuthorizedKeysFile .ssh/authorized_keys

   # Save and restart SSH
   sudo systemctl restart sshd
   ```

2. **Check key format:**
   ```bash
   # Public key should start with: ssh-ed25519
   cat ~/.ssh/ckey-deploy.pub
   ```

3. **Try verbose mode to see error:**
   ```bash
   ssh -v -i ~/.ssh/ckey-deploy ubuntu@SERVER_IP
   # Look for error messages
   ```

### Issue 6: "Identity file not accessible" (WSL Users)

**Problem:** Created SSH key in Windows but using WSL (or vice versa)

**Error Message:**
```
Warning: Identity file /home/username/.ssh/ckey-deploy not accessible: No such file or directory.
```

**Solutions:**

1. **Check where your key actually is:**
   ```bash
   # In WSL, check WSL location
   ls -la ~/.ssh/

   # In WSL, check Windows location
   ls -la /mnt/c/Users/Admin/.ssh/
   ```

2. **Copy keys from Windows to WSL:**
   ```bash
   # In WSL terminal
   mkdir -p ~/.ssh
   chmod 700 ~/.ssh
   cp /mnt/c/Users/Admin/.ssh/ckey-deploy ~/.ssh/
   cp /mnt/c/Users/Admin/.ssh/ckey-deploy.pub ~/.ssh/
   chmod 600 ~/.ssh/ckey-deploy
   chmod 644 ~/.ssh/ckey-deploy.pub
   ```

3. **Or use Windows path in WSL commands:**
   ```bash
   ssh -i /mnt/c/Users/Admin/.ssh/ckey-deploy root@server
   ```

### Issue 7: "Permission denied" on CKey.com servers

**Problem:** Can't connect to CKey.com server even with correct key

**Common Causes:**

1. **Wrong SSH port** (CKey uses non-standard ports!)
   ```bash
   # ❌ WRONG (default port 22):
   ssh -i ~/.ssh/ckey-deploy root@n1.ckey.vn

   # ✅ CORRECT (use custom port from panel):
   ssh -p 1424 -i ~/.ssh/ckey-deploy root@n1.ckey.vn
   ```

2. **Wrong username:**
   ```bash
   # ❌ WRONG:
   ssh -p 1424 -i ~/.ssh/ckey-deploy ubuntu@n1.ckey.vn

   # ✅ CORRECT (CKey usually uses 'root'):
   ssh -p 1424 -i ~/.ssh/ckey-deploy root@n1.ckey.vn
   ```

3. **Check CKey.com panel for correct connection command:**
   - Log in to CKey.com
   - Go to server details
   - Look for "SSH Connection" or "Connection Info"
   - Copy the exact command shown

**Complete CKey.com Connection Example:**
```bash
# What CKey panel shows:
ssh -p 1424 root@n1.ckey.vn

# Add your SSH key to it:
ssh -p 1424 -i ~/.ssh/ckey-deploy root@n1.ckey.vn
```

## Summary Checklist

### SSH Key Creation
- [ ] Opened terminal
- [ ] Created `.ssh` directory
- [ ] Generated SSH key pair: `ssh-keygen -t ed25519 -C "ckey-deployment" -f ckey-deploy`
- [ ] Verified two files created: `ckey-deploy` and `ckey-deploy.pub`

### Public Key (Lock) - Added to Servers
- [ ] Copied public key: `cat ~/.ssh/ckey-deploy.pub`
- [ ] Added to Server 1 `~/.ssh/authorized_keys`
- [ ] Added to Server 2 `~/.ssh/authorized_keys`
- [ ] Added to Server 3 `~/.ssh/authorized_keys`
- [ ] Added to Server 4 `~/.ssh/authorized_keys`
- [ ] Added to Server 5 `~/.ssh/authorized_keys`
- [ ] Set permissions: `chmod 600 ~/.ssh/authorized_keys` on each server

### Private Key (Key) - Added to GitHub
- [ ] Installed GitHub CLI: `gh`
- [ ] Logged in: `gh auth login`
- [ ] Added private key to GitHub: `gh secret set DEPLOY_SSH_KEY < ~/.ssh/ckey-deploy`
- [ ] Added server IPs: `gh secret set STAGING_SERVER_IPS -b "IP1,IP2,IP3"`
- [ ] Added production IPs: `gh secret set PRODUCTION_SERVER_IPS -b "IP4,IP5"`
- [ ] Verified secrets: `gh secret list`

### Testing
- [ ] Can SSH to all servers without password
- [ ] GitHub deployment works
- [ ] All 5 servers responding

**You're all set!** 🎉

## Quick Reference Card

**Print this and keep it handy:**

```
╔══════════════════════════════════════════════════════════╗
║              SSH KEY QUICK REFERENCE                     ║
╠══════════════════════════════════════════════════════════╣
║ YOUR COMPUTER                                            ║
║ Location: ~/.ssh/ckey-deploy                             ║
║ Private Key: KEEP SECRET!                                ║
║ Public Key: ~/.ssh/ckey-deploy.pub (safe to share)      ║
╠══════════════════════════════════════════════════════════╣
║ SERVERS (all 5)                                          ║
║ Location: ~/.ssh/authorized_keys                         ║
║ Contains: Your PUBLIC key                                ║
║ Permissions: chmod 600 ~/.ssh/authorized_keys           ║
╠══════════════════════════════════════════════════════════╣
║ GITHUB                                                   ║
║ Location: Settings → Secrets → Actions                   ║
║ DEPLOY_SSH_KEY: Your PRIVATE key (encrypted)            ║
╠══════════════════════════════════════════════════════════╣
║ COMMON COMMANDS                                          ║
║ Connect: ssh -i ~/.ssh/ckey-deploy ubuntu@SERVER_IP     ║
║ CKey.com: ssh -p PORT -i ~/.ssh/ckey-deploy root@HOST   ║
║ Copy public key: cat ~/.ssh/ckey-deploy.pub             ║
║ Add to GitHub: gh secret set DEPLOY_SSH_KEY < ~/.ssh/ckey-deploy ║
╠══════════════════════════════════════════════════════════╣
║ WSL USERS (Windows Subsystem for Linux)                 ║
║ Copy keys: cp /mnt/c/Users/Admin/.ssh/ckey-deploy ~/.ssh/║
║ Or use: ssh -i /mnt/c/Users/Admin/.ssh/ckey-deploy ...  ║
╠══════════════════════════════════════════════════════════╣
║ CKEY.COM SPECIFIC                                        ║
║ Check panel for: Port mapping (e.g., 1424 -> 22)        ║
║ Username: Usually 'root' (not ubuntu)                    ║
║ Example: ssh -p 1424 -i ~/.ssh/ckey-deploy root@n1.ckey.vn ║
╚══════════════════════════════════════════════════════════╝
```

---

**Congratulations!** You now understand SSH keys and how to use them! 🎉

**Next:** [CKEY-QUICKSTART.md](CKEY-QUICKSTART.md) for complete server setup
