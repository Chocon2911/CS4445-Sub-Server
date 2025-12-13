# Complete Beginner's Guide to the Fake Packet Server

## What Is This Project?

This is a **test server** - think of it as a special program that runs on your computer and pretends to be very busy. It's designed to make your computer work hard (using CPU and RAM) so you can test how your computer handles heavy workloads.

### Simple Analogy
Imagine you have a factory (your computer), and this program is like giving the factory workers (CPU) a lot of tasks to do, while also filling up the warehouse (RAM) with materials. You get to control how busy the workers are and how full the warehouse gets.

## What Does It Do?

When you send a request to this server, it:
1. **Makes your CPU work hard** by doing complex calculations (like finding prime numbers, creating security hashes, doing math)
2. **Uses up your RAM** by creating large amounts of temporary data (lists, tables, arrays)
3. **Saves information to a database** (like writing to a notebook to keep track of what happened)
4. **Tells you what it did** by sending back a report

## What You Need Before Starting

### 1. Java (The Programming Language)
- **What it is**: Java is like the engine that runs this program
- **Version needed**: Java 25 or newer
- **Why**: This project is written in Java, so you need Java installed to run it

### 2. Docker (For the Database)
- **What it is**: Docker is like a virtual container that runs a mini-computer inside your computer
- **Why**: We use it to run PostgreSQL (a database - like an organized filing cabinet for data)

### 3. A Terminal/Command Prompt
- **Windows**: Use "Command Prompt" or "PowerShell"
- **Mac/Linux**: Use "Terminal"
- **What it is**: A text-based way to give commands to your computer

## Step-by-Step Setup Instructions

### Step 1: Install Java

**Option A: Using SDKMAN (Recommended for Mac/Linux)**
1. Open Terminal
2. Copy and paste this command:
   ```bash
   curl -s "https://get.sdkman.io" | bash
   ```
3. Close and reopen Terminal
4. Install Java 25:
   ```bash
   sdk install java 25-open
   ```

**Option B: Manual Installation (Windows/Mac/Linux)**
1. Go to https://www.oracle.com/java/technologies/downloads/
2. Download Java 25 for your operating system
3. Run the installer
4. Follow the installation wizard

**How to check if Java is installed:**
```bash
java -version
```
You should see something like "java version 25..."

### Step 2: Install Docker

1. Go to https://www.docker.com/products/docker-desktop/
2. Download Docker Desktop for your operating system
3. Install it
4. Start Docker Desktop (you'll see a whale icon)

**How to check if Docker is running:**
```bash
docker --version
```

### Step 3: Navigate to the Project Folder

Open Terminal/Command Prompt and go to the project folder:

**Windows:**
```bash
cd "C:\Users\Admin\OneDrive - Hanoi University of Science and Technology\New folder\year 4-1\CS4445\TeamProject\CS4445-Sub-Server"
```

**Mac/Linux/WSL:**
```bash
cd "/mnt/c/Users/Admin/OneDrive - Hanoi University of Science and Technology/New folder/year 4-1/CS4445/TeamProject/CS4445-Sub-Server"
```

### Step 4: Start the Server

Type this command:
```bash
./mvnw spring-boot:run
```

**What happens:**
- Maven (a build tool) will download necessary files (first time only - may take a few minutes)
- Docker will automatically start PostgreSQL database
- The server will start and show lots of text
- When you see "Started Cs4445SubServerApplication" - it's ready!

**The server is now running on:** `http://localhost:8080`

## How to Use the Server

### What is an API Request?

Think of it like sending a letter:
- **You write a letter** (the request) with specific instructions
- **You mail it** to the server
- **The server reads it**, does the work, and **writes back** (the response)

### Method 1: Using the Test Scripts (Easiest)

We've created simple scripts that automatically send requests for you.

**On Windows:**
1. Open a NEW Command Prompt (keep the server running in the first one)
2. Navigate to the project folder (see Step 3 above)
3. Run:
   ```bash
   test-api.bat
   ```

**On Mac/Linux/WSL:**
1. Open a NEW Terminal (keep the server running in the first one)
2. Navigate to the project folder
3. Run:
   ```bash
   ./test-api.sh
   ```

**What you'll see:**
- The script will send different test requests
- You'll see responses with processing times and resource usage
- Check Task Manager (Windows) or Activity Monitor (Mac) to see CPU/RAM spike!

### Method 2: Manual Requests (More Control)

You can use a tool called `curl` to send custom requests.

**Basic format:**
```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{
    "packetId": "my-test-001",
    "cpuIntensity": 5,
    "ramIntensity": 5,
    "processingTimeMs": 2000,
    "payload": "my test data"
  }'
```

**Understanding the parameters:**

- **packetId**: A unique name for this request (like "test-1", "test-2")
- **cpuIntensity**: How hard to work the CPU (1 = easy, 10 = very hard)
- **ramIntensity**: How much memory to use (1 = little, 10 = a lot)
- **processingTimeMs**: Minimum time to process in milliseconds (1000 = 1 second)
- **payload**: Any text you want to send (optional)

**Examples:**

**Light load (won't stress your computer much):**
```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"test-1","cpuIntensity":2,"ramIntensity":2,"processingTimeMs":500}'
```

**Medium load (moderate stress):**
```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"test-2","cpuIntensity":5,"ramIntensity":5,"processingTimeMs":2000}'
```

**Heavy load (will make your computer work hard!):**
```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"test-3","cpuIntensity":10,"ramIntensity":10,"processingTimeMs":5000}'
```

### Method 3: Using Postman (Visual Tool)

Postman is a free app that makes sending API requests easier with a visual interface.

1. Download Postman from https://www.postman.com/downloads/
2. Install and open Postman
3. Click "New" → "HTTP Request"
4. Set up the request:
   - **Method**: Change "GET" to "POST"
   - **URL**: `http://localhost:8080/api/v1/fakePacket`
   - **Headers**: Click "Headers" tab
     - Add: `Content-Type` = `application/json`
   - **Body**: Click "Body" tab → Select "raw" → Select "JSON"
     - Paste:
       ```json
       {
         "packetId": "postman-test-1",
         "cpuIntensity": 5,
         "ramIntensity": 5,
         "processingTimeMs": 2000,
         "payload": "testing from Postman"
       }
       ```
5. Click "Send"
6. See the response below!

## Understanding the Response

When you send a request, you get back a response like this:

```json
{
  "packetId": "test-1",
  "status": "SUCCESS",
  "processingTimeMs": 2500,
  "cpuCycles": 150000,
  "memoryUsedBytes": 52428800,
  "result": "Packet processed. Total cycles for this packet ID: 150000, Logs count: 1",
  "timestamp": "2025-12-13T10:30:45.123"
}
```

**What each field means:**

- **packetId**: The ID you sent (so you know which request this is)
- **status**: Either "SUCCESS" or "FAILED"
- **processingTimeMs**: How long it took (in milliseconds)
  - 1000 ms = 1 second
  - 2500 ms = 2.5 seconds
- **cpuCycles**: Number of calculations performed (bigger = more CPU work)
- **memoryUsedBytes**: How much RAM was used (in bytes)
  - 1,048,576 bytes = 1 MB
  - 52,428,800 bytes = 50 MB
- **result**: A message describing what happened
- **timestamp**: When the processing finished

## How to See the Computer Load

### Windows:
1. Press `Ctrl + Shift + Esc` to open Task Manager
2. Click the "Performance" tab
3. Watch "CPU" and "Memory" graphs
4. Send a heavy request and watch the graphs spike!

### Mac:
1. Press `Cmd + Space`, type "Activity Monitor", press Enter
2. Click the "CPU" tab to see processor usage
3. Click the "Memory" tab to see RAM usage
4. Send a heavy request and watch the usage increase!

### Linux:
1. Open Terminal
2. Type: `htop` (install it first: `sudo apt install htop`)
3. You'll see a live view of CPU and memory
4. Send requests and watch the bars fill up!

## Stress Testing (Making Your Computer Really Work!)

If you want to really push your computer, send multiple requests at the same time:

**Windows (Command Prompt):**
```bash
FOR /L %i IN (1,1,10) DO start /B curl -X POST http://localhost:8080/api/v1/fakePacket -H "Content-Type: application/json" -d "{\"packetId\":\"stress-%i\",\"cpuIntensity\":8,\"ramIntensity\":8,\"processingTimeMs\":3000}"
```

**Mac/Linux/WSL:**
```bash
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/v1/fakePacket \
    -H "Content-Type: application/json" \
    -d "{\"packetId\":\"stress-$i\",\"cpuIntensity\":8,\"ramIntensity\":8,\"processingTimeMs\":3000}" &
done
wait
```

This sends 10 requests simultaneously - watch your CPU and RAM usage skyrocket!

## Server Control (Open/Close the Server)

**NEW FEATURE**: You can now "close" and "open" the server without actually shutting it down!

### What Does This Mean?

Think of it like a shop:
- **OPEN**: The shop accepts customers (server processes fakePacket requests)
- **CLOSED**: The shop has a "Closed" sign but is still there (server rejects requests but keeps running)

**Important**: This does NOT shut down your computer or the server program - it just makes the server refuse to process packets.

### How to Use It

**Check if server is open or closed:**
```bash
curl http://localhost:8080/api/v1/server/status
```

**Close the server:**
```bash
curl -X POST http://localhost:8080/api/v1/server/close
```

After closing, if you try to send a packet:
```bash
curl -X POST http://localhost:8080/api/v1/fakePacket \
  -H "Content-Type: application/json" \
  -d '{"packetId":"test","cpuIntensity":5,"ramIntensity":5}'
```

You'll get:
```json
{
  "packetId": "test",
  "status": "REJECTED",
  "result": "Server is currently closed. Please open the server first..."
}
```

**Open the server again:**
```bash
curl -X POST http://localhost:8080/api/v1/server/open
```

Now packets will be accepted again!

### Why Is This Useful?

1. **Practice error handling**: See what happens when a server is down
2. **Controlled testing**: Close the server, prepare your test, then open it exactly when you want
3. **Simulating maintenance**: Pretend the server is under maintenance
4. **Learning**: Understand how systems handle unavailable services

### Quick Test Script

We've created a script that tests this automatically!

**Windows:**
```bash
test-server-control.bat
```

**Mac/Linux:**
```bash
./test-server-control.sh
```

The script will:
1. Check the server status
2. Send a packet (should work)
3. Close the server
4. Try to send a packet (should be rejected)
5. Open the server
6. Send a packet again (should work)

**For complete details**, see [docs/server-control-api.md](server-control-api.md)

## How to Stop the Server

1. Go to the Terminal/Command Prompt where the server is running
2. Press `Ctrl + C`
3. Wait a few seconds
4. The server will shut down

## Common Problems and Solutions

### Problem 1: "Port 8080 is already in use"
**What it means**: Another program is using port 8080
**Solution**:
- Option A: Stop the other program using port 8080
- Option B: Change the port in `src/main/resources/application.properties`:
  ```
  server.port=8081
  ```
  Then use `http://localhost:8081` instead

### Problem 2: "JAVA_HOME is not defined"
**What it means**: Java is not properly installed or configured
**Solution**:
1. Make sure Java is installed: `java -version`
2. Set JAVA_HOME environment variable (Google "set JAVA_HOME [your operating system]")

### Problem 3: "Cannot connect to Docker"
**What it means**: Docker is not running
**Solution**:
1. Start Docker Desktop
2. Wait for it to fully start (whale icon stops animating)
3. Try running the server again

### Problem 4: "curl: command not found"
**What it means**: curl is not installed
**Solution**:
- **Windows**: curl comes with Windows 10+. If missing, download from https://curl.se/windows/
- **Mac**: curl is pre-installed
- **Linux**: Install with `sudo apt install curl`

### Problem 5: Server starts but requests fail
**What it means**: Database might not be ready
**Solution**:
1. Wait 10-20 seconds after server starts
2. Check Docker Desktop to see if PostgreSQL container is running
3. Try the health check: `curl http://localhost:8080/api/v1/health`

## What's Happening Behind the Scenes?

When you send a request with `cpuIntensity: 10` and `ramIntensity: 10`, the server:

1. **CPU Work:**
   - Finds prime numbers (checks if 1,000,000+ numbers are prime)
   - Creates 10,000 security hashes (like passwords)
   - Does 500,000 complex math calculations
   - Generates 100,000 random IDs and manipulates text

2. **RAM Work:**
   - Creates a list with 1,000,000 text entries
   - Creates a table with 100,000 rows of nested data
   - Allocates 1,000 chunks of memory (10KB each)
   - Builds complex nested data structures

3. **Database Work:**
   - Saves all the results to PostgreSQL
   - Reads previous results
   - Calculates statistics

All of this makes your computer CPU and RAM work hard, simulating a heavy workload!

## Project File Structure (What's What?)

```
CS4445-Sub-Server/
├── docs/
│   └── summary_v1.md          ← You are here!
├── src/
│   ├── main/
│   │   ├── java/              ← The actual program code
│   │   └── resources/         ← Configuration files
│   └── test/                  ← Test code
├── test-api.sh                ← Test script for Mac/Linux
├── test-api.bat               ← Test script for Windows
├── README.md                  ← Technical documentation
├── pom.xml                    ← Maven configuration (tells Maven what to download)
├── compose.yaml               ← Docker configuration for database
└── mvnw / mvnw.cmd           ← Maven wrapper (runs Maven without installing it)
```

## Useful Commands Cheat Sheet

| Task | Command |
|------|---------|
| Start the server | `./mvnw spring-boot:run` |
| Stop the server | Press `Ctrl + C` in the server terminal |
| Check if server is running | `curl http://localhost:8080/api/v1/health` |
| Run tests | `./mvnw test` |
| Build the project | `./mvnw clean install` |
| Check Java version | `java -version` |
| Check Docker | `docker --version` |
| See Docker containers | `docker ps` |
| Stop all Docker containers | `docker compose down` |

## Next Steps

1. **Start small**: Begin with low intensity values (2-3) to see how it works
2. **Monitor**: Keep Task Manager/Activity Monitor open to see the effects
3. **Increase gradually**: Slowly increase intensity to see the difference
4. **Experiment**: Try different combinations of CPU and RAM intensity
5. **Stress test**: When comfortable, try concurrent requests
6. **Learn**: Check the main README.md for more technical details

## Getting Help

If you're stuck:
1. Check the "Common Problems" section above
2. Read the detailed README.md in the project root
3. Make sure Docker Desktop is running
4. Make sure Java 25 is installed
5. Try restarting your computer (classic but effective!)

## Summary

You now have a working server that can:
- ✅ Accept HTTP requests
- ✅ Perform CPU-intensive calculations
- ✅ Allocate large amounts of RAM
- ✅ Save data to a database
- ✅ Return detailed processing reports

This is perfect for:
- Testing how your computer handles load
- Learning about servers and APIs
- Understanding CPU and RAM usage
- Practicing with HTTP requests
- Course projects and demonstrations

**Remember**: This is a testing tool. The "work" it does is intentionally meaningless - it's designed to create load, not to accomplish anything productive. That's why it's called "fake packet" processing!

Good luck with your testing! 🚀
