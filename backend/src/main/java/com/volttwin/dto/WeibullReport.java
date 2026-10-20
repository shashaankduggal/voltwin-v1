package com.volttwin.dto;

/**
 * Response DTO carrying Weibull reliability statistics for the battery fleet.
 */
public class WeibullReport {
    private double betaShape;
    private double etaScaleCycles;
    private double mttfCycles;
    private double b10LifeCycles;
    private double b50LifeCycles;
    private String failureMode;

    public WeibullReport() {}

    public WeibullReport(double betaShape, double etaScaleCycles, double mttfCycles,
                         double b10LifeCycles, double b50LifeCycles, String failureMode) {
        this.betaShape = betaShape;
        this.etaScaleCycles = etaScaleCycles;
        this.mttfCycles = mttfCycles;
        this.b10LifeCycles = b10LifeCycles;
        this.b50LifeCycles = b50LifeCycles;
        this.failureMode = failureMode;
    }

    public static WeibullReportBuilder builder() {
        return new WeibullReportBuilder();
    }

    public static class WeibullReportBuilder {
        private double betaShape;
        private double etaScaleCycles;
        private double mttfCycles;
        private double b10LifeCycles;
        private double b50LifeCycles;
        private String failureMode;

        public WeibullReportBuilder betaShape(double d) { this.betaShape = d; return this; }
        public WeibullReportBuilder etaScaleCycles(double d) { this.etaScaleCycles = d; return this; }
        public WeibullReportBuilder mttfCycles(double d) { this.mttfCycles = d; return this; }
        public WeibullReportBuilder b10LifeCycles(double d) { this.b10LifeCycles = d; return this; }
        public WeibullReportBuilder b50LifeCycles(double d) { this.b50LifeCycles = d; return this; }
        public WeibullReportBuilder failureMode(String s) { this.failureMode = s; return this; }

        public WeibullReport build() {
            return new WeibullReport(betaShape, etaScaleCycles, mttfCycles, b10LifeCycles, b50LifeCycles, failureMode);
        }
    }

    public double getBetaShape() { return betaShape; }
    public void setBetaShape(double d) { this.betaShape = d; }
    public double getEtaScaleCycles() { return etaScaleCycles; }
    public void setEtaScaleCycles(double d) { this.etaScaleCycles = d; }
    public double getMttfCycles() { return mttfCycles; }
    public void setMttfCycles(double d) { this.mttfCycles = d; }
    public double getB10LifeCycles() { return b10LifeCycles; }
    public void setB10LifeCycles(double d) { this.b10LifeCycles = d; }
    public double getB50LifeCycles() { return b50LifeCycles; }
    public void setB50LifeCycles(double d) { this.b50LifeCycles = d; }
    public String getFailureMode() { return failureMode; }
    public void setFailureMode(String s) { this.failureMode = s; }

    /** Compute reliability R(t) = exp(-(t/eta)^beta) at a given cycle */
    public double reliabilityAt(int cycle) {
        return Math.exp(-Math.pow((double) cycle / etaScaleCycles, betaShape));
    }

    /** Compute failure probability F(t) = 1 - R(t) at a given cycle */
    public double failureProbabilityAt(int cycle) {
        return 1.0 - reliabilityAt(cycle);
    }

    /** Compute hazard rate h(t) = (beta/eta) * (t/eta)^(beta-1) at a given cycle */
    public double hazardRateAt(int cycle) {
        double ratio = (double) cycle / etaScaleCycles;
        return (betaShape / etaScaleCycles) * Math.pow(ratio, betaShape - 1);
    }
}
