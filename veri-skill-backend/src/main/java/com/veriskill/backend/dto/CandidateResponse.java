package com.veriskill.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateResponse {
    private String id;
    private String jobId;
    private String email;
    private String fullName;

    @JsonProperty("ai_score")
    private Integer aiScore;

    @JsonProperty("verification_status")
    private String verificationStatus;

    @JsonProperty("proof_verified")
    private Boolean proofVerified;

    @JsonProperty("zk_proof")
    private String zkProof;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
