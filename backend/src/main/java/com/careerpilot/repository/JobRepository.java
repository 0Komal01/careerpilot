package com.careerpilot.repository;

import com.careerpilot.entity.Job;
import com.careerpilot.entity.JobStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobRepository extends JpaRepository<Job, Long> {
    @EntityGraph(attributePaths = {"company", "skills"})
    @Query("select j from Job j")
    List<Job> findAllDetailed();

    long countByCompanyId(Long companyId);
    long countByStatus(JobStatus status);
}
