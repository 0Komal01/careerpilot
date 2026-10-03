package com.careerpilot.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity @Table(name = "resumes")
@Getter @Setter @NoArgsConstructor
public class Resume {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "student_id") private Student student;
    @Column(name = "file_name") private String fileName;
    @Column(name = "extracted_text", length = 20000) private String extractedText;
    @Column(name = "uploaded_at") private Instant uploadedAt = Instant.now();
}
