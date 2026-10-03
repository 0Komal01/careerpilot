package com.careerpilot.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity @Table(name = "interview_sessions")
@Getter @Setter @NoArgsConstructor
public class InterviewSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "student_id") private Student student;
    @Column(name = "target_role") private String targetRole;
    @Column(name = "created_at") private Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewQuestion> questions = new ArrayList<>();
}
