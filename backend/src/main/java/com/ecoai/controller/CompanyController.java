package com.ecoai.controller;

import com.ecoai.dto.CompanyDTO;
import com.ecoai.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Company management.
 */
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@CrossOrigin
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<CompanyDTO> createCompany(@RequestBody CompanyDTO dto) {
        CompanyDTO created = companyService.createCompany(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<CompanyDTO>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDTO> getCompany(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.getCompany(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyDTO> updateCompany(@PathVariable UUID id, @RequestBody CompanyDTO dto) {
        return ResponseEntity.ok(companyService.updateCompany(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}
