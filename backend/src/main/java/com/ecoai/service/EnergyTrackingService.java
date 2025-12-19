package com.ecoai.service;

import com.ecoai.dto.EnergyUsageDTO;
import com.ecoai.entity.Company;
import com.ecoai.entity.Department;
import com.ecoai.entity.EnergyUsage;
import com.ecoai.repository.CompanyRepository;
import com.ecoai.repository.DepartmentRepository;
import com.ecoai.repository.EnergyUsageRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Energy Tracking operations.
 * Feature 1: Company-Level Energy Tracking
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EnergyTrackingService {

    private final EnergyUsageRepository energyUsageRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final AttributionEngineService attributionEngineService;
    private final CarbonCalculationService carbonCalculationService;

    /**
     * Record a new energy usage entry.
     */
    public EnergyUsageDTO recordEnergyUsage(UUID companyId, EnergyUsageDTO dto) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));

        Department department = null;
        if (dto.getDepartmentId() != null) {
            department = departmentRepository.findById(dto.getDepartmentId())
                    .orElse(null);
        }

        EnergyUsage energyUsage = EnergyUsage.builder()
                .company(company)
                .department(department)
                .totalKwh(dto.getTotalKwh())
                .usageDate(dto.getUsageDate())
                .periodType(dto.getPeriodType() != null ? dto.getPeriodType() : EnergyUsage.PeriodType.DAILY)
                .region(dto.getRegion() != null ? dto.getRegion() : company.getRegion())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : company.getCurrency())
                .dataSource("MANUAL")
                .build();

        // Calculate AI attribution
        BigDecimal aiKwh = attributionEngineService.calculateAiAttribution(
                dto.getTotalKwh(), company, department);
        energyUsage.setAiAttributedKwh(aiKwh);

        // Calculate cost
        BigDecimal cost = dto.getTotalKwh().multiply(company.getElectricityCostPerKwh());
        energyUsage.setCost(cost);

        EnergyUsage saved = energyUsageRepository.save(energyUsage);

        // Calculate and save carbon emissions
        carbonCalculationService.calculateAndSaveEmission(saved);

        return mapToDTO(saved);
    }

    /**
     * Import energy data from CSV file.
     * Expected CSV format: date,totalKwh,departmentName,region
     */
    public List<EnergyUsageDTO> importFromCsv(UUID companyId, MultipartFile file) throws IOException, CsvException {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));

        List<EnergyUsageDTO> imported = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();

            // Skip header row
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length < 2)
                    continue;

                try {
                    LocalDate date = LocalDate.parse(row[0].trim(), formatter);
                    BigDecimal totalKwh = new BigDecimal(row[1].trim());
                    String departmentName = row.length > 2 ? row[2].trim() : null;
                    String region = row.length > 3 ? row[3].trim() : company.getRegion();

                    // Find department by name
                    Department department = null;
                    if (departmentName != null && !departmentName.isEmpty()) {
                        department = departmentRepository.findByCompanyId(companyId).stream()
                                .filter(d -> d.getName().equalsIgnoreCase(departmentName))
                                .findFirst()
                                .orElse(null);
                    }

                    EnergyUsage energyUsage = EnergyUsage.builder()
                            .company(company)
                            .department(department)
                            .totalKwh(totalKwh)
                            .usageDate(date)
                            .periodType(EnergyUsage.PeriodType.DAILY)
                            .region(region)
                            .currency(company.getCurrency())
                            .dataSource("CSV_IMPORT")
                            .build();

                    // Calculate AI attribution
                    BigDecimal aiKwh = attributionEngineService.calculateAiAttribution(
                            totalKwh, company, department);
                    energyUsage.setAiAttributedKwh(aiKwh);

                    // Calculate cost
                    BigDecimal cost = totalKwh.multiply(company.getElectricityCostPerKwh());
                    energyUsage.setCost(cost);

                    EnergyUsage saved = energyUsageRepository.save(energyUsage);
                    carbonCalculationService.calculateAndSaveEmission(saved);

                    imported.add(mapToDTO(saved));
                } catch (Exception e) {
                    log.warn("Error parsing row {}: {}", i, e.getMessage());
                }
            }
        }

        log.info("Imported {} energy records from CSV for company {}", imported.size(), companyId);
        return imported;
    }

    /**
     * Get energy usage by date range.
     */
    @Transactional(readOnly = true)
    public List<EnergyUsageDTO> getEnergyUsageByDateRange(UUID companyId, LocalDate startDate, LocalDate endDate) {
        return energyUsageRepository.findByCompanyIdAndUsageDateBetween(companyId, startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all energy usage for a company.
     */
    @Transactional(readOnly = true)
    public List<EnergyUsageDTO> getAllEnergyUsage(UUID companyId) {
        return energyUsageRepository.findByCompanyIdOrderByDateDesc(companyId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get energy usage by region.
     */
    @Transactional(readOnly = true)
    public List<EnergyUsageDTO> getEnergyUsageByRegion(UUID companyId, String region) {
        return energyUsageRepository.findByCompanyIdAndRegion(companyId, region)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Delete an energy usage record.
     */
    public void deleteEnergyUsage(UUID id) {
        energyUsageRepository.deleteById(id);
    }

    private EnergyUsageDTO mapToDTO(EnergyUsage usage) {
        EnergyUsageDTO dto = EnergyUsageDTO.builder()
                .id(usage.getId())
                .companyId(usage.getCompany().getId())
                .departmentId(usage.getDepartment() != null ? usage.getDepartment().getId() : null)
                .departmentName(usage.getDepartment() != null ? usage.getDepartment().getName() : null)
                .totalKwh(usage.getTotalKwh())
                .aiAttributedKwh(usage.getAiAttributedKwh())
                .cost(usage.getCost())
                .currency(usage.getCurrency())
                .usageDate(usage.getUsageDate())
                .periodType(usage.getPeriodType())
                .region(usage.getRegion())
                .dataSource(usage.getDataSource())
                .build();

        // Include carbon data if available
        if (usage.getCarbonEmission() != null) {
            dto.setCo2eGrams(usage.getCarbonEmission().getCo2eGrams());
            dto.setCo2eKg(usage.getCarbonEmission().getCo2eKg());
        }

        return dto;
    }
}
