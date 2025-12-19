package com.ecoai.dto;

import lombok.*;

/**
 * DTO for optimization suggestions / insights.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsightDTO {
    private String category; // SCHEDULING, REGION, EFFICIENCY, BATCHING
    private String title;
    private String description;
    private String impact; // Potential savings/reduction
    private String priority; // HIGH, MEDIUM, LOW
    private String actionable; // Specific action to take
}
