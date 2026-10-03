package com.careerpilot.service;

import com.careerpilot.dto.Dtos.EligibilityResult;
import com.careerpilot.entity.Job;
import com.careerpilot.entity.JobStatus;
import com.careerpilot.entity.Student;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class EligibilityService {

    public EligibilityResult check(Student s, Job j) { return check(s, j, LocalDate.now()); }

    public EligibilityResult check(Student s, Job j, LocalDate today) {
        List<String> reasons = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        if (j.getStatus() == JobStatus.CLOSED) {
            missing.add("Applications for this job are closed");
        } else if (j.getDeadline() != null && today.isAfter(j.getDeadline())) {
            missing.add("The deadline passed on " + j.getDeadline());
        } else {
            reasons.add("This job is open for applications");
        }

        if (s.getCgpa() >= j.getMinimumCgpa()) {
            reasons.add(String.format(Locale.ROOT, "Your CGPA %.2f meets the minimum of %.2f", s.getCgpa(), j.getMinimumCgpa()));
        } else {
            String m = String.format(Locale.ROOT, "CGPA of at least %.2f (yours is %.2f)", j.getMinimumCgpa(), s.getCgpa());
            missing.add(m);
        }

        if (j.getGraduationYear() != null) {
            if (j.getGraduationYear().equals(s.getGraduationYear())) {
                reasons.add("Your graduation year matches the " + j.getGraduationYear() + " batch");
            } else {
                missing.add("Graduation year " + j.getGraduationYear() + " (yours is "
                        + (s.getGraduationYear() == null ? "not set" : s.getGraduationYear()) + ")");
            }
        }

        if (j.getAllowedDegrees() != null && !j.getAllowedDegrees().isBlank()) {
            String degree = s.getDegree() == null ? "" : s.getDegree().toLowerCase(Locale.ROOT);
            boolean ok = false;
            for (String token : j.getAllowedDegrees().split(",")) {
                String t = token.trim().toLowerCase(Locale.ROOT);
                if (!t.isEmpty() && degree.contains(t)) { ok = true; break; }
            }
            if (ok) reasons.add("Your degree is accepted for this role");
            else missing.add("Degree in: " + j.getAllowedDegrees());
        }

        return new EligibilityResult(missing.isEmpty(), reasons, missing);
    }
}
