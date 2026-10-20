package com.volttwin.service;

import com.volttwin.dto.WeibullReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReliabilityServiceTest {

    private ReliabilityService reliabilityService;

    @BeforeEach
    void setUp() throws Exception {
        reliabilityService = new ReliabilityService();
        String paramsPath = "../data/processed/weibull_parameters.json";
        ReflectionTestUtils.setField(reliabilityService, "weibullParamsFile", paramsPath);
        reliabilityService.loadWeibullParameters();
    }

    @Test
    @DisplayName("Should load Weibull parameters correctly from JSON")
    void testLoadParameters() {
        WeibullReport report = reliabilityService.getWeibullReport();
        assertNotNull(report);
        assertTrue(report.getBetaShape() > 1.0, "Beta should be > 1 indicating wear-out degradation");
        assertEquals(4.8488, report.getBetaShape(), 0.001);
        assertEquals(135.91, report.getEtaScaleCycles(), 0.01);
        assertEquals(124.56, report.getMttfCycles(), 0.01);
        assertEquals(85.44, report.getB10LifeCycles(), 0.01);
    }

    @Test
    @DisplayName("Reliability R(t) should strictly decrease with cycles")
    void testReliabilityMonotonicity() {
        WeibullReport report = reliabilityService.getWeibullReport();
        double r10 = report.reliabilityAt(10);
        double r50 = report.reliabilityAt(50);
        double r100 = report.reliabilityAt(100);
        double r150 = report.reliabilityAt(150);

        assertTrue(r10 > r50, "R(10) > R(50)");
        assertTrue(r50 > r100, "R(50) > R(100)");
        assertTrue(r100 > r150, "R(100) > R(150)");
    }

    @Test
    @DisplayName("B10 life should correspond to ~90% reliability")
    void testB10Life() {
        WeibullReport report = reliabilityService.getWeibullReport();
        double rB10 = report.reliabilityAt((int) Math.round(report.getB10LifeCycles()));
        assertEquals(0.90, rB10, 0.02, "Reliability at B10 cycle should be approximately 0.90");
    }

    @Test
    @DisplayName("Should generate reliability curve data points")
    void testReliabilityCurveGeneration() {
        List<Map<String, Object>> curve = reliabilityService.getReliabilityCurve(50);
        assertEquals(50, curve.size());
        Map<String, Object> point = curve.get(0);
        assertTrue(point.containsKey("cycle"));
        assertTrue(point.containsKey("reliability"));
        assertTrue(point.containsKey("failureProbability"));
        assertTrue(point.containsKey("hazardRate"));
    }
}
