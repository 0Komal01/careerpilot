package com.careerpilot.controller;

import com.careerpilot.dto.Dtos.*;
import com.careerpilot.service.ApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/applications") @RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService apps;

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ApplicationDto apply(Authentication a, @Valid @RequestBody ApplyRequest r) { return apps.apply(a.getName(), r.jobId()); }

    @GetMapping
    public List<ApplicationDto> list(Authentication a) { return apps.list(a.getName(), JobController.isAdmin(a)); }

    @GetMapping("/{id}")
    public ApplicationDto get(Authentication a, @PathVariable Long id) { return apps.get(id, a.getName(), JobController.isAdmin(a)); }

    @PutMapping("/{id}/status")
    public ApplicationDto status(@PathVariable Long id, @Valid @RequestBody StatusRequest r) { return apps.updateStatus(id, r.status()); }
}
