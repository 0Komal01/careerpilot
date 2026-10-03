package com.careerpilot.service;

import static org.junit.jupiter.api.Assertions.*;

import com.careerpilot.dto.Dtos.EligibilityResult;
import com.careerpilot.entity.Job;
import com.careerpilot.entity.JobStatus;
import com.careerpilot.entity.Student;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EligibilityServiceTest {
    private final EligibilityService service = new EligibilityService();
    private final LocalDate today = LocalDate.of(2026, 10, 3);
    private Student student;
    private Job job;

    @BeforeEach void setUp() {
        student = new Student();
        student.setCgpa(8.0); student.setGraduationYear(2027); student.setDegree("B.Tech Computer Engineering");
        job = new Job();
        job.setMinimumCgpa(7.0); job.setDeadline(today.plusDays(10)); job.setStatus(JobStatus.OPEN);
    }

    @Test void eligibleWhenAllCriteriaPass() {
        job.setGraduationYear(2027); job.setAllowedDegrees("B.Tech,MCA");
        EligibilityResult r = service.check(student, job, today);
        assertTrue(r.eligible());
        assertTrue(r.missingRequirements().isEmpty());
    }

    @Test void notEligibleWhenCgpaTooLow() {
        job.setMinimumCgpa(8.5);
        EligibilityResult r = service.check(student, job, today);
        assertFalse(r.eligible());
        assertEquals(1, r.missingRequirements().size());
    }

    @Test void notEligibleWhenGraduationYearDiffers() {
        job.setGraduationYear(2026);
        assertFalse(service.check(student, job, today).eligible());
    }

    @Test void notEligibleWhenDegreeNotAccepted() {
        job.setAllowedDegrees("MBA");
        assertFalse(service.check(student, job, today).eligible());
    }

    @Test void notEligibleAfterDeadlineOrWhenClosed() {
        job.setDeadline(today.minusDays(1));
        assertFalse(service.check(student, job, today).eligible());
        job.setDeadline(today.plusDays(5)); job.setStatus(JobStatus.CLOSED);
        assertFalse(service.check(student, job, today).eligible());
    }
}
