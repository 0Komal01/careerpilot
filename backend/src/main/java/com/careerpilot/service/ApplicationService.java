package com.careerpilot.service;

import com.careerpilot.dto.Dtos.*;
import com.careerpilot.entity.*;
import com.careerpilot.exception.BadRequestException;
import com.careerpilot.exception.DuplicateApplicationException;
import com.careerpilot.exception.ResourceNotFoundException;
import com.careerpilot.repository.ApplicationRepository;
import com.careerpilot.repository.JobRepository;
import com.careerpilot.repository.StudentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository appRepo;
    private final StudentRepository studentRepo;
    private final JobRepository jobRepo;
    private final EligibilityService eligibility;
    private final MatchService matchService;

    @Transactional
    public ApplicationDto apply(String email, Long jobId) {
        Student s = studentRepo.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
        Job j = jobRepo.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job " + jobId + " not found"));

        // Duplicate guard: checked here for a friendly message, and enforced again by a DB unique constraint.
        if (appRepo.existsByStudentIdAndJobId(s.getId(), j.getId()))
            throw new DuplicateApplicationException("You have already applied to this job");

        EligibilityResult el = eligibility.check(s, j);
        if (!el.eligible())
            throw new BadRequestException("You are not eligible: " + String.join("; ", el.missingRequirements()));

        Application a = new Application();
        a.setStudent(s);
        a.setJob(j);
        a.setStatus(ApplicationStatus.APPLIED);
        return toDto(appRepo.save(a));
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto> list(String email, boolean admin) {
        List<Application> apps = admin ? appRepo.findAllByOrderByAppliedAtDesc()
                : appRepo.findByStudentIdOrderByAppliedAtDesc(
                        studentRepo.findByUserEmail(email).orElseThrow(() -> new ResourceNotFoundException("Student profile not found")).getId());
        return apps.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ApplicationDto get(Long id, String email, boolean admin) {
        Application a = find(id);
        if (!admin && !a.getStudent().getUser().getEmail().equals(email)) throw new AccessDeniedException("Not your application");
        return toDto(a);
    }

    @Transactional
    public ApplicationDto updateStatus(Long id, String statusText) {
        Application a = find(id);
        ApplicationStatus next = ApplicationStatus.parse(statusText);
        ApplicationStatus current = a.getStatus();
        if (!current.canTransitionTo(next)) {
            String allowed = current.allowedNext().isEmpty() ? "none (final status)"
                    : current.allowedNext().stream().map(ApplicationStatus::getLabel).toList().toString();
            throw new BadRequestException("Cannot move from " + current.getLabel() + " to " + next.getLabel() + ". Allowed: " + allowed);
        }
        a.setStatus(next);
        return toDto(appRepo.save(a));
    }

    private Application find(Long id) {
        return appRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Application " + id + " not found"));
    }

    private ApplicationDto toDto(Application a) {
        Student s = a.getStudent();
        Job j = a.getJob();
        return new ApplicationDto(a.getId(), j.getId(), j.getTitle(), j.getCompany().getName(), s.getId(), s.getName(),
                s.getUser().getEmail(), s.getCgpa(), a.getStatus().name(), a.getStatus().getLabel(), a.getAppliedAt(),
                a.getUpdatedAt(), a.getStatus().allowedNext().stream().map(Enum::name).toList(),
                matchService.calculate(s, j).percentage());
    }
}
