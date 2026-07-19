# Veri-Skill: Zero-Knowledge Resume Verification System

## 📋 Project Overview

**Veri-Skill** is a cutting-edge resume screening system that leverages:
- 🧠 **AI/ML** for intelligent resume scoring
- 🔐 **Zero-Knowledge Proofs (ZK)** for privacy-preserving verification
- ⛓️ **Blockchain** (Midnight) for immutable proof storage

The system allows recruiters to verify candidate qualifications without ever seeing the actual AI scores or resume details.

## 🎯 System Goals

✅ **Privacy:** Recruiters only see "Verified" or "Not Verified" — never the actual score
✅ **Integrity:** ZK proofs guarantee the verification is authentic
✅ **Fairness:** Consistent, algorithm-driven evaluation
✅ **Transparency:** All verification logic is auditable

---

## 🏗️ Project Structure

```
veri-skill-project/
├── src/                          # Midnight ZK Contract (TypeScript)
│   ├── index.ts                  # Main contract logic
│   └── test/veriskill.test.ts    # Circuit tests (✅ PASSING)
│
├── contracts/                    # Contract configurations
│
├── veri-skill-ai/               # 🧠 AI/ML Microservice (Python/Flask)
│   ├── app.py                   # Flask server (port 5001)
│   ├── ai_scorer.py             # LLM scoring engine (Gemini)
│   ├── utils.py                 # PDF text extraction
│   ├── .env                     # GOOGLE_API_KEY config
│   └── venv/                    # Python virtualenv
│
├── veri-skill-backend/          # 🔌 REST API (Spring Boot/Java)
│   ├── pom.xml                  # Maven configuration
│   ├── src/main/java/           # Java source code
│   │   └── com/veriskill/backend/
│   │       ├── model/           # Job, Candidate JPA entities
│   │       ├── repository/      # Spring Data JPA repos
│   │       ├── controller/      # REST endpoints
│   │       ├── service/         # Business logic
│   │       ├── dto/             # Request/response DTOs
│   │       └── config/          # Spring config
│   ├── src/main/resources/
│   │   └── application.yml      # Database & service URLs
│   └── README.md                # Backend API documentation
│
├── veri-skill-zk-bridge/        # 🔐 ZK Proof Service (Node.js/TypeScript)
│   ├── package.json             # NPM dependencies
│   ├── tsconfig.json            # TypeScript config
│   ├── src/
│   │   ├── index.ts             # Express server (port 3000)
│   │   ├── services/            # ZK proof generation
│   │   ├── routes/              # /prove, /verify endpoints
│   │   └── types/               # TypeScript interfaces
│   └── README.md                # ZK Bridge documentation
│
├── veri-skill-frontend/         # 🎨 UI (React/TypeScript) - TO CREATE
│   ├── src/
│   │   ├── components/          # React components
│   │   ├── pages/               # Page containers
│   │   ├── services/            # API client
│   │   └── App.tsx              # Main app
│   └── README.md
│
├── package.json                 # Root npm config
├── tsconfig.json               # Root TypeScript config
├── vitest.config.ts            # Test configuration
├── compose.yml                 # Docker Compose (services)
├── SYSTEM_ARCHITECTURE.md      # Complete system design
└── README.md                   # This file
```

---

## 📊 Architecture Overview

```
┌─────────────────────────────────┐
│  Frontend (React)               │  Port 3000/5173
│  • Candidate upload form        │
│  • Recruiter dashboard          │
└──────────────┬──────────────────┘
               │
    ┌──────────▼──────────┐
    │ Spring Boot API     │  Port 8080
    │ • POST /jobs        │
    │ • POST /candidates  │
    │ • GET /verification │
    └────┬────┬────┬──────┘
         │    │    │
    ┌────▼┐  │    │
    │MySQL│  │    │
    │ DB  │  │    │
    └─────┘  │    │
         ┌───▼────▼──────────┐      ┌──────────────────┐
         │ Flask AI Service  │      │ Node.js ZK Bridge│
         │ /score endpoint   │      │ /prove /verify   │
         │ • PDF extraction  │      │ • ZK proofs      │
         │ • Gemini scoring  │      │ • Proof gen/ver  │
         └───────────────────┘      └────────┬─────────┘
                                             │
                                    ┌────────▼─────────┐
                                    │ Midnight Network │
                                    │ • Circuit logic  │
                                    │ • ZK proofs      │
                                    └──────────────────┘
```

---

## ✅ Completion Status

| Task | Component | Status | Details |
|------|-----------|--------|---------|
| 1️⃣ ZK Circuit | Midnight contract | ✅ DONE | `yarn test:local` passes |
| 2️⃣ AI Engine | Flask AI service | ✅ DONE | Running on port 5001 |
| 3️⃣ Backend API | Spring Boot | ✅ DONE | Fully implemented REST API |
| 3️⃣ ZK Bridge | Node.js wrapper | ✅ DONE | Proof generation service |
| 4️⃣ Frontend | React UI | 🔄 IN PROGRESS | To be created |
| 5️⃣ Deployment | Preview Testnet | ⏳ PENDING | After testing complete |

---

## 🚀 Quick Start

### Prerequisites
- Node.js 22+
- Python 3.12+
- Java 17+
- MySQL 8.0+
- Docker (optional, for MySQL)

### Local Setup

#### 1. Start MySQL (Docker)
```bash
docker run -d \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=veriskill \
  -p 3306:3306 \
  mysql:8.0
```

#### 2. Flask AI Service
```bash
cd veri-skill-ai
python3 -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
python3 app.py
# 🟢 Running on http://127.0.0.1:5001
```

#### 3. Node.js ZK Bridge
```bash
cd veri-skill-zk-bridge
npm install
npm run dev
# 🟢 Running on http://127.0.0.1:3000
```

#### 4. Spring Boot Backend
```bash
cd veri-skill-backend
mvn clean install
mvn spring-boot:run
# 🟢 Running on http://127.0.0.1:8080
# 📚 Swagger UI: http://127.0.0.1:8080/swagger-ui.html
```

#### 5. Verify ZK Circuit
```bash
yarn test:local
# ✓ Deploys the Veri-Skill contract
# ✓ Verifies candidate qualification
```

---

## 💡 Example Workflow

### Step 1: Recruiter Creates Job
```bash
curl -X POST http://localhost:8080/api/v1/jobs \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Senior Java Developer",
    "description": "Need 10+ years Java/Spring Boot experience",
    "recruiterId": "rec-001",
    "required_threshold": 75
  }'
```

**Response:**
```json
{
  "id": "job-abc123",
  "title": "Senior Java Developer",
  "required_threshold": 75,
  "status": "OPEN"
}
```

### Step 2: Candidate Uploads Resume
```bash
curl -X POST http://localhost:8080/api/v1/candidates/upload \
  -F "jobId=job-abc123" \
  -F "email=john@example.com" \
  -F "fullName=John Doe" \
  -F "resume=@john_resume.pdf"
```

**Behind the scenes:**
1. ✅ Extract text from PDF
2. ✅ Call Flask AI: Score resume (gets 82/100)
3. ✅ Call ZK Bridge: Generate proof (82 >= 75)
4. ✅ Store in database

**Response:**
```json
{
  "id": "candidate-xyz789",
  "email": "john@example.com",
  "verification_status": "VERIFIED",
  "proof_verified": true,
  "ai_score": 82
}
```

### Step 3: Recruiter Views Verification
```bash
curl http://localhost:8080/api/v1/candidates/candidate-xyz789/verification \
  ?jobId=job-abc123
```

**Response (Recruiter sees only):**
```json
{
  "verification_status": "VERIFIED",
  "proof_verified": true,
  "message": "Candidate verified: meets job requirements"
}
```

**⚠️ Recruiter DOES NOT see:**
- ❌ Actual AI score (82)
- ❌ Resume content
- ❌ Private verification details

---

## 🔒 Privacy & Security

### How Zero-Knowledge Proofs Work

1. **AI scores resume:** 82/100
2. **Recruiter requires:** 75/100
3. **ZK proof proves:** `82 >= 75` ✓
4. **Proof does NOT contain:** The actual score 82
5. **Recruiter sees:** ✓ Verified (but doesn't know score)

**Privacy Win:** ✅ Qualified candidates confirmed WITHOUT exposing scores

### Data Security
- ✅ Resume text extracted to text (no files stored)
- ✅ AI scores stored internally only
- ✅ ZK proofs are public but don't reveal scores
- ✅ Database credentials in `.env` files (not in code)
- ✅ API doesn't leak internal state

---

## 🧪 Testing

### Unit Tests
```bash
# ZK Circuit
yarn test:local

# Backend
cd veri-skill-backend
mvn test

# ZK Bridge
cd veri-skill-zk-bridge
npm test
```

### Integration Tests
```bash
# Test complete workflow
./scripts/test-workflow.sh
```

### Load Testing
```bash
# Benchmark resume scoring
wrk -t4 -c100 -d30s http://localhost:8080/api/v1/candidates/upload
```

---

## 📈 Performance Targets

| Operation | Target Time | Status |
|-----------|------------|--------|
| Resume PDF extraction | < 500ms | ✅ |
| AI scoring (Gemini) | 1-3 sec | ✅ |
| ZK proof generation | < 1 sec | ✅ |
| Proof verification | < 500ms | ✅ |
| **Total workflow** | 2-5 sec | ✅ |

---

## 🚢 Deployment

### Local Testing (✅ Current)
All services running on localhost with test data.

### Preview Testnet (📋 Next)
```bash
# Update environment
export MIDNIGHT_NETWORK=preview

# Re-run tests against Midnight Preview
yarn test:preview

# Update backend to use preview network endpoints
# See .env.preview.example
```

### Mainnet (🔮 Future)
```bash
export MIDNIGHT_NETWORK=mainnet
# Deploy to production Midnight network
```

---

## 📚 Documentation

- **[SYSTEM_ARCHITECTURE.md](./SYSTEM_ARCHITECTURE.md)** - Complete technical design
- **[veri-skill-ai/README.md](./veri-skill-ai/README.md)** - Flask AI service guide
- **[veri-skill-backend/README.md](./veri-skill-backend/README.md)** - Backend API documentation
- **[veri-skill-zk-bridge/README.md](./veri-skill-zk-bridge/README.md)** - ZK Bridge service guide
- **[CONTRIBUTING.md](./CONTRIBUTING.md)** - Development guidelines

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

See [CONTRIBUTING.md](./CONTRIBUTING.md) for details.

---

## ⚠️ Known Issues & Limitations

| Issue | Status | Workaround |
|-------|--------|-----------|
| Resume size limit 10MB | ✅ | Reject oversized files |
| Gemini API rate limits | ⚠️ | Implement rate limiting |
| ZK proof size ~2KB | ✅ | Store in LONGBLOB |
| MySQL on localhost only | 🔄 | Config in application.yml |
| No authentication (v0.1) | 🔄 | Add JWT in v0.2 |

---

## 🗺️ Roadmap

### v0.1.0 (Current)
- ✅ ZK circuit
- ✅ AI scoring service
- ✅ Backend REST API
- ✅ ZK proof bridge
- 🔄 React frontend
- ⏳ Preview testnet

### v0.2.0 (Next)
- JWT authentication
- Email notifications
- Batch processing
- Advanced analytics
- Mobile app

### v0.3.0 (Future)
- Mainnet deployment
- Multiple proof types
- Proof expiration
- Recruiter ratings
- Candidate matching

---

## 🆘 Troubleshooting

### "Port already in use"
```bash
# Find process using port 5001
lsof -i :5001

# Kill process (macOS/Linux)
kill -9 <PID>

# Kill process (Windows)
netstat -ano | findstr :5001
taskkill /PID <PID> /F
```

### "GOOGLE_API_KEY not set"
```bash
# Check .env file in veri-skill-ai/
cat veri-skill-ai/.env

# Should contain:
# GOOGLE_API_KEY=your_key_here

# Or set environment variable
export GOOGLE_API_KEY=your_key_here
```

### "MySQL connection refused"
```bash
# Check if MySQL is running
docker ps | grep mysql

# Start MySQL
docker run -d \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=veriskill \
  -p 3306:3306 \
  mysql:8.0

# Test connection
mysql -h 127.0.0.1 -u root -proot -e "SELECT 1;"
```

---

## 📞 Support

- **Issues:** GitHub Issues
- **Discussions:** GitHub Discussions
- **Email:** team@veriskill.dev

---

## 📄 License

This project is licensed under the MIT License - see [LICENSE](./LICENSE) file for details.

---

## 👥 Team

**Veri-Skill Development Team**
- Project Lead: [Your Name]
- AI/ML Engineer: [Name]
- Backend Engineer: [Name]
- Frontend Engineer: [Name]
- ZK Specialist: [Name]

---

## 🎓 Learning Resources

- [Midnight Documentation](https://docs.midnightnetwork.io)
- [Zero-Knowledge Proofs Explained](https://en.wikipedia.org/wiki/Zero-knowledge_proof)
- [Spring Boot Guide](https://spring.io/guides)
- [React Documentation](https://react.dev)
- [LangChain Docs](https://python.langchain.com)

---

## 🔄 Changelog

### v0.1.0 - 2026-07-18
- Initial project setup
- Midnight ZK circuit implementation
- Flask AI scoring service
- Spring Boot REST backend
- Node.js ZK proof bridge
- Complete system documentation

---

**Last Updated:** 2026-07-18  
**Version:** 0.1.0  
**Status:** 🟡 Development (Backend Complete, Frontend In Progress)

---

For detailed technical information, see [SYSTEM_ARCHITECTURE.md](./SYSTEM_ARCHITECTURE.md)
