package com.volttwin.repository;

import com.volttwin.model.BatteryCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for battery cycle telemetry records.
 */
@Repository
public interface BatteryCycleRepository extends JpaRepository<BatteryCycle, Long> {

    List<BatteryCycle> findByBatteryIdOrderByCycleAsc(String batteryId);

    @Query("SELECT DISTINCT b.batteryId FROM BatteryCycle b")
    List<String> findDistinctBatteryIds();

    @Query("SELECT b FROM BatteryCycle b WHERE b.batteryId = :batteryId AND b.cycle = " +
           "(SELECT MAX(b2.cycle) FROM BatteryCycle b2 WHERE b2.batteryId = :batteryId)")
    BatteryCycle findLatestCycleForBattery(String batteryId);
}
