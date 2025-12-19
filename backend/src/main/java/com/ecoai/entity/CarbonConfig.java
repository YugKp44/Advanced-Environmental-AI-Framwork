package com.ecoai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CarbonConfig entity - stores region-specific carbon intensity factors.
 * Carbon intensity = grams of CO₂ emitted per kWh of electricity.
 */
@Entity
@Table(name = "carbon_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarbonConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /**
     * Region/country code for this configuration.
     * Example: "IN" for India, "US" for USA, "EU" for European Union
     */
    @Column(nullable = false, length = 50)
    private String region;

    /**
     * Carbon intensity factor in gCO₂/kWh.
     * Default values by region:
     * - India: 708
     * - USA: 386
     * - EU: 276
     * - Norway: 26
     * - Canada: 120
     * - Australia: 656
     */
    @Column(name = "carbon_intensity", precision = 10, scale = 4, nullable = false)
    private BigDecimal carbonIntensity;

    /**
     * Unit of measurement (default: gCO2/kWh)
     */
    @Column(length = 20)
    @Builder.Default
    private String unit = "gCO2/kWh";

    /**
     * Year this factor is valid for (carbon intensities change over time)
     */
    @Column(name = "valid_year")
    @Builder.Default
    private Integer validYear = 2024;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
