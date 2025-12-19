package com.ecoai.controller;

import com.ecoai.dto.SimulationDTO;
import com.ecoai.service.SimulationEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for What-If Simulation - Feature 5.
 */
@RestController
@RequestMapping("/api/companies/{companyId}/simulate")
@RequiredArgsConstructor
@CrossOrigin
public class SimulationController {

    private final SimulationEngineService simulationEngineService;

    /**
     * Simulate AI usage growth scenario.
     * "What if AI usage grows by X%?"
     */
    @PostMapping("/growth")
    public ResponseEntity<SimulationDTO> simulateGrowth(
            @PathVariable UUID companyId,
            @RequestBody Map<String, Object> request) {
        BigDecimal growthPercent = new BigDecimal(request.get("growthPercent").toString());
        Integer monthsAhead = request.containsKey("monthsAhead")
                ? Integer.parseInt(request.get("monthsAhead").toString())
                : 12;

        SimulationDTO result = simulationEngineService.simulateGrowth(companyId, growthPercent, monthsAhead);
        return ResponseEntity.ok(result);
    }

    /**
     * Simulate region change scenario.
     * "What if workloads move from region A to region B?"
     */
    @PostMapping("/region")
    public ResponseEntity<SimulationDTO> simulateRegionChange(
            @PathVariable UUID companyId,
            @RequestBody Map<String, String> request) {
        String fromRegion = request.get("fromRegion");
        String toRegion = request.get("toRegion");

        SimulationDTO result = simulationEngineService.simulateRegionChange(companyId, fromRegion, toRegion);
        return ResponseEntity.ok(result);
    }

    /**
     * Simulate efficiency improvement scenario.
     * "What if we improve AI efficiency by X%?"
     */
    @PostMapping("/efficiency")
    public ResponseEntity<SimulationDTO> simulateEfficiency(
            @PathVariable UUID companyId,
            @RequestBody Map<String, Object> request) {
        BigDecimal efficiencyPercent = new BigDecimal(request.get("efficiencyPercent").toString());

        SimulationDTO result = simulationEngineService.simulateEfficiency(companyId, efficiencyPercent);
        return ResponseEntity.ok(result);
    }

    /**
     * Save a simulation scenario for future reference.
     */
    @PostMapping("/save")
    public ResponseEntity<SimulationDTO> saveScenario(
            @PathVariable UUID companyId,
            @RequestBody SimulationDTO dto) {
        SimulationDTO saved = simulationEngineService.saveScenario(companyId, dto);
        return ResponseEntity.ok(saved);
    }

    /**
     * Get all saved scenarios for a company.
     */
    @GetMapping("/scenarios")
    public ResponseEntity<List<SimulationDTO>> getSavedScenarios(@PathVariable UUID companyId) {
        return ResponseEntity.ok(simulationEngineService.getSavedScenarios(companyId));
    }
}
