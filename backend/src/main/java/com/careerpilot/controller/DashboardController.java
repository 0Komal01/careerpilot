package com.careerpilot.controller;

import com.careerpilot.dto.Dtos.*;
import com.careerpilot.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/dashboard") @RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboard;

    @GetMapping("/student") public StudentDashboard student(Authentication a) { return dashboard.student(a.getName()); }
    @GetMapping("/admin") public AdminDashboard admin() { return dashboard.admin(); }
}
