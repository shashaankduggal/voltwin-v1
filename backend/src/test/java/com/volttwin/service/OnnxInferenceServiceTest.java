package com.volttwin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class OnnxInferenceServiceTest {

    private OnnxInferenceService onnxService;

    @BeforeEach
    void setUp() throws Exception {
        onnxService = new OnnxInferenceService();
        ReflectionTestUtils.setField(onnxService, "sohModelPath", "../ml-models/soh_regressor.onnx");
        ReflectionTestUtils.setField(onnxService, "rulModelPath", "../ml-models/rul_regressor.onnx");
        onnxService.loadModels();
    }

    @Test
    @DisplayName("Should run SOH inference via ONNX and return valid percentage")
    void testSohInference() throws Exception {
        // Sample feature vector: healthy battery near beginning of life
        float[] healthyFeatures = new float[] {
                1.0f,     // cycle
                24.0f,    // ambient_temp
                2.612f,   // min_voltage
                3.530f,   // avg_voltage
                38.98f,   // max_temp_c
                32.57f,   // avg_temp_c
                3690.0f,  // duration_sec
                0.135f,   // estimated_r_int_ohms
                14.98f,   // delta_temp_c
                0.540f,   // joule_heating_watts
                0.0f,     // capacity_fade_rate
                0.0f,     // cumulative_capacity_loss
                0.170f,   // voltage_sag
                0.0f,     // r_int_growth_rate
                6.55f,    // energy_wh (1.856 * 3.53)
                14.98f    // thermal_stress_cumulative
        };

        float predictedSoh = onnxService.predictSOH(healthyFeatures);
        assertTrue(predictedSoh > 85.0f && predictedSoh < 100.0f,
                "Early cycle SOH should be between 85% and 100%, got: " + predictedSoh);

        float predictedRul = onnxService.predictRUL(healthyFeatures);
        assertTrue(predictedRul > 80.0f,
                "Early cycle RUL should be high (>80 cycles), got: " + predictedRul);
    }
}
