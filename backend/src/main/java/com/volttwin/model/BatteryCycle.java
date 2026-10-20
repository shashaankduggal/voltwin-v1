package com.volttwin.model;

import jakarta.persistence.*;

/**
 * JPA Entity representing a single battery discharge cycle measurement.
 * Maps 1:1 to rows in the battery_cycle_summary.csv dataset.
 */
@Entity
@Table(name = "battery_cycles")
public class BatteryCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String batteryId;

    @Column(nullable = false)
    private int cycle;

    private double ambientTemperature;
    private double capacityAh;
    private double minVoltage;
    private double avgVoltage;
    private double maxTempC;
    private double avgTempC;
    private double durationSec;
    private double sohPct;
    private int eolCycle;
    private int rulCycles;

    public BatteryCycle() {}

    public BatteryCycle(Long id, String batteryId, int cycle, double ambientTemperature,
                        double capacityAh, double minVoltage, double avgVoltage,
                        double maxTempC, double avgTempC, double durationSec,
                        double sohPct, int eolCycle, int rulCycles) {
        this.id = id;
        this.batteryId = batteryId;
        this.cycle = cycle;
        this.ambientTemperature = ambientTemperature;
        this.capacityAh = capacityAh;
        this.minVoltage = minVoltage;
        this.avgVoltage = avgVoltage;
        this.maxTempC = maxTempC;
        this.avgTempC = avgTempC;
        this.durationSec = durationSec;
        this.sohPct = sohPct;
        this.eolCycle = eolCycle;
        this.rulCycles = rulCycles;
    }

    public static BatteryCycleBuilder builder() {
        return new BatteryCycleBuilder();
    }

    public static class BatteryCycleBuilder {
        private Long id;
        private String batteryId;
        private int cycle;
        private double ambientTemperature;
        private double capacityAh;
        private double minVoltage;
        private double avgVoltage;
        private double maxTempC;
        private double avgTempC;
        private double durationSec;
        private double sohPct;
        private int eolCycle;
        private int rulCycles;

        public BatteryCycleBuilder id(Long id) { this.id = id; return this; }
        public BatteryCycleBuilder batteryId(String batteryId) { this.batteryId = batteryId; return this; }
        public BatteryCycleBuilder cycle(int cycle) { this.cycle = cycle; return this; }
        public BatteryCycleBuilder ambientTemperature(double v) { this.ambientTemperature = v; return this; }
        public BatteryCycleBuilder capacityAh(double v) { this.capacityAh = v; return this; }
        public BatteryCycleBuilder minVoltage(double v) { this.minVoltage = v; return this; }
        public BatteryCycleBuilder avgVoltage(double v) { this.avgVoltage = v; return this; }
        public BatteryCycleBuilder maxTempC(double v) { this.maxTempC = v; return this; }
        public BatteryCycleBuilder avgTempC(double v) { this.avgTempC = v; return this; }
        public BatteryCycleBuilder durationSec(double v) { this.durationSec = v; return this; }
        public BatteryCycleBuilder sohPct(double v) { this.sohPct = v; return this; }
        public BatteryCycleBuilder eolCycle(int v) { this.eolCycle = v; return this; }
        public BatteryCycleBuilder rulCycles(int v) { this.rulCycles = v; return this; }

        public BatteryCycle build() {
            return new BatteryCycle(id, batteryId, cycle, ambientTemperature, capacityAh,
                    minVoltage, avgVoltage, maxTempC, avgTempC, durationSec, sohPct, eolCycle, rulCycles);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBatteryId() { return batteryId; }
    public void setBatteryId(String batteryId) { this.batteryId = batteryId; }
    public int getCycle() { return cycle; }
    public void setCycle(int cycle) { this.cycle = cycle; }
    public double getAmbientTemperature() { return ambientTemperature; }
    public void setAmbientTemperature(double v) { this.ambientTemperature = v; }
    public double getCapacityAh() { return capacityAh; }
    public void setCapacityAh(double v) { this.capacityAh = v; }
    public double getMinVoltage() { return minVoltage; }
    public void setMinVoltage(double v) { this.minVoltage = v; }
    public double getAvgVoltage() { return avgVoltage; }
    public void setAvgVoltage(double v) { this.avgVoltage = v; }
    public double getMaxTempC() { return maxTempC; }
    public void setMaxTempC(double v) { this.maxTempC = v; }
    public double getAvgTempC() { return avgTempC; }
    public void setAvgTempC(double v) { this.avgTempC = v; }
    public double getDurationSec() { return durationSec; }
    public void setDurationSec(double v) { this.durationSec = v; }
    public double getSohPct() { return sohPct; }
    public void setSohPct(double v) { this.sohPct = v; }
    public int getEolCycle() { return eolCycle; }
    public void setEolCycle(int v) { this.eolCycle = v; }
    public int getRulCycles() { return rulCycles; }
    public void setRulCycles(int v) { this.rulCycles = v; }
}
