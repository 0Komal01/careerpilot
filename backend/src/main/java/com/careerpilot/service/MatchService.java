package com.careerpilot.service;

import com.careerpilot.dto.Dtos.SkillMatch;
import com.careerpilot.entity.Job;
import com.careerpilot.entity.Skill;
import com.careerpilot.entity.Student;
import java.util.*;
import org.springframework.stereotype.Service;

/**
 * Deterministic skill matching (no AI involved).
 * The student's skills go into a HashSet, so each "does the student have this skill?" check is O(1).
 * Total cost: O(S + R) for S student skills and R required skills.
 */
@Service
public class MatchService {

    public SkillMatch calculate(Collection<String> studentSkills, Collection<String> requiredSkills) {
        Set<String> have = new HashSet<>();
        for (String s : studentSkills) have.add(norm(s));

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String req : requiredSkills) {
            if (have.contains(norm(req))) matched.add(req); else missing.add(req);
        }
        int total = requiredSkills.size();
        // A job with no required skills has nothing to miss: treat as a full match instead of dividing by zero.
        double pct = total == 0 ? 100.0 : Math.round(matched.size() * 1000.0 / total) / 10.0;
        return new SkillMatch(pct, matched, missing);
    }

    public SkillMatch calculate(Student student, Job job) {
        List<String> have = student.getSkills().stream().map(Skill::getName).toList();
        List<String> need = job.getSkills().stream().map(Skill::getName).sorted().toList();
        return calculate(have, need);
    }

    private String norm(String s) { return s == null ? "" : s.trim().toLowerCase(Locale.ROOT); }
}
