package com.ecoai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Department entity - represents a department/team within a company.
 * Departments have an AI usage weight for attribution calculations.
 */
@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;

    @Column
    private String team;

    @Column
    private String product;

    @Column
    private String description;

    /**
     * AI usage weight for this department (0.0 to 1.0).
     * Used in attribution formula: AI_kWh = Total_kWh × Company_AI% × Dept_Weight
     * Example: ML Team might have weight 0.8, Marketing might have 0.1
     */
    @Column(name = "ai_usage_weight", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal aiUsageWeight = new BigDecimal("0.50");

    /**
     * Number of employees in this department (used for per-capita calculations)
     */
    @Column(name = "employee_count")
    @Builder.Default
    private Integer employeeCount = 10;

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
