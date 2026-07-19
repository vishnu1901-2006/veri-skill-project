package com.veriskill.backend.repository;

import com.veriskill.backend.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, String> {
    List<Candidate> findByJobId(String jobId);
    List<Candidate> findByJobIdAndVerificationStatus(String jobId, Candidate.VerificationStatus status);
    Optional<Candidate> findByIdAndJobId(String id, String jobId);
}
