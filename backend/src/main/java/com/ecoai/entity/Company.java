package com.ecoai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Company entity - represents an organization using the framework.
 * Companies can have multiple departments and track their energy usage.
 */
@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column
    private String industry;

    @Column
    private String country;

    @Column
    private String region;

    /**
     * Base percentage of total energy attributed to AI workloads.
     * Example: 0.30 means 30% of company energy is AI-related.
     */
    @Column(name = "base_ai_percentage", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal baseAiPercentage = new BigDecimal("0.30");

    /**
     * Default electricity cost per kWh in company's currency.
     */
    @Column(name = "electricity_cost_per_kwh", precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal electricityCostPerKwh = new BigDecimal("0.12");

    @Column
    @Builder.Default
    private String currency = "USD";

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Department> departments = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EnergyUsage> energyUsages = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CarbonConfig> carbonConfigs = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AlertThreshold> alertThresholds = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
