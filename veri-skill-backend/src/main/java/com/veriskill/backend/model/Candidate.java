package com.veriskill.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @Column(name = "resume_path")
    private String resumePath;

    @Column(name = "ai_score")
    private Integer aiScore;

    @Column(columnDefinition = "TEXT", name = "resume_text")
    private String resumeText;

    @Column(name = "zk_proof", columnDefinition = "LONGTEXT")
    @Lob
    private String zkProof;

    @Column(name = "proof_verified")
    private Boolean proofVerified;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        verificationStatus = VerificationStatus.PENDING;
        proofVerified = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum VerificationStatus {
        PENDING, PROCESSING, VERIFIED, REJECTED
    }
}
