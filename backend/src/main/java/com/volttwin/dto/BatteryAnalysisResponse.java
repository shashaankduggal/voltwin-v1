package com.volttwin.dto;

import java.util.List;
import java.util.Map;

public class BatteryAnalysisResponse {
    private String batteryName;
    private int currentCycle;
    private double calculatedSohPct;
    private double predictedSohPct;
    private int predictedRulCycles;
    private int estimatedTotalCycleLife;
    
    // Physics / Mechanical metrics
    private double internalResistanceOhms;
    private double thermalRiseDeltaC;
    private double jouleHeatingWatts;
    private double energyDeliveredWh;
    
    // Fleet reliability risk
    private double currentFailureRiskPct;
    private double b10WarrantyBenchmark;
    private String healthStatus; // e.g. "Optimal", "Degrading", "End-of-Life Imminent"
    
    // Projections for interactive charting
    private List<Map<String, Object>> projectedDegradationCurve;

    public BatteryAnalysisResponse() {}

    public String getBatteryName() { return batteryName; }
    public void setBatteryName(String batteryName) { this.batteryName = batteryName; }
    public int getCurrentCycle() { return currentCycle; }
    public void setCurrentCycle(int currentCycle) { this.currentCycle = currentCycle; }
    public double getCalculatedSohPct() { return calculatedSohPct; }
    public void setCalculatedSohPct(double calculatedSohPct) { this.calculatedSohPct = calculatedSohPct; }
    public double getPredictedSohPct() { return predictedSohPct; }
    public void setPredictedSohPct(double predictedSohPct) { this.predictedSohPct = predictedSohPct; }
    public int getPredictedRulCycles() { return predictedRulCycles; }
    public void setPredictedRulCycles(int predictedRulCycles) { this.predictedRulCycles = predictedRulCycles; }
    public int getEstimatedTotalCycleLife() { return estimatedTotalCycleLife; }
    public void setEstimatedTotalCycleLife(int estimatedTotalCycleLife) { this.estimatedTotalCycleLife = estimatedTotalCycleLife; }
    public double getInternalResistanceOhms() { return internalResistanceOhms; }
    public void setInternalResistanceOhms(double internalResistanceOhms) { this.internalResistanceOhms = internalResistanceOhms; }
    public double getThermalRiseDeltaC() { return thermalRiseDeltaC; }
    public void setThermalRiseDeltaC(double thermalRiseDeltaC) { this.thermalRiseDeltaC = thermalRiseDeltaC; }
    public double getJouleHeatingWatts() { return jouleHeatingWatts; }
    public void setJouleHeatingWatts(double jouleHeatingWatts) { this.jouleHeatingWatts = jouleHeatingWatts; }
    public double getEnergyDeliveredWh() { return energyDeliveredWh; }
    public void setEnergyDeliveredWh(double energyDeliveredWh) { this.energyDeliveredWh = energyDeliveredWh; }
    public double getCurrentFailureRiskPct() { return currentFailureRiskPct; }
    public void setCurrentFailureRiskPct(double currentFailureRiskPct) { this.currentFailureRiskPct = currentFailureRiskPct; }
    public double getB10WarrantyBenchmark() { return b10WarrantyBenchmark; }
    public void setB10WarrantyBenchmark(double b10WarrantyBenchmark) { this.b10WarrantyBenchmark = b10WarrantyBenchmark; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public List<Map<String, Object>> getProjectedDegradationCurve() { return projectedDegradationCurve; }
    public void setProjectedDegradationCurve(List<Map<String, Object>> projectedDegradationCurve) { this.projectedDegradationCurve = projectedDegradationCurve; }
}
