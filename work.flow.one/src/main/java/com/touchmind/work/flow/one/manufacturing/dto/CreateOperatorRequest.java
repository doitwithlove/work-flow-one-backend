package com.touchmind.work.flow.one.manufacturing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOperatorRequest(

        @NotBlank
        String employeeCode,

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotBlank
        String role,

        @NotBlank
        String skillLevel,

        @NotNull
        Boolean active
) {
}
