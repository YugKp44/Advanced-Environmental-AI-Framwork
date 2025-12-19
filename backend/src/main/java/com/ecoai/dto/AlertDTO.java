package com.ecoai.dto;

import com.ecoai.entity.AlertThreshold.MetricType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for active alerts and threshold configurations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDTO {
    private UUID id;
    private UUID companyId;
    private MetricType metricType;
    private String alertTitle;
    private String alertMessage;
    private BigDecimal thresholdValue;
    private BigDecimal currentValue;
    private BigDecimal percentOfThreshold;
    private String severity; // INFO, WARNING, CRITICAL
    private LocalDateTime triggeredAt;
    private Boolean active;
}
