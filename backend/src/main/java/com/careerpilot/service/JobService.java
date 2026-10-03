package com.careerpilot.service;

import com.careerpilot.dto.Dtos.*;
import com.careerpilot.entity.*;
import com.careerpilot.exception.BadRequestException;
import com.careerpilot.exception.ConflictException;
import com.careerpilot.exception.ResourceNotFoundException;
import com.careerpilot.repository.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepo;
    private final CompanyRepository companyRepo;
    private final ApplicationRepository appRepo;
    private final StudentService studentService;
    private final SkillService skillService;
    private final MatchService matchService;
    private final EligibilityService eligibilityService;

    @Transactional(readOnly = true)
    public List<JobDto> list(String email, boolean admin, String q, String location, String sort) {
        Student s = admin ? null : studentService.requireStudent(email);
        Set<Long> applied = s == null ? Set.of() : appRepo.findByStudentIdOrderByAppliedAtDesc(s.getId()).stream()
                .map(a -> a.getJob().getId()).collect(Collectors.toSet());
        Map<Long, Long> counts = s == null ? countMap() : Map.of();
        String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        String loc = location == null ? "" : location.trim().toLowerCase(Locale.ROOT);

        List<JobDto> out = jobRepo.findAllDetailed().stream()
                .filter(j -> admin || j.getStatus() == JobStatus.OPEN)
                .filter(j -> query.isEmpty() || j.getTitle().toLowerCase(Locale.ROOT).contains(query)
                        || j.getCompany().getName().toLowerCase(Locale.ROOT).contains(query)
                        || j.getSkills().stream().anyMatch(k -> k.getName().toLowerCase(Locale.ROOT).contains(query)))
                .filter(j -> loc.isEmpty() || (j.getLocation() != null && j.getLocation().toLowerCase(Locale.ROOT).contains(loc)))
                .map(j -> toDto(j, s, applied, counts))
                .collect(Collectors.toCollection(ArrayList::new));
        out.sort(comparator(sort));
        return out;
    }

    @Transactional(readOnly = true)
    public JobDto get(String email, boolean admin, Long id) {
        Job j = find(id);
        Student s = admin ? null : studentService.requireStudent(email);
        Set<Long> applied = s != null && appRepo.existsByStudentIdAndJobId(s.getId(), id) ? Set.of(id) : Set.of();
        return toDto(j, s, applied, s == null ? countMap() : Map.of());
    }

    @Transactional(readOnly = true)
    public MatchResult match(String email, Long id) {
        Job j = find(id);
        Student s = studentService.requireStudent(email);
        SkillMatch m = matchService.calculate(s, j);
        return new MatchResult(id, m.percentage(), m.matched(), m.missing(), eligibilityService.check(s, j));
    }

    @Transactional
    public JobDto create(JobRequest r) {
        Job j = new Job();
        fill(j, r);
        return toDto(jobRepo.save(j), null, Set.of(), Map.of());
    }

    @Transactional
    public JobDto update(Long id, JobRequest r) {
        Job j = find(id);
        fill(j, r);
        return toDto(jobRepo.save(j), null, Set.of(), countMap());
    }

    @Transactional
    public void delete(Long id) {
        Job j = find(id);
        if (appRepo.countByJobId(id) > 0) throw new ConflictException("This job has applications. Close it instead of deleting it.");
        jobRepo.delete(j);
    }

    // ---------- helpers ----------
    private Job find(Long id) {
        return jobRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job " + id + " not found"));
    }

    private void fill(Job j, JobRequest r) {
        Company c = companyRepo.findById(r.companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company " + r.companyId() + " not found"));
        j.setCompany(c);
        j.setTitle(r.title().trim());
        j.setDescription(r.description());
        j.setMinimumCgpa(r.minimumCgpa() == null ? 0 : r.minimumCgpa());
        j.setLocation(r.location());
        j.setSalaryLpa(r.salaryLpa());
        j.setDeadline(r.deadline());
        j.setJobType(r.jobType() == null || r.jobType().isBlank() ? "Full-time" : r.jobType());
        j.setGraduationYear(r.graduationYear());
        j.setAllowedDegrees(r.allowedDegrees());
        try {
            j.setStatus(r.status() == null || r.status().isBlank() ? JobStatus.OPEN : JobStatus.valueOf(r.status().trim().toUpperCase()));
        } catch (IllegalArgumentException e) { throw new BadRequestException("Status must be OPEN or CLOSED"); }
        j.setSkills(skillService.resolve(r.skills()));
    }

    private Map<Long, Long> countMap() {
        Map<Long, Long> m = new HashMap<>();
        for (Object[] row : appRepo.countPerJob()) m.put((Long) row[0], (Long) row[1]);
        return m;
    }

    JobDto toDto(Job j, Student s, Set<Long> applied, Map<Long, Long> counts) {
        List<String> skills = j.getSkills().stream().map(Skill::getName).sorted(String.CASE_INSENSITIVE_ORDER).toList();
        Double pct = null; Boolean eligible = null, isApplied = null; Integer applicants = null;
        if (s != null) {
            pct = matchService.calculate(s, j).percentage();
            eligible = eligibilityService.check(s, j).eligible();
            isApplied = applied.contains(j.getId());
        } else {
            applicants = counts.getOrDefault(j.getId(), 0L).intValue();
        }
        Company c = j.getCompany();
        return new JobDto(j.getId(), c.getId(), c.getName(), c.getLocation(), j.getTitle(), j.getDescription(),
                j.getMinimumCgpa(), j.getLocation(), j.getSalaryLpa(), j.getDeadline(), j.getStatus().name(),
                j.getJobType(), j.getGraduationYear(), j.getAllowedDegrees(), skills, pct, eligible, isApplied, applicants);
    }

    /** sort=match -> highest match first, then earliest deadline (documented tie-breaker). */
    private Comparator<JobDto> comparator(String sort) {
        Comparator<JobDto> byDeadline = Comparator.comparing(JobDto::deadline, Comparator.nullsLast(Comparator.naturalOrder()));
        if ("match".equals(sort)) {
            return Comparator.comparing(JobDto::matchPercentage, Comparator.nullsLast(Comparator.<Double>reverseOrder())).thenComparing(byDeadline);
        }
        if ("deadline".equals(sort)) return byDeadline;
        if ("salary".equals(sort)) return Comparator.comparing(JobDto::salaryLpa, Comparator.nullsLast(Comparator.<Double>reverseOrder()));
        return Comparator.comparing(JobDto::id).reversed();
    }
}
