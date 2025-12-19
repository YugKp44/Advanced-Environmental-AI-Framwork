package com.ecoai.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for Company entity - used for API requests/responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDTO {
    private UUID id;
    private String name;
    private String industry;
    private String country;
    private String region;
    private BigDecimal baseAiPercentage;
    private BigDecimal electricityCostPerKwh;
    private String currency;
}
