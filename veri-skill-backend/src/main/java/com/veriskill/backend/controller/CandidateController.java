package com.veriskill.backend.controller;

import com.veriskill.backend.dto.CandidateResponse;
import com.veriskill.backend.dto.VerificationResponse;
import com.veriskill.backend.model.Candidate;
import com.veriskill.backend.model.Job;
import com.veriskill.backend.repository.CandidateRepository;
import com.veriskill.backend.repository.JobRepository;
import com.veriskill.backend.service.ResumeVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
@Slf4j
public class CandidateController {

    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;
    private final ResumeVerificationService verificationService;

    /**
     * Upload a resume for a specific job
     * Creates a candidate record and extracts text from PDF
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(
            @RequestParam String jobId,
            @RequestParam String email,
            @RequestParam String fullName,
            @RequestParam("resume") MultipartFile resumeFile) {

        log.info("Uploading resume for job: {} by candidate: {}", jobId, email);

        // Verify job exists
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Job not found"));
        }

        try {
            // Extract text from PDF
            String resumeText = extractTextFromPDF(resumeFile);

            // Create candidate record
            Candidate candidate = Candidate.builder()
                    .job(jobOpt.get())
                    .email(email)
                    .fullName(fullName)
                    .resumePath(resumeFile.getOriginalFilename())
                    .resumeText(resumeText)
                    .verificationStatus(Candidate.VerificationStatus.PENDING)
                    .proofVerified(false)
                    .build();

            Candidate savedCandidate = candidateRepository.save(candidate);
            log.info("Candidate created: {}", savedCandidate.getId());

            // Trigger async verification (in production, use async processing)
            verificationService.processCandidate(jobId, savedCandidate.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(mapToResponse(savedCandidate));

        } catch (IOException e) {
            log.error("Error processing PDF file", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid PDF file"));
        } catch (Exception e) {
            log.error("Error uploading resume", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error processing resume"));
        }
    }

    /**
     * Get candidate verification status
     */
    @GetMapping("/{candidateId}/verification")
    public ResponseEntity<?> getVerification(
            @PathVariable String candidateId,
            @RequestParam String jobId) {

        log.info("Fetching verification status for candidate: {}", candidateId);

        Optional<Candidate> candidateOpt = candidateRepository.findByIdAndJobId(candidateId, jobId);
        if (candidateOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Candidate candidate = candidateOpt.get();
        Job job = candidate.getJob();

        VerificationResponse response = VerificationResponse.builder()
                .candidateId(candidate.getId())
                .jobId(job.getId())
                .verificationStatus(candidate.getVerificationStatus().toString())
                .proofVerified(candidate.getProofVerified())
                .aiScore(candidate.getAiScore())
                .requiredThreshold(job.getRequiredThreshold())
                .message(candidate.getProofVerified() ?
                        "Candidate verified: AI score meets job requirements" :
                        "Candidate not verified: AI score does not meet requirements")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get candidate details (only resume path, no actual resume content for privacy)
     */
    @GetMapping("/{candidateId}")
    public ResponseEntity<?> getCandidate(
            @PathVariable String candidateId,
            @RequestParam String jobId) {

        log.info("Fetching candidate details: {}", candidateId);

        Optional<Candidate> candidateOpt = candidateRepository.findByIdAndJobId(candidateId, jobId);
        if (candidateOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(candidateOpt.get()));
    }

    /**
     * Extract text from PDF using PDFBox
     */
    private String extractTextFromPDF(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {

            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Map Candidate entity to CandidateResponse DTO
     */
    private CandidateResponse mapToResponse(Candidate candidate) {
        return CandidateResponse.builder()
                .id(candidate.getId())
                .jobId(candidate.getJob().getId())
                .email(candidate.getEmail())
                .fullName(candidate.getFullName())
                .aiScore(candidate.getAiScore())
                .verificationStatus(candidate.getVerificationStatus().toString())
                .proofVerified(candidate.getProofVerified())
                .zkProof(candidate.getZkProof())
                .createdAt(candidate.getCreatedAt())
                .build();
    }
}
