package com.ecoai.controller;

import com.ecoai.dto.EnergyUsageDTO;
import com.ecoai.dto.TrendDataPointDTO;
import com.ecoai.service.AnalyticsService;
import com.ecoai.service.EnergyTrackingService;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Energy Usage - Feature 1.
 */
@RestController
@RequestMapping("/api/companies/{companyId}/energy")
@RequiredArgsConstructor
@CrossOrigin
public class EnergyController {

    private final EnergyTrackingService energyTrackingService;
    private final AnalyticsService analyticsService;

    @PostMapping
    public ResponseEntity<EnergyUsageDTO> recordEnergyUsage(
            @PathVariable UUID companyId,
            @RequestBody EnergyUsageDTO dto) {
        EnergyUsageDTO created = energyTrackingService.recordEnergyUsage(companyId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/csv")
    public ResponseEntity<Map<String, Object>> importFromCsv(
            @PathVariable UUID companyId,
            @RequestParam("file") MultipartFile file) {
        try {
            List<EnergyUsageDTO> imported = energyTrackingService.importFromCsv(companyId, file);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "recordsImported", imported.size(),
                    "records", imported));
        } catch (IOException | CsvException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<EnergyUsageDTO>> getEnergyUsage(
            @PathVariable UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(energyTrackingService.getEnergyUsageByDateRange(companyId, startDate, endDate));
        }
        return ResponseEntity.ok(energyTrackingService.getAllEnergyUsage(companyId));
    }

    @GetMapping("/region/{region}")
    public ResponseEntity<List<EnergyUsageDTO>> getEnergyUsageByRegion(
            @PathVariable UUID companyId,
            @PathVariable String region) {
        return ResponseEntity.ok(energyTrackingService.getEnergyUsageByRegion(companyId, region));
    }

    @GetMapping("/trends")
    public ResponseEntity<List<TrendDataPointDTO>> getEnergyTrends(
            @PathVariable UUID companyId,
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(analyticsService.getHistoricalTrends(companyId, months));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnergyUsage(@PathVariable UUID companyId, @PathVariable UUID id) {
        energyTrackingService.deleteEnergyUsage(id);
        return ResponseEntity.noContent().build();
    }
}
