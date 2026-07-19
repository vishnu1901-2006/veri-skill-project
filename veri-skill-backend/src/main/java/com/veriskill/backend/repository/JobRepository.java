package com.veriskill.backend.repository;

import com.veriskill.backend.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {
    List<Job> findByRecruiterId(String recruiterId);
    List<Job> findByStatus(Job.JobStatus status);
}
