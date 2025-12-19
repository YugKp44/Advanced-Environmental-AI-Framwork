package com.ecoai.service;

import com.ecoai.dto.CarbonConfigDTO;
import com.ecoai.entity.CarbonConfig;
import com.ecoai.entity.CarbonEmission;
import com.ecoai.entity.Company;
import com.ecoai.entity.EnergyUsage;
import com.ecoai.repository.CarbonConfigRepository;
import com.ecoai.repository.CarbonEmissionRepository;
import com.ecoai.repository.CompanyRepository;
import com.ecoai.util.CarbonIntensityDefaults;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Carbon Emission Calculation Service.
 * Feature 3: ESG-Ready Carbon Tracking
 * 
 * Formula: CO2e_grams = AI_kWh × Carbon_Intensity_Factor
 * 
 * Region-based carbon intensities allow accurate ESG reporting.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CarbonCalculationService {

    private final CarbonConfigRepository carbonConfigRepository;
    private final CarbonEmissionRepository carbonEmissionRepository;
    private final CompanyRepository companyRepository;

    /**
     * Calculate and save carbon emission for an energy usage record.
     */
    public CarbonEmission calculateAndSaveEmission(EnergyUsage energyUsage) {
        if (energyUsage.getAiAttributedKwh() == null) {
            return null;
        }

        // Get carbon intensity for the region
        String region = energyUsage.getRegion() != null
                ? energyUsage.getRegion()
                : energyUsage.getCompany().getRegion();

        BigDecimal carbonIntensity = getEffectiveCarbonIntensity(
                energyUsage.getCompany().getId(), region);

        // Calculate CO2e: AI_kWh × carbonIntensity (gCO2/kWh)
        BigDecimal co2eGrams = energyUsage.getAiAttributedKwh()
                .multiply(carbonIntensity)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal co2eKg = co2eGrams.divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP);

        CarbonEmission emission = CarbonEmission.builder()
                .energyUsage(energyUsage)
                .co2eGrams(co2eGrams)
                .co2eKg(co2eKg)
                .carbonIntensityUsed(carbonIntensity)
                .regionUsed(region)
                .build();

        return carbonEmissionRepository.save(emission);
    }

    /**
     * Get effective carbon intensity for a region.
     * First checks company-specific config, then falls back to defaults.
     */
    public BigDecimal getEffectiveCarbonIntensity(UUID companyId, String region) {
        // Try company-specific configuration first
        return carbonConfigRepository.findByCompanyIdAndRegion(companyId, region)
                .map(CarbonConfig::getCarbonIntensity)
                .orElseGet(() -> CarbonIntensityDefaults.getIntensity(region));
    }

    /**
     * Configure custom carbon intensity for a company and region.
     */
    public CarbonConfigDTO configureCarbonIntensity(UUID companyId, String region, BigDecimal intensity) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));

        CarbonConfig config = carbonConfigRepository.findByCompanyIdAndRegion(companyId, region)
                .orElse(CarbonConfig.builder()
                        .company(company)
                        .region(region)
                        .build());

        config.setCarbonIntensity(intensity);
        CarbonConfig saved = carbonConfigRepository.save(config);

        return mapToDTO(saved, false);
    }

    /**
     * Get all carbon configurations for a company.
     */
    @Transactional(readOnly = true)
    public List<CarbonConfigDTO> getCompanyCarbonConfigs(UUID companyId) {
        return carbonConfigRepository.findByCompanyId(companyId).stream()
                .map(c -> mapToDTO(c, false))
                .collect(Collectors.toList());
    }

    /**
     * Get all default carbon intensities.
     */
    public List<CarbonConfigDTO> getDefaultCarbonIntensities() {
        Map<String, CarbonIntensityDefaults.RegionData> defaults = CarbonIntensityDefaults.getAllDefaults();

        return defaults.entrySet().stream()
                .map(entry -> CarbonConfigDTO.builder()
                        .region(entry.getKey())
                        .regionName(entry.getValue().name)
                        .carbonIntensity(entry.getValue().intensity)
                        .unit("gCO2/kWh")
                        .validYear(2024)
                        .isDefault(true)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Recalculate all carbon emissions for a company (useful after config changes).
     */
    public int recalculateEmissions(UUID companyId) {
        // This would be implemented to recalculate all emissions
        // after a carbon intensity configuration change
        log.info("Recalculating emissions for company: {}", companyId);
        return 0; // Return count of updated records
    }

    private CarbonConfigDTO mapToDTO(CarbonConfig config, boolean isDefault) {
        return CarbonConfigDTO.builder()
                .region(config.getRegion())
                .regionName(CarbonIntensityDefaults.getRegionName(config.getRegion()))
                .carbonIntensity(config.getCarbonIntensity())
                .unit(config.getUnit())
                .validYear(config.getValidYear())
                .isDefault(isDefault)
                .build();
    }
}
