package com.careerpilot.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** All request/response shapes live here as records, so controllers never expose entities directly. */
public final class Dtos {
    private Dtos() { }

    // ---- auth ----
    public record RegisterRequest(
            @NotBlank(message = "Name is required") String name,
            @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") String email,
            @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password) { }
    public record LoginRequest(@NotBlank String email, @NotBlank String password) { }
    public record AuthResponse(String token, String email, String name, String role) { }

    // ---- student ----
    public record ProfileRequest(
            @NotBlank(message = "Name is required") String name,
            String college, String degree,
            @DecimalMin(value = "0.0", message = "CGPA must be between 0 and 10")
            @DecimalMax(value = "10.0", message = "CGPA must be between 0 and 10") Double cgpa,
            @Min(value = 2000, message = "Enter a valid year") @Max(value = 2100, message = "Enter a valid year") Integer graduationYear,
            String phone, String projects, String certifications, List<String> skills) { }
    public record ResumeDto(String fileName, String extractedText, Instant uploadedAt) { }
    public record ProfileResponse(String email, String name, String college, String degree, double cgpa,
                                  Integer graduationYear, String phone, String projects, String certifications,
                                  List<String> skills, int completion, ResumeDto latestResume) { }

    // ---- company / job ----
    public record CompanyRequest(@NotBlank(message = "Company name is required") String name, String location, String website) { }
    public record CompanyDto(Long id, String name, String location, String website, long jobs) { }
    public record JobRequest(
            @NotNull(message = "Choose a company") Long companyId,
            @NotBlank(message = "Job title is required") String title,
            String description,
            @DecimalMin(value = "0.0", message = "Minimum CGPA must be 0-10") @DecimalMax(value = "10.0", message = "Minimum CGPA must be 0-10") Double minimumCgpa,
            String location, Double salaryLpa, LocalDate deadline, String status, String jobType,
            Integer graduationYear, String allowedDegrees, List<String> skills) { }
    public record JobDto(Long id, Long companyId, String companyName, String companyLocation, String title,
                         String description, double minimumCgpa, String location, Double salaryLpa, LocalDate deadline,
                         String status, String jobType, Integer graduationYear, String allowedDegrees,
                         List<String> skills, Double matchPercentage, Boolean eligible, Boolean applied, Integer applicants) { }

    // ---- matching / eligibility ----
    public record SkillMatch(double percentage, List<String> matched, List<String> missing) { }
    public record EligibilityResult(boolean eligible, List<String> reasons, List<String> missingRequirements) { }
    public record MatchResult(Long jobId, double matchPercentage, List<String> matchedSkills, List<String> missingSkills,
                              EligibilityResult eligibility) { }

    // ---- applications ----
    public record ApplyRequest(@NotNull(message = "jobId is required") Long jobId) { }
    public record StatusRequest(@NotBlank(message = "status is required") String status) { }
    public record ApplicationDto(Long id, Long jobId, String jobTitle, String companyName, Long studentId,
                                 String studentName, String studentEmail, double studentCgpa, String status,
                                 String statusLabel, Instant appliedAt, Instant updatedAt,
                                 List<String> allowedNext, double matchPercentage) { }

    // ---- AI ----
    public record ResumeAnalysisRequest(@NotBlank(message = "Paste your resume text") String text, String targetRole) { }
    public record ResumeAnalysisResponse(List<String> detectedSkills, List<String> strengths, List<String> missingSkills,
                                         List<String> suggestions, int score, String scoreExplanation, String source, String notice) { }
    public record JdAnalysisRequest(@NotBlank(message = "Paste a job description") String text) { }
    public record JdAnalysisResponse(String role, List<String> requiredSkills, List<String> preferredSkills,
                                     String experience, String education, List<String> otherRequirements,
                                     String source, String notice) { }
    public record InterviewRequest(@NotBlank(message = "Target role is required") String targetRole,
                                   List<String> topics, String difficulty, @Min(1) @Max(15) Integer count) { }
    public record QuestionDto(String question, String category, String difficulty, List<String> expectedPoints) { }
    public record InterviewResponse(Long sessionId, String targetRole, List<QuestionDto> questions, String source, String notice) { }

    // ---- dashboards ----
    public record StudentDashboard(int profileCompletion, long totalApplications, long inPipeline, long selected,
                                   Map<String, Long> statusCounts, List<JobDto> topMatches) { }
    public record JobCount(String title, String company, long applications) { }
    public record AdminDashboard(long companies, long jobs, long openJobs, long students, long applications,
                                 long selected, Map<String, Long> statusCounts, List<JobCount> topJobs) { }
}
