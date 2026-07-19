package com.veriskill.backend.service;

import com.veriskill.backend.model.Candidate;
import com.veriskill.backend.model.Job;
import com.veriskill.backend.repository.CandidateRepository;
import com.veriskill.backend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeVerificationService {

    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;
    private final FlaskAIService flaskAIService;
    private final ZKProofService zkProofService;

    /**
     * Process a candidate's resume submission
     * 1. Score resume using AI
     * 2. Generate ZK proof if score meets threshold
     * 3. Store verification result
     */
    @Transactional
    public void processCandidate(String jobId, String candidateId) {
        log.info("Processing candidate {} for job {}", candidateId, jobId);

        Optional<Candidate> candidateOpt = candidateRepository.findByIdAndJobId(candidateId, jobId);
        if (candidateOpt.isEmpty()) {
            log.error("Candidate {} not found for job {}", candidateId, jobId);
            return;
        }

        Candidate candidate = candidateOpt.get();
        Job job = candidate.getJob();

        // Mark as processing
        candidate.setVerificationStatus(Candidate.VerificationStatus.PROCESSING);
        candidateRepository.save(candidate);

        try {
            // Step 1: Get AI score
            if (candidate.getResumeText() == null || candidate.getResumeText().isEmpty()) {
                log.error("Resume text missing for candidate {}", candidateId);
                candidate.setVerificationStatus(Candidate.VerificationStatus.REJECTED);
                candidateRepository.save(candidate);
                return;
            }

            Integer aiScore = flaskAIService.scoreResume(job.getDescription(), candidate.getResumeText());
            candidate.setAiScore(aiScore);
            log.info("AI score for candidate {}: {}", candidateId, aiScore);

            // Step 2: Generate ZK proof
            String zkProof = zkProofService.generateProof(candidateId, aiScore, job.getRequiredThreshold());

            if (zkProof == null) {
                log.error("Failed to generate ZK proof for candidate {}", candidateId);
                candidate.setVerificationStatus(Candidate.VerificationStatus.REJECTED);
                candidateRepository.save(candidate);
                return;
            }

            candidate.setZkProof(zkProof);

            // Step 3: Verify proof
            boolean isVerified = zkProofService.verifyProof(zkProof, aiScore, job.getRequiredThreshold());
            candidate.setProofVerified(isVerified);

            if (isVerified) {
                candidate.setVerificationStatus(Candidate.VerificationStatus.VERIFIED);
            } else {
                candidate.setVerificationStatus(Candidate.VerificationStatus.REJECTED);
            }

            candidateRepository.save(candidate);
            log.info("Candidate {} verification complete. Status: {}", candidateId, candidate.getVerificationStatus());

        } catch (Exception e) {
            log.error("Error processing candidate {}", candidateId, e);
            candidate.setVerificationStatus(Candidate.VerificationStatus.REJECTED);
            candidateRepository.save(candidate);
        }
    }

    /**
     * Get all candidates for a job with their verification status
     */
    public List<Candidate> getCandidatesForJob(String jobId) {
        return candidateRepository.findByJobId(jobId);
    }

    /**
     * Get verified candidates for a job (anonymized view for recruiter)
     */
    public List<Candidate> getVerifiedCandidates(String jobId) {
        return candidateRepository.findByJobIdAndVerificationStatus(
                jobId,
                Candidate.VerificationStatus.VERIFIED
        );
    }

    /**
     * Get candidate details with verification status
     */
    public Optional<Candidate> getCandidate(String candidateId, String jobId) {
        return candidateRepository.findByIdAndJobId(candidateId, jobId);
    }
}
