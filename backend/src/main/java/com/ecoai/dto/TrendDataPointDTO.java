package com.ecoai.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for trend data points (for line charts).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendDataPointDTO {
    private LocalDate date;
    private String period; // "Jan 2024", "Week 1", etc.
    private BigDecimal totalEnergyKwh;
    private BigDecimal aiEnergyKwh;
    private BigDecimal co2eKg;
    private BigDecimal cost;
}
