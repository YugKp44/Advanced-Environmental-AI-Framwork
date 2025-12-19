package com.ecoai.controller;

import com.ecoai.dto.ForecastDTO;
import com.ecoai.dto.TrendDataPointDTO;
import com.ecoai.service.AnalyticsService;
import com.ecoai.service.AttributionEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Analytics & Forecasting - Feature 6.
 */
@RestController
@RequestMapping("/api/companies/{companyId}/analytics")
@RequiredArgsConstructor
@CrossOrigin
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AttributionEngineService attributionEngineService;

    /**
     * Get historical trends.
     */
    @GetMapping("/trends")
    public ResponseEntity<List<TrendDataPointDTO>> getHistoricalTrends(
            @PathVariable UUID companyId,
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(analyticsService.getHistoricalTrends(companyId, months));
    }

    /**
     * Get usage forecasts.
     */
    @GetMapping("/forecast")
    public ResponseEntity<List<ForecastDTO>> getForecast(
            @PathVariable UUID companyId,
            @RequestParam(defaultValue = "3") int months) {
        return ResponseEntity.ok(analyticsService.forecastUsage(companyId, months));
    }

    /**
     * Get department-wise comparison.
     */
    @GetMapping("/comparison")
    public ResponseEntity<?> getDepartmentComparison(@PathVariable UUID companyId) {
        return ResponseEntity.ok(attributionEngineService.getAttributionByDepartment(companyId));
    }

    /**
     * Get year-over-year comparison.
     */
    @GetMapping("/yoy")
    public ResponseEntity<Map<String, Object>> getYearOverYear(@PathVariable UUID companyId) {
        return ResponseEntity.ok(analyticsService.getYearOverYearComparison(companyId));
    }
}
