package com.touchmind.work.flow.one.sample.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "oeeReports")
public record OeeShiftSummary(

        @Id
        String id,

        @Indexed
        LocalDate date,

        String shift,

        @Indexed
        String machineId,

        int plannedProductionTimeMin,

        double runtimeMin,

        double downtimeMin,

        double idealCycleTimeSec,

        int totalCount,

        int goodCount,

        int rejectCount,

        double availabilityPct,

        double performancePct,

        double qualityPct,

        double oeePct

) {
}
