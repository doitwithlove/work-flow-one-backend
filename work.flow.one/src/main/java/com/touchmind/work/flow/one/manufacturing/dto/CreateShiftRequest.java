package com.touchmind.work.flow.one.manufacturing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record CreateShiftRequest(

        @NotBlank
        String name,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime,

        @NotNull
        @Min(1)
        Integer targetOutput,

        @NotNull
        Boolean active
) {
}
