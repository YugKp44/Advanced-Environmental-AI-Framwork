package com.ecoai.controller;

import com.ecoai.dto.CarbonConfigDTO;
import com.ecoai.service.CarbonCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Carbon Configuration - Feature 3.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin
public class CarbonController {

    private final CarbonCalculationService carbonCalculationService;

    @GetMapping("/carbon/intensities")
    public ResponseEntity<List<CarbonConfigDTO>> getDefaultCarbonIntensities() {
        return ResponseEntity.ok(carbonCalculationService.getDefaultCarbonIntensities());
    }

    @GetMapping("/companies/{companyId}/carbon/config")
    public ResponseEntity<List<CarbonConfigDTO>> getCompanyCarbonConfigs(@PathVariable UUID companyId) {
        return ResponseEntity.ok(carbonCalculationService.getCompanyCarbonConfigs(companyId));
    }

    @PostMapping("/companies/{companyId}/carbon/config")
    public ResponseEntity<CarbonConfigDTO> configureCarbonIntensity(
            @PathVariable UUID companyId,
            @RequestBody Map<String, Object> request) {
        String region = (String) request.get("region");
        BigDecimal intensity = new BigDecimal(request.get("carbonIntensity").toString());

        return ResponseEntity.ok(carbonCalculationService.configureCarbonIntensity(companyId, region, intensity));
    }

    @GetMapping("/companies/{companyId}/carbon/intensity/{region}")
    public ResponseEntity<Map<String, Object>> getEffectiveIntensity(
            @PathVariable UUID companyId,
            @PathVariable String region) {
        BigDecimal intensity = carbonCalculationService.getEffectiveCarbonIntensity(companyId, region);
        return ResponseEntity.ok(Map.of(
                "region", region,
                "carbonIntensity", intensity,
                "unit", "gCO2/kWh"));
    }
}
