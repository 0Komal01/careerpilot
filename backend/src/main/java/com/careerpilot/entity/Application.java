package com.careerpilot.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "applications",
        uniqueConstraints = @UniqueConstraint(name = "uk_student_job", columnNames = {"student_id", "job_id"}),
        indexes = @Index(name = "idx_applications_status", columnList = "status"))
@Getter @Setter @NoArgsConstructor
public class Application {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "student_id") private Student student;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "job_id") private Job job;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ApplicationStatus status = ApplicationStatus.APPLIED;
    @Column(name = "applied_at") private Instant appliedAt = Instant.now();
    @Column(name = "updated_at") private Instant updatedAt = Instant.now();

    @PreUpdate void touch() { updatedAt = Instant.now(); }
}
