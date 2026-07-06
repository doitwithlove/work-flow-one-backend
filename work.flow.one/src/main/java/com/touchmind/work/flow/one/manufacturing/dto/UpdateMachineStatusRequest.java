package com.touchmind.work.flow.one.manufacturing.dto;

import com.touchmind.work.flow.one.manufacturing.enums.MachineStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateMachineStatusRequest(

        @NotNull
        MachineStatus status
) {
}
