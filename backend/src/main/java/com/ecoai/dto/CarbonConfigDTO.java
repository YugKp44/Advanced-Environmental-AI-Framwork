package com.ecoai.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO for carbon configuration and default intensities.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarbonConfigDTO {
    private String region;
    private String regionName;
    private BigDecimal carbonIntensity; // gCO2/kWh
    private String unit;
    private Integer validYear;
    private Boolean isDefault; // True if using default value
}
