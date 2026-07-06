package com.touchmind.work.flow.one.sample.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "productionTelemetry")
public record ProductionTelemetry(

        @Id
        String id,

        @Indexed
        Instant timestamp,

        @Indexed
        String machineId,

        String jobId,

        String partNumber,

        String program,

        String status,

        int spindleRpm,

        double feedRate,

        double axisX,

        double axisY,

        double axisZ,

        Double axisA,

        Double axisB,

        Double axisC,

        String tool,

        double toolLifePct,

        double spindleLoadPct,

        double servoLoadPct,

        double temperatureC,

        double vibrationMmSec,

        String coolantStatus,

        Double hydraulicPressureBar,

        Double airPressureBar,

        String lubricationStatus,

        int cycleTimeSec,

        String alarmCode,

        double powerKW,

        long partCount,

        Integer toolChangeCount

) {
}
