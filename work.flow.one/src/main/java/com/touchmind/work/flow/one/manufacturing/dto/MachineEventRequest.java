package com.touchmind.work.flow.one.manufacturing.dto;

import com.touchmind.work.flow.one.manufacturing.enums.MachineEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record MachineEventRequest(

        @NotBlank
        String machineId,

        @NotBlank
        String partId,

        @NotNull
        MachineEventType eventType,

        @NotBlank
        String status,

        Map<String, Object> payload
) {
}
