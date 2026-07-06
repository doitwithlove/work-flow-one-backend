package com.touchmind.work.flow.one.manufacturing.dto;

import com.touchmind.work.flow.one.manufacturing.enums.MachineStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMachineRequest(

        @NotBlank
        @Size(max = 64)
        String machineCode,

        @NotBlank
        @Size(max = 128)
        String name,

        @NotBlank
        @Size(max = 128)
        String type,

        @NotNull
        MachineStatus status
) {
}
