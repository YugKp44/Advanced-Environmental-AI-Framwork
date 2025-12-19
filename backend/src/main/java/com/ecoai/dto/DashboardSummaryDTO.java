package com.ecoai.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO for executive dashboard summary - KPIs at a glance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {
    // Energy metrics
    private BigDecimal totalEnergyKwh;
    private BigDecimal aiEnergyKwh;
    private BigDecimal aiPercentage;

    // Carbon metrics
    private BigDecimal totalCo2eKg;
    private BigDecimal aiCo2eKg;

    // Cost metrics
    private BigDecimal totalCost;
    private BigDecimal aiCost;
    private String currency;

    // Trends (compared to previous period)
    private BigDecimal energyChangePercent;
    private BigDecimal carbonChangePercent;
    private BigDecimal costChangePercent;

    // Period info
    private String periodType; // LAST_30_DAYS, LAST_MONTH, etc.
    private Integer departmentCount;
    private Integer dataPointCount;
}
