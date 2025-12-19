package com.ecoai.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for Department entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDTO {
    private UUID id;
    private UUID companyId;
    private String name;
    private String team;
    private String product;
    private String description;
    private BigDecimal aiUsageWeight;
    private Integer employeeCount;
}
