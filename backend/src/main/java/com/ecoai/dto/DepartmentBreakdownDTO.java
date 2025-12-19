package com.ecoai.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for department-wise breakdown in charts.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentBreakdownDTO {
    private UUID departmentId;
    private String departmentName;
    private String team;
    private BigDecimal aiEnergyKwh;
    private BigDecimal co2eKg;
    private BigDecimal cost;
    private BigDecimal percentage; // % of total AI energy
    private BigDecimal aiUsageWeight;
}
