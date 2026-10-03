package com.careerpilot.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Entity @Table(name = "students")
@Getter @Setter @NoArgsConstructor
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(optional = false) @JoinColumn(name = "user_id", unique = true) private User user;
    @Column(nullable = false) private String name;
    private String college;
    private String degree;
    private double cgpa;
    @Column(name = "graduation_year") private Integer graduationYear;
    private String phone;
    @Column(length = 2000) private String projects;
    @Column(length = 2000) private String certifications;

    @ManyToMany
    @JoinTable(name = "student_skills",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new HashSet<>();
}
