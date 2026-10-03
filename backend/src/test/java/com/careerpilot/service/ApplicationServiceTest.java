package com.careerpilot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.careerpilot.dto.Dtos.ApplicationDto;
import com.careerpilot.entity.*;
import com.careerpilot.exception.BadRequestException;
import com.careerpilot.exception.DuplicateApplicationException;
import com.careerpilot.exception.ResourceNotFoundException;
import com.careerpilot.repository.ApplicationRepository;
import com.careerpilot.repository.JobRepository;
import com.careerpilot.repository.StudentRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {
    @Mock ApplicationRepository appRepo;
    @Mock StudentRepository studentRepo;
    @Mock JobRepository jobRepo;
    private ApplicationService service;
    private Student student;
    private Job job;

    @BeforeEach void setUp() {
        service = new ApplicationService(appRepo, studentRepo, jobRepo, new EligibilityService(), new MatchService());
        User user = new User(); user.setEmail("aarav@careerpilot.dev");
        student = new Student(); student.setId(1L); student.setUser(user); student.setName("Aarav"); student.setCgpa(9.0);
        Company company = new Company(); company.setId(5L); company.setName("Nimbus Labs");
        job = new Job(); job.setId(10L); job.setCompany(company); job.setTitle("Java Dev");
        job.setMinimumCgpa(7.0); job.setDeadline(LocalDate.now().plusDays(7)); job.setStatus(JobStatus.OPEN);
    }

    @Test void duplicateApplicationIsRejected() {
        when(studentRepo.findByUserEmail("aarav@careerpilot.dev")).thenReturn(Optional.of(student));
        when(jobRepo.findById(10L)).thenReturn(Optional.of(job));
        when(appRepo.existsByStudentIdAndJobId(1L, 10L)).thenReturn(true);
        assertThrows(DuplicateApplicationException.class, () -> service.apply("aarav@careerpilot.dev", 10L));
        verify(appRepo, never()).save(any());
    }

    @Test void ineligibleStudentCannotApply() {
        job.setMinimumCgpa(9.5);
        when(studentRepo.findByUserEmail(any())).thenReturn(Optional.of(student));
        when(jobRepo.findById(10L)).thenReturn(Optional.of(job));
        when(appRepo.existsByStudentIdAndJobId(1L, 10L)).thenReturn(false);
        assertThrows(BadRequestException.class, () -> service.apply("aarav@careerpilot.dev", 10L));
    }

    @Test void missingJobGives404() {
        when(studentRepo.findByUserEmail(any())).thenReturn(Optional.of(student));
        when(jobRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.apply("aarav@careerpilot.dev", 99L));
    }

    @Test void validApplicationStartsAsApplied() {
        when(studentRepo.findByUserEmail(any())).thenReturn(Optional.of(student));
        when(jobRepo.findById(10L)).thenReturn(Optional.of(job));
        when(appRepo.existsByStudentIdAndJobId(1L, 10L)).thenReturn(false);
        when(appRepo.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));
        ApplicationDto dto = service.apply("aarav@careerpilot.dev", 10L);
        assertEquals("APPLIED", dto.status());
    }

    @Test void invalidStatusTransitionIsRejected() {
        Application a = new Application();
        a.setId(3L); a.setStudent(student); a.setJob(job); a.setStatus(ApplicationStatus.APPLIED);
        when(appRepo.findById(3L)).thenReturn(Optional.of(a));
        assertThrows(BadRequestException.class, () -> service.updateStatus(3L, "SELECTED"));
        verify(appRepo, never()).save(any());
    }

    @Test void validStatusTransitionIsSaved() {
        Application a = new Application();
        a.setId(3L); a.setStudent(student); a.setJob(job); a.setStatus(ApplicationStatus.APPLIED);
        when(appRepo.findById(3L)).thenReturn(Optional.of(a));
        when(appRepo.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals("SHORTLISTED", service.updateStatus(3L, "Shortlisted").status());
    }
}
