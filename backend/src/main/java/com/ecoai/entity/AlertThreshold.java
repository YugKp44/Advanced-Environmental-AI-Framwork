package com.ecoai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AlertThreshold entity - configurable thresholds for monitoring and alerts.
 * Triggers alerts when metrics exceed configured thresholds.
 */
@Entity
@Table(name = "alert_thresholds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /**
     * Type of metric to monitor.
     * Examples: AI_USAGE_KWH, CARBON_EMISSION_KG, MONTHLY_COST, AI_PERCENTAGE
     */
    @Column(name = "metric_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private MetricType metricType;

    /**
     * Threshold value that triggers an alert when exceeded.
     */
    @Column(name = "threshold_value", precision = 15, scale = 4, nullable = false)
    private BigDecimal thresholdValue;

    /**
     * Threshold operator: GREATER_THAN, LESS_THAN, EQUALS
     */
    @Column(name = "operator", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ThresholdOperator operator = ThresholdOperator.GREATER_THAN;

    /**
     * Whether this threshold is actively monitored.
     */
    @Column
    @Builder.Default
    private Boolean active = true;

    /**
     * Custom message to display when alert is triggered.
     */
    @Column(name = "alert_message", length = 500)
    private String alertMessage;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum MetricType {
        AI_USAGE_KWH,
        TOTAL_ENERGY_KWH,
        CARBON_EMISSION_KG,
        MONTHLY_COST,
        AI_PERCENTAGE,
        ENERGY_GROWTH_RATE
    }

    public enum ThresholdOperator {
        GREATER_THAN,
        LESS_THAN,
        EQUALS,
        GREATER_THAN_OR_EQUALS,
        LESS_THAN_OR_EQUALS
    }
}
