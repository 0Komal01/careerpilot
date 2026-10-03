package com.careerpilot.controller;

import com.careerpilot.dto.Dtos.*;
import com.careerpilot.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/ai") @RequiredArgsConstructor
public class AiController {
    private final AiService ai;

    @PostMapping("/resume-analysis")
    public ResumeAnalysisResponse resume(@Valid @RequestBody ResumeAnalysisRequest r) { return ai.analyzeResume(r); }

    @PostMapping("/jd-analysis")
    public JdAnalysisResponse jd(@Valid @RequestBody JdAnalysisRequest r) { return ai.analyzeJd(r); }

    @PostMapping("/interview")
    public InterviewResponse interview(Authentication a, @Valid @RequestBody InterviewRequest r) { return ai.interview(a.getName(), r); }
}
