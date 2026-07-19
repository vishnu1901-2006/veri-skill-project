export interface ProveRequest {
  candidate_id: string;
  private_score: number;
  required_threshold: number;
}

export interface ProveResponse {
  candidate_id: string;
  proof: string;
  timestamp: number;
}

export interface VerifyRequest {
  proof: string;
  private_score: number;
  required_threshold: number;
}

export interface VerifyResponse {
  valid: boolean;
  message: string;
}

export interface ProofGenerationResult {
  success: boolean;
  proof?: string;
  error?: string;
}
