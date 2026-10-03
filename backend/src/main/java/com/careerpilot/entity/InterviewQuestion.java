package com.careerpilot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "interview_questions")
@Getter @Setter @NoArgsConstructor
public class InterviewQuestion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "session_id") private InterviewSession session;
    @Column(length = 1000) private String question;
    private String category;
    private String difficulty;
    @Column(name = "expected_points", length = 2000) private String expectedPoints;
}
