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
public class CandidateSummary {
    private String id;
    private String fullName;
    private String email;

    @JsonProperty("verification_status")
    private String verificationStatus;

    @JsonProperty("proof_verified")
    private Boolean proofVerified;
}
