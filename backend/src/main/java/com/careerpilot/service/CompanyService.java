package com.careerpilot.service;

import com.careerpilot.dto.Dtos.*;
import com.careerpilot.entity.Company;
import com.careerpilot.exception.ConflictException;
import com.careerpilot.exception.ResourceNotFoundException;
import com.careerpilot.repository.CompanyRepository;
import com.careerpilot.repository.JobRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository repo;
    private final JobRepository jobRepo;

    public List<CompanyDto> list() {
        return repo.findAll().stream().map(this::toDto)
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name())).toList();
    }

    @Transactional
    public CompanyDto create(CompanyRequest r) {
        if (repo.existsByNameIgnoreCase(r.name().trim())) throw new ConflictException("A company with this name already exists");
        Company c = new Company();
        fill(c, r);
        return toDto(repo.save(c));
    }

    @Transactional
    public CompanyDto update(Long id, CompanyRequest r) {
        Company c = find(id);
        fill(c, r);
        return toDto(repo.save(c));
    }

    @Transactional
    public void delete(Long id) {
        Company c = find(id);
        if (jobRepo.countByCompanyId(id) > 0) throw new ConflictException("Delete or move this company's jobs first");
        repo.delete(c);
    }

    private Company find(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Company " + id + " not found"));
    }
    private void fill(Company c, CompanyRequest r) { c.setName(r.name().trim()); c.setLocation(r.location()); c.setWebsite(r.website()); }
    private CompanyDto toDto(Company c) { return new CompanyDto(c.getId(), c.getName(), c.getLocation(), c.getWebsite(), jobRepo.countByCompanyId(c.getId())); }
}
