package com.touchmind.work.flow.one.manufacturing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePartRequest(

        @NotBlank
        @Size(max = 64)
        String partNumber,

        @NotBlank
        @Size(max = 64)
        String batchNumber,

        String currentStepId
) {
}
