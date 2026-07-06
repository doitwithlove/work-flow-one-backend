package com.touchmind.work.flow.one.manufacturing.dto;

import jakarta.validation.constraints.NotBlank;

public record StartOperatorSessionRequest(

        @NotBlank
        String operatorId,

        @NotBlank
        String machineId,

        @NotBlank
        String shiftId
) {
}
