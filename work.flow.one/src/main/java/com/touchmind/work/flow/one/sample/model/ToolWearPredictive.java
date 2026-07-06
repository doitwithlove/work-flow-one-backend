package com.touchmind.work.flow.one.sample.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "tools")
public record ToolWearPredictive(

        @Id
        String id,

        @Indexed
        Instant timestamp,

        @Indexed
        String machineId,

        String tool,

        String toolType,

        String material,

        double toolLifePct,

        double cuttingTimeMin,

        double spindleLoadAvgPct,

        double vibrationAvgMmSec,

        double temperatureAvgC,

        String wearLevel,

        double predictedRemainingLifeMin,

        String maintenanceRecommendation

) {
}
