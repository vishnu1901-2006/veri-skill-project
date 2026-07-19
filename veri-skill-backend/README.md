# Veri-Skill Backend API

Spring Boot REST API for managing job postings, candidate submissions, and resume verification workflow.

## Architecture

```
┌─────────────────────────────────────────┐
│        Frontend (React/Vue)             │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│   Spring Boot Backend (REST API)        │
│   - Job Management                      │
│   - Candidate Submissions               │
│   - Resume Scoring & Verification       │
└─────────────────────────────────────────┘
                 │
     ┌───────────┼───────────┐
     ▼           ▼           ▼
┌─────────┐ ┌──────────┐ ┌────────────────┐
│ Flask   │ │ Database │ │ Node.js Bridge │
│ AI/ML   │ │ (MySQL)  │ │ (ZK Proofs)    │
│ Scorer  │ │          │ │ Midnight SDK   │
└─────────┘ └──────────┘ └────────────────┘
```

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Flask AI service running on `http://127.0.0.1:5001`
- Node.js ZK Bridge running on `http://127.0.0.1:3000`

### Installation

```bash
cd veri-skill-backend
mvn clean install
```

### Running the Service

```bash
mvn spring-boot:run
```

The API will start on `http://127.0.0.1:8080`

API documentation available at: `http://127.0.0.1:8080/swagger-ui.html`

## Database Setup

Create MySQL database:

```sql
CREATE DATABASE veriskill CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

The schema is auto-created via Hibernate on first run with `spring.jpa.hibernate.ddl-auto: update`.

## API Endpoints

### Jobs Management

#### 1. Create a Job Posting
**POST** `/api/v1/jobs`

Create a new job posting with a required qualification threshold.

**Request:**
```json
{
  "title": "Senior Java Developer",
  "description": "Looking for experienced Java developer with Spring Boot expertise",
  "recruiterId": "recruiter-001",
  "required_threshold": 75
}
```

**Response:** `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Senior Java Developer",
  "description": "Looking for experienced Java developer with Spring Boot expertise",
  "recruiterId": "recruiter-001",
  "required_threshold": 75,
  "status": "OPEN",
  "created_at": "2026-07-18T20:34:31.273Z",
  "updated_at": "2026-07-18T20:34:31.273Z",
  "candidates": []
}
```

#### 2. Get Job by ID
**GET** `/api/v1/jobs/{jobId}`

Retrieve a specific job and its associated candidates.

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Senior Java Developer",
  "description": "...",
  "recruiterId": "recruiter-001",
  "required_threshold": 75,
  "status": "OPEN",
  "created_at": "2026-07-18T20:34:31.273Z",
  "updated_at": "2026-07-18T20:34:31.273Z",
  "candidates": [
    {
      "id": "candidate-001",
      "fullName": "John Doe",
      "email": "john@example.com",
      "verification_status": "VERIFIED",
      "proof_verified": true
    }
  ]
}
```

#### 3. Get Recruiter's Jobs
**GET** `/api/v1/jobs/recruiter/{recruiterId}`

Retrieve all jobs posted by a recruiter.

**Response:** `200 OK`
```json
[
  {
    "id": "...",
    "title": "...",
    ...
  }
]
```

#### 4. Close a Job
**PUT** `/api/v1/jobs/{jobId}/close`

Mark a job as closed (no new submissions).

**Response:** `200 OK`

### Candidate Management

#### 1. Upload Resume
**POST** `/api/v1/candidates/upload`

Submit a resume for a specific job.

**Request:**
- `jobId` (query param): The job ID
- `email` (query param): Candidate email
- `fullName` (query param): Candidate full name
- `resume` (file upload): PDF resume file

**cURL Example:**
```bash
curl -X POST http://127.0.0.1:8080/api/v1/candidates/upload \
  -F "jobId=550e8400-e29b-41d4-a716-446655440000" \
  -F "email=john@example.com" \
  -F "fullName=John Doe" \
  -F "resume=@resume.pdf"
```

**Response:** `201 Created`
```json
{
  "id": "candidate-001",
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john@example.com",
  "fullName": "John Doe",
  "ai_score": 85,
  "verification_status": "VERIFIED",
  "proof_verified": true,
  "zk_proof": "eyJwcm9vZkRhdGEiOiAi...",
  "created_at": "2026-07-18T20:34:31.273Z"
}
```

#### 2. Get Verification Status
**GET** `/api/v1/candidates/{candidateId}/verification`

Check if a candidate's resume meets the job requirements.

**Query Parameters:**
- `jobId` (required): The job ID

**Response:** `200 OK`
```json
{
  "candidateId": "candidate-001",
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "verification_status": "VERIFIED",
  "proof_verified": true,
  "ai_score": 85,
  "required_threshold": 75,
  "message": "Candidate verified: AI score meets job requirements"
}
```

**Note:** The API does NOT return the actual AI score to the recruiter — only the verification status and the ZK proof.

#### 3. Get Candidate Details
**GET** `/api/v1/candidates/{candidateId}`

Retrieve candidate information (no resume content for privacy).

**Query Parameters:**
- `jobId` (required): The job ID

**Response:** `200 OK`
```json
{
  "id": "candidate-001",
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john@example.com",
  "fullName": "John Doe",
  "ai_score": 85,
  "verification_status": "VERIFIED",
  "proof_verified": true,
  "created_at": "2026-07-18T20:34:31.273Z"
}
```

## Data Model

### Job Entity
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Unique job identifier |
| title | String | Job title |
| description | Text | Full job description |
| recruiterId | String | Recruiter's unique ID |
| requiredThreshold | Integer | Minimum AI score (0-100) |
| status | Enum | OPEN, CLOSED, ARCHIVED |
| createdAt | DateTime | Job creation timestamp |
| updatedAt | DateTime | Last update timestamp |

### Candidate Entity
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Unique candidate identifier |
| jobId | FK | Reference to Job |
| email | String | Candidate email |
| fullName | String | Candidate full name |
| resumePath | String | Filename of uploaded resume |
| resumeText | Text | Extracted text from resume |
| aiScore | Integer | AI-generated score (0-100) |
| zkProof | Text | Zero-knowledge proof (base64) |
| proofVerified | Boolean | Whether proof is valid |
| verificationStatus | Enum | PENDING, PROCESSING, VERIFIED, REJECTED |
| createdAt | DateTime | Submission timestamp |
| updatedAt | DateTime | Last update timestamp |

## Workflow

1. **Recruiter creates job** → `POST /jobs`
2. **Candidate uploads resume** → `POST /candidates/upload`
3. **Backend extracts resume text** (PDF parsing)
4. **Backend calls Flask AI** → Score resume
5. **Backend calls ZK Bridge** → Generate proof
6. **Proof stored** → Database
7. **Recruiter views results** → `GET /candidates/{id}/verification`
   - Sees: ✓ Verified / ✗ Not Verified
   - Does NOT see: Actual AI score

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/veriskill
    username: root
    password: your_password

flask:
  ai:
    url: http://127.0.0.1:5001

zk:
  proof:
    service:
      url: http://127.0.0.1:3000

server:
  port: 8080
```

## Future Enhancements

- [ ] Authentication & authorization (JWT)
- [ ] Async job processing (Spring Batch)
- [ ] Candidate notifications via email
- [ ] Admin dashboard
- [ ] Batch resume processing
- [ ] Proof expiration & re-verification

## Testing

```bash
mvn test
```

## Troubleshooting

### Maven fails with `release version 17 not supported`

This backend targets Java 17 because it uses Spring Boot 3.x. If Maven prints:

```text
Fatal error compiling: error: release version 17 not supported
```

then Maven is running with an older JDK inside your shell, even if another terminal has a newer Java installed.

In WSL, check the active Java version:

```bash
java -version
mvn -version
echo "$JAVA_HOME"
```

Install JDK 17 if needed:

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk
```

Point WSL to Java 17:

```bash
sudo update-alternatives --config java
sudo update-alternatives --config javac
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
```

To make it persistent:

```bash
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

Then rerun:

```bash
mvn clean install
```

## License

MIT
