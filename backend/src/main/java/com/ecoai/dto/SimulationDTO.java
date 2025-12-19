package com.ecoai.dto;

import com.ecoai.entity.SimulationScenario.SimulationType;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for simulation requests and results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationDTO {
    private UUID id;
    private UUID companyId;
    private String name;
    private String description;
    private SimulationType simulationType;

    // Input parameters
    private BigDecimal growthPercent; // For GROWTH simulation
    private String fromRegion; // For REGION_CHANGE simulation
    private String toRegion; // For REGION_CHANGE simulation
    private BigDecimal efficiencyPercent; // For EFFICIENCY simulation
    private Integer monthsAhead; // Projection period

    // Baseline values
    private BigDecimal baselineAiKwh;
    private BigDecimal baselineCo2eKg;
    private BigDecimal baselineCost;

    // Projected results
    private BigDecimal projectedAiKwh;
    private BigDecimal projectedCo2eKg;
    private BigDecimal projectedCost;

    // Impact analysis
    private BigDecimal energyDeltaKwh;
    private BigDecimal carbonDeltaKg;
    private BigDecimal costDelta;
    private BigDecimal percentChange;
}
