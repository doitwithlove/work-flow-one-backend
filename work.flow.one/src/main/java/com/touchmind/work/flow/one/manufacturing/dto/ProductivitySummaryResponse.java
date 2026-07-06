package com.touchmind.work.flow.one.manufacturing.dto;

import java.time.Instant;

public record ProductivitySummaryResponse(

        String operatorId,

        String operatorName,

        String employeeCode,

        String shiftId,

        String shiftName,

        String machineId,

        String machineName,

        int totalPartsProcessed,

        int passedParts,

        int failedParts,

        int reworkParts,

        int totalRuntimeMinutes,

        int totalDowntimeMinutes,

        double qualityRate,

        double productionRate,

        double machineUtilization,

        double productivityScore,

        Instant calculatedAt
) {
}
