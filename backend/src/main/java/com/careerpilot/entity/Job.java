package com.careerpilot.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "idx_jobs_status", columnList = "status"),
        @Index(name = "idx_jobs_deadline", columnList = "deadline")})
@Getter @Setter @NoArgsConstructor
public class Job {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "company_id") private Company company;
    @Column(nullable = false) private String title;
    @Column(length = 4000) private String description;
    @Column(name = "minimum_cgpa") private double minimumCgpa;
    private String location;
    @Column(name = "salary_lpa") private Double salaryLpa;
    private LocalDate deadline;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private JobStatus status = JobStatus.OPEN;
    @Column(name = "job_type") private String jobType = "Full-time";
    @Column(name = "graduation_year") private Integer graduationYear;
    @Column(name = "allowed_degrees") private String allowedDegrees;

    @ManyToMany
    @JoinTable(name = "job_skills",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new HashSet<>();
}
