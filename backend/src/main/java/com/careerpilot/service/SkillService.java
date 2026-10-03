package com.careerpilot.service;

import com.careerpilot.entity.Skill;
import com.careerpilot.exception.BadRequestException;
import com.careerpilot.repository.SkillRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class SkillService {
    private final SkillRepository repo;

    /** Turn raw skill names into Skill entities, creating any that don't exist yet. */
    @Transactional
    public Set<Skill> resolve(Collection<String> names) {
        Set<Skill> out = new HashSet<>();
        if (names == null) return out;
        for (String raw : names) {
            if (raw == null || raw.isBlank()) continue;
            String n = raw.trim();
            if (n.length() > 50) throw new BadRequestException("Skill name is too long: " + n);
            out.add(repo.findByNameIgnoreCase(n).orElseGet(() -> repo.save(new Skill(n))));
        }
        return out;
    }

    public List<String> allNames() {
        return repo.findAll().stream().map(Skill::getName).sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }
}
