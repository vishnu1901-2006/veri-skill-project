# Veri-Skill ZK Bridge Service

HTTP wrapper around the Midnight SDK for generating and verifying zero-knowledge proofs.

## Architecture

This service acts as a bridge between the Spring Boot backend and the Midnight SDK. It provides REST endpoints for:

- **Proof Generation** (`POST /prove`): Generate a ZK proof that `score >= threshold` without revealing the actual score
- **Proof Verification** (`POST /verify`): Verify that a ZK proof is valid

## Getting Started

### Installation

```bash
cd veri-skill-zk-bridge
npm install
```

### Running the Service

**Development mode:**
```bash
npm run dev
```

**Production mode:**
```bash
npm run build
npm start
```

The service will start on `http://127.0.0.1:3000` by default.

## API Endpoints

### 1. Generate Proof
**POST** `/prove`

Generates a zero-knowledge proof that the candidate's score meets the job requirement threshold.

**Request:**
```json
{
  "candidate_id": "abc-123",
  "private_score": 85,
  "required_threshold": 70
}
```

**Response:**
```json
{
  "candidate_id": "abc-123",
  "proof": "eyJwcm9vZkRhdGEiOiAi...",
  "timestamp": 1721347200000
}
```

### 2. Verify Proof
**POST** `/verify`

Verifies that a ZK proof is valid.

**Request:**
```json
{
  "proof": "eyJwcm9vZkRhdGEiOiAi...",
  "private_score": 85,
  "required_threshold": 70
}
```

**Response:**
```json
{
  "valid": true,
  "message": "Proof is valid: candidate meets requirements"
}
```

### 3. Health Check
**GET** `/health`

Returns service health status.

**Response:**
```json
{
  "status": "ok",
  "service": "veri-skill-zk-bridge",
  "timestamp": "2026-07-18T20:34:31.273Z"
}
```

## Environment Variables

- `PORT`: Server port (default: 3000)
- `MIDNIGHT_PROVE_URL`: Midnight network RPC URL for proof generation (default: http://127.0.0.1:9944)
- `MIDNIGHT_VERIFY_URL`: Midnight network RPC URL for proof verification (default: http://127.0.0.1:9944)

## How Zero-Knowledge Proofs Work

In this system:

1. **Candidate submits resume** → AI scores it (e.g., 85/100)
2. **Backend calls `/prove`** with:
   - `private_score: 85` (kept private, never revealed)
   - `required_threshold: 70` (public)
3. **ZK service generates proof** that proves: `85 >= 70` ✓
4. **Proof is stored** on the blockchain or backend database
5. **Recruiter verifies** by calling `/verify` endpoint
   - The recruiter sees: "Candidate verified" ✓
   - The recruiter does NOT see: The actual score (85)

This provides **privacy** while ensuring **integrity**.

## Integration with Spring Boot Backend

The Spring Boot backend at `http://127.0.0.1:8080` calls this service via HTTP:

```java
// In Spring Boot
ZKProofService zkService = new ZKProofService("http://127.0.0.1:3000");
String proof = zkService.generateProof(candidateId, score, threshold);
boolean isValid = zkService.verifyProof(proof, score, threshold);
```

## Future Enhancements

- [ ] Integration with actual Midnight SDK's `prove()` and `verify()` functions
- [ ] On-chain proof submission and verification
- [ ] Support for multiple proof types (range proofs, membership proofs, etc.)
- [ ] Proof caching and optimization
- [ ] Rate limiting and authentication

## Testing

```bash
npm test
```

## License

MIT
