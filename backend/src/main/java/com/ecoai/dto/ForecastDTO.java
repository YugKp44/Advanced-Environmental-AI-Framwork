package com.ecoai.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for forecast data points.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForecastDTO {
    private LocalDate date;
    private String period;
    private BigDecimal predictedAiKwh;
    private BigDecimal predictedCo2eKg;
    private BigDecimal predictedCost;
    private BigDecimal confidenceLow;
    private BigDecimal confidenceHigh;
    private Boolean isProjection; // True = forecasted, False = actual
}
