package com.careerpilot.controller;

import com.careerpilot.dto.Dtos.*;
import com.careerpilot.service.JobService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/jobs") @RequiredArgsConstructor
public class JobController {
    private final JobService jobs;

    static boolean isAdmin(Authentication a) {
        return a.getAuthorities().stream().anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping
    public List<JobDto> list(Authentication a, @RequestParam(required = false) String q,
                             @RequestParam(required = false) String location, @RequestParam(required = false) String sort) {
        return jobs.list(a.getName(), isAdmin(a), q, location, sort);
    }

    @GetMapping("/{id}")
    public JobDto get(Authentication a, @PathVariable Long id) { return jobs.get(a.getName(), isAdmin(a), id); }

    @GetMapping("/{id}/match")
    public MatchResult match(Authentication a, @PathVariable Long id) { return jobs.match(a.getName(), id); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public JobDto create(@Valid @RequestBody JobRequest r) { return jobs.create(r); }

    @PutMapping("/{id}")
    public JobDto update(@PathVariable Long id, @Valid @RequestBody JobRequest r) { return jobs.update(id, r); }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { jobs.delete(id); }
}
