package com.touchmind.work.flow.one.manufacturing.dto;

import com.touchmind.work.flow.one.manufacturing.enums.TestResultStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTestResultRequest(

        @NotBlank
        String partId,

        @NotBlank
        String machineId,

        @NotBlank
        String testType,

        @NotNull
        Double expectedValue,

        @NotNull
        Double actualValue,

        @NotNull
        Double toleranceMin,

        @NotNull
        Double toleranceMax,

        @NotNull
        TestResultStatus result
) {
}
