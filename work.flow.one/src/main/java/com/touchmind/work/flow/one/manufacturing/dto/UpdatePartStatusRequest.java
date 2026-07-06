package com.touchmind.work.flow.one.manufacturing.dto;

import com.touchmind.work.flow.one.manufacturing.enums.PartStatus;
import com.touchmind.work.flow.one.manufacturing.enums.TestStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePartStatusRequest(

        @NotNull
        PartStatus status,

        TestStatus testStatus,

        String currentMachineId,

        String currentStepId
) {
}
