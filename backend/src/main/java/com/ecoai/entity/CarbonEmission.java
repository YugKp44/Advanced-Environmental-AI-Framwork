package com.ecoai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CarbonEmission entity - calculated carbon emissions for energy usage.
 * Linked 1:1 with EnergyUsage records.
 */
@Entity
@Table(name = "carbon_emissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarbonEmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "energy_usage_id", nullable = false)
    private EnergyUsage energyUsage;

    /**
     * Total CO₂ equivalent emissions in grams.
     * Calculated: AI_kWh × carbonIntensity
     */
    @Column(name = "co2e_grams", precision = 15, scale = 4, nullable = false)
    private BigDecimal co2eGrams;

    /**
     * CO₂ equivalent in kilograms (for easier reading).
     */
    @Column(name = "co2e_kg", precision = 15, scale = 4)
    private BigDecimal co2eKg;

    /**
     * Carbon intensity factor used for this calculation.
     */
    @Column(name = "carbon_intensity_used", precision = 10, scale = 4)
    private BigDecimal carbonIntensityUsed;

    /**
     * Region used for the carbon intensity lookup.
     */
    @Column(name = "region_used", length = 50)
    private String regionUsed;

    @Column(name = "calculated_at")
    @Builder.Default
    private LocalDateTime calculatedAt = LocalDateTime.now();
}
