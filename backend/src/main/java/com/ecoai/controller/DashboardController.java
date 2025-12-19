package com.ecoai.controller;

import com.ecoai.dto.DashboardSummaryDTO;
import com.ecoai.dto.DepartmentBreakdownDTO;
import com.ecoai.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Dashboard - Feature 8.
 */
@RestController
@RequestMapping("/api/companies/{companyId}/dashboard")
@RequiredArgsConstructor
@CrossOrigin
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get full dashboard data (all widgets).
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getFullDashboard(@PathVariable UUID companyId) {
        return ResponseEntity.ok(dashboardService.getFullDashboard(companyId));
    }

    /**
     * Get executive summary KPIs.
     */
    @GetMapping("/kpis")
    public ResponseEntity<DashboardSummaryDTO> getKpis(@PathVariable UUID companyId) {
        return ResponseEntity.ok(dashboardService.getExecutiveSummary(companyId));
    }

    /**
     * Get department breakdown for charts.
     */
    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentBreakdownDTO>> getDepartmentBreakdown(@PathVariable UUID companyId) {
        return ResponseEntity.ok(dashboardService.getDepartmentBreakdown(companyId));
    }

    /**
     * Get region breakdown.
     */
    @GetMapping("/regions")
    public ResponseEntity<List<Map<String, Object>>> getRegionBreakdown(@PathVariable UUID companyId) {
        return ResponseEntity.ok(dashboardService.getRegionBreakdown(companyId));
    }
}
