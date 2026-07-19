# Veri-Skill: Zero-Knowledge Resume Verification System
## Complete System Architecture & Implementation Guide

### Project Status

✅ **Task 1: ZK Circuit & Local Network** - COMPLETE
- Midnight contract deployed and tested locally (`yarn test:local` passes)
- Circuit validates: `private_score >= required_threshold`
- Outputs: Boolean flag (Verified / Not Verified)

✅ **Task 2: AI/ML Evaluation Engine** - COMPLETE  
- Flask microservice (`veri-skill-ai/`) running on port 5001
- Resume PDF text extraction via PyPDF2
- LLM scoring via LangChain + Google Gemini (gemini-2.5-flash)
- Endpoint: `POST /score` → Returns 0-100 integer score

✅ **Task 3: Backend Orchestrator** - COMPLETE
- Spring Boot REST API (`veri-skill-backend/`) on port 8080
- MySQL database schema (Jobs, Candidates, Verification records)
- Job management endpoints (CREATE, READ, LIST)
- Resume upload & extraction endpoints
- Service integration layer:
  - Calls Flask AI for scoring
  - Calls Node.js ZK Bridge for proof generation
- Node.js ZK Bridge (`veri-skill-zk-bridge/`) on port 3000
  - Wraps Midnight SDK's prove() function
  - Generates ZK proofs for `score >= threshold`
  - Verification endpoints

🔄 **Task 4: Frontend Interface** - IN PROGRESS
- Candidate upload form
- Recruiter dashboard with verification badges
- Tech: React/TypeScript with Tailwind CSS

📋 **Task 5: Network Deployment** - PENDING
- Swap `test:local` → `test:preview`
- Deploy to Midnight Preview Testnet

---

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    CANDIDATE & RECRUITER CLIENTS                │
│  (React/TypeScript SPA on port 3000 or 5173)                    │
└──────────────────────┬──────────────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
    ┌───▼────┐     ┌───▼────┐    ┌───▼────┐
    │Candidate│     │Recruiter│    │Recruiter│
    │Upload   │     │Dashboard│    │Analytics│
    │Form     │     │(View    │    │         │
    │         │     │Results) │    │         │
    └────┬────┘     └────┬────┘    └────┬────┘
         │              │              │
         └──────────────┼──────────────┘
                        │ (HTTPS)
         ┌──────────────▼──────────────┐
         │  Spring Boot Backend        │
         │  http://127.0.0.1:8080      │
         │  (REST API - Port 8080)     │
         │  ├─ POST /jobs              │
         │  ├─ POST /candidates/upload │
         │  ├─ GET /candidates/{id}    │
         │  └─ GET /jobs/{id}          │
         │                              │
         │ [ResumeVerificationService] │
         └────────┬─────┬──────┬───────┘
                  │     │      │
    ┌─────────────▼─┐   │      │
    │  MySQL DB     │   │      │
    │  • Jobs       │   │      │
    │  • Candidates │   │      │
    │  • Proofs     │   │      │
    └───────────────┘   │      │
                        │      │
         ┌──────────────▼──┐   │
         │ Flask AI Service│   │
         │ :5001/score     │   │
         │                 │   │
         │ [Gemini LLM]    │   │
         │ [PDFBox extract]│   │
         │ [LangChain]     │   │
         └─────────────────┘   │
                               │
         ┌─────────────────────▼───┐
         │  Node.js ZK Bridge      │
         │  http://127.0.0.1:3000  │
         │  ├─ POST /prove         │
         │  └─ POST /verify        │
         │                         │
         │ [Midnight SDK Wrapper]  │
         │ [ZK Proof Generation]   │
         └────────────┬────────────┘
                      │
         ┌────────────▼───────────┐
         │  Midnight Local Network│
         │  (TypeScript/WASM)     │
         │                        │
         │ • prove() - Generate   │
         │   ZK proof that score  │
         │   >= threshold         │
         │                        │
         │ • verify() - Verify    │
         │   proof on-chain       │
         └────────────────────────┘

```

---

## Component Details

### 1. Frontend (React/TypeScript)
**Location:** `veri-skill-frontend/` (TO BE CREATED)
**Port:** 3000 or 5173 (dev)

**Features:**
- **Candidate View:**
  - Upload resume (PDF/DOCX)
  - Select job to apply for
  - View submission status
  - Receive notification when verified

- **Recruiter View:**
  - Create job postings with threshold
  - View applicant list
  - See verification badges (✓ Verified / ✗ Not Verified)
  - Download applicant metadata (no resume content)
  - Analytics dashboard

**Tech Stack:**
- React 18
- TypeScript
- Tailwind CSS
- React Query (data fetching)
- Axios (HTTP client)
- React Router

### 2. Backend API (Spring Boot)
**Location:** `veri-skill-backend/`
**Port:** 8080
**Database:** MySQL (localhost:3306)

**Services:**
- `JobController` - Job CRUD operations
- `CandidateController` - Resume upload & verification
- `ResumeVerificationService` - Orchestrates AI + ZK workflow
- `FlaskAIService` - HTTP client to Flask
- `ZKProofService` - HTTP client to Node.js ZK Bridge

**Database Schema:**
```sql
Jobs Table:
  - id (UUID)
  - title, description
  - recruiterId
  - requiredThreshold (0-100)
  - status (OPEN/CLOSED/ARCHIVED)
  - createdAt, updatedAt

Candidates Table:
  - id (UUID)
  - jobId (FK to Jobs)
  - email, fullName
  - resumePath, resumeText
  - aiScore (0-100)
  - zkProof (base64 encoded)
  - proofVerified (boolean)
  - verificationStatus (PENDING/PROCESSING/VERIFIED/REJECTED)
  - createdAt, updatedAt
```

### 3. AI/ML Microservice (Flask + Python)
**Location:** `veri-skill-ai/`
**Port:** 5001
**Technology:** Python 3.12 + Flask + LangChain

**Endpoint:**
```
POST /score
  Input: { job_description, resume_text }
  Output: { score: 0-100 }
```

**Flow:**
1. Receives job description & extracted resume text
2. Calls Google Gemini via LangChain
3. Prompt engineering ensures JSON response: `{"score": 85}`
4. Returns integer score

**Models Available:**
- `gemini-2.5-flash` (current, recommended)
- `gemini-2.0-flash`
- Custom fallback to latest available

### 4. ZK Proof Service (Node.js + Midnight SDK)
**Location:** `veri-skill-zk-bridge/`
**Port:** 3000
**Technology:** Node.js + TypeScript + Express

**Endpoints:**
```
POST /prove
  Input: { candidate_id, private_score, required_threshold }
  Output: { proof: "base64..." }
  
POST /verify
  Input: { proof, private_score, required_threshold }
  Output: { valid: boolean }
```

**How It Works:**
1. Backend sends `private_score` (e.g., 85) + `required_threshold` (e.g., 70)
2. ZK Bridge generates proof proving: `85 >= 70` WITHOUT revealing 85
3. Proof is returned as base64 string
4. Proof is stored in database
5. When recruiter verifies, only the boolean result is shown

### 5. Midnight Contract & ZK Circuit
**Location:** `src/` and `contracts/`
**Technology:** TypeScript + WASM

**Circuit Logic:**
```typescript
// Pseudocode
if (privateScore >= requiredThreshold) {
  return VERIFIED = true;  // Publicly known
} else {
  return VERIFIED = false; // Publicly known
}
// privateScore is NEVER revealed
```

**Status:** ✅ Tested locally with `yarn test:local`

---

## Data Flow: Complete Workflow

```
1. RECRUITER CREATES JOB
   ┌─────────────────────────────────────┐
   │ Frontend: Create Job Form            │
   │ Inputs:                             │
   │  - Title: "Senior Java Dev"         │
   │  - Threshold: 75                    │
   └────────┬────────────────────────────┘
            │
            ▼ (POST /jobs)
   ┌─────────────────────────────────────┐
   │ Spring Boot: JobController::createJob│
   │ Saves to DB                         │
   │ Returns: jobId="abc-123"            │
   └─────────────────────────────────────┘

2. CANDIDATE UPLOADS RESUME
   ┌─────────────────────────────────────┐
   │ Frontend: Upload Resume Form         │
   │ Inputs:                             │
   │  - Name: "John Doe"                 │
   │  - Email: "john@example.com"        │
   │  - Resume: resume.pdf               │
   │  - jobId: "abc-123"                 │
   └────────┬────────────────────────────┘
            │
            ▼ (POST /candidates/upload)
   ┌─────────────────────────────────────┐
   │ Spring Boot: CandidateController    │
   │  1. Extract text from PDF           │
   │  2. Create Candidate record         │
   │  3. Call ResumeVerificationService  │
   └────────┬────────────────────────────┘
            │
         ┌──┴────────────────────────┐
         ▼                           ▼
   ┌──────────────────┐      ┌──────────────────┐
   │ Flask AI Service │      │ ZK Bridge        │
   │ score = 85       │      │ proof = generate │
   │                  │      │ (85 >= 75)       │
   └──────────────────┘      └──────────────────┘
         │                           │
         └──────────┬────────────────┘
                    │
                    ▼
   ┌─────────────────────────────────────┐
   │ Spring Boot: Save to DB             │
   │  - aiScore: 85                      │
   │  - zkProof: "proof_base64..."       │
   │  - verificationStatus: VERIFIED     │
   │  - proofVerified: true              │
   └─────────────────────────────────────┘

3. RECRUITER VIEWS RESULTS
   ┌─────────────────────────────────────┐
   │ Frontend: Dashboard                 │
   │ GET /jobs/abc-123/candidates        │
   │                                     │
   │ Displays:                           │
   │ ✓ John Doe - VERIFIED              │
   │ ✓ Jane Smith - VERIFIED            │
   │ ✗ Bob Jones - NOT VERIFIED         │
   │                                     │
   │ Recruiter DOES NOT see:            │
   │  - Actual AI scores (85, 90, 60)   │
   │  - Resume content                   │
   │  - Private score data               │
   └─────────────────────────────────────┘
```

---

## Security & Privacy Features

1. **Zero-Knowledge Proofs**
   - Proves `score >= threshold` without revealing actual score
   - Recruiter only sees: ✓ Verified or ✗ Not Verified

2. **Data Minimization**
   - Resume text extracted and stored (for AI processing)
   - Actual resume file NOT stored
   - Recruiter cannot access resume content

3. **Separation of Concerns**
   - AI service isolated in Python
   - ZK proof generation isolated in Node.js
   - Backend orchestrates both without exposing raw data

4. **No Score Leakage**
   - Backend stores aiScore internally
   - API returns only verification status
   - Proof is verifiable but doesn't contain the score

---

## Environment Setup

### Local Development Environment

#### 1. **MySQL Database**
```bash
# Start MySQL (Docker recommended)
docker run -d \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=veriskill \
  -p 3306:3306 \
  mysql:8.0
```

#### 2. **Flask AI Service**
```bash
cd veri-skill-ai
source venv/bin/activate
python3 app.py
# Runs on http://127.0.0.1:5001
```

#### 3. **Node.js ZK Bridge**
```bash
cd veri-skill-zk-bridge
npm install
npm run dev
# Runs on http://127.0.0.1:3000
```

#### 4. **Spring Boot Backend**
```bash
cd veri-skill-backend
mvn spring-boot:run
# Runs on http://127.0.0.1:8080
```

#### 5. **Frontend (React)**
```bash
cd veri-skill-frontend
npm install
npm run dev
# Runs on http://127.0.0.1:5173
```

#### 6. **Midnight Local Network** (Already running)
```bash
# From project root
yarn test:local  # Validates circuit
```

---

## API Quick Reference

### Job Endpoints
```
POST   /api/v1/jobs                      - Create job
GET    /api/v1/jobs/{jobId}              - Get job details
GET    /api/v1/jobs/recruiter/{recruiterId} - List recruiter's jobs
PUT    /api/v1/jobs/{jobId}/close        - Close job
```

### Candidate Endpoints
```
POST   /api/v1/candidates/upload         - Upload resume
GET    /api/v1/candidates/{candidateId}/verification - Check status
GET    /api/v1/candidates/{candidateId}  - Get candidate info
```

### ZK Proof Endpoints
```
POST   /prove                            - Generate proof
POST   /verify                           - Verify proof
GET    /health                           - Health check
```

---

## Next Steps: Frontend Implementation

### Task 4: Frontend (React/TypeScript)

**Directory Structure:**
```
veri-skill-frontend/
├── src/
│   ├── components/
│   │   ├── CandidateUpload.tsx
│   │   ├── RecruiterDashboard.tsx
│   │   ├── JobForm.tsx
│   │   ├── VerificationBadge.tsx
│   │   └── ...
│   ├── pages/
│   │   ├── CandidatePage.tsx
│   │   ├── RecruiterPage.tsx
│   │   └── ...
│   ├── services/
│   │   └── api.ts (Axios client)
│   ├── types/
│   │   └── index.ts (TypeScript interfaces)
│   └── App.tsx
├── package.json
├── tsconfig.json
└── README.md
```

**Key Features to Build:**
1. Candidate resume upload form
2. Recruiter job creation form
3. Results dashboard with verification badges
4. Real-time verification status updates
5. Error handling & loading states

**Dependencies:**
```json
{
  "react": "^18.2.0",
  "react-dom": "^18.2.0",
  "typescript": "^5.0.0",
  "axios": "^1.6.0",
  "react-query": "^3.39.3",
  "react-router-dom": "^6.0.0",
  "tailwindcss": "^3.0.0"
}
```

---

## Deployment & Testing

### Local Testing (All Services)
```bash
# Terminal 1: Flask
cd veri-skill-ai && source venv/bin/activate && python3 app.py

# Terminal 2: Node.js ZK Bridge
cd veri-skill-zk-bridge && npm run dev

# Terminal 3: Spring Boot
cd veri-skill-backend && mvn spring-boot:run

# Terminal 4: Frontend
cd veri-skill-frontend && npm run dev

# Terminal 5: Test the circuit
yarn test:local
```

### End-to-End Test Workflow
```bash
# 1. Create job
curl -X POST http://localhost:8080/api/v1/jobs \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Java Developer",
    "description": "Senior developer needed",
    "recruiterId": "rec-001",
    "required_threshold": 70
  }'

# Response includes jobId

# 2. Upload resume
curl -X POST http://localhost:8080/api/v1/candidates/upload \
  -F "jobId=<jobId>" \
  -F "email=candidate@example.com" \
  -F "fullName=John Doe" \
  -F "resume=@resume.pdf"

# 3. Check verification status
curl http://localhost:8080/api/v1/candidates/<candidateId>/verification?jobId=<jobId>

# Response shows: verified: true/false
```

---

## Security Checklist

- [ ] HTTPS enabled in production
- [ ] MySQL user with restricted permissions
- [ ] API authentication (JWT tokens)
- [ ] Rate limiting on upload endpoint
- [ ] Input validation on all endpoints
- [ ] CORS configured properly
- [ ] Sensitive data logging disabled
- [ ] Database backups automated
- [ ] Error messages don't leak system info

---

## Performance Considerations

| Component | Typical Time |
|-----------|-------------|
| Resume PDF extraction | 100-500ms |
| LLM scoring (Gemini) | 1-3 seconds |
| ZK proof generation | 500-1000ms |
| Proof verification | 200-500ms |
| **Total workflow** | **2-5 seconds** |

**Optimization Tips:**
- Cache scores for identical resumes
- Use async/background jobs for proof generation
- Add proof result caching (TTL: 1 hour)
- Batch process multiple candidates

---

## Testing Strategy

### Unit Tests
- DAO/Repository layer
- Service layer (business logic)
- Utility functions

### Integration Tests
- API endpoint tests
- Database transaction tests
- Service-to-service calls

### E2E Tests
- Complete workflow (upload → score → verify)
- Error scenarios (invalid PDF, low score, etc.)

### ZK Proof Tests
- Valid proof generation
- Invalid proof rejection
- Edge cases (score == threshold)

---

## Future Enhancements

1. **Async Processing**
   - Use Spring Batch for bulk resume processing
   - Message queue (RabbitMQ) for job submission

2. **Advanced Analytics**
   - Heatmaps of score distributions
   - Time-to-hire metrics
   - Candidate pool analysis

3. **Multi-language Support**
   - Resume translation before scoring
   - Multilingual Gemini model usage

4. **Proof Expiration**
   - Regenerate proofs periodically
   - Expiration timestamps in DB

5. **Blockchain Integration**
   - Store proofs on Midnight mainnet
   - Immutable verification records

6. **Mobile App**
   - React Native for iOS/Android
   - Push notifications for candidates

---

## Support & Troubleshooting

### Flask AI Service Issues
```
Error: Model not found
→ Check .env GOOGLE_API_KEY
→ Verify Gemini model is available (gemini-2.5-flash)
→ Run: python3 list_models.py
```

### ZK Proof Generation Fails
```
Error: Cannot connect to Midnight network
→ Check if veri-skill-zk-bridge is running on :3000
→ Check Spring Boot application.yml zk service URL
```

### Database Connection Issues
```
Error: Connection refused
→ Verify MySQL is running on localhost:3306
→ Check database credentials in application.yml
→ Ensure database 'veriskill' exists
```

### Resume Upload Fails
```
Error: Invalid PDF file
→ Ensure file is valid PDF (not corrupted)
→ Check file size < 10MB
→ Try uploading sample resume
```

---

## License

MIT - All components

---

**Last Updated:** 2026-07-18
**Project Maintainer:** Veri-Skill Team
