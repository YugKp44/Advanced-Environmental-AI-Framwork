package com.ecoai.controller;

import com.ecoai.dto.AlertDTO;
import com.ecoai.dto.InsightDTO;
import com.ecoai.entity.AlertThreshold.MetricType;
import com.ecoai.service.AlertsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Alerts & Insights - Feature 7.
 */
@RestController
@RequestMapping("/api/companies/{companyId}")
@RequiredArgsConstructor
@CrossOrigin
public class AlertsController {

    private final AlertsService alertsService;

    /**
     * Get active alerts for a company.
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<AlertDTO>> getActiveAlerts(@PathVariable UUID companyId) {
        return ResponseEntity.ok(alertsService.checkThresholds(companyId));
    }

    /**
     * Get all configured thresholds.
     */
    @GetMapping("/alerts/thresholds")
    public ResponseEntity<List<AlertDTO>> getAllThresholds(@PathVariable UUID companyId) {
        return ResponseEntity.ok(alertsService.getAllThresholds(companyId));
    }

    /**
     * Configure a threshold.
     */
    @PostMapping("/alerts/thresholds")
    public ResponseEntity<AlertDTO> configureThreshold(
            @PathVariable UUID companyId,
            @RequestBody Map<String, Object> request) {
        MetricType metricType = MetricType.valueOf((String) request.get("metricType"));
        BigDecimal thresholdValue = new BigDecimal(request.get("thresholdValue").toString());
        String message = (String) request.get("message");

        return ResponseEntity.ok(alertsService.configureThreshold(companyId, metricType, thresholdValue, message));
    }

    /**
     * Get optimization insights.
     */
    @GetMapping("/insights")
    public ResponseEntity<List<InsightDTO>> getOptimizationInsights(@PathVariable UUID companyId) {
        return ResponseEntity.ok(alertsService.getOptimizationSuggestions(companyId));
    }
}
