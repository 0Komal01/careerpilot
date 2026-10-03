package com.careerpilot.repository;

import com.careerpilot.entity.Resume;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findFirstByStudentIdOrderByUploadedAtDesc(Long studentId);
}
