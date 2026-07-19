package com.veriskill.backend.controller;

import com.veriskill.backend.dto.JobRequest;
import com.veriskill.backend.dto.JobResponse;
import com.veriskill.backend.dto.CandidateSummary;
import com.veriskill.backend.model.Job;
import com.veriskill.backend.model.Candidate;
import com.veriskill.backend.repository.JobRepository;
import com.veriskill.backend.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;

    /**
     * Create a new job posting
     */
    @PostMapping
    public ResponseEntity<JobResponse> createJob(@RequestBody JobRequest request) {
        log.info("Creating new job: {}", request.getTitle());

        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .recruiterId(request.getRecruiterId())
                .requiredThreshold(request.getRequiredThreshold())
                .build();

        Job savedJob = jobRepository.save(job);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(savedJob));
    }

    /**
     * Get job by ID
     */
    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable String jobId) {
        log.info("Fetching job: {}", jobId);

        Optional<Job> job = jobRepository.findById(jobId);
        if (job.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(job.get()));
    }

    /**
     * Get all jobs for a recruiter
     */
    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<JobResponse>> getRecruiterJobs(@PathVariable String recruiterId) {
        log.info("Fetching jobs for recruiter: {}", recruiterId);

        List<Job> jobs = jobRepository.findByRecruiterId(recruiterId);
        List<JobResponse> responses = jobs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Close a job posting
     */
    @PutMapping("/{jobId}/close")
    public ResponseEntity<JobResponse> closeJob(@PathVariable String jobId) {
        log.info("Closing job: {}", jobId);

        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Job job = jobOpt.get();
        job.setStatus(Job.JobStatus.CLOSED);
        Job updated = jobRepository.save(job);

        return ResponseEntity.ok(mapToResponse(updated));
    }

    /**
     * Map Job entity to JobResponse DTO
     */
    private JobResponse mapToResponse(Job job) {
        List<CandidateSummary> candidateSummaries = candidateRepository.findByJobId(job.getId())
                .stream()
                .map(c -> CandidateSummary.builder()
                        .id(c.getId())
                        .fullName(c.getFullName())
                        .email(c.getEmail())
                        .verificationStatus(c.getVerificationStatus().toString())
                        .proofVerified(c.getProofVerified())
                        .build())
                .collect(Collectors.toList());

        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .recruiterId(job.getRecruiterId())
                .requiredThreshold(job.getRequiredThreshold())
                .status(job.getStatus().toString())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .candidates(candidateSummaries)
                .build();
    }
}
