package com.ecoai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * EnergyUsage entity - records electricity consumption data.
 * This is the core data that drives all calculations and analytics.
 */
@Entity
@Table(name = "energy_usage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnergyUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    /**
     * Total electricity consumption in kWh.
     */
    @Column(name = "total_kwh", precision = 15, scale = 4, nullable = false)
    private BigDecimal totalKwh;

    /**
     * AI-attributed electricity consumption in kWh.
     * Calculated using: totalKwh × company.baseAiPercentage ×
     * department.aiUsageWeight
     */
    @Column(name = "ai_attributed_kwh", precision = 15, scale = 4)
    private BigDecimal aiAttributedKwh;

    /**
     * Electricity cost for this usage period.
     */
    @Column(precision = 15, scale = 2)
    private BigDecimal cost;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    /**
     * Date of the energy usage record.
     */
    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    /**
     * Period type: DAILY, WEEKLY, MONTHLY
     */
    @Column(name = "period_type", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PeriodType periodType = PeriodType.DAILY;

    /**
     * Region/location of energy consumption (affects carbon calculations).
     */
    @Column(length = 50)
    private String region;

    /**
     * Data source: MANUAL, CSV_IMPORT, API
     */
    @Column(name = "data_source", length = 20)
    @Builder.Default
    private String dataSource = "MANUAL";

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(mappedBy = "energyUsage", cascade = CascadeType.ALL, orphanRemoval = true)
    private CarbonEmission carbonEmission;

    public enum PeriodType {
        DAILY, WEEKLY, MONTHLY
    }
}
