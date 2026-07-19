package com.veriskill.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationResponse {
    private String candidateId;
    private String jobId;

    @JsonProperty("verification_status")
    private String verificationStatus;

    @JsonProperty("proof_verified")
    private Boolean proofVerified;

    @JsonProperty("ai_score")
    private Integer aiScore;

    @JsonProperty("required_threshold")
    private Integer requiredThreshold;

    private String message;
}
