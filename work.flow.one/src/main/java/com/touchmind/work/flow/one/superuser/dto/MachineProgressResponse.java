package com.touchmind.work.flow.one.superuser.dto;

import com.touchmind.work.flow.one.manufacturing.enums.MachineStatus;

import java.time.Instant;

public record MachineProgressResponse(

        String machineId,

        String machineCode,

        String machineName,

        MachineStatus status,

        String currentPartId,

        String currentPartNumber,

        String currentOperatorId,

        String currentOperatorName,

        String currentStepName,

        double progressPercentage,

        Instant lastSignalAt
) {
}
