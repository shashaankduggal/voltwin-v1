package com.volttwin.dto;

public class BatteryAnalysisRequest {
    private String batteryName;
    private double ratedCapacityAh = 2.0;
    private double currentCapacityAh = 1.85;
    private int cycleCount = 10;
    private double avgOperatingVoltage = 3.65;
    private double minDischargeVoltage = 2.8;
    private double maxOperatingTempC = 35.0;
    private double ambientTempC = 24.0;
    private double dischargeDurationSec = 3600.0;
    private double dischargeCurrentA = 2.0;

    public BatteryAnalysisRequest() {}

    public String getBatteryName() { return batteryName; }
    public void setBatteryName(String batteryName) { this.batteryName = batteryName; }
    public double getRatedCapacityAh() { return ratedCapacityAh; }
    public void setRatedCapacityAh(double ratedCapacityAh) { this.ratedCapacityAh = ratedCapacityAh; }
    public double getCurrentCapacityAh() { return currentCapacityAh; }
    public void setCurrentCapacityAh(double currentCapacityAh) { this.currentCapacityAh = currentCapacityAh; }
    public int getCycleCount() { return cycleCount; }
    public void setCycleCount(int cycleCount) { this.cycleCount = cycleCount; }
    public double getAvgOperatingVoltage() { return avgOperatingVoltage; }
    public void setAvgOperatingVoltage(double avgOperatingVoltage) { this.avgOperatingVoltage = avgOperatingVoltage; }
    public double getMinDischargeVoltage() { return minDischargeVoltage; }
    public void setMinDischargeVoltage(double minDischargeVoltage) { this.minDischargeVoltage = minDischargeVoltage; }
    public double getMaxOperatingTempC() { return maxOperatingTempC; }
    public void setMaxOperatingTempC(double maxOperatingTempC) { this.maxOperatingTempC = maxOperatingTempC; }
    public double getAmbientTempC() { return ambientTempC; }
    public void setAmbientTempC(double ambientTempC) { this.ambientTempC = ambientTempC; }
    public double getDischargeDurationSec() { return dischargeDurationSec; }
    public void setDischargeDurationSec(double dischargeDurationSec) { this.dischargeDurationSec = dischargeDurationSec; }
    public double getDischargeCurrentA() { return dischargeCurrentA; }
    public void setDischargeCurrentA(double dischargeCurrentA) { this.dischargeCurrentA = dischargeCurrentA; }
}
