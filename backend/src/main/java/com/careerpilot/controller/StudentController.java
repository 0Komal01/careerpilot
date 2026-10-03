package com.careerpilot.controller;

import com.careerpilot.dto.Dtos.*;
import com.careerpilot.service.SkillService;
import com.careerpilot.service.StudentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class StudentController {
    private final StudentService students;
    private final SkillService skills;

    @GetMapping("/students/profile")
    public ProfileResponse profile(Authentication a) { return students.getProfile(a.getName()); }

    @PutMapping("/students/profile")
    public ProfileResponse update(Authentication a, @Valid @RequestBody ProfileRequest r) { return students.updateProfile(a.getName(), r); }

    @PostMapping("/students/resume")
    public ProfileResponse resume(Authentication a, @RequestParam("file") MultipartFile file) { return students.uploadResume(a.getName(), file); }

    @GetMapping("/skills")
    public List<String> skills() { return skills.allNames(); }
}
