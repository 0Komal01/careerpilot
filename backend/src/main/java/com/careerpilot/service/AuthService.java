package com.careerpilot.service;

import com.careerpilot.dto.Dtos.*;
import com.careerpilot.entity.Role;
import com.careerpilot.entity.Student;
import com.careerpilot.entity.User;
import com.careerpilot.exception.ConflictException;
import com.careerpilot.repository.StudentRepository;
import com.careerpilot.repository.UserRepository;
import com.careerpilot.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    /** Public sign-up always creates a STUDENT. Admin accounts are created by the seeder / DBA only. */
    @Transactional
    public AuthResponse register(RegisterRequest r) {
        String email = r.email().trim().toLowerCase();
        if (userRepo.existsByEmailIgnoreCase(email)) throw new ConflictException("An account with this email already exists");
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(r.password()));   // BCrypt, never plaintext
        u.setRole(Role.STUDENT);
        userRepo.save(u);
        Student s = new Student();
        s.setUser(u);
        s.setName(r.name().trim());
        studentRepo.save(s);
        return new AuthResponse(jwt.generate(email, Role.STUDENT.name()), email, s.getName(), Role.STUDENT.name());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest r) {
        String email = r.email().trim().toLowerCase();
        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, r.password())); // throws BadCredentialsException
        User u = userRepo.findByEmail(email).orElseThrow();
        String name = u.getRole() == Role.ADMIN ? "Placement Admin"
                : studentRepo.findByUserEmail(email).map(Student::getName).orElse(email);
        return new AuthResponse(jwt.generate(email, u.getRole().name()), email, name, u.getRole().name());
    }
}
