package com.volttwin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulates real-time BMS telemetry streaming.
 */
@Service
public class TelemetryStreamService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryStreamService.class);

    private final SimpMessagingTemplate messagingTemplate;

    @Value("${volttwin.data.telemetry-csv}")
    private String telemetryCsvPath;

    private String[] lines;
    private final AtomicInteger cursor = new AtomicInteger(0);
    private boolean initialized = false;

    public TelemetryStreamService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    private void loadTelemetryData() {
        if (initialized) return;
        try (BufferedReader br = new BufferedReader(new FileReader(telemetryCsvPath))) {
            lines = br.lines().skip(1).toArray(String[]::new);
            initialized = true;
            log.info("Loaded {} telemetry samples for streaming", lines.length);
        } catch (Exception e) {
            log.error("Failed to load telemetry CSV: {}", e.getMessage());
            lines = new String[0];
            initialized = true;
        }
    }

    @Scheduled(fixedRate = 1000)
    public void streamTelemetry() {
        loadTelemetryData();
        if (lines == null || lines.length == 0) return;

        int idx = cursor.getAndUpdate(i -> (i + 1) % lines.length);
        String[] cols = lines[idx].split(",");
        if (cols.length < 6) return;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("batteryId", cols[0].trim());
        payload.put("cycle", Integer.parseInt(cols[1].trim()));
        payload.put("timeSec", Double.parseDouble(cols[2].trim()));
        payload.put("voltageV", Double.parseDouble(cols[3].trim()));
        payload.put("currentA", Double.parseDouble(cols[4].trim()));
        payload.put("tempC", Double.parseDouble(cols[5].trim()));
        payload.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/telemetry/" + cols[0].trim(), payload);
    }
}
