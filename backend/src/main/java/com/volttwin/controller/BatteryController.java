package com.volttwin.controller;

import com.volttwin.dto.PrognosticsResult;
import com.volttwin.dto.WeibullReport;
import com.volttwin.model.BatteryCycle;
import com.volttwin.service.BatteryService;
import com.volttwin.service.ReliabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API controller exposing battery telemetry, ML prognostics, and reliability endpoints.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BatteryController {

    private final BatteryService batteryService;
    private final ReliabilityService reliabilityService;

    public BatteryController(BatteryService batteryService, ReliabilityService reliabilityService) {
        this.batteryService = batteryService;
        this.reliabilityService = reliabilityService;
    }

    @GetMapping("/batteries")
    public ResponseEntity<List<String>> getAllBatteries() {
        return ResponseEntity.ok(batteryService.getAllBatteryIds());
    }

    @GetMapping("/batteries/{batteryId}/cycles")
    public ResponseEntity<List<BatteryCycle>> getCycles(@PathVariable String batteryId) {
        return ResponseEntity.ok(batteryService.getCyclesForBattery(batteryId));
    }

    @GetMapping("/batteries/{batteryId}/predict")
    public ResponseEntity<PrognosticsResult> predictLatest(@PathVariable String batteryId) throws Exception {
        return ResponseEntity.ok(batteryService.predictForLatestCycle(batteryId));
    }

    /**
     * Submit custom battery parameters and calculate instantaneous SOH, physics metrics,
     * failure probability, and projected remaining useful life (RUL).
     */
    @PostMapping("/battery/analyze")
    public ResponseEntity<com.volttwin.dto.BatteryAnalysisResponse> analyzeBattery(
            @RequestBody com.volttwin.dto.BatteryAnalysisRequest request) throws Exception {
        return ResponseEntity.ok(batteryService.analyzeCustomBattery(request, reliabilityService));
    }

    @GetMapping("/batteries/{batteryId}/predict/all")
    public ResponseEntity<List<PrognosticsResult>> predictAll(@PathVariable String batteryId) throws Exception {
        return ResponseEntity.ok(batteryService.predictAllCycles(batteryId));
    }

    @GetMapping("/reliability/weibull")
    public ResponseEntity<WeibullReport> getWeibullReport() {
        return ResponseEntity.ok(reliabilityService.getWeibullReport());
    }

    @GetMapping("/reliability/curve")
    public ResponseEntity<List<Map<String, Object>>> getReliabilityCurve(
            @RequestParam(defaultValue = "200") int maxCycle) {
        return ResponseEntity.ok(reliabilityService.getReliabilityCurve(maxCycle));
    }

    @GetMapping("/reliability/at/{cycle}")
    public ResponseEntity<Map<String, Object>> getReliabilityAt(@PathVariable int cycle) {
        WeibullReport w = reliabilityService.getWeibullReport();
        return ResponseEntity.ok(Map.of(
                "cycle", cycle,
                "reliability", w.reliabilityAt(cycle),
                "failureProbability", w.failureProbabilityAt(cycle),
                "hazardRate", w.hazardRateAt(cycle)
        ));
    }
}
