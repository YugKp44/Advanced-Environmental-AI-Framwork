package com.ecoai.service;

import com.ecoai.dto.SimulationDTO;
import com.ecoai.entity.Company;
import com.ecoai.entity.SimulationScenario;
import com.ecoai.entity.SimulationScenario.SimulationType;
import com.ecoai.repository.CompanyRepository;
import com.ecoai.repository.EnergyUsageRepository;
import com.ecoai.repository.SimulationScenarioRepository;
import com.ecoai.util.CarbonIntensityDefaults;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * What-If Simulation Engine Service.
 * Feature 5: High-Impact Simulation Feature
 * 
 * Allows companies to explore scenarios like:
 * - "What if AI usage grows 20%?"
 * - "What if workloads move to EU?"
 * - "What if we improve efficiency by 15%?"
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SimulationEngineService {

    private final SimulationScenarioRepository scenarioRepository;
    private final EnergyUsageRepository energyUsageRepository;
    private final CompanyRepository companyRepository;
    private final CarbonCalculationService carbonCalculationService;
    private final ObjectMapper objectMapper;

    /**
     * Simulate AI usage growth scenario.
     * "What if AI usage grows by X%?"
     */
    public SimulationDTO simulateGrowth(UUID companyId, BigDecimal growthPercent, Integer monthsAhead) {
        Company company = getCompany(companyId);

        // Get baseline values (last 30 days average extrapolated)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        BigDecimal baselineAiKwh = energyUsageRepository.sumAiKwhByCompanyAndDateRange(
                companyId, startDate, endDate);
        if (baselineAiKwh == null)
            baselineAiKwh = new BigDecimal("1000"); // Default for demo

        // Project for specified months
        BigDecimal monthlyBaseline = baselineAiKwh; // Assuming 30-day data
        BigDecimal projectedMonths = new BigDecimal(monthsAhead != null ? monthsAhead : 12);

        // Apply growth: baseline × (1 + growth%) ^ months
        BigDecimal growthMultiplier = BigDecimal.ONE.add(
                growthPercent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        BigDecimal projectedAiKwh = monthlyBaseline
                .multiply(growthMultiplier.pow(monthsAhead != null ? monthsAhead : 12))
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate carbon impact
        BigDecimal carbonIntensity = carbonCalculationService.getEffectiveCarbonIntensity(
                companyId, company.getRegion());
        BigDecimal baselineCo2eKg = baselineAiKwh.multiply(carbonIntensity)
                .divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
        BigDecimal projectedCo2eKg = projectedAiKwh.multiply(carbonIntensity)
                .divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);

        // Calculate cost impact
        BigDecimal baselineCost = baselineAiKwh.multiply(company.getElectricityCostPerKwh())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal projectedCost = projectedAiKwh.multiply(company.getElectricityCostPerKwh())
                .setScale(2, RoundingMode.HALF_UP);

        return buildSimulationDTO(
                SimulationType.GROWTH,
                "AI Growth Simulation (" + growthPercent + "% over " + monthsAhead + " months)",
                baselineAiKwh, baselineCo2eKg, baselineCost,
                projectedAiKwh, projectedCo2eKg, projectedCost,
                growthPercent, null, null, null, monthsAhead);
    }

    /**
     * Simulate region change scenario.
     * "What if workloads move from region A to region B?"
     */
    public SimulationDTO simulateRegionChange(UUID companyId, String fromRegion, String toRegion) {
        Company company = getCompany(companyId);

        // Get current AI usage
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        BigDecimal aiKwh = energyUsageRepository.sumAiKwhByCompanyAndDateRange(
                companyId, startDate, endDate);
        if (aiKwh == null)
            aiKwh = new BigDecimal("1000");

        // Get carbon intensities for both regions
        BigDecimal fromIntensity = CarbonIntensityDefaults.getIntensity(fromRegion);
        BigDecimal toIntensity = CarbonIntensityDefaults.getIntensity(toRegion);

        // Calculate carbon for both scenarios
        BigDecimal fromCo2eKg = aiKwh.multiply(fromIntensity)
                .divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
        BigDecimal toCo2eKg = aiKwh.multiply(toIntensity)
                .divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);

        // Cost remains same (energy consumption unchanged)
        BigDecimal cost = aiKwh.multiply(company.getElectricityCostPerKwh())
                .setScale(2, RoundingMode.HALF_UP);

        String description = String.format("Region Change: %s → %s (Carbon intensity: %.0f → %.0f gCO₂/kWh)",
                CarbonIntensityDefaults.getRegionName(fromRegion),
                CarbonIntensityDefaults.getRegionName(toRegion),
                fromIntensity, toIntensity);

        return buildSimulationDTO(
                SimulationType.REGION_CHANGE,
                description,
                aiKwh, fromCo2eKg, cost,
                aiKwh, toCo2eKg, cost,
                null, fromRegion, toRegion, null, null);
    }

    /**
     * Simulate efficiency improvement scenario.
     * "What if we improve AI efficiency by X%?"
     */
    public SimulationDTO simulateEfficiency(UUID companyId, BigDecimal efficiencyPercent) {
        Company company = getCompany(companyId);

        // Get current AI usage
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        BigDecimal baselineAiKwh = energyUsageRepository.sumAiKwhByCompanyAndDateRange(
                companyId, startDate, endDate);
        if (baselineAiKwh == null)
            baselineAiKwh = new BigDecimal("1000");

        // Apply efficiency gain (reduces energy consumption)
        BigDecimal efficiencyMultiplier = BigDecimal.ONE.subtract(
                efficiencyPercent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        BigDecimal projectedAiKwh = baselineAiKwh.multiply(efficiencyMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate carbon impact
        BigDecimal carbonIntensity = carbonCalculationService.getEffectiveCarbonIntensity(
                companyId, company.getRegion());
        BigDecimal baselineCo2eKg = baselineAiKwh.multiply(carbonIntensity)
                .divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
        BigDecimal projectedCo2eKg = projectedAiKwh.multiply(carbonIntensity)
                .divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);

        // Calculate cost impact
        BigDecimal baselineCost = baselineAiKwh.multiply(company.getElectricityCostPerKwh())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal projectedCost = projectedAiKwh.multiply(company.getElectricityCostPerKwh())
                .setScale(2, RoundingMode.HALF_UP);

        return buildSimulationDTO(
                SimulationType.EFFICIENCY,
                "Efficiency Improvement Simulation (" + efficiencyPercent + "% reduction)",
                baselineAiKwh, baselineCo2eKg, baselineCost,
                projectedAiKwh, projectedCo2eKg, projectedCost,
                null, null, null, efficiencyPercent, null);
    }

    /**
     * Save a simulation scenario for future reference.
     */
    public SimulationDTO saveScenario(UUID companyId, SimulationDTO dto) {
        Company company = getCompany(companyId);

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("growthPercent", dto.getGrowthPercent());
            params.put("fromRegion", dto.getFromRegion());
            params.put("toRegion", dto.getToRegion());
            params.put("efficiencyPercent", dto.getEfficiencyPercent());
            params.put("monthsAhead", dto.getMonthsAhead());

            Map<String, Object> results = new HashMap<>();
            results.put("projectedAiKwh", dto.getProjectedAiKwh());
            results.put("projectedCo2eKg", dto.getProjectedCo2eKg());
            results.put("projectedCost", dto.getProjectedCost());

            Map<String, Object> baseline = new HashMap<>();
            baseline.put("aiKwh", dto.getBaselineAiKwh());
            baseline.put("co2eKg", dto.getBaselineCo2eKg());
            baseline.put("cost", dto.getBaselineCost());

            SimulationScenario scenario = SimulationScenario.builder()
                    .company(company)
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .simulationType(dto.getSimulationType())
                    .parameters(objectMapper.writeValueAsString(params))
                    .results(objectMapper.writeValueAsString(results))
                    .baselineValues(objectMapper.writeValueAsString(baseline))
                    .build();

            SimulationScenario saved = scenarioRepository.save(scenario);
            dto.setId(saved.getId());
            dto.setCompanyId(companyId);

            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save scenario", e);
        }
    }

    /**
     * Get all saved scenarios for a company.
     */
    @Transactional(readOnly = true)
    public List<SimulationDTO> getSavedScenarios(UUID companyId) {
        return scenarioRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private Company getCompany(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));
    }

    private SimulationDTO buildSimulationDTO(
            SimulationType type, String description,
            BigDecimal baselineKwh, BigDecimal baselineCo2e, BigDecimal baselineCost,
            BigDecimal projectedKwh, BigDecimal projectedCo2e, BigDecimal projectedCost,
            BigDecimal growthPercent, String fromRegion, String toRegion,
            BigDecimal efficiencyPercent, Integer monthsAhead) {

        BigDecimal energyDelta = projectedKwh.subtract(baselineKwh);
        BigDecimal carbonDelta = projectedCo2e.subtract(baselineCo2e);
        BigDecimal costDelta = projectedCost.subtract(baselineCost);
        BigDecimal percentChange = baselineKwh.compareTo(BigDecimal.ZERO) > 0
                ? energyDelta.divide(baselineKwh, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return SimulationDTO.builder()
                .simulationType(type)
                .name(type.name() + " Simulation")
                .description(description)
                .growthPercent(growthPercent)
                .fromRegion(fromRegion)
                .toRegion(toRegion)
                .efficiencyPercent(efficiencyPercent)
                .monthsAhead(monthsAhead)
                .baselineAiKwh(baselineKwh)
                .baselineCo2eKg(baselineCo2e)
                .baselineCost(baselineCost)
                .projectedAiKwh(projectedKwh)
                .projectedCo2eKg(projectedCo2e)
                .projectedCost(projectedCost)
                .energyDeltaKwh(energyDelta)
                .carbonDeltaKg(carbonDelta)
                .costDelta(costDelta)
                .percentChange(percentChange)
                .build();
    }

    private SimulationDTO mapToDTO(SimulationScenario scenario) {
        return SimulationDTO.builder()
                .id(scenario.getId())
                .companyId(scenario.getCompany().getId())
                .name(scenario.getName())
                .description(scenario.getDescription())
                .simulationType(scenario.getSimulationType())
                .build();
    }
}
