package com.volttwin.config;

import com.volttwin.model.BatteryCycle;
import com.volttwin.repository.BatteryCycleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Loads battery_cycle_summary.csv into the H2 database on application startup.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final BatteryCycleRepository repository;

    @Value("${volttwin.data.cycle-summary-csv}")
    private String csvPath;

    public DataLoader(BatteryCycleRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() > 0) {
            log.info("Database already populated with {} records, skipping CSV load", repository.count());
            return;
        }

        log.info("Loading battery cycle data from: {}", csvPath);
        int count = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String header = br.readLine();
            String line;

            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",");
                if (cols.length < 12) continue;

                BatteryCycle cycle = BatteryCycle.builder()
                        .batteryId(cols[0].trim())
                        .cycle(Integer.parseInt(cols[1].trim()))
                        .ambientTemperature(Double.parseDouble(cols[2].trim()))
                        .capacityAh(Double.parseDouble(cols[3].trim()))
                        .minVoltage(Double.parseDouble(cols[4].trim()))
                        .avgVoltage(Double.parseDouble(cols[5].trim()))
                        .maxTempC(Double.parseDouble(cols[6].trim()))
                        .avgTempC(Double.parseDouble(cols[7].trim()))
                        .durationSec(Double.parseDouble(cols[8].trim()))
                        .sohPct(Double.parseDouble(cols[9].trim()))
                        .eolCycle(Integer.parseInt(cols[10].trim()))
                        .rulCycles(Integer.parseInt(cols[11].trim()))
                        .build();

                repository.save(cycle);
                count++;
            }
        }

        log.info("Loaded {} battery cycle records into database", count);
    }
}
