package com.careerpilot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "companies")
@Getter @Setter @NoArgsConstructor
public class Company {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String name;
    private String location;
    private String website;
}
