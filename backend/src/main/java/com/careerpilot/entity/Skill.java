package com.careerpilot.entity;

import jakarta.persistence.*;
import java.util.Locale;
import java.util.Objects;
import lombok.*;

@Entity @Table(name = "skills")
@Getter @Setter @NoArgsConstructor
public class Skill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 60) private String name;

    public Skill(String name) { this.name = name; }

    @Override public boolean equals(Object o) {
        return o instanceof Skill s && name != null && s.name != null
                && name.toLowerCase(Locale.ROOT).equals(s.name.toLowerCase(Locale.ROOT));
    }
    @Override public int hashCode() { return Objects.hash(name == null ? "" : name.toLowerCase(Locale.ROOT)); }
}
