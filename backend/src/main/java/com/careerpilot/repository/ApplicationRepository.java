package com.careerpilot.repository;

import com.careerpilot.entity.Application;
import com.careerpilot.entity.ApplicationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByStudentIdAndJobId(Long studentId, Long jobId);
    List<Application> findByStudentIdOrderByAppliedAtDesc(Long studentId);
    List<Application> findAllByOrderByAppliedAtDesc();
    long countByJobId(Long jobId);
    long countByStatus(ApplicationStatus status);

    @Query("select a.job.id, count(a) from Application a group by a.job.id")
    List<Object[]> countPerJob();
}
