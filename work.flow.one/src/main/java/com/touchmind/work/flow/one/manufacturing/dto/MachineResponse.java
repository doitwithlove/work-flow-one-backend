package com.touchmind.work.flow.one.manufacturing.dto;

import com.touchmind.work.flow.one.manufacturing.enums.MachineStatus;

import java.time.Instant;

public record MachineResponse(

        String id,

        String machineCode,

        String name,

        String type,

        MachineStatus status,

        Instant lastSignalAt,

        String currentOperatorId,

        String currentOperatorName,

        Instant activeSessionStartTime
) {
}
