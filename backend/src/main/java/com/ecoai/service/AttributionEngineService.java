package com.ecoai.service;

import com.ecoai.dto.DepartmentBreakdownDTO;
import com.ecoai.entity.Company;
import com.ecoai.entity.Department;
import com.ecoai.repository.DepartmentRepository;
import com.ecoai.repository.EnergyUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI Energy Attribution Engine Service.
 * Feature 2: Core Innovation - Calculates AI energy usage from total
 * consumption.
 * 
 * Formula: AI_kWh = Total_kWh × Company_AI_Percentage × Department_Weight
 * 
 * This is transparent, formula-based, and explainable - not guesswork.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AttributionEngineService {

    private final DepartmentRepository departmentRepository;
    private final EnergyUsageRepository energyUsageRepository;

    /**
     * Calculate AI-attributed energy usage.
     * 
     * @param totalKwh   Total energy consumption in kWh
     * @param company    Company with base AI percentage
     * @param department Department with AI usage weight (optional)
     * @return AI-attributed energy in kWh
     */
    public BigDecimal calculateAiAttribution(BigDecimal totalKwh, Company company, Department department) {
        if (totalKwh == null || company == null) {
            return BigDecimal.ZERO;
        }

        // Company's base AI percentage (e.g., 0.30 = 30%)
        BigDecimal companyAiPercentage = company.getBaseAiPercentage();
        if (companyAiPercentage == null) {
            companyAiPercentage = new BigDecimal("0.30"); // Default 30%
        }

        // Department's AI usage weight (e.g., 0.8 for ML team, 0.1 for HR)
        BigDecimal departmentWeight = BigDecimal.ONE; // Default: 100%
        if (department != null && department.getAiUsageWeight() != null) {
            departmentWeight = department.getAiUsageWeight();
        }

        // Formula: AI_kWh = Total_kWh × Company_AI% × Dept_Weight
        BigDecimal aiKwh = totalKwh
                .multiply(companyAiPercentage)
                .multiply(departmentWeight)
                .setScale(4, RoundingMode.HALF_UP);

        log.debug("Attribution: {} kWh × {} × {} = {} AI kWh",
                totalKwh, companyAiPercentage, departmentWeight, aiKwh);

        return aiKwh;
    }

    /**
     * Get attribution breakdown by department.
     */
    @Transactional(readOnly = true)
    public List<DepartmentBreakdownDTO> getAttributionByDepartment(UUID companyId) {
        List<Department> departments = departmentRepository.findByCompanyId(companyId);
        List<Object[]> aiKwhByDept = energyUsageRepository.sumAiKwhByDepartment(companyId);

        // Map department ID to AI kWh
        Map<UUID, BigDecimal> deptAiKwh = aiKwhByDept.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO));

        // Calculate total AI kWh for percentage calculation
        BigDecimal totalAiKwh = deptAiKwh.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DepartmentBreakdownDTO> breakdown = new ArrayList<>();
        for (Department dept : departments) {
            BigDecimal aiKwh = deptAiKwh.getOrDefault(dept.getId(), BigDecimal.ZERO);
            BigDecimal percentage = totalAiKwh.compareTo(BigDecimal.ZERO) > 0
                    ? aiKwh.divide(totalAiKwh, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;

            breakdown.add(DepartmentBreakdownDTO.builder()
                    .departmentId(dept.getId())
                    .departmentName(dept.getName())
                    .team(dept.getTeam())
                    .aiEnergyKwh(aiKwh)
                    .percentage(percentage)
                    .aiUsageWeight(dept.getAiUsageWeight())
                    .build());
        }

        // Sort by AI energy descending
        breakdown.sort((a, b) -> b.getAiEnergyKwh().compareTo(a.getAiEnergyKwh()));

        return breakdown;
    }

    /**
     * Explain the attribution calculation for a specific record.
     */
    public String explainAttribution(BigDecimal totalKwh, BigDecimal companyAiPercent,
            BigDecimal deptWeight, BigDecimal result) {
        return String.format(
                "Attribution Calculation:\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "Total Energy:      %,.2f kWh\n" +
                        "Company AI %%:      %,.1f%%\n" +
                        "Department Weight: %,.1f%%\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "Formula: %.2f × %.4f × %.4f\n" +
                        "AI Energy:         %,.2f kWh\n",
                totalKwh,
                companyAiPercent.multiply(new BigDecimal("100")),
                deptWeight.multiply(new BigDecimal("100")),
                totalKwh, companyAiPercent, deptWeight,
                result);
    }
}
