package com.touchmind.work.flow.one.manufacturing.dto;

import com.touchmind.work.flow.one.manufacturing.enums.PartStatus;
import com.touchmind.work.flow.one.manufacturing.enums.TestStatus;

import java.time.Instant;

public record PartResponse(

        String id,

        String partNumber,

        String batchNumber,

        String currentStepId,

        String currentMachineId,

        PartStatus status,

        TestStatus testStatus,

        Instant createdAt,

        Instant updatedAt
) {
}
