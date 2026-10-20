package com.volttwin.dto;

/**
 * Response DTO carrying the ML prognostics inference result for a battery cell.
 */
public class PrognosticsResult {
    private String batteryId;
    private int currentCycle;
    private double predictedSohPct;
    private double predictedRulCycles;
    private double actualSohPct;
    private double actualRulCycles;
    private double sohErrorPct;
    private double rulErrorCycles;

    public PrognosticsResult() {}

    public PrognosticsResult(String batteryId, int currentCycle, double predictedSohPct,
                             double predictedRulCycles, double actualSohPct, double actualRulCycles,
                             double sohErrorPct, double rulErrorCycles) {
        this.batteryId = batteryId;
        this.currentCycle = currentCycle;
        this.predictedSohPct = predictedSohPct;
        this.predictedRulCycles = predictedRulCycles;
        this.actualSohPct = actualSohPct;
        this.actualRulCycles = actualRulCycles;
        this.sohErrorPct = sohErrorPct;
        this.rulErrorCycles = rulErrorCycles;
    }

    public static PrognosticsResultBuilder builder() {
        return new PrognosticsResultBuilder();
    }

    public static class PrognosticsResultBuilder {
        private String batteryId;
        private int currentCycle;
        private double predictedSohPct;
        private double predictedRulCycles;
        private double actualSohPct;
        private double actualRulCycles;
        private double sohErrorPct;
        private double rulErrorCycles;

        public PrognosticsResultBuilder batteryId(String s) { this.batteryId = s; return this; }
        public PrognosticsResultBuilder currentCycle(int i) { this.currentCycle = i; return this; }
        public PrognosticsResultBuilder predictedSohPct(double d) { this.predictedSohPct = d; return this; }
        public PrognosticsResultBuilder predictedRulCycles(double d) { this.predictedRulCycles = d; return this; }
        public PrognosticsResultBuilder actualSohPct(double d) { this.actualSohPct = d; return this; }
        public PrognosticsResultBuilder actualRulCycles(double d) { this.actualRulCycles = d; return this; }
        public PrognosticsResultBuilder sohErrorPct(double d) { this.sohErrorPct = d; return this; }
        public PrognosticsResultBuilder rulErrorCycles(double d) { this.rulErrorCycles = d; return this; }

        public PrognosticsResult build() {
            return new PrognosticsResult(batteryId, currentCycle, predictedSohPct, predictedRulCycles,
                    actualSohPct, actualRulCycles, sohErrorPct, rulErrorCycles);
        }
    }

    public String getBatteryId() { return batteryId; }
    public void setBatteryId(String s) { this.batteryId = s; }
    public int getCurrentCycle() { return currentCycle; }
    public void setCurrentCycle(int i) { this.currentCycle = i; }
    public double getPredictedSohPct() { return predictedSohPct; }
    public void setPredictedSohPct(double d) { this.predictedSohPct = d; }
    public double getPredictedRulCycles() { return predictedRulCycles; }
    public void setPredictedRulCycles(double d) { this.predictedRulCycles = d; }
    public double getActualSohPct() { return actualSohPct; }
    public void setActualSohPct(double d) { this.actualSohPct = d; }
    public double getActualRulCycles() { return actualRulCycles; }
    public void setActualRulCycles(double d) { this.actualRulCycles = d; }
    public double getSohErrorPct() { return sohErrorPct; }
    public void setSohErrorPct(double d) { this.sohErrorPct = d; }
    public double getRulErrorCycles() { return rulErrorCycles; }
    public void setRulErrorCycles(double d) { this.rulErrorCycles = d; }
}
