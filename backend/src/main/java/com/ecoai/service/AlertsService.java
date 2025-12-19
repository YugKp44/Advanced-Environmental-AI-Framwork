package com.ecoai.service;

import com.ecoai.dto.AlertDTO;
import com.ecoai.dto.InsightDTO;
import com.ecoai.entity.AlertThreshold;
import com.ecoai.entity.AlertThreshold.MetricType;
import com.ecoai.entity.Company;
import com.ecoai.repository.AlertThresholdRepository;
import com.ecoai.repository.CarbonEmissionRepository;
import com.ecoai.repository.CompanyRepository;
import com.ecoai.repository.EnergyUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Alerts & Insights Service.
 * Feature 7: Actionable Insights and Threshold-based Alerts
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AlertsService {

    private final AlertThresholdRepository alertThresholdRepository;
    private final EnergyUsageRepository energyUsageRepository;
    private final CarbonEmissionRepository carbonEmissionRepository;
    private final CompanyRepository companyRepository;

    /**
     * Configure a threshold for monitoring.
     */
    public AlertDTO configureThreshold(UUID companyId, MetricType metricType,
            BigDecimal thresholdValue, String message) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));

        // Check if threshold already exists
        List<AlertThreshold> existing = alertThresholdRepository
                .findByCompanyIdAndMetricType(companyId, metricType);

        AlertThreshold threshold;
        if (!existing.isEmpty()) {
            threshold = existing.get(0);
            threshold.setThresholdValue(thresholdValue);
            threshold.setAlertMessage(message);
        } else {
            threshold = AlertThreshold.builder()
                    .company(company)
                    .metricType(metricType)
                    .thresholdValue(thresholdValue)
                    .alertMessage(message)
                    .active(true)
                    .build();
        }

        AlertThreshold saved = alertThresholdRepository.save(threshold);
        return mapToDTO(saved, null, "INFO");
    }

    /**
     * Check all thresholds and return triggered alerts.
     */
    @Transactional(readOnly = true)
    public List<AlertDTO> checkThresholds(UUID companyId) {
        List<AlertThreshold> thresholds = alertThresholdRepository
                .findByCompanyIdAndActive(companyId, true);

        List<AlertDTO> triggeredAlerts = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);

        for (AlertThreshold threshold : thresholds) {
            BigDecimal currentValue = getCurrentMetricValue(companyId, threshold.getMetricType(),
                    monthStart, now);

            if (currentValue == null)
                continue;

            BigDecimal percentOfThreshold = currentValue
                    .divide(threshold.getThresholdValue(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));

            boolean isTriggered = isThresholdTriggered(threshold, currentValue);
            String severity = determineSeverity(percentOfThreshold);

            if (percentOfThreshold.compareTo(new BigDecimal("80")) >= 0 || isTriggered) {
                triggeredAlerts.add(AlertDTO.builder()
                        .id(threshold.getId())
                        .companyId(companyId)
                        .metricType(threshold.getMetricType())
                        .alertTitle(getAlertTitle(threshold.getMetricType(), isTriggered))
                        .alertMessage(threshold.getAlertMessage() != null
                                ? threshold.getAlertMessage()
                                : getDefaultMessage(threshold.getMetricType(), percentOfThreshold))
                        .thresholdValue(threshold.getThresholdValue())
                        .currentValue(currentValue)
                        .percentOfThreshold(percentOfThreshold)
                        .severity(severity)
                        .triggeredAt(LocalDateTime.now())
                        .active(true)
                        .build());
            }
        }

        // Sort by severity
        triggeredAlerts.sort((a, b) -> getSeverityOrder(b.getSeverity()) - getSeverityOrder(a.getSeverity()));

        return triggeredAlerts;
    }

    /**
     * Get optimization suggestions based on current data.
     */
    @Transactional(readOnly = true)
    public List<InsightDTO> getOptimizationSuggestions(UUID companyId) {
        List<InsightDTO> insights = new ArrayList<>();
        Company company = companyRepository.findById(companyId).orElse(null);
        if (company == null)
            return insights;

        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);

        // Get current metrics
        BigDecimal monthlyAiKwh = energyUsageRepository.sumAiKwhByCompanyAndDateRange(
                companyId, monthStart, now);

        // 1. Region optimization suggestion
        if (company.getRegion() != null &&
                (company.getRegion().equals("IN") || company.getRegion().equals("AU") ||
                        company.getRegion().equals("CN"))) {
            insights.add(InsightDTO.builder()
                    .category("REGION")
                    .title("Consider Greener Regions")
                    .description("Your workloads are running in a high carbon-intensity region. " +
                            "Moving to EU or Nordic regions could significantly reduce emissions.")
                    .impact("Up to 60% carbon reduction possible")
                    .priority("HIGH")
                    .actionable("Evaluate moving non-latency-critical workloads to EU-NORTH or NO regions")
                    .build());
        }

        // 2. Batching suggestion
        insights.add(InsightDTO.builder()
                .category("BATCHING")
                .title("Batch AI Workloads")
                .description("Running AI tasks in batches during off-peak hours can improve " +
                        "efficiency and potentially reduce costs.")
                .impact("10-20% cost savings possible")
                .priority("MEDIUM")
                .actionable("Schedule batch inference jobs during night hours (10 PM - 6 AM)")
                .build());

        // 3. Efficiency improvement suggestion
        insights.add(InsightDTO.builder()
                .category("EFFICIENCY")
                .title("Model Optimization")
                .description("Optimizing AI models through quantization, pruning, or distillation " +
                        "can reduce energy consumption while maintaining accuracy.")
                .impact("15-30% energy reduction per inference")
                .priority("MEDIUM")
                .actionable("Review top energy-consuming models for optimization opportunities")
                .build());

        // 4. Department-specific suggestion
        if (monthlyAiKwh != null && monthlyAiKwh.compareTo(new BigDecimal("5000")) > 0) {
            insights.add(InsightDTO.builder()
                    .category("SCHEDULING")
                    .title("Spread Peak Loads")
                    .description("High AI energy usage detected. Distributing workloads more evenly " +
                            "across time can reduce peak demand charges.")
                    .impact("5-10% cost reduction on peak charges")
                    .priority("LOW")
                    .actionable("Implement workload queue with rate limiting")
                    .build());
        }

        // 5. Carbon budget suggestion
        insights.add(InsightDTO.builder()
                .category("CARBON_BUDGET")
                .title("Set Carbon Budgets")
                .description("Establishing monthly carbon budgets per department helps track " +
                        "and manage environmental impact systematically.")
                .impact("Improved ESG reporting and accountability")
                .priority("MEDIUM")
                .actionable("Define monthly CO₂e limits for each department")
                .build());

        return insights;
    }

    /**
     * Get all configured thresholds for a company.
     */
    @Transactional(readOnly = true)
    public List<AlertDTO> getAllThresholds(UUID companyId) {
        return alertThresholdRepository.findByCompanyId(companyId).stream()
                .map(t -> mapToDTO(t, null, "INFO"))
                .toList();
    }

    private BigDecimal getCurrentMetricValue(UUID companyId, MetricType metricType,
            LocalDate startDate, LocalDate endDate) {
        return switch (metricType) {
            case AI_USAGE_KWH -> energyUsageRepository.sumAiKwhByCompanyAndDateRange(
                    companyId, startDate, endDate);
            case TOTAL_ENERGY_KWH -> energyUsageRepository.sumTotalKwhByCompanyAndDateRange(
                    companyId, startDate, endDate);
            case CARBON_EMISSION_KG -> carbonEmissionRepository.sumCo2eKgByCompanyAndDateRange(
                    companyId, startDate, endDate);
            default -> null;
        };
    }

    private boolean isThresholdTriggered(AlertThreshold threshold, BigDecimal currentValue) {
        int comparison = currentValue.compareTo(threshold.getThresholdValue());
        return switch (threshold.getOperator()) {
            case GREATER_THAN -> comparison > 0;
            case GREATER_THAN_OR_EQUALS -> comparison >= 0;
            case LESS_THAN -> comparison < 0;
            case LESS_THAN_OR_EQUALS -> comparison <= 0;
            case EQUALS -> comparison == 0;
        };
    }

    private String determineSeverity(BigDecimal percentOfThreshold) {
        if (percentOfThreshold.compareTo(new BigDecimal("100")) >= 0) {
            return "CRITICAL";
        } else if (percentOfThreshold.compareTo(new BigDecimal("90")) >= 0) {
            return "WARNING";
        } else {
            return "INFO";
        }
    }

    private int getSeverityOrder(String severity) {
        return switch (severity) {
            case "CRITICAL" -> 3;
            case "WARNING" -> 2;
            case "INFO" -> 1;
            default -> 0;
        };
    }

    private String getAlertTitle(MetricType metricType, boolean isTriggered) {
        String status = isTriggered ? "Threshold Exceeded" : "Approaching Threshold";
        return switch (metricType) {
            case AI_USAGE_KWH -> "AI Energy Usage " + status;
            case TOTAL_ENERGY_KWH -> "Total Energy " + status;
            case CARBON_EMISSION_KG -> "Carbon Emission " + status;
            case MONTHLY_COST -> "Monthly Cost " + status;
            default -> "Alert: " + status;
        };
    }

    private String getDefaultMessage(MetricType metricType, BigDecimal percent) {
        return String.format("Current %s is at %.1f%% of the configured threshold.",
                metricType.name().replace("_", " ").toLowerCase(), percent);
    }

    private AlertDTO mapToDTO(AlertThreshold threshold, BigDecimal currentValue, String severity) {
        return AlertDTO.builder()
                .id(threshold.getId())
                .companyId(threshold.getCompany().getId())
                .metricType(threshold.getMetricType())
                .thresholdValue(threshold.getThresholdValue())
                .currentValue(currentValue)
                .alertMessage(threshold.getAlertMessage())
                .active(threshold.getActive())
                .severity(severity)
                .build();
    }
}
