package com.volttwin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volttwin.dto.WeibullReport;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that loads the pre-computed Weibull reliability parameters
 * and provides fleet reliability calculations.
 */
@Service
public class ReliabilityService {

    private static final Logger log = LoggerFactory.getLogger(ReliabilityService.class);

    @Value("${volttwin.weibull.params-file}")
    private String weibullParamsFile;

    private WeibullReport weibullReport;

    @PostConstruct
    public void loadWeibullParameters() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(weibullParamsFile));

        weibullReport = WeibullReport.builder()
                .betaShape(root.get("beta_shape").asDouble())
                .etaScaleCycles(root.get("eta_scale_cycles").asDouble())
                .mttfCycles(root.get("mttf_cycles").asDouble())
                .b10LifeCycles(root.get("b10_life_cycles").asDouble())
                .b50LifeCycles(root.get("b50_life_cycles").asDouble())
                .failureMode(root.get("failure_mode").asText())
                .build();

        log.info("Loaded Weibull parameters: beta={}, eta={}, MTTF={}, B10={}, B50={}",
                weibullReport.getBetaShape(),
                weibullReport.getEtaScaleCycles(),
                weibullReport.getMttfCycles(),
                weibullReport.getB10LifeCycles(),
                weibullReport.getB50LifeCycles());
    }

    public WeibullReport getWeibullReport() {
        return weibullReport;
    }

    public List<Map<String, Object>> getReliabilityCurve(int maxCycle) {
        List<Map<String, Object>> curve = new ArrayList<>();
        for (int t = 1; t <= maxCycle; t++) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("cycle", t);
            point.put("reliability", Math.round(weibullReport.reliabilityAt(t) * 100000.0) / 100000.0);
            point.put("failureProbability", Math.round(weibullReport.failureProbabilityAt(t) * 100000.0) / 100000.0);
            point.put("hazardRate", Math.round(weibullReport.hazardRateAt(t) * 1000000.0) / 1000000.0);
            curve.add(point);
        }
        return curve;
    }
}
