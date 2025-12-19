package com.ecoai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SimulationScenario entity - stores what-if simulation parameters and results.
 * Allows companies to explore different scenarios for planning purposes.
 */
@Entity
@Table(name = "simulation_scenarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /**
     * Name of the simulation scenario for easy identification.
     */
    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    /**
     * Type of simulation: GROWTH, REGION_CHANGE, EFFICIENCY, CUSTOM
     */
    @Column(name = "simulation_type", length = 30)
    @Enumerated(EnumType.STRING)
    private SimulationType simulationType;

    /**
     * JSON-stored simulation parameters.
     * Example: {"growthPercent": 20, "months": 12}
     */
    @Column(columnDefinition = "TEXT")
    private String parameters;

    /**
     * JSON-stored simulation results.
     * Example: {"projectedAiKwh": 15000, "projectedCo2eKg": 4500, "projectedCost":
     * 1800}
     */
    @Column(columnDefinition = "TEXT")
    private String results;

    /**
     * Baseline values before simulation.
     */
    @Column(name = "baseline_values", columnDefinition = "TEXT")
    private String baselineValues;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum SimulationType {
        GROWTH, // Simulate AI usage growth
        REGION_CHANGE, // Simulate moving workloads to different region
        EFFICIENCY, // Simulate efficiency improvements
        CUSTOM // Custom simulation with multiple parameters
    }
}
