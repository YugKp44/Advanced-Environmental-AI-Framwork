package com.ecoai.service;

import com.ecoai.dto.CompanyDTO;
import com.ecoai.entity.Company;
import com.ecoai.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Company management operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyDTO createCompany(CompanyDTO dto) {
        Company company = Company.builder()
                .name(dto.getName())
                .industry(dto.getIndustry())
                .country(dto.getCountry())
                .region(dto.getRegion())
                .baseAiPercentage(dto.getBaseAiPercentage())
                .electricityCostPerKwh(dto.getElectricityCostPerKwh())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "USD")
                .build();

        Company saved = companyRepository.save(company);
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public CompanyDTO getCompany(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found: " + id));
        return mapToDTO(company);
    }

    @Transactional(readOnly = true)
    public List<CompanyDTO> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CompanyDTO updateCompany(UUID id, CompanyDTO dto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found: " + id));

        if (dto.getName() != null)
            company.setName(dto.getName());
        if (dto.getIndustry() != null)
            company.setIndustry(dto.getIndustry());
        if (dto.getCountry() != null)
            company.setCountry(dto.getCountry());
        if (dto.getRegion() != null)
            company.setRegion(dto.getRegion());
        if (dto.getBaseAiPercentage() != null)
            company.setBaseAiPercentage(dto.getBaseAiPercentage());
        if (dto.getElectricityCostPerKwh() != null)
            company.setElectricityCostPerKwh(dto.getElectricityCostPerKwh());
        if (dto.getCurrency() != null)
            company.setCurrency(dto.getCurrency());

        Company saved = companyRepository.save(company);
        return mapToDTO(saved);
    }

    public void deleteCompany(UUID id) {
        companyRepository.deleteById(id);
    }

    private CompanyDTO mapToDTO(Company company) {
        return CompanyDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .industry(company.getIndustry())
                .country(company.getCountry())
                .region(company.getRegion())
                .baseAiPercentage(company.getBaseAiPercentage())
                .electricityCostPerKwh(company.getElectricityCostPerKwh())
                .currency(company.getCurrency())
                .build();
    }
}
