package com.volttwin.service;

import com.volttwin.dto.PrognosticsResult;
import com.volttwin.model.BatteryCycle;
import com.volttwin.repository.BatteryCycleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestration service bridging telemetry data with ML inference.
 */
@Service
public class BatteryService {

    private static final Logger log = LoggerFactory.getLogger(BatteryService.class);

    private final BatteryCycleRepository repository;
    private final OnnxInferenceService onnxService;

    private static final double NOMINAL_OCV = 3.8;
    private static final double DISCHARGE_CURRENT = 2.0;

    public BatteryService(BatteryCycleRepository repository, OnnxInferenceService onnxService) {
        this.repository = repository;
        this.onnxService = onnxService;
    }

    public PrognosticsResult predictForLatestCycle(String batteryId) throws Exception {
        BatteryCycle latest = repository.findLatestCycleForBattery(batteryId);
        if (latest == null) {
            throw new IllegalArgumentException("No data found for battery: " + batteryId);
        }

        List<BatteryCycle> allCycles = repository.findByBatteryIdOrderByCycleAsc(batteryId);
        return predictForCycle(latest, allCycles);
    }

    public List<PrognosticsResult> predictAllCycles(String batteryId) throws Exception {
        List<BatteryCycle> allCycles = repository.findByBatteryIdOrderByCycleAsc(batteryId);
        List<PrognosticsResult> results = new ArrayList<>();

        for (BatteryCycle cycle : allCycles) {
            results.add(predictForCycle(cycle, allCycles));
        }
        return results;
    }

    private PrognosticsResult predictForCycle(BatteryCycle cycle, List<BatteryCycle> allCycles) throws Exception {
        float[] features = engineerFeatures(cycle, allCycles);

        float predictedSoh = onnxService.predictSOH(features);
        float predictedRul = onnxService.predictRUL(features);

        return PrognosticsResult.builder()
                .batteryId(cycle.getBatteryId())
                .currentCycle(cycle.getCycle())
                .predictedSohPct(Math.round(predictedSoh * 100.0) / 100.0)
                .predictedRulCycles(Math.round(predictedRul * 100.0) / 100.0)
                .actualSohPct(cycle.getSohPct())
                .actualRulCycles(cycle.getRulCycles())
                .sohErrorPct(Math.round(Math.abs(predictedSoh - cycle.getSohPct()) * 100.0) / 100.0)
                .rulErrorCycles(Math.round(Math.abs(predictedRul - cycle.getRulCycles()) * 100.0) / 100.0)
                .build();
    }

    private float[] engineerFeatures(BatteryCycle cycle, List<BatteryCycle> allCycles) {
        int idx = cycle.getCycle() - 1;

        double rInt = Math.max(0.01, (NOMINAL_OCV - cycle.getAvgVoltage()) / DISCHARGE_CURRENT);
        double deltaT = cycle.getMaxTempC() - cycle.getAmbientTemperature();
        double jouleHeat = DISCHARGE_CURRENT * DISCHARGE_CURRENT * rInt;

        double fadeRate = 0.0;
        if (idx > 0 && idx < allCycles.size()) {
            fadeRate = cycle.getCapacityAh() - allCycles.get(idx - 1).getCapacityAh();
        }

        double initialCapacity = allCycles.get(0).getCapacityAh();
        double cumLoss = initialCapacity - cycle.getCapacityAh();
        double voltageSag = 3.7 - cycle.getAvgVoltage();

        double rIntGrowth = 0.0;
        if (idx > 0 && idx < allCycles.size()) {
            double prevRInt = Math.max(0.01,
                    (NOMINAL_OCV - allCycles.get(idx - 1).getAvgVoltage()) / DISCHARGE_CURRENT);
            rIntGrowth = rInt - prevRInt;
        }

        double energyWh = cycle.getCapacityAh() * cycle.getAvgVoltage();

        double thermalStressCum = 0.0;
        for (int i = 0; i <= idx && i < allCycles.size(); i++) {
            thermalStressCum += allCycles.get(i).getMaxTempC() - allCycles.get(i).getAmbientTemperature();
        }

        return new float[] {
                (float) cycle.getCycle(),
                (float) cycle.getAmbientTemperature(),
                (float) cycle.getMinVoltage(),
                (float) cycle.getAvgVoltage(),
                (float) cycle.getMaxTempC(),
                (float) cycle.getAvgTempC(),
                (float) cycle.getDurationSec(),
                (float) rInt,
                (float) deltaT,
                (float) jouleHeat,
                (float) fadeRate,
                (float) cumLoss,
                (float) voltageSag,
                (float) rIntGrowth,
                (float) energyWh,
                (float) thermalStressCum
        };
    }

    public List<String> getAllBatteryIds() {
        return repository.findDistinctBatteryIds();
    }

    public List<BatteryCycle> getCyclesForBattery(String batteryId) {
        return repository.findByBatteryIdOrderByCycleAsc(batteryId);
    }

    /**
     * Perform custom battery analysis and predictive life calculations from user inputs.
     */
    public com.volttwin.dto.BatteryAnalysisResponse analyzeCustomBattery(
            com.volttwin.dto.BatteryAnalysisRequest req, ReliabilityService reliabilityService) throws Exception {

        double ratedCap = req.getRatedCapacityAh() > 0 ? req.getRatedCapacityAh() : 2.0;
        double currCap = req.getCurrentCapacityAh() > 0 ? req.getCurrentCapacityAh() : 1.85;
        int cycle = Math.max(1, req.getCycleCount());
        double avgV = req.getAvgOperatingVoltage() > 0 ? req.getAvgOperatingVoltage() : 3.65;
        double minV = req.getMinDischargeVoltage() > 0 ? req.getMinDischargeVoltage() : 2.8;
        double maxT = req.getMaxOperatingTempC() > 0 ? req.getMaxOperatingTempC() : 35.0;
        double ambT = req.getAmbientTempC() > 0 ? req.getAmbientTempC() : 24.0;
        double duration = req.getDischargeDurationSec() > 0 ? req.getDischargeDurationSec() : 3600.0;
        double currentA = req.getDischargeCurrentA() > 0 ? req.getDischargeCurrentA() : 2.0;

        // 1. Direct Physics Calculations
        double calculatedSoh = (currCap / ratedCap) * 100.0;
        double rInt = Math.max(0.01, (3.8 - avgV) / currentA);
        double deltaT = Math.max(0.0, maxT - ambT);
        double jouleHeat = currentA * currentA * rInt;
        double energyWh = currCap * avgV;
        double cumLoss = Math.max(0.0, ratedCap - currCap);
        double fadeRate = -(cumLoss / cycle);
        double voltageSag = 3.7 - avgV;
        double cumThermalStress = deltaT * cycle;

        // 2. Prepare 16-feature vector for ONNX GBR models
        float[] features = new float[] {
                (float) cycle,
                (float) ambT,
                (float) minV,
                (float) avgV,
                (float) maxT,
                (float) ((maxT + ambT) / 2.0), // approx avg temp
                (float) duration,
                (float) rInt,
                (float) deltaT,
                (float) jouleHeat,
                (float) fadeRate,
                (float) cumLoss,
                (float) voltageSag,
                0.0f, // r_int_growth_rate
                (float) energyWh,
                (float) cumThermalStress
        };

        float predictedSoh = onnxService.predictSOH(features);
        float predictedRul = onnxService.predictRUL(features);
        int cleanRul = Math.max(0, Math.round(predictedRul));

        // 3. Reliability & Failure Risk
        com.volttwin.dto.WeibullReport weibull = reliabilityService.getWeibullReport();
        double failureRisk = weibull != null ? (weibull.failureProbabilityAt(cycle) * 100.0) : 0.0;
        double b10 = weibull != null ? weibull.getB10LifeCycles() : 85.4;

        // 4. Health Classification
        String status;
        if (calculatedSoh >= 85.0) {
            status = "Optimal Health (Minimal Degradation)";
        } else if (calculatedSoh >= 75.0) {
            status = "Moderate Degradation (Within Safe Limits)";
        } else if (calculatedSoh >= 70.0) {
            status = "Nearing End-of-Life (Inspection Recommended)";
        } else {
            status = "End-of-Life Exceeded (<70% SOH Replacement Due)";
        }

        // 5. Generate Projected Degradation Curve forward from current cycle
        java.util.List<java.util.Map<String, Object>> projection = new java.util.ArrayList<>();
        int projectSteps = Math.max(20, cleanRul + 20);
        double decayPerCycle = cleanRul > 0 ? (calculatedSoh - 70.0) / cleanRul : 0.2;
        if (decayPerCycle <= 0.01) decayPerCycle = 0.15;

        for (int i = 0; i <= projectSteps; i += Math.max(1, projectSteps / 25)) {
            int futureCycle = cycle + i;
            double projectedSoh = Math.max(50.0, calculatedSoh - (i * decayPerCycle));
            projection.add(java.util.Map.of(
                    "cycle", futureCycle,
                    "sohPct", Math.round(projectedSoh * 10.0) / 10.0,
                    "isHistorical", i == 0
            ));
        }

        com.volttwin.dto.BatteryAnalysisResponse resp = new com.volttwin.dto.BatteryAnalysisResponse();
        resp.setBatteryName(req.getBatteryName() != null && !req.getBatteryName().isBlank() ? req.getBatteryName() : "Custom Battery Cell");
        resp.setCurrentCycle(cycle);
        resp.setCalculatedSohPct(Math.round(calculatedSoh * 100.0) / 100.0);
        resp.setPredictedSohPct(Math.round(predictedSoh * 100.0) / 100.0);
        resp.setPredictedRulCycles(cleanRul);
        resp.setEstimatedTotalCycleLife(cycle + cleanRul);
        resp.setInternalResistanceOhms(Math.round(rInt * 10000.0) / 10000.0);
        resp.setThermalRiseDeltaC(Math.round(deltaT * 10.0) / 10.0);
        resp.setJouleHeatingWatts(Math.round(jouleHeat * 1000.0) / 1000.0);
        resp.setEnergyDeliveredWh(Math.round(energyWh * 100.0) / 100.0);
        resp.setCurrentFailureRiskPct(Math.round(failureRisk * 10.0) / 10.0);
        resp.setB10WarrantyBenchmark(b10);
        resp.setHealthStatus(status);
        resp.setProjectedDegradationCurve(projection);

        return resp;
    }
}
