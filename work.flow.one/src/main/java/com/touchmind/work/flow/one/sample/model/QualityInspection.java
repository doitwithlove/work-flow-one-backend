package com.touchmind.work.flow.one.sample.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "qualityInspections")
public record QualityInspection(

        @Id
        String id,

        @Indexed
        Instant timestamp,

        @Indexed
        String machineId,

        String partNumber,

        String jobId,

        String dimensionName,

        double nominalValueMm,

        double measuredValueMm,

        double tolerancePlusMm,

        double toleranceMinusMm,

        String result,

        double surfaceRaUm,

        String inspectorId

) {
}
