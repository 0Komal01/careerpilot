package com.careerpilot.service;

import com.careerpilot.dto.Dtos.*;
import com.careerpilot.entity.*;
import com.careerpilot.repository.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class DashboardService {
    private final StudentService studentService;
    private final JobService jobService;
    private final ApplicationRepository appRepo;
    private final JobRepository jobRepo;
    private final CompanyRepository companyRepo;
    private final StudentRepository studentRepo;

    @Transactional(readOnly = true)
    public StudentDashboard student(String email) {
        Student s = studentService.requireStudent(email);
        List<Application> apps = appRepo.findByStudentIdOrderByAppliedAtDesc(s.getId());
        Map<String, Long> counts = new LinkedHashMap<>();
        for (ApplicationStatus st : ApplicationStatus.values()) counts.put(st.getLabel(), 0L);
        apps.forEach(a -> counts.merge(a.getStatus().getLabel(), 1L, Long::sum));
        long pipeline = apps.stream().filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED
                || a.getStatus() == ApplicationStatus.TECHNICAL_INTERVIEW || a.getStatus() == ApplicationStatus.HR_INTERVIEW).count();
        long selected = apps.stream().filter(a -> a.getStatus() == ApplicationStatus.SELECTED).count();
        List<JobDto> top = jobService.list(email, false, null, null, "match").stream()
                .filter(j -> Boolean.TRUE.equals(j.eligible()) && !Boolean.TRUE.equals(j.applied())).limit(5).toList();
        return new StudentDashboard(studentService.completion(s), apps.size(), pipeline, selected, counts, top);
    }

    @Transactional(readOnly = true)
    public AdminDashboard admin() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (ApplicationStatus st : ApplicationStatus.values()) counts.put(st.getLabel(), appRepo.countByStatus(st));
        Map<Long, Long> perJob = new HashMap<>();
        for (Object[] row : appRepo.countPerJob()) perJob.put((Long) row[0], (Long) row[1]);
        List<JobCount> top = jobRepo.findAllDetailed().stream()
                .map(j -> new JobCount(j.getTitle(), j.getCompany().getName(), perJob.getOrDefault(j.getId(), 0L)))
                .sorted(Comparator.comparingLong(JobCount::applications).reversed()).limit(6).collect(Collectors.toList());
        return new AdminDashboard(companyRepo.count(), jobRepo.count(), jobRepo.countByStatus(JobStatus.OPEN),
                studentRepo.count(), appRepo.count(), counts.get(ApplicationStatus.SELECTED.getLabel()), counts, top);
    }
}
