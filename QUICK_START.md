# Veri-Skill Quick Start Guide

## 🚀 Get Everything Running in 5 Minutes

### Prerequisites
- Node.js 22+
- Python 3.12+
- Java 17+
- Docker (for MySQL)

---

## 1️⃣ Start MySQL (Docker)

```bash
docker run -d \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=veriskill \
  -p 3306:3306 \
  --name veriskill-mysql \
  mysql:8.0

# Verify
docker ps | grep veriskill-mysql
```

---

## 2️⃣ Start Flask AI Service

**Terminal 1:**
```bash
cd veri-skill-ai
source venv/bin/activate  # WSL/Linux: source venv/bin/activate
                           # Windows: venv\Scripts\activate
python3 app.py
# 🟢 Running on http://127.0.0.1:5001
```

**Test it:**
```bash
curl -X POST http://127.0.0.1:5001/score \
  -F "job_description=Java developer" \
  -F "resume=@Updated_Resume_Vishnukant.pdf"
```

---

## 3️⃣ Start ZK Bridge Service

**Terminal 2:**
```bash
cd veri-skill-zk-bridge
npm install
npm run dev
# 🟢 Running on http://localhost:3000
```

**Test it:**
```bash
curl -X POST http://localhost:3000/prove \
  -H "Content-Type: application/json" \
  -d '{
    "private_score": 85,
    "required_threshold": 75
  }'
```

---

## 4️⃣ Start Spring Boot Backend

**Terminal 3:**
```bash
cd veri-skill-backend
mvn spring-boot:run
# 🟢 Running on http://localhost:8080
# 📚 API Docs: http://localhost:8080/swagger-ui.html
```

**Test it:**
```bash
# Create a job
curl -X POST http://localhost:8080/api/v1/jobs \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Senior Java Developer",
    "description": "10+ years experience",
    "required_threshold": 75,
    "recruiterId": "rec-001"
  }'

# Should return job ID
```

---

## 5️⃣ Test ZK Circuit

**Terminal 4:**
```bash
# At project root
yarn test:local
# ✅ All tests should pass
```

---

## 6️⃣ (Optional) Start React Frontend

**Terminal 5:**
```bash
cd veri-skill-frontend
npm start
# 🟢 Running on http://localhost:3000
```

---

## ✅ Verify Everything Works

### Check all services running:
```bash
# Test Flask
curl http://127.0.0.1:5001/health

# Test ZK Bridge
curl http://localhost:3000/health

# Test Backend
curl http://localhost:8080/actuator/health

# Test Database
mysql -h 127.0.0.1 -u root -proot -e "SELECT 1;"
```

### Expected responses:
```
Flask: 200 OK
ZK Bridge: 200 OK
Backend: 200 OK
MySQL: 1
```

---

## 📊 Service Status Dashboard

```
┌────────────────────────────────────────────────────┐
│        SERVICE STATUS                              │
├────────────────────────────────────────────────────┤
│ ✅ MySQL          → 3306 (veriskill database)      │
│ ✅ Flask AI       → 5001 (resume scoring)          │
│ ✅ ZK Bridge      → 3000 (proof generation)        │
│ ✅ Spring Boot    → 8080 (REST API)                │
│ ✅ React          → 3000 (Frontend - optional)     │
├────────────────────────────────────────────────────┤
│ Overall Status: 🟢 ALL SERVICES RUNNING            │
└────────────────────────────────────────────────────┘
```

---

## 🧪 Complete Workflow Test

### Step 1: Create a Job
```bash
JOB_ID=$(curl -s -X POST http://localhost:8080/api/v1/jobs \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Senior Developer",
    "description": "10+ years exp",
    "required_threshold": 75,
    "recruiterId": "rec-001"
  }' | jq -r '.id')

echo "Created Job: $JOB_ID"
```

### Step 2: Upload Resume & Score
```bash
CANDIDATE=$(curl -s -X POST http://localhost:8080/api/v1/candidates/upload \
  -F "jobId=$JOB_ID" \
  -F "fullName=John Doe" \
  -F "email=john@example.com" \
  -F "resume=@Updated_Resume_Vishnukant.pdf")

CANDIDATE_ID=$(echo $CANDIDATE | jq -r '.id')
echo "Candidate: $CANDIDATE_ID"
echo "Status: $(echo $CANDIDATE | jq -r '.verification_status')"
```

### Step 3: Check Verification
```bash
curl -s http://localhost:8080/api/v1/candidates/$CANDIDATE_ID/verification \
  -H "Content-Type: application/json" | jq '.'

# Should show: verification_status = VERIFIED or REJECTED
```

---

## 🔧 Troubleshooting

### Port Already in Use
```bash
# Find process on port
lsof -i :3000     # Change port number as needed
lsof -i :5001
lsof -i :8080
lsof -i :3306

# Kill process (macOS/Linux)
kill -9 <PID>

# Kill process (Windows)
netstat -ano | findstr :PORT
taskkill /PID <PID> /F
```

### GOOGLE_API_KEY Not Set
```bash
# Check .env file
cat veri-skill-ai/.env

# Should contain:
GOOGLE_API_KEY=your_actual_key_here

# If missing, add it:
echo "GOOGLE_API_KEY=your_key" >> veri-skill-ai/.env
```

### MySQL Connection Failed
```bash
# Check if Docker is running
docker ps

# Check MySQL logs
docker logs veriskill-mysql

# Connect directly
mysql -h 127.0.0.1 -u root -proot

# Test query
mysql -h 127.0.0.1 -u root -proot -e "SELECT 1;"
```

### Maven: "release version 17 not supported"
This means Maven is using an older JDK in the current shell. The backend requires Java 17+.

```bash
java -version
mvn -version
echo "$JAVA_HOME"
```

For WSL/Ubuntu:

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk
sudo update-alternatives --config java
sudo update-alternatives --config javac
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
mvn clean install
```

### ZK Circuit Tests Failing
```bash
# Clear node_modules and reinstall
rm -rf node_modules
yarn install

# Re-run tests
yarn test:local

# If still failing, check Node version
node --version  # Should be 22+
```

---

## 📚 Documentation

For detailed information, see:
- 📋 **IMPLEMENTATION_CHECKLIST.md** - Full status and roadmap
- 📖 **SYSTEM_ARCHITECTURE.md** - Technical design details
- 🎨 **PHASE3_FRONTEND_GUIDE.md** - Frontend implementation templates
- 📝 **README_SUMMARY.md** - Project overview

---

## 🎯 Next Steps

### If All Services ✅ Running:

**Option 1: Test End-to-End**
```bash
# Run complete workflow test script
./scripts/test-workflow.sh
```

**Option 2: Start Frontend Development**
```bash
cd veri-skill-frontend
npm start
# Build candidate upload form & recruiter dashboard
```

**Option 3: Integrate with Database**
```bash
# Run migrations (if any)
# Create test data
# Verify database connectivity
```

---

## 🚨 Common Issues Solved

### Issue: "Address already in use"
**Solution:** Kill process on that port and restart service

### Issue: "CORS error from frontend"
**Solution:** Check Spring Boot has `@CrossOrigin` annotation on controller

### Issue: "Resume file not uploading"
**Solution:** 
1. Check file size < 10MB
2. Verify PDF is valid
3. Check API URL in `.env.local`

### Issue: "Gemini API returns 404"
**Solution:** Update model name in `ai_scorer.py` to `gemini-2.5-flash`

### Issue: "ZK tests fail in WSL"
**Solution:** Reinstall `node_modules` to fix native bindings

---

## 🎓 Learning Path

1. **Start here:** `QUICK_START.md` (this file)
2. **Understand design:** `SYSTEM_ARCHITECTURE.md`
3. **Build frontend:** `PHASE3_FRONTEND_GUIDE.md`
4. **Deploy:** `IMPLEMENTATION_CHECKLIST.md`

---

## 📊 Project Status

```
Phase 1: ZK Circuit          ✅ COMPLETE
Phase 2: Backend             ✅ COMPLETE
Phase 3: Frontend            🔄 IN PROGRESS
Phase 4: Testnet Deployment  ⏳ PENDING
```

**Overall:** 50% Complete → Ready for Phase 3 (Frontend Development)

---

## 💡 Tips & Tricks

### Auto-reload services during development
```bash
# Flask: Already has auto-reload in debug mode
# React: Built-in hot reload with npm start
# Spring Boot: Install spring-boot-devtools
# Node: Use nodemon: npm install -D nodemon
```

### View API documentation
```bash
# Spring Boot Swagger UI
open http://localhost:8080/swagger-ui.html
```

### Monitor services
```bash
# Watch logs from all services
tmux new-session -d -s veri-skill
tmux send-keys -t veri-skill "cd veri-skill-ai && python3 app.py" Enter
tmux new-window -t veri-skill
tmux send-keys -t veri-skill "cd veri-skill-backend && mvn spring-boot:run" Enter
# etc.
```

### Generate test data
```bash
# Flask endpoint automatically scores resume
# Spring Boot automatically creates database records
# ZK Bridge automatically generates proofs
# All systems work together!
```

---

## 🔐 Security Notes

⚠️ **IMPORTANT: Before Production**
- [ ] Remove debug mode from Flask
- [ ] Add authentication to all APIs
- [ ] Use HTTPS instead of HTTP
- [ ] Secure database credentials
- [ ] Enable CORS properly (not *)
- [ ] Add rate limiting
- [ ] Enable logging/monitoring
- [ ] Regular security audits

---

## 📞 Need Help?

1. Check **Troubleshooting** section above
2. Search in **SYSTEM_ARCHITECTURE.md** for detailed technical info
3. Check individual service READMEs:
   - `veri-skill-backend/README.md`
   - `veri-skill-zk-bridge/README.md`
   - `veri-skill-ai/README.md`

---

## ✨ What's Next?

Once all services are running:
1. ✅ Verify the complete workflow works
2. 🚀 Start Phase 3: Build React Frontend
3. 🧪 Run integration tests
4. 📊 Performance testing
5. 🔐 Security audit
6. 🚢 Deploy to production

---

**Ready to build? Start with Step 1 above!**

Last updated: 2026-07-18  
Status: Active Development 🟢
