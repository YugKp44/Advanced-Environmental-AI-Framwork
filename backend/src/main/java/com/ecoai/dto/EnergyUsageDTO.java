package com.ecoai.dto;

import com.ecoai.entity.EnergyUsage.PeriodType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for Energy Usage data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnergyUsageDTO {
    private UUID id;
    private UUID companyId;
    private UUID departmentId;
    private String departmentName;
    private BigDecimal totalKwh;
    private BigDecimal aiAttributedKwh;
    private BigDecimal cost;
    private String currency;
    private LocalDate usageDate;
    private PeriodType periodType;
    private String region;
    private String dataSource;

    // Carbon emission data (if calculated)
    private BigDecimal co2eGrams;
    private BigDecimal co2eKg;
}
