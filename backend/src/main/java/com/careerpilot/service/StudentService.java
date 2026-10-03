package com.careerpilot.service;

import com.careerpilot.dto.Dtos.*;
import com.careerpilot.entity.Resume;
import com.careerpilot.entity.Skill;
import com.careerpilot.entity.Student;
import com.careerpilot.exception.BadRequestException;
import com.careerpilot.exception.ResourceNotFoundException;
import com.careerpilot.repository.ResumeRepository;
import com.careerpilot.repository.StudentRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service @RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepo;
    private final ResumeRepository resumeRepo;
    private final SkillService skillService;

    public Student requireStudent(String email) {
        return studentRepo.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) { return toResponse(requireStudent(email)); }

    @Transactional
    public ProfileResponse updateProfile(String email, ProfileRequest r) {
        Student s = requireStudent(email);
        s.setName(r.name().trim());
        s.setCollege(r.college());
        s.setDegree(r.degree());
        s.setCgpa(r.cgpa() == null ? 0 : r.cgpa());
        s.setGraduationYear(r.graduationYear());
        s.setPhone(r.phone());
        s.setProjects(r.projects());
        s.setCertifications(r.certifications());
        s.setSkills(skillService.resolve(r.skills()));
        return toResponse(studentRepo.save(s));
    }

    @Transactional
    public ProfileResponse uploadResume(String email, MultipartFile file) {
        Student s = requireStudent(email);
        if (file == null || file.isEmpty()) throw new BadRequestException("Choose a resume file to upload");
        String name = file.getOriginalFilename() == null ? "resume" : file.getOriginalFilename();
        String lower = name.toLowerCase();
        String text;
        try {
            if (lower.endsWith(".pdf")) {
                try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
                    text = new PDFTextStripper().getText(doc);
                }
            } else if (lower.endsWith(".txt") || lower.endsWith(".md")) {
                text = new String(file.getBytes(), StandardCharsets.UTF_8);
            } else {
                throw new BadRequestException("Upload a PDF or TXT file");
            }
        } catch (IOException e) {
            throw new BadRequestException("We could not read that file. Try exporting it as a text-based PDF.");
        }
        text = text.strip();
        if (text.length() < 30) throw new BadRequestException("No readable text found in that file");
        if (text.length() > 20000) text = text.substring(0, 20000);

        Resume r = new Resume();
        r.setStudent(s); r.setFileName(name); r.setExtractedText(text);
        resumeRepo.save(r);
        return toResponse(s);
    }

    /** Profile completion: ten equal checkpoints. */
    public int completion(Student s) {
        int done = 0;
        if (notBlank(s.getName())) done++;
        if (notBlank(s.getCollege())) done++;
        if (notBlank(s.getDegree())) done++;
        if (s.getCgpa() > 0) done++;
        if (s.getGraduationYear() != null) done++;
        if (notBlank(s.getPhone())) done++;
        if (notBlank(s.getProjects())) done++;
        if (notBlank(s.getCertifications())) done++;
        if (s.getSkills().size() >= 3) done++;
        if (resumeRepo.findFirstByStudentIdOrderByUploadedAtDesc(s.getId()).isPresent()) done++;
        return done * 10;
    }

    private ProfileResponse toResponse(Student s) {
        List<String> skills = s.getSkills().stream().map(Skill::getName).sorted(String.CASE_INSENSITIVE_ORDER).toList();
        ResumeDto resume = resumeRepo.findFirstByStudentIdOrderByUploadedAtDesc(s.getId())
                .map(r -> new ResumeDto(r.getFileName(), r.getExtractedText(), r.getUploadedAt())).orElse(null);
        return new ProfileResponse(s.getUser().getEmail(), s.getName(), s.getCollege(), s.getDegree(), s.getCgpa(),
                s.getGraduationYear(), s.getPhone(), s.getProjects(), s.getCertifications(), skills, completion(s), resume);
    }

    private boolean notBlank(String v) { return v != null && !v.isBlank(); }
}
