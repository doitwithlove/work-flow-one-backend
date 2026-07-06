package com.touchmind.work.flow.one.manufacturing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProcessStepRequest(

        @NotNull
        Integer stepNumber,

        @NotBlank
        @Size(max = 128)
        String name,

        @NotBlank
        @Size(max = 64)
        String machineType,

        @NotBlank
        @Size(max = 64)
        String requiredTestType,

        String nextStepId
) {
}
