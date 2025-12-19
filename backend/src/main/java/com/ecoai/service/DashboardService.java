package com.ecoai.service;

import com.ecoai.dto.*;
import com.ecoai.entity.Company;
import com.ecoai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Dashboard Service.
 * Feature 8: Executive-Friendly Dashboard Data
 * 
 * Provides aggregated data for the executive dashboard.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DashboardService {

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final EnergyUsageRepository energyUsageRepository;
    private final CarbonEmissionRepository carbonEmissionRepository;
    private final AttributionEngineService attributionEngineService;
    private final AnalyticsService analyticsService;
    private final AlertsService alertsService;

    /**
     * Get executive summary with all key KPIs.
     */
    public DashboardSummaryDTO getExecutiveSummary(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));

        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysAgo = now.minusDays(30);
        LocalDate sixtyDaysAgo = now.minusDays(60);

        // Current period (last 30 days)
        BigDecimal totalEnergyKwh = energyUsageRepository.sumTotalKwhByCompanyAndDateRange(
                companyId, thirtyDaysAgo, now);
        BigDecimal aiEnergyKwh = energyUsageRepository.sumAiKwhByCompanyAndDateRange(
                companyId, thirtyDaysAgo, now);
        BigDecimal totalCo2eKg = carbonEmissionRepository.sumCo2eKgByCompanyAndDateRange(
                companyId, thirtyDaysAgo, now);

        // Previous period (30-60 days ago) for comparison
        BigDecimal prevTotalEnergyKwh = energyUsageRepository.sumTotalKwhByCompanyAndDateRange(
                companyId, sixtyDaysAgo, thirtyDaysAgo);
        BigDecimal prevAiEnergyKwh = energyUsageRepository.sumAiKwhByCompanyAndDateRange(
                companyId, sixtyDaysAgo, thirtyDaysAgo);
        BigDecimal prevCo2eKg = carbonEmissionRepository.sumCo2eKgByCompanyAndDateRange(
                companyId, sixtyDaysAgo, thirtyDaysAgo);

        // Handle nulls
        totalEnergyKwh = totalEnergyKwh != null ? totalEnergyKwh : BigDecimal.ZERO;
        aiEnergyKwh = aiEnergyKwh != null ? aiEnergyKwh : BigDecimal.ZERO;
        totalCo2eKg = totalCo2eKg != null ? totalCo2eKg : BigDecimal.ZERO;

        // Calculate AI percentage
        BigDecimal aiPercentage = totalEnergyKwh.compareTo(BigDecimal.ZERO) > 0
                ? aiEnergyKwh.divide(totalEnergyKwh, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Calculate costs
        BigDecimal totalCost = totalEnergyKwh.multiply(company.getElectricityCostPerKwh())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal aiCost = aiEnergyKwh.multiply(company.getElectricityCostPerKwh())
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate changes
        BigDecimal energyChange = calculatePercentChange(prevTotalEnergyKwh, totalEnergyKwh);
        BigDecimal carbonChange = calculatePercentChange(prevCo2eKg, totalCo2eKg);
        BigDecimal prevCost = prevTotalEnergyKwh != null
                ? prevTotalEnergyKwh.multiply(company.getElectricityCostPerKwh())
                : BigDecimal.ZERO;
        BigDecimal costChange = calculatePercentChange(prevCost, totalCost);

        // Get department count
        int departmentCount = departmentRepository.findByCompanyId(companyId).size();
        int dataPointCount = energyUsageRepository.findByCompanyIdAndUsageDateBetween(
                companyId, thirtyDaysAgo, now).size();

        return DashboardSummaryDTO.builder()
                .totalEnergyKwh(totalEnergyKwh)
                .aiEnergyKwh(aiEnergyKwh)
                .aiPercentage(aiPercentage)
                .totalCo2eKg(totalCo2eKg)
                .aiCo2eKg(totalCo2eKg) // All carbon is from AI in this model
                .totalCost(totalCost)
                .aiCost(aiCost)
                .currency(company.getCurrency())
                .energyChangePercent(energyChange)
                .carbonChangePercent(carbonChange)
                .costChangePercent(costChange)
                .periodType("LAST_30_DAYS")
                .departmentCount(departmentCount)
                .dataPointCount(dataPointCount)
                .build();
    }

    /**
     * Get department breakdown for charts.
     */
    public List<DepartmentBreakdownDTO> getDepartmentBreakdown(UUID companyId) {
        return attributionEngineService.getAttributionByDepartment(companyId);
    }

    /**
     * Get all dashboard data in one call.
     */
    public Map<String, Object> getFullDashboard(UUID companyId) {
        Map<String, Object> dashboard = new HashMap<>();

        // Summary KPIs
        dashboard.put("summary", getExecutiveSummary(companyId));

        // Department breakdown
        dashboard.put("departmentBreakdown", getDepartmentBreakdown(companyId));

        // Historical trends (6 months)
        dashboard.put("trends", analyticsService.getHistoricalTrends(companyId, 6));

        // Forecasts (3 months)
        dashboard.put("forecasts", analyticsService.forecastUsage(companyId, 3));

        // Active alerts
        dashboard.put("alerts", alertsService.checkThresholds(companyId));

        // Optimization insights
        dashboard.put("insights", alertsService.getOptimizationSuggestions(companyId));

        // Region breakdown
        dashboard.put("regionBreakdown", getRegionBreakdown(companyId));

        return dashboard;
    }

    /**
     * Get energy/carbon breakdown by region.
     */
    public List<Map<String, Object>> getRegionBreakdown(UUID companyId) {
        List<Object[]> regionData = energyUsageRepository.sumKwhByRegion(companyId);
        List<Map<String, Object>> breakdown = new ArrayList<>();

        for (Object[] row : regionData) {
            Map<String, Object> regionInfo = new HashMap<>();
            regionInfo.put("region", row[0]);
            regionInfo.put("totalKwh", row[1] != null ? row[1] : BigDecimal.ZERO);
            regionInfo.put("aiKwh", row[2] != null ? row[2] : BigDecimal.ZERO);
            breakdown.add(regionInfo);
        }

        return breakdown;
    }

    private BigDecimal calculatePercentChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue == null || oldValue.compareTo(BigDecimal.ZERO) == 0) {
            return newValue != null && newValue.compareTo(BigDecimal.ZERO) > 0
                    ? new BigDecimal("100")
                    : BigDecimal.ZERO;
        }
        if (newValue == null) {
            return new BigDecimal("-100");
        }
        return newValue.subtract(oldValue)
                .divide(oldValue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(1, RoundingMode.HALF_UP);
    }
}
