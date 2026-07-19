export type JobStatus = 'OPEN' | 'CLOSED' | 'ARCHIVED';
export type VerificationStatus = 'PENDING' | 'PROCESSING' | 'VERIFIED' | 'REJECTED';

export interface CandidateSummary {
  id: string;
  fullName: string;
  email: string;
  verification_status: VerificationStatus;
  proof_verified: boolean;
}

export interface Job {
  id: string;
  title: string;
  description: string;
  recruiterId: string;
  required_threshold: number;
  status: JobStatus;
  created_at: string;
  updated_at: string;
  candidates: CandidateSummary[];
}

export interface CreateJobRequest {
  title: string;
  description: string;
  recruiterId: string;
  required_threshold: number;
}

export interface CandidateUploadRequest {
  jobId: string;
  email: string;
  fullName: string;
  resume: File;
}

export interface CandidateResponse {
  id: string;
  jobId: string;
  email: string;
  fullName: string;
  ai_score: number | null;
  verification_status: VerificationStatus;
  proof_verified: boolean;
  zk_proof: string | null;
  created_at: string;
}

export interface VerificationResponse {
  candidateId: string;
  jobId: string;
  verification_status: VerificationStatus;
  proof_verified: boolean;
  ai_score?: number | null;
  required_threshold?: number;
  message: string;
}

export interface HealthResponse {
  status: 'UP' | 'DOWN' | string;
}
