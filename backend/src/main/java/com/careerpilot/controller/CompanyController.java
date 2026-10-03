package com.careerpilot.controller;

import com.careerpilot.dto.Dtos.*;
import com.careerpilot.service.CompanyService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/companies") @RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companies;

    @GetMapping public List<CompanyDto> list() { return companies.list(); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CompanyDto create(@Valid @RequestBody CompanyRequest r) { return companies.create(r); }

    @PutMapping("/{id}")
    public CompanyDto update(@PathVariable Long id, @Valid @RequestBody CompanyRequest r) { return companies.update(id, r); }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { companies.delete(id); }
}
