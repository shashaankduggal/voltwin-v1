package com.volttwin.service;

import ai.onnxruntime.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service that loads ONNX ML models and performs in-JVM inference.
 */
@Service
public class OnnxInferenceService {

    private static final Logger log = LoggerFactory.getLogger(OnnxInferenceService.class);

    @Value("${volttwin.onnx.soh-model}")
    private String sohModelPath;

    @Value("${volttwin.onnx.rul-model}")
    private String rulModelPath;

    private OrtEnvironment env;
    private OrtSession sohSession;
    private OrtSession rulSession;

    @PostConstruct
    public void loadModels() throws OrtException {
        env = OrtEnvironment.getEnvironment();

        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);
        // Constrain ONNX thread pool to avoid runaway OS native thread memory allocation
        options.setIntraOpNumThreads(1);
        options.setInterOpNumThreads(1);

        sohSession = env.createSession(sohModelPath, options);
        log.info("Loaded SOH ONNX model from: {}", sohModelPath);

        rulSession = env.createSession(rulModelPath, options);
        log.info("Loaded RUL ONNX model from: {}", rulModelPath);
    }

    public float predictSOH(float[] features) throws OrtException {
        return runInference(sohSession, features);
    }

    public float predictRUL(float[] features) throws OrtException {
        return runInference(rulSession, features);
    }

    private float runInference(OrtSession session, float[] features) throws OrtException {
        float[][] input = new float[][] { features };
        String inputName = session.getInputNames().iterator().next();

        try (OnnxTensor tensor = OnnxTensor.createTensor(env, input);
             OrtSession.Result result = session.run(Map.of(inputName, tensor))) {

            Object rawOutput = result.get(0).getValue();
            if (rawOutput instanceof float[] arr) {
                return arr[0];
            } else if (rawOutput instanceof float[][] arr2d) {
                return arr2d[0][0];
            } else if (rawOutput instanceof double[] darr) {
                return (float) darr[0];
            }
            throw new OrtException("Unexpected output type: " + rawOutput.getClass());
        }
    }

    @PreDestroy
    public void cleanup() throws OrtException {
        if (sohSession != null) sohSession.close();
        if (rulSession != null) rulSession.close();
        log.info("ONNX sessions closed");
    }
}
